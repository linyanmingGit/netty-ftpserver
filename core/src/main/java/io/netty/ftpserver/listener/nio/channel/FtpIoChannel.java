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
import io.netty.channel.Channel;
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
 * FTP IO Channel
 *
 * @author yanming Lin
 */
public class FtpIoChannel<T extends AbstractChannel> implements FtpChannel {
    private ChannelAttrProxy channelAttrProxy;

    private T TChannel;

    public FtpIoChannel(Channel channel) {
        TChannel = (T)channel;
        channelAttrProxy = new ChannelAttrProxy(this);
    }

    @Override
    public void resetState() {
        channelAttrProxy.resetState();
    }

    @Override
    public FileSystemView getFileSystemView() {
        return channelAttrProxy.getFileSystemView();
    }

    public FtpServerContext getContext() {
        return channelAttrProxy.getContext();
    }

    @Override
    public User getUser() {
        return channelAttrProxy.getUser();
    }

    @Override
    public boolean isLoggedIn() {
        return channelAttrProxy.isLoggedIn();
    }

    @Override
    public Listener getListener() {
        return channelAttrProxy.getListener();
    }

    @Override
    public void setLanguage(String language) {
        channelAttrProxy.setLanguage(language);
    }


    @Override
    public String getLanguage() {
        return channelAttrProxy.getLanguage();
    }

    @Override
    public String getUserArgument() {
        return channelAttrProxy.getUserArgument();
    }

    @Override
    public int getFailedLogins() {
        return channelAttrProxy.getFailedLogins();
    }

    @Override
    public void reinitialize() {
        channelAttrProxy.reinitialize();
    }

    @Override
    public long getFileOffset() {
        return channelAttrProxy.getFileOffset();
    }

    @Override
    public void setFileOffset(long fileOffset) {
        channelAttrProxy.setFileOffset(fileOffset);
    }

    @Override
    public void logoutUser() {
        channelAttrProxy.logoutUser();
    }

    @Override
    public void setLogin(FileSystemView fsview) {
        channelAttrProxy.setLogin(fsview);
    }

    @Override
    public void setRenameFrom(FtpFile renFr) {
        channelAttrProxy.setRenameFrom(renFr);
    }

    @Override
    public FtpFile getRenameFrom() {
        return channelAttrProxy.getRenameFrom();
    }

    @Override
    public void setUserArgument(String userArgument) {
        channelAttrProxy.setUserArgument(userArgument);
    }

    @Override
    public void setDataType(DataType dataType) {
        channelAttrProxy.setDataType(dataType);
    }

    @Override
    public Date getLastAccessTime() {
        return channelAttrProxy.getLastAccessTime();
    }

    @Override
    public Date getLoginTime() {
        return channelAttrProxy.getLoginTime();
    }

    @Override
    public DataType getDataType() {
        return channelAttrProxy.getDataType();
    }

    @Override
    public void setStructure(Structure structure) {
        channelAttrProxy.setStructure(structure);
    }

    @Override
    public Structure getStructure() {
        return channelAttrProxy.getStructure();
    }

    @Override
    public long getCreationTime() {
        return channelAttrProxy.getCreationTime();
    }

    @Override
    public void setUser(User user) {
        channelAttrProxy.setUser(user);
    }

    @Override
    public void increaseFailedLogins() {
        channelAttrProxy.increaseFailedLogins();
    }

    @Override
    public void increaseWrittenDataBytes(int increment) {
        channelAttrProxy.increaseWrittenDataBytes(increment);
    }

    @Override
    public void increaseReadDataBytes(int increment) {
        channelAttrProxy.increaseReadDataBytes(increment);
    }

    @Override
    public void updateLastAccessTime() {
        channelAttrProxy.updateLastAccessTime();
    }

    @Override
    public synchronized ServerDataConnectionFactory getDataConnection() {
        return channelAttrProxy.getDataConnection();
    }

    public Object setAttribute(String key, Object value) {
        return channelAttrProxy.setAttribute(key, value);
    }

    @Override
    public Object getAttribute(String key) {
        return channelAttrProxy.getAttribute(key);
    }

    @Override
    public T getTChannel() {
        return TChannel;
    }

    @Override
    public InetSocketAddress localAddress() {
        return (InetSocketAddress)TChannel.localAddress();
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return (InetSocketAddress)TChannel.remoteAddress();
    }

    @Override
    public ChannelFuture writeAndFlush(FtpReply ftpReply) {
        channelAttrProxy.setFtpReply(ftpReply);
        return TChannel.writeAndFlush(ftpReply);
    }

    @Override
    public ChannelPipeline pipeline() {
        return TChannel.pipeline();
    }

    @Override
    public ChannelFuture close() {
        return TChannel.close();
    }

    public FtpReply getFtpReply(){
        return channelAttrProxy.getFtpReply();
    }

}
