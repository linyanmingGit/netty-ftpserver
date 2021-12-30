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
import io.netty.ftpserver.impl.reply.LocalizedFtpReply;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;
import io.netty.ftpserver.util.IllegalInetAddressException;
import io.netty.ftpserver.util.IllegalPortException;
import io.netty.ftpserver.util.SocketAddressEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * <code>PORT &lt;SP&gt; <host-port> &lt;CRLF&gt;</code><br>
 *
 * The argument is a HOST-PORT specification for the data port to be used in
 * data connection. There are defaults for both the user and server data ports,
 * and under normal circumstances this command and its reply are not needed. If
 * this command is used, the argument is the concatenation of a 32-bit internet
 * host address and a 16-bit TCP port address. This address information is
 * broken into 8-bit fields and the value of each field is transmitted as a
 * decimal number (in character string representation). The fields are separated
 * by commas. A port command would be:
 *
 * PORT h1,h2,h3,h4,p1,p2
 *
 * where h1 is the high order 8 bits of the internet host address.
 *
 * @author Io Netty Project
 */
@Sharable
public class PORT extends AbstractCommand {
    private final Logger LOG = LoggerFactory.getLogger(PORT.class);

    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        // reset state variables
        channel.resetState();

        // argument check
        if (!request.hasArgument()) {
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                    "PORT", null));
            return;
        }

        // is port enabled
        DataConnectionConfiguration dataCfg = channel.getListener().getDataConnectionConfiguration();
        if (!dataCfg.isActiveEnabled()) {
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "PORT.disabled", null));
            return;
        }

        InetSocketAddress address;
        try {
            address = SocketAddressEncoder.decode(request.getArgument());

            // port must not be 0
            if(address.getPort() == 0) {
                throw new IllegalPortException("PORT port must not be 0");
            }
        } catch (IllegalInetAddressException e) {
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "PORT", null));
            return;
        } catch (IllegalPortException e) {
            LOG.debug("Invalid data port: " + request.getArgument(), e);
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                    "PORT.invalid", null));
            return;
        } catch (UnknownHostException e) {
            LOG.debug("Unknown host", e);
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                    "PORT.host", null));
            return;
        }

        // check IP
        if (dataCfg.isActiveIpCheck()) {
            InetAddress clientAddr = channel.remoteAddress().getAddress();
            if (!address.getAddress().equals(clientAddr)) {
                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                        "PORT.mismatch", null));
                return;
            }
        }

        channel.getDataConnection().initActiveDataConnection(address);
        channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                FtpReply.REPLY_200_COMMAND_OKAY, "PORT", null));
    }
}
