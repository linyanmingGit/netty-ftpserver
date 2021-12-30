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
import io.netty.ftpserver.ftplet.DefaultFtpReply;
import io.netty.ftpserver.ftplet.FtpException;
import io.netty.ftpserver.ftplet.FtpReply;
import io.netty.ftpserver.ftplet.FtpRequest;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * Displays the FTP server timezone in RFC 822 format.
 *
 * @author Io Netty Project
 */
@Sharable
public class SITE_ZONE extends AbstractCommand {
    private final static SimpleDateFormat TIMEZONE_FMT = new SimpleDateFormat(
            "Z");

    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
// reset state variables
        channel.resetState();

        // send timezone data
        String timezone = TIMEZONE_FMT.format(new Date());
        channel.writeAndFlush(new DefaultFtpReply(FtpReply.REPLY_200_COMMAND_OKAY,
                timezone));
    }
}
