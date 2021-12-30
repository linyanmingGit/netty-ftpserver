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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * Handle SITE command.
 *
 * @author Io Netty Project
 */
@Sharable
public class SITE extends AbstractCommand {
    private final Logger LOG = LoggerFactory.getLogger(SITE.class);

    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        // get request name
        String argument = request.getArgument();
        if (argument != null) {
            int spaceIndex = argument.indexOf(' ');
            if (spaceIndex != -1) {
                argument = argument.substring(0, spaceIndex);
            }
            argument = argument.toUpperCase();
        }

        // no params
        if (argument == null) {
            channel.resetState();
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_200_COMMAND_OKAY, "SITE", null));
            return;
        }

        // call appropriate command method
        String siteRequest = "SITE_" + argument;
        AbstractCommand command = channel.getContext().getCommandFactory().getCommand(siteRequest);
        try {
            if (command != null) {
                channel.pipeline().addLast(siteRequest, command);
                context.fireChannelRead(request);
            } else {
                channel.resetState();
                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        FtpReply.REPLY_502_COMMAND_NOT_IMPLEMENTED, "SITE",
                        argument));
            }
        } catch (Exception ex) {
            LOG.warn("SITE.execute()", ex);
            channel.resetState();
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_500_SYNTAX_ERROR_COMMAND_UNRECOGNIZED,
                    "SITE", null));
        }
    }
}
