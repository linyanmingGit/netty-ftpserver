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

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.ftpserver.ftplet.FtpException;
import io.netty.ftpserver.ftplet.FtpReply;
import io.netty.ftpserver.ftplet.FtpRequest;
import io.netty.ftpserver.impl.reply.LocalizedFtpReply;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;

import java.io.IOException;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * Client-Server encoding negotiation. Force server from default encoding to
 * UTF-8 and back. Note that the servers default encoding is UTF-8. So this
 * command has no effect.
 *
 * @author Io Netty Project
 */
@Sharable
public class OPTS_UTF8 extends AbstractCommand {
    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        // reset state
        channel.resetState();

        // send default message
        channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                FtpReply.REPLY_200_COMMAND_OKAY, "OPTS.UTF8", null));
    }
}
