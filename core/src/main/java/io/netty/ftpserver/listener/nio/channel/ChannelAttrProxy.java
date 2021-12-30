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

import io.netty.ftpserver.ftplet.DataType;
import io.netty.ftpserver.ftplet.FileSystemView;
import io.netty.ftpserver.ftplet.FtpFile;
import io.netty.ftpserver.ftplet.FtpReply;
import io.netty.ftpserver.ftplet.Structure;
import io.netty.ftpserver.ftplet.User;
import io.netty.ftpserver.impl.FtpServerContext;
import io.netty.ftpserver.impl.IODataConnectionFactory;
import io.netty.ftpserver.impl.ServerDataConnectionFactory;
import io.netty.ftpserver.listener.Listener;
import io.netty.util.AttributeKey;

import java.util.Date;

/**
 * @author Io Netty Project
 */
public class ChannelAttrProxy {
    private final FtpChannel channel;

    public ChannelAttrProxy(FtpChannel channel) {
        this.channel = channel;
        long currentTime = System.currentTimeMillis();
        this.channel.getTChannel().attr(AttributeKey.valueOf(FtpStatus.ATTRIBUTE_CREATION_TIME)).set(currentTime);
        this.channel.getTChannel().attr(AttributeKey.valueOf(
                FtpStatus.ATTRIBUTE_LAST_THROUGHPUT_CALCULATION_TIME)).set(currentTime);
        this.channel.getTChannel().attr(AttributeKey.valueOf(FtpStatus.ATTRIBUTE_LAST_READ_TIME)).set(currentTime);
        this.channel.getTChannel().attr(AttributeKey.valueOf(FtpStatus.ATTRIBUTE_LAST_WRITE_TIME)).set(currentTime);
        this.channel.getTChannel().attr(AttributeKey.valueOf(FtpStatus.ATTRIBUTE_LAST_IDLE_TIME_FOR_BOTH)).set(currentTime);
        this.channel.getTChannel().attr(AttributeKey.valueOf(FtpStatus.ATTRIBUTE_LAST_IDLE_TIME_FOR_READ)).set(currentTime);
        this.channel.getTChannel().attr(AttributeKey.valueOf(FtpStatus.ATTRIBUTE_LAST_IDLE_TIME_FOR_WRITE)).set(currentTime);
    }

    public void resetState() {
        removeAttribute(FtpStatus.ATTRIBUTE_RENAME_FROM);
        removeAttribute(FtpStatus.ATTRIBUTE_FILE_OFFSET);
    }

    public FileSystemView getFileSystemView() {
        return (FileSystemView) getAttribute(FtpStatus.ATTRIBUTE_FILE_SYSTEM);
    }

    public FtpServerContext getContext() {
        return (FtpServerContext) getAttribute(FtpStatus.ATTRIBUTE_CONTEXT);
    }

    public User getUser() {
        return (User) getAttribute(FtpStatus.ATTRIBUTE_USER);
    }

    public boolean isLoggedIn() {
        return containsAttribute(FtpStatus.ATTRIBUTE_USER);
    }

    public Listener getListener() {
        return (Listener) getAttribute(FtpStatus.ATTRIBUTE_LISTENER);
    }

    public void setListener(Listener listener) {
        setAttribute(FtpStatus.ATTRIBUTE_LISTENER, listener);
    }

    public void setLanguage(String language) {
        setAttribute(FtpStatus.ATTRIBUTE_LANGUAGE, language);
    }

    public String getLanguage() {
        return (String) getAttribute(FtpStatus.ATTRIBUTE_LANGUAGE);
    }

    public String getUserArgument() {
        return (String) getAttribute(FtpStatus.ATTRIBUTE_USER_ARGUMENT, "");
    }

    public int getFailedLogins() {
        return (Integer) getAttribute(FtpStatus.ATTRIBUTE_FAILED_LOGINS, 0);
    }

    public void reinitialize() {
        logoutUser();
        removeAttribute(FtpStatus.ATTRIBUTE_USER);
        removeAttribute(FtpStatus.ATTRIBUTE_USER_ARGUMENT);
        removeAttribute(FtpStatus.ATTRIBUTE_LOGIN_TIME);
        removeAttribute(FtpStatus.ATTRIBUTE_FILE_SYSTEM);
        removeAttribute(FtpStatus.ATTRIBUTE_RENAME_FROM);
        removeAttribute(FtpStatus.ATTRIBUTE_FILE_OFFSET);
    }

    public void logoutUser() {

    }

    public void setLogin(FileSystemView fsview) {
        setAttribute(FtpStatus.ATTRIBUTE_LOGIN_TIME, new Date());
        setAttribute(FtpStatus.ATTRIBUTE_FILE_SYSTEM, fsview);
    }

    public void setFileOffset(long fileOffset) {
        setAttribute(FtpStatus.ATTRIBUTE_FILE_OFFSET, fileOffset);
    }

