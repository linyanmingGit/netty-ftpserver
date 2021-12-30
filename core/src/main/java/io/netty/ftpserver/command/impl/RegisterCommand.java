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

package io.netty.ftpserver.command.impl;

import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.ftpserver.ftplet.FileSystemView;
import io.netty.ftpserver.ftplet.FtpReply;
import io.netty.ftpserver.ftplet.FtpRequest;
import io.netty.ftpserver.ftplet.FtpletResult;
import io.netty.ftpserver.ftpletcontainer.FtpletContainer;
import io.netty.ftpserver.impl.ServerDataConnectionFactory;
import io.netty.ftpserver.impl.ServerFtpStatistics;
import io.netty.ftpserver.impl.reply.LocalizedFtpReply;
import io.netty.ftpserver.listener.nio.channel.FtpIoChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Io Netty Project
 */
@Sharable
public class RegisterCommand extends SimpleChannelInboundHandler<FtpRequest> {

    private final Logger LOG = LoggerFactory.getLogger(RegisterCommand.class);

    private ChannelGroup channels;

    private final static Set<String> NON_AUTHENTICATED_COMMANDS = new HashSet<String>(){{
        add("USER");add("PASS");add("AUTH");add("QUIT");add("PROT");add("PBSZ");
    }};

    public RegisterCommand (ChannelGroup channels){
        this.channels = channels;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channels.add(ctx.channel());
        FtpIoChannel<AbstractChannel> ftpChannel= new FtpIoChannel<>(ctx.channel());
        FtpletContainer ftplets = ftpChannel.getContext().getFtpletContainer();

        FtpletResult ftpletRet;
        try {
            ftpletRet = ftplets.onConnect(ftpChannel);
        } catch (Exception e) {
            LOG.debug("Ftplet threw exception", e);
            ftpletRet = FtpletResult.DISCONNECT;
        }
        if (ftpletRet == FtpletResult.DISCONNECT) {
            LOG.debug("Ftplet returned DISCONNECT, session will be closed");
            ftpChannel.close().awaitUninterruptibly(10000);
        } else {
            ftpChannel.updateLastAccessTime();

            ftpChannel.writeAndFlush(LocalizedFtpReply.translate(ftpChannel, null,
                    FtpReply.REPLY_220_SERVICE_READY, null, null));
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, FtpRequest request) throws Exception {

        FtpIoChannel<AbstractChannel> ftpChannel= new FtpIoChannel<>(context.channel());

        if (!ftpChannel.isLoggedIn()
                && !isCommandOkWithoutAuthentication(request.getCommand())) {
            ftpChannel.writeAndFlush(LocalizedFtpReply.translate(ftpChannel, request, FtpReply.REPLY_530_NOT_LOGGED_IN,
                    "permission", null));
            return;
        }

        AbstractCommand command =ftpChannel.getContext().getCommandFactory().getCommand(request.getCommand());
        if (command == null){
            context.writeAndFlush(LocalizedFtpReply.translate(ftpChannel, request,
                    FtpReply.REPLY_502_COMMAND_NOT_IMPLEMENTED,
                    "not.implemented", null));
        } else {
            context.pipeline().addLast(request.getCommand(),command);
            context.fireChannelRead(request);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx)throws Exception {
        channels.remove(ctx.channel());
        LOG.debug("Closing session");
        FtpIoChannel<AbstractChannel> ftpChannel= new FtpIoChannel<>(ctx.channel());
        try {
            ftpChannel.getContext().getFtpletContainer().onDisconnect(ftpChannel);
        } catch (Exception e) {
            // swallow the exception, we're closing down the session anyways
            LOG.warn("Ftplet threw an exception on disconnect", e);
        }

        // make sure we close the data connection if it happens to be open
        try {
            ServerDataConnectionFactory dc = ftpChannel.getDataConnection();
            if(dc != null) {
                dc.closeDataConnection();
            }
        } catch (Exception e) {
            // swallow the exception, we're closing down the session anyways
            LOG.warn("Data connection threw an exception on disconnect", e);
        }

        FileSystemView fs = ftpChannel.getFileSystemView();
        if(fs != null) {
            try  {
                fs.dispose();
            } catch (Exception e) {
                LOG.warn("FileSystemView threw an exception on disposal", e);
            }
        }

        ServerFtpStatistics stats = ((ServerFtpStatistics) ftpChannel.getContext().getFtpStatistics());

        if (stats != null) {
            stats.setLogout(ftpChannel);
            stats.setCloseConnection(ftpChannel);
            LOG.debug("Statistics login and connection count decreased due to session close");
        } else {
            LOG.warn("Statistics not available in session, can not decrease login and connection count");
        }
        LOG.debug("Session closed");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("Exception caught, closing session", cause);
        ctx.channel().close().awaitUninterruptibly(10000);
    }

    private boolean isCommandOkWithoutAuthentication(String command) {
        boolean okay = false;
        if(NON_AUTHENTICATED_COMMANDS.contains(command)){
            okay = true;
        }
        return okay;
    }
}
