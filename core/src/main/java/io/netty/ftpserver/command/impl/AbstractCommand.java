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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.ftpserver.ftplet.FtpException;
import io.netty.ftpserver.ftplet.FtpRequest;
import io.netty.ftpserver.ftplet.FtpletResult;
import io.netty.ftpserver.ftpletcontainer.FtpletContainer;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;
import io.netty.ftpserver.listener.nio.channel.FtpIoChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Io Netty Project
 */
public abstract class AbstractCommand extends SimpleChannelInboundHandler<FtpRequest> {
    private final Logger LOG = LoggerFactory.getLogger(AbstractCommand.class);

    /**
     * Execute the command set
     *
     * @param context ChannelHandlerContext
     * @param channel FtpChannel
     * @param request FtpRequest
     * @throws IOException
     * @throws FtpException
     */
    public abstract void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException;

    @Override
    protected void channelRead0(ChannelHandlerContext context, FtpRequest request) throws Exception {
        FtpIoChannel<AbstractChannel> ftpChannel= new FtpIoChannel<>(context.channel());
        try {
            ftpChannel.updateLastAccessTime();
            FtpletContainer ftplet = ftpChannel.getContext().getFtpletContainer();
            FtpletResult ftpletRet;
            try {
                ftpletRet = ftplet.beforeCommand(ftpChannel, request);
            } catch (Exception e) {
                LOG.debug("Ftplet container threw exception", e);
                ftpletRet = FtpletResult.DISCONNECT;
            }

            if (ftpletRet == FtpletResult.DISCONNECT) {
                LOG.debug("Ftplet returned DISCONNECT, session will be closed");
                ftpChannel.close().awaitUninterruptibly(10000);
                return;
            } else if (ftpletRet != FtpletResult.SKIP) {
                execute(context,ftpChannel, request);
            }

            try {
                ftpletRet = ftplet.afterCommand(ftpChannel, request, ftpChannel.getFtpReply());
            } catch (Exception e) {
                LOG.debug("Ftplet container threw exception", e);
                ftpletRet = FtpletResult.DISCONNECT;
            }
            if (ftpletRet == FtpletResult.DISCONNECT) {
                LOG.debug("Ftplet returned DISCONNECT, session will be closed");

                ftpChannel.close().awaitUninterruptibly(10000);
                return;
            }
        } catch (FtpException e) {
            LOG.debug("Ftp threw exception", e);
            ftpChannel.close().awaitUninterruptibly(10000);
            throw e;
        } catch (IOException e) {
            LOG.debug("Ftp IO threw exception", e);
            ftpChannel.close().awaitUninterruptibly(10000);
            throw e;
        }finally {
            context.pipeline().remove(this);
        }
    }
}
