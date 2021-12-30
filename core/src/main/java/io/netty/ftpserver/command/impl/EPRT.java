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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * The EPRT command allows for the specification of an extended address for the
 * data connection. The extended address MUST consist of the network protocol as
 * well as the network and transport addresses. The format of EPRT is:
 *
 * EPRT<space><d><net-prt><d><net-addr><d><tcp-port><d>
 *
 * @author Io Netty Project
 */
@Sharable
public class EPRT extends AbstractCommand{
    private final Logger LOG = LoggerFactory.getLogger(EPRT.class);

    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {

        // reset state variables
        channel.resetState();

        // argument check
        String arg = request.getArgument();
        if (arg == null) {
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                    "EPRT", null));
            return;
        }

        // is port enabled
        DataConnectionConfiguration dataCfg = channel.getListener()
                .getDataConnectionConfiguration();
        if (!dataCfg.isActiveEnabled()) {
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                    "EPRT.disabled", null));
            return;
        }

        // parse argument
        String host = null;
        String port = null;
        try {
            char delim = arg.charAt(0);
            int lastDelimIdx = arg.indexOf(delim, 3);
            host = arg.substring(3, lastDelimIdx);
            port = arg.substring(lastDelimIdx + 1, arg.length() - 1);
        } catch (Exception ex) {
            LOG.debug("Exception parsing host and port: " + arg, ex);
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS, "EPRT", null));
            return;
        }

        // get data server
        InetAddress dataAddr = null;
        try {
            dataAddr = InetAddress.getByName(host);
        } catch (UnknownHostException ex) {
            LOG.debug("Unknown host: " + host, ex);
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                    "EPRT.host", null));
            return;
        }

        // check IP
        if (dataCfg.isActiveIpCheck()) {
            InetAddress clientAddr = channel.remoteAddress().getAddress();
            if (!dataAddr.equals(clientAddr)) {
                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                        "EPRT.mismatch", null));
                return;
            }
        }

        // get data server port
        int dataPort = 0;
        try {
            dataPort = Integer.parseInt(port);
        } catch (NumberFormatException ex) {
            LOG.debug("Invalid port: " + port, ex);
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                    "EPRT.invalid", null));
            return;
        }

        channel.getDataConnection().initActiveDataConnection(
                new InetSocketAddress(dataAddr, dataPort));
        channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                FtpReply.REPLY_200_COMMAND_OKAY, "EPRT", null));
    }
}
