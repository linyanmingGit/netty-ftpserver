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

package io.netty.ftpserver.listener.nio.channel;

/**
 * @author Io Netty Project
 */
public class FtpStatus {

    /**
     * Contains user name between USER and PASS commands
     */
    private static final String ATTRIBUTE_PREFIX = "netty.io.ftpserver.";
    public static final String ATTRIBUTE_USER_ARGUMENT = ATTRIBUTE_PREFIX
            + "user-argument";
    public static final String ATTRIBUTE_SESSION_ID = ATTRIBUTE_PREFIX
            + "session-id";
    public static final String ATTRIBUTE_USER = ATTRIBUTE_PREFIX + "user";
    public static final String ATTRIBUTE_LANGUAGE = ATTRIBUTE_PREFIX
            + "language";
    public static final String ATTRIBUTE_LOGIN_TIME = ATTRIBUTE_PREFIX
            + "login-time";
    public static final String ATTRIBUTE_DATA_CONNECTION = ATTRIBUTE_PREFIX
            + "data-connection";
    public static final String ATTRIBUTE_FILE_SYSTEM = ATTRIBUTE_PREFIX
            + "file-system";
    public static final String ATTRIBUTE_RENAME_FROM = ATTRIBUTE_PREFIX
            + "rename-from";
    public static final String ATTRIBUTE_FILE_OFFSET = ATTRIBUTE_PREFIX
            + "file-offset";
    public static final String ATTRIBUTE_DATA_TYPE = ATTRIBUTE_PREFIX
            + "data-type";
    public static final String ATTRIBUTE_STRUCTURE = ATTRIBUTE_PREFIX
            + "structure";
    public static final String ATTRIBUTE_FAILED_LOGINS = ATTRIBUTE_PREFIX
            + "failed-logins";
    public static final String ATTRIBUTE_LISTENER = ATTRIBUTE_PREFIX
            + "listener";
    public static final String ATTRIBUTE_MAX_IDLE_TIME = ATTRIBUTE_PREFIX
            + "max-idle-time";
    public static final String ATTRIBUTE_LAST_ACCESS_TIME = ATTRIBUTE_PREFIX
            + "last-access-time";
    public static final String ATTRIBUTE_CACHED_REMOTE_ADDRESS = ATTRIBUTE_PREFIX
            + "cached-remote-address";
    /** The Session creation's time */
    public static final String ATTRIBUTE_CREATION_TIME = ATTRIBUTE_PREFIX + "creation-time";

    public static final String ATTRIBUTE_LAST_READ_TIME = ATTRIBUTE_PREFIX + "last-read-time";

    public static final String ATTRIBUTE_LAST_WRITE_TIME = ATTRIBUTE_PREFIX + "last-write-time";

    public static final String ATTRIBUTE_LAST_THROUGHPUT_CALCULATION_TIME = ATTRIBUTE_PREFIX
            + "last-throughput-calculation-time";

    public static final String ATTRIBUTE_LAST_IDLE_TIME_FOR_BOTH = ATTRIBUTE_PREFIX
            + "last-idle-time-for-both";

    public static final String ATTRIBUTE_LAST_IDLE_TIME_FOR_READ = ATTRIBUTE_PREFIX
            + "last-idle-time-for-read";

    public static final String ATTRIBUTE_LAST_IDLE_TIME_FOR_WRITE = ATTRIBUTE_PREFIX
            + "last-idle-time-for-write";

    public static final String ATTRIBUTE_WRITTEN_BYTES = ATTRIBUTE_PREFIX
            + "written-bytes";

    public static final String ATTRIBUTE_READ_BYTES = ATTRIBUTE_PREFIX
            + "read-bytes";

    public static final String ATTRIBUTE_CONTEXT = ATTRIBUTE_PREFIX + "context";

    public static final String ATTRIBUTE_FTPLET = ATTRIBUTE_PREFIX + "ftplet";

}
