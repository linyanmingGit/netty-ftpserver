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
import io.netty.ftpserver.DataConnectionConfiguration;
import io.netty.ftpserver.ftplet.FtpException;
import io.netty.ftpserver.ftplet.FtpReply;
import io.netty.ftpserver.ftplet.FtpRequest;
import io.netty.ftpserver.impl.ServerDataConnectionFactory;
import io.netty.ftpserver.impl.reply.LocalizedFtpReply;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;
import io.netty.ftpserver.ssl.SslConfiguration;

import java.io.IOException;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * Data channel protection level.
 *
 * @author Io Netty Project
 */
@Sharable
public class PROT extends AbstractCommand {
    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        // reset state variables
        channel.resetState();

        // check argument
        String arg = request.getArgument();
        if (arg == null) {
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                    "PROT", null));
            return;
        }

        // check argument
        arg = arg.toUpperCase();
        ServerDataConnectionFactory dcon = channel.getDataConnection();
        if (arg.equals("C")) {
            dcon.setSecure(false);
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_200_COMMAND_OKAY, "PROT", null));
        } else if (arg.equals("P")) {
            if (getSslConfiguration(channel) == null) {
                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        431, "PROT", null));
            } else {
                dcon.setSecure(true);
                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        FtpReply.REPLY_200_COMMAND_OKAY, "PROT", null));
            }
        } else {
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_504_COMMAND_NOT_IMPLEMENTED_FOR_THAT_PARAMETER,
                    "PROT", null));
        }
    }

    private SslConfiguration getSslConfiguration(final FtpChannel channel) {
        DataConnectionConfiguration dataCfg = channel.getListener().getDataConnectionConfiguration();

        SslConfiguration configuration = dataCfg.getSslConfiguration();

        // fall back if no configuration has been provided on the data connection config
        if(configuration == null) {
            configuration = channel.getListener().getSslConfiguration();
        }

        return configuration;
    }
}
