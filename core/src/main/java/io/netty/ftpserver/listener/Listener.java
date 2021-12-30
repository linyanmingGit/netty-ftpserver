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

import io.netty.channel.group.ChannelGroup;
import io.netty.ftpserver.DataConnectionConfiguration;
import io.netty.ftpserver.impl.FtpServerContext;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;
import io.netty.ftpserver.ssl.SslConfiguration;

import java.net.InetAddress;

/**
 * Interface for the component responsible for waiting for incoming socket
 * requests and kicking off {@link FtpChannel}s
 *
 * @author Io Netty Project
 */
public interface Listener {

    /**
     * Start the listener, will initiate the listener waiting on the socket. The
     * method should not return until the listener has started accepting socket
     * requests.
     * @param serverContext The current {@link FtpServerContext}
     * 
     * @throws Exception
     *             On error during start up
     */
    void start(FtpServerContext serverContext) throws ClassNotFoundException, InstantiationException, IllegalAccessException, Exception;

    /**
     * Stop the listener, it should no longer except socket requests. The method
     * should not return until the listener has stopped accepting socket
     * requests.
     */
    void stop();

    /**
     * Checks if the listener is currently started.
     * 
     * @return False if the listener is started
     */
    boolean isStopped();

    /**
     * Temporarily stops the listener from accepting socket requests. Resume the
     * listener by using the {@link #resume()} method. The method should not
     * return until the listener has stopped accepting socket requests.
     */
    void suspend();

    /**
     * Resumes a suspended listener. The method should not return until the
     * listener has started accepting socket requests.
     */
    void resume();

//    /**
//     * Returns the currently active sessions for this listener. If no sessions
//     * are active, an empty {@link Set} would be returned.
//     *
//     * @return The currently active sessions
//     */
    ChannelGroup getActiveChannel();

    /**
     * Is this listener in SSL mode automatically or must the client explicitly
     * request to use SSL
     * 
     * @return true is the listener is automatically in SSL mode, false
     *         otherwise
     */
    boolean isImplicitSsl();

    /**
     * Get the {@link SslConfiguration} used for this listener
     * 
     * @return The current {@link SslConfiguration}
     */
    SslConfiguration getSslConfiguration();

    /**
     * Get the port on which this listener is waiting for requests. For
     * listeners where the port is automatically assigned, this will return the
     * bound port.
     * 
     * @return The port
     */
    int getPort();

    /**
     * Get the {@link InetAddress} used for binding the local socket. Defaults
     * to null, that is, the server binds to all available network interfaces
     * 
     * @return The local socket {@link InetAddress}, if set
     */
    String getServerAddress();

    /**
     * Get configuration for data connections made within this listener
     * 
     * @return The data connection configuration
     */
    DataConnectionConfiguration getDataConnectionConfiguration();

    /**
     * Get the number of seconds during which no network activity 
     * is allowed before a session is closed due to inactivity.  
     * @return The idle time out
     */
    int getIdleTimeout();

    String getChannelType();
}