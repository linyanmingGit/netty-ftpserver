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

import io.netty.ftpserver.ftplet.FtpFile;
import io.netty.ftpserver.ftplet.FtpStatistics;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;

/**
 * <strong>Internal class, do not use directly.</strong>
 * 
 * This is same as <code>io.netty.ftpserver.ftplet.FtpStatistics</code> with
 * added observer and setting values functionalities.
 *
 * @author Io Netty Project
 */
public interface ServerFtpStatistics extends FtpStatistics {

    /**
     * Set statistics observer.
     */
    void setObserver(StatisticsObserver observer);

    /**
     * Set file observer.
     */
    void setFileObserver(FileObserver observer);

    /**
     * Increment upload count.
     */
    void setUpload(FtpChannel channel, FtpFile file, long size);

    /**
     * Increment download count.
     */
    void setDownload(FtpChannel channel, FtpFile file, long size);

    /**
     * Increment make directory count.
     */
    void setMkdir(FtpChannel channel, FtpFile dir);

    /**
     * Decrement remove directory count.
     */
    void setRmdir(FtpChannel channel, FtpFile dir);

    /**
     * Increment delete count.
     */
    void setDelete(FtpChannel channel, FtpFile file);

    /**
     * Increment current connection count.
     */
    void setOpenConnection(FtpChannel channel);

    /**
     * Decrement close connection count.
     */
    void setCloseConnection(FtpChannel channel);

    /**
     * Increment current login count.
     */
    void setLogin(FtpChannel channel);

    /**
     * Increment failed login count.
     */
    void setLoginFail(FtpChannel channel);

    /**
     * Decrement current login count.
     */
    void setLogout(FtpChannel channel);

    /**
     * Reset all cumulative total counters. Do not reset current counters, like
     * current logins, otherwise these will become negative when someone
     * disconnects.
     */
    void resetStatisticsCounters();
}
