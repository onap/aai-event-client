/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2018 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2018 European Software Marketing Ltd.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.aai.event.client;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.naming.ConfigurationException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultSaslConfig;

public class RabbitMqUtils {
    private static ConnectionFactory factory = new ConnectionFactory();

    /**
     * (intended for testing purpose only)
     * @param connFactory
     */
    static void setConnectionFactory(ConnectionFactory connFactory) {
        factory = connFactory;
    }

    public static Connection createConnection(RabbitMqClientConfig config) throws Exception {
        if (config.getHosts() == null) {
            throw new ConfigurationException("Mandatory config param hosts not set");
        }

        List<Address> addresses = new ArrayList<Address>();
        String[] hosts = config.getHosts().split(",");
        for (String host : hosts) {
            String[] parts = host.split(":");
            if (parts.length != 2) {
                throw new ConfigurationException("Hosts must be specified in 'host:port' format");
            }

            int port = Integer.parseInt(parts[1]);
            Address add = new Address(parts[0], port);
            addresses.add(add);
        }

        if (config.getUsername() == null && !config.getEnableSsl()) {
            throw new ConfigurationException("Mandatory config param username not set");
        }

        factory.setUsername(config.getUsername());

        if (config.getPassword() == null && !config.getEnableSsl()) {
            throw new ConfigurationException("Mandatory config param password not set");
        }

        factory.setPassword(config.getPassword());

        if (config.getVirtualHost() != null) {
            factory.setVirtualHost(config.getVirtualHost());
        }

        checkSSL(factory,config);
        factory.setConnectionTimeout(120000);
        if (config.getConnectionTimeout() != null) {
            try {
                int timeout = Integer.parseInt(config.getConnectionTimeout());
                factory.setConnectionTimeout(timeout);
            } catch (NumberFormatException ex) {
            }
        }
        
        return factory.newConnection(addresses);
    }
    
    private static void checkSSL(ConnectionFactory factory, RabbitMqClientConfig config) throws Exception {

        // Check if any of SSL params is configured
        if (config.getEnableSsl()) {
            if (config.getSslKeyStoreFile() == null || config.getSslKeyStorePassword() == null
                    || config.getSslTrustStoreFile() == null || config.getSslTrustStorePassword() == null) {
                throw new ConfigurationException(
                        "Missing SSL configuration : sslKeyStoreFile , sslKeyStorePassword , sslTrustStorePassword or sslTrustStoreFile");
            }
            char[] keyPassphrase = config.getSslKeyStorePassword().toCharArray();
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new FileInputStream(config.getSslKeyStoreFile()), keyPassphrase);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, keyPassphrase);

            char[] trustPassphrase = config.getSslTrustStorePassword().toCharArray();
            KeyStore tks = KeyStore.getInstance("JKS");
            tks.load(new FileInputStream(config.getSslTrustStoreFile()), trustPassphrase);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(tks);
            SSLContext c = SSLContext.getInstance("TLSv1.2");
            c.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            factory.setSaslConfig(DefaultSaslConfig.EXTERNAL);
            factory.useSslProtocol(c);
        }

    }

}
