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

import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.ftpserver.ftplet.DataType;
import io.netty.ftpserver.ftplet.FileSystemView;
import io.netty.ftpserver.ftplet.FtpFile;
import io.netty.ftpserver.ftplet.FtpReply;
import io.netty.ftpserver.ftplet.Structure;
import io.netty.ftpserver.ftplet.User;
import io.netty.ftpserver.impl.FtpServerContext;
import io.netty.ftpserver.impl.ServerDataConnectionFactory;
import io.netty.ftpserver.listener.Listener;

import java.net.InetSocketAddress;
import java.util.Date;

/**
 * @author Io Netty Project
 */
public interface FtpChannel<T extends AbstractChannel>{
    void resetState();

    FileSystemView getFileSystemView();

    FtpServerContext getContext();

    User getUser();

    boolean isLoggedIn();

    Listener getListener();

    void setLanguage(String language);

    String getLanguage();

    String getUserArgument();

    void setUserArgument(String userArgument);

    int getFailedLogins();

    void reinitialize();

    void  logoutUser();

    void setLogin(FileSystemView fsview);

    long getFileOffset();

    void setFileOffset(long fileOffset);

    void setRenameFrom(FtpFile renFr);

    FtpFile getRenameFrom();

    void setDataType(DataType dataType);

    Date getLastAccessTime();

    Date getLoginTime();

    DataType getDataType();

    void setStructure(Structure structure);

    Structure getStructure();

    long getCreationTime();

    void setUser(User user);

    void increaseFailedLogins();

    void increaseWrittenDataBytes(int increment);

    void increaseReadDataBytes(int increment);

    void updateLastAccessTime();

    ServerDataConnectionFactory getDataConnection();

    Object setAttribute(String key, Object value);

    Object getAttribute(String key);

    T getTChannel();

    InetSocketAddress localAddress();

    InetSocketAddress remoteAddress();

    ChannelFuture writeAndFlush(FtpReply ftpReply);

    ChannelPipeline pipeline();

    ChannelFuture close();
}
