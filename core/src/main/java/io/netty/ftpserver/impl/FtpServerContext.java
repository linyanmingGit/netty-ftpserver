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

package io.netty.ftpserver.impl;

import io.netty.ftpserver.ConnectionConfig;
import io.netty.ftpserver.ftpletcontainer.FtpletContext;
import io.netty.ftpserver.ftpletcontainer.FtpletContainer;
import io.netty.ftpserver.listener.Listener;
import io.netty.ftpserver.message.MessageResource;
import io.netty.ftpserver.command.CommandFactory;

import java.security.cert.Certificate;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <strong>Internal class, do not use directly.</strong>
 * 
 * This is basically <code>io.netty.ftpserver.ftpletcontainer.FtpletContext</code> with
 * added connection manager, message resource functionalities.
 *
 * @author Io Netty Project
 */
public interface FtpServerContext extends FtpletContext {

    ConnectionConfig getConnectionConfig();

    /**
     * Get message resource.
     */
    MessageResource getMessageResource();

    /**
     * Get ftplet container.
     */
    FtpletContainer getFtpletContainer();

    Listener getListener(String name);

    Map<String, Listener> getListeners();

    /**
     * Get the command factory.
     */
    CommandFactory getCommandFactory();

    /**
     * Release all components.
     */
    void dispose();
    
    /**
     * Returns the thread pool executor for this context.  
     * @return the thread pool executor for this context.
     */
    ThreadPoolExecutor getThreadPoolExecutor();

    Certificate[] getClientCertificates();
}
