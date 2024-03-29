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

package io.netty.ftpserver.listener;

import io.netty.ftpserver.FtpServerConfigurationException;
import io.netty.ftpserver.DataConnectionConfiguration;
import io.netty.ftpserver.DataConnectionConfigurationFactory;
import io.netty.ftpserver.FtpServerConfigurationException;
import io.netty.ftpserver.listener.nio.NioListener;
import io.netty.ftpserver.ssl.SslConfiguration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Factory for listeners. Listeners themselves are immutable and must be 
 * created using this factory.
 *
 * @author Io Netty Project
 */
public class ListenerFactory {

    private String serverAddress;

    private int port = 21;

    private SslConfiguration ssl;

    private boolean implicitSsl = false;

    private DataConnectionConfiguration dataConnectionConfig = new DataConnectionConfigurationFactory()
            .createDataConnectionConfiguration();

    private int idleTimeout = 300;

    private String channelType;

    /**
     * Default constructor
     */
    public ListenerFactory() {
        // do nothing
    }

    /**
     * Copy constructor, will copy properties from the provided listener.
     * @param listener The listener which properties will be used for this factory
     */
    public ListenerFactory(Listener listener) {
        serverAddress = listener.getServerAddress();
        port = listener.getPort();
        ssl = listener.getSslConfiguration();
        implicitSsl = listener.isImplicitSsl();
        dataConnectionConfig = listener.getDataConnectionConfiguration();
        idleTimeout = listener.getIdleTimeout();
        channelType = listener.getChannelType();
        // TODO remove the next two lines if and when we remove the deprecated
    }

    /**
     * Create a listener based on the settings of this factory. The listener is immutable.
     * @return The created listener
     */
    public Listener createListener() {
        try {
            InetAddress.getByName(serverAddress);
        } catch (UnknownHostException e) {
            throw new FtpServerConfigurationException("Unknown host", e);
        }
        // Deal with the old style black list and new session Filter here.
        return new NioListener(serverAddress, port, implicitSsl, ssl,
                dataConnectionConfig, idleTimeout, channelType);
    }

    /**
     * Is listeners created by this factory in SSL mode automatically or must the client explicitly
     * request to use SSL
     * 
     * @return true is listeners created by this factory is automatically in SSL mode, false
     *         otherwise
     */
    public boolean isImplicitSsl() {
        return implicitSsl;
    }

    /**
     * Should listeners created by this factory be in SSL mode automatically or must the client
     * explicitly request to use SSL
     * 
     * @param implicitSsl
     *            true is listeners created by this factory should automatically be in SSL mode,
     *            false otherwise
     */
    public void setImplicitSsl(boolean implicitSsl) {
        this.implicitSsl = implicitSsl;
    }

    /**
     * Get the port on which listeners created by this factory is waiting for requests. 
     * 
     * @return The port
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the port on which listeners created by this factory will accept requests. Or set to 0
     * (zero) is the port should be automatically assigned
     * 
     * @param port
     *            The port to use.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Get the {@link InetAddress} used for binding the local socket. Defaults
     * to null, that is, the server binds to all available network interfaces
     * 
     * @return The local socket {@link InetAddress}, if set
     */
    public String getServerAddress()  {
        return serverAddress;
    }

    /**
     * Set the {@link InetAddress} used for binding the local socket. Defaults
     * to null, that is, the server binds to all available network interfaces
     * 
     * @param serverAddress
     *            The local socket {@link InetAddress}
     */
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    /**
     * Get the {@link SslConfiguration} used for listeners created by this factory
     * 
     * @return The {@link SslConfiguration}
     */
    public SslConfiguration getSslConfiguration() {
        return ssl;
    }

    /**
     * Set the {@link SslConfiguration} to use by listeners created by this factory
     * @param ssl The {@link SslConfiguration}
     */
    public void setSslConfiguration(SslConfiguration ssl) {
        this.ssl = ssl;
    }

    /**
     * Get configuration for data connections made within listeners created by this factory
     * 
     * @return The data connection configuration
     */
    public DataConnectionConfiguration getDataConnectionConfiguration() {
        return dataConnectionConfig;
    }

    /**
     * Set configuration for data connections made within listeners created by this factory
     * 
     * @param dataConnectionConfig
     *            The data connection configuration
     */
    public void setDataConnectionConfiguration(
            DataConnectionConfiguration dataConnectionConfig) {
        this.dataConnectionConfig = dataConnectionConfig;
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
     * Set the number of seconds during which no network activity 
     * is allowed before a session is closed due to inactivity.  
     *
     * @param idleTimeout The idle timeout in seconds
     */
    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    /**
     * Gets the channel startup type
     *
     * @return channel type
     */
    public String getChannelType() {
        return channelType;
    }

    /**
     * Sets the channel startup type
     *
     * @param channelType channel type
     */
    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }
}