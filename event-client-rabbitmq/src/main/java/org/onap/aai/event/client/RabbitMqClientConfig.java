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

import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.BuiltinExchangeType;

public class RabbitMqClientConfig {
    
    private static String BINDING_CONSUME_ALL = "#";

    private String hosts;
    private String username;
    private String password;
    private String exchangeName;
    private String exchangeType;
    private String routingKey;
    private String virtualHost;
    private String queue;
    private String bindingKey;
    private Map<String, Object> queueArguments;
    private Map<String, Object> exchangeArguments;
    private String sslKeyStoreFile;
    private String sslTrustStoreFile;
    private String sslKeyStorePassword;
    private String sslTrustStorePassword;
    private String connectionTimeout;
    private String enableSsl;
    
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getHosts() {
        return hosts;
    }
    public void setHosts(String hosts) {
        this.hosts = hosts;
    }
    
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getExchangeName() {
        return exchangeName;
    }
    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }
    
    public BuiltinExchangeType getExchangeType() {
        if (exchangeType == null) {
            return BuiltinExchangeType.TOPIC;
        }
        return BuiltinExchangeType.valueOf(exchangeType);
    }
    public void setExchangeType(String exchangeType) {
        this.exchangeType = exchangeType;
    }
    
    public String getRoutingKey() {
        if (routingKey == null) {
            return "";
        }
        return routingKey;
    }
    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }
    
    public String getVirtualHost() {
        return virtualHost;
    }
    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }
    
    public String getQueue() {
        return queue;
    }
    public void setQueue(String queue) {
        this.queue = queue;
    }
    
    public Map<String, Object> getQueueArguments() {
        if (queueArguments == null) {
            return new HashMap<String, Object>();
        }
        return queueArguments;
    }
    public void setQueueArguments(Map<String, Object> queueArguments) {
        this.queueArguments = queueArguments;
    }
    
    public Map<String, Object> getExchangeArguments() {
        if (exchangeArguments == null) {
            return new HashMap<String, Object>();
        }
        return exchangeArguments;
    }
    public void setExchangeArguments(Map<String, Object> exchangeArguments) {
        this.exchangeArguments = exchangeArguments;
    }
    
    public String getBindingKey() {
        if (bindingKey == null) {
            return BINDING_CONSUME_ALL;
        }
        return bindingKey;
    }

    public void setBindingKey(String bindingKey) {
        this.bindingKey = bindingKey;
    }

    public String getSslKeyStoreFile() {
        return sslKeyStoreFile;
    }

    public void setSslKeyStoreFile(String sslKeyStoreFile) {
        this.sslKeyStoreFile = sslKeyStoreFile;
    }

    public String getSslTrustStoreFile() {
        return sslTrustStoreFile;
    }

    public void setSslTrustStoreFile(String sslTrustStoreFile) {
        this.sslTrustStoreFile = sslTrustStoreFile;
    }

    public String getSslKeyStorePassword() {
        return sslKeyStorePassword;
    }

    public void setSslKeyStorePassword(String sslKeyStorePassword) {
        this.sslKeyStorePassword = sslKeyStorePassword;
    }

    public String getSslTrustStorePassword() {
        return sslTrustStorePassword;
    }

    public void setSslTrustStorePassword(String sslTrustStorePassword) {
        this.sslTrustStorePassword = sslTrustStorePassword;
    }

    public String getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(String connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Boolean getEnableSsl() {
        if (enableSsl != null) {
            return Boolean.valueOf(enableSsl);
        } else {
            return false;
        }
    }

    public void setEnableSsl(String enableSsl) {
        this.enableSsl = enableSsl;
    }
    
    
    
  }
