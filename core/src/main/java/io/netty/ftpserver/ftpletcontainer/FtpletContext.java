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

package io.netty.ftpserver.ftpletcontainer;

import io.netty.ftpserver.ftplet.FileSystemFactory;
import io.netty.ftpserver.ftplet.FtpStatistics;
import io.netty.ftpserver.ftplet.UserManager;

/**
 * A ftplet configuration object used by a ftplet container used to pass
 * information to a ftplet during initialization. The configuration information
 * contains initialization parameters.
 *
 * @author Io Netty Project
 */
public interface FtpletContext {

    /**
     * Get the user manager.
     * @return The {@link UserManager}
     */
    UserManager getUserManager();

    /**
     * Get file system manager
     * @return The {@link FileSystemFactory}
     */
    FileSystemFactory getFileSystemManager();

    /**
     * Get ftp statistics.
     * @return The {@link FtpStatistics}
     */
    FtpStatistics getFtpStatistics();

    /**
     * Get Ftplet.
     * @param name The name identifying the {@link Ftplet}
     * @return The {@link Ftplet} registred with the provided name, or null if none exists
     */
    Ftplet getFtplet(String name);
}
