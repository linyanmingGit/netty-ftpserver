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
import io.netty.ftpserver.ftplet.FtpStatistics;
import io.netty.ftpserver.ftplet.UserManager;
import io.netty.ftpserver.impl.reply.LocalizedFtpReply;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;
import io.netty.ftpserver.util.DateUtils;

import java.io.IOException;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * Show all statistics information.
 *
 * @author Io Netty Project
 */
@Sharable
public class SITE_STAT extends AbstractCommand {
    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        // reset state variables
        channel.resetState();

        // only administrator can execute this
        UserManager userManager = channel.getContext().getUserManager();
        boolean isAdmin = userManager.isAdmin(channel.getUser().getName());
        if (!isAdmin) {
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_530_NOT_LOGGED_IN, "SITE", null));
            return;
        }

        // get statistics information
        FtpStatistics stat = channel.getContext().getFtpStatistics();
        StringBuilder sb = new StringBuilder(256);
        sb.append('\n');
        sb.append("Start Time               : ").append(
                DateUtils.getISO8601Date(stat.getStartTime().getTime()))
                .append('\n');
        sb.append("File Upload Number       : ").append(
                stat.getTotalUploadNumber()).append('\n');
        sb.append("File Download Number     : ").append(
                stat.getTotalDownloadNumber()).append('\n');
        sb.append("File Delete Number       : ").append(
                stat.getTotalDeleteNumber()).append('\n');
        sb.append("File Upload Bytes        : ").append(
                stat.getTotalUploadSize()).append('\n');
        sb.append("File Download Bytes      : ").append(
                stat.getTotalDownloadSize()).append('\n');
        sb.append("Directory Create Number  : ").append(
                stat.getTotalDirectoryCreated()).append('\n');
        sb.append("Directory Remove Number  : ").append(
                stat.getTotalDirectoryRemoved()).append('\n');
        sb.append("Current Logins           : ").append(
                stat.getCurrentLoginNumber()).append('\n');
        sb.append("Total Logins             : ").append(
                stat.getTotalLoginNumber()).append('\n');
        sb.append("Current Anonymous Logins : ").append(
                stat.getCurrentAnonymousLoginNumber()).append('\n');
        sb.append("Total Anonymous Logins   : ").append(
                stat.getTotalAnonymousLoginNumber()).append('\n');
        sb.append("Current Connections      : ").append(
                stat.getCurrentConnectionNumber()).append('\n');
        sb.append("Total Connections        : ").append(
                stat.getTotalConnectionNumber()).append('\n');
        sb.append('\n');
        channel.writeAndFlush(new DefaultFtpReply(FtpReply.REPLY_200_COMMAND_OKAY, sb
                .toString()));
    }
}
