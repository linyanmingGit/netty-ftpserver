/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.netty.ftpserver.listener.nio;

import io.netty.ftpserver.DataConnectionConfiguration;
import io.netty.ftpserver.listener.Listener;
import io.netty.ftpserver.listener.ListenerFactory;
import io.netty.ftpserver.ssl.SslConfiguration;

/**
 * <strong>Internal class, do not use directly.</strong>
 * 
 * Common base class for listener implementations
 *
 * @author Apache MINA Project
 */
public abstract class AbstractListener implements Listener {

    private final String serverAddress;

    private int port = 21;

    private final SslConfiguration ssl;

    private final boolean implicitSsl;
    
    private final int idleTimeout;

    private final String channelType;

    private final DataConnectionConfiguration dataConnectionConfig;

    /**
     * @deprecated Use the constructor with IpFilter instead. 
     * Constructor for internal use, do not use directly. Instead use {@link ListenerFactory}
     */
    @Deprecated
    public AbstractListener(String serverAddress, int port, boolean implicitSsl,
                            SslConfiguration sslConfiguration, DataConnectionConfiguration dataConnectionConfig,
                            int idleTimeout, String channelType) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.implicitSsl = implicitSsl;
        this.dataConnectionConfig = dataConnectionConfig;
        this.ssl = sslConfiguration;
        this.idleTimeout = idleTimeout;
        this.channelType = channelType;
    }
    
    /**
     * Creates a SessionFilter that blacklists the given IP addresses and/or
     * Subnets.
     * 
     * @param blockedAddresses
     *            the addresses to block
     * @param blockedSubnets
     *            the subnets to block
     * @return a SessionFilter that blacklists the given IP addresses and/or
     *         Subnets.
     */
//    private static SessionFilter createBlackListFilter(
//            List<InetAddress> blockedAddresses, List<Subnet> blockedSubnets) {
//        if (blockedAddresses == null && blockedSubnets == null) {
//            return null;
//        }
//        // Initialize the IP filter with Deny type
//        RemoteIpFilter ipFilter = new RemoteIpFilter(IpFilterType.DENY);
//        if (blockedSubnets != null) {
//            ipFilter.addAll(blockedSubnets);
//        }
//        if (blockedAddresses != null) {
//            for (InetAddress address : blockedAddresses) {
//                ipFilter.add(new Subnet(address, 32));
//            }
//        }
//        return ipFilter;
//    }

    /**
     * {@inheritDoc}
     */
    public boolean isImplicitSsl() {
        return implicitSsl;
    }

    /**
     * {@inheritDoc}
     */
    public int getPort() {
        return port;
    }

    /**
     * Used internally to update the port after binding
     * @param port
     */
    protected void setPort(int port) {
        this.port = port;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * {@inheritDoc}
     */
    public SslConfiguration getSslConfiguration() {
        return ssl;
    }

    /**
     * {@inheritDoc}
     */
    public DataConnectionConfiguration getDataConnectionConfiguration() {
        return dataConnectionConfig;
    }

    /**
     * Get the number of seconds during which no network activity 
     * is allowed before a session is closed due to inactivity.  
     * @return The idle time out
     */
    public int getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Gets the channel startup type
     *
     * @return channel type
     */
    public String getChannelType() {
        return channelType;
    }
}