    public long getFileOffset() {
        return (long)getAttribute(FtpStatus.ATTRIBUTE_FILE_OFFSET, 0L);
    }

    public void setRenameFrom(FtpFile renFr) {
        setAttribute(FtpStatus.ATTRIBUTE_RENAME_FROM, renFr);
    }

    public FtpFile getRenameFrom() {
        return (FtpFile)getAttribute(FtpStatus.ATTRIBUTE_RENAME_FROM);
    }

    public void setUserArgument(String userArgument) {
        setAttribute(FtpStatus.ATTRIBUTE_USER_ARGUMENT, userArgument);
    }

    public void setStructure(Structure structure) {
        setAttribute(FtpStatus.ATTRIBUTE_STRUCTURE, structure);
    }

    public Structure getStructure() {
        return (Structure) getAttribute(FtpStatus.ATTRIBUTE_STRUCTURE, Structure.FILE);
    }

    public void setDataType(DataType dataType) {
        setAttribute(FtpStatus.ATTRIBUTE_DATA_TYPE, dataType);
    }

    public Date getLastAccessTime() {
        return (Date) getAttribute(FtpStatus.ATTRIBUTE_LAST_ACCESS_TIME);
    }

    public Date getLoginTime() {
        return (Date) getAttribute(FtpStatus.ATTRIBUTE_LOGIN_TIME);
    }

    public DataType getDataType() {
        return (DataType) getAttribute(FtpStatus.ATTRIBUTE_DATA_TYPE, DataType.ASCII);
    }

    public long getCreationTime() {
        return (long) getAttribute(FtpStatus.ATTRIBUTE_CREATION_TIME);
    }

    public void setUser(User user) {
        setAttribute(FtpStatus.ATTRIBUTE_USER, user);
    }

    public void increaseFailedLogins() {
        int failedLogins = (Integer) getAttribute(FtpStatus.ATTRIBUTE_FAILED_LOGINS, 0);
        failedLogins++;
        setAttribute(FtpStatus.ATTRIBUTE_FAILED_LOGINS, failedLogins);
    }

    public void increaseWrittenDataBytes(int increment) {
        if (increment <= 0) {
            return;
        }
        long writtenBytes = (long) getAttribute(FtpStatus.ATTRIBUTE_WRITTEN_BYTES, 0L);
        writtenBytes += increment;
        setAttribute(FtpStatus.ATTRIBUTE_WRITTEN_BYTES,writtenBytes);
        setAttribute(FtpStatus.ATTRIBUTE_LAST_WRITE_TIME,System.currentTimeMillis());
    }

    public void increaseReadDataBytes(int increment) {
        if (increment <= 0) {
            return;
        }
        long readBytes = (long) getAttribute(FtpStatus.ATTRIBUTE_READ_BYTES, 0L);
        readBytes += increment;
        setAttribute(FtpStatus.ATTRIBUTE_READ_BYTES,readBytes);
        setAttribute(FtpStatus.ATTRIBUTE_LAST_READ_TIME,System.currentTimeMillis());
    }

    public void updateLastAccessTime() {
        setAttribute(FtpStatus.ATTRIBUTE_LAST_ACCESS_TIME, new Date());
    }

    public synchronized ServerDataConnectionFactory getDataConnection() {
        if (containsAttribute(FtpStatus.ATTRIBUTE_DATA_CONNECTION)) {
            return (ServerDataConnectionFactory) getAttribute(FtpStatus.ATTRIBUTE_DATA_CONNECTION);
        } else {
            IODataConnectionFactory dataCon = new IODataConnectionFactory(channel);
            dataCon.setServerControlAddress(channel.localAddress().getAddress());
            setAttribute(FtpStatus.ATTRIBUTE_DATA_CONNECTION, dataCon);
            return dataCon;
        }
    }

    public void setFtpReply(FtpReply ftpReply) {
        setAttribute(FtpStatus.ATTRIBUTE_FTPLET, ftpReply);
    }

    public FtpReply getFtpReply() {
        return (FtpReply)getAttribute(FtpStatus.ATTRIBUTE_FTPLET);
    }

    public Object removeAttribute(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key");
        }

        return this.channel.getTChannel().attr(AttributeKey.valueOf(key)).getAndSet(null);
    }

    public final Object getAttribute(String key) {
        return getAttribute(key, null);
    }

    public Object getAttribute(String key, Object defaultValue) {
        if (key == null) {
            throw new IllegalArgumentException("key");
        }

        Object object = this.channel.getTChannel().attr(AttributeKey.valueOf(key)).setIfAbsent(defaultValue);

        if (object == null) {
            return defaultValue;
        } else {
            return object;
        }
    }

    public boolean containsAttribute(String key) {
        return getAttribute(key, null) != null;
    }

    public Object setAttribute(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key");
        }

        return this.channel.getTChannel().attr(AttributeKey.valueOf(key)).getAndSet(value);
    }
}
