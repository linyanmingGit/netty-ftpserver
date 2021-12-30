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
import io.netty.ftpserver.ftplet.User;
import io.netty.ftpserver.ftplet.UserManager;
import io.netty.ftpserver.impl.reply.LocalizedFtpReply;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;
import io.netty.ftpserver.usermanager.impl.TransferRateRequest;
import io.netty.ftpserver.usermanager.impl.WriteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * This SITE command returns the specified user information.
 *
 * @author Io Netty Project
 */
@Sharable
public class SITE_DESCUSER extends AbstractCommand{
    private final Logger LOG = LoggerFactory.getLogger(SITE_DESCUSER.class);

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

        // get the user name
        String argument = request.getArgument();
        int spIndex = argument.indexOf(' ');
        if (spIndex == -1) {
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_503_BAD_SEQUENCE_OF_COMMANDS,
                    "SITE.DESCUSER", null));
            return;
        }
        String userName = argument.substring(spIndex + 1);

        // check the user existance
        UserManager usrManager = channel.getContext().getUserManager();
        User user = null;
        try {
            if (usrManager.doesExist(userName)) {
                user = usrManager.getUserByName(userName);
            }
        } catch (FtpException ex) {
            LOG.debug("Exception trying to get user from user manager", ex);
        }
        if (user == null) {
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                    "SITE.DESCUSER", userName));
            return;
        }

        // send the user information
        StringBuilder sb = new StringBuilder(128);
        sb.append("\n");
        sb.append("userid          : ").append(user.getName()).append("\n");
        sb.append("userpassword    : ********\n");
        sb.append("homedirectory   : ").append(user.getHomeDirectory()).append(
                "\n");
        sb.append("writepermission : ").append(
                user.authorize(new WriteRequest()) != null).append("\n");
        sb.append("enableflag      : ").append(user.getEnabled()).append("\n");
        sb.append("idletime        : ").append(user.getMaxIdleTime()).append(
                "\n");

        TransferRateRequest transferRateRequest = new TransferRateRequest();
        transferRateRequest = (TransferRateRequest) channel.getUser()
                .authorize(transferRateRequest);

        if (transferRateRequest != null) {
            sb.append("uploadrate      : ").append(
                    transferRateRequest.getMaxUploadRate()).append("\n");
            sb.append("downloadrate    : ").append(
                    transferRateRequest.getMaxDownloadRate()).append("\n");
        } else {
            sb.append("uploadrate      : 0\n");
            sb.append("downloadrate    : 0\n");
        }
        sb.append('\n');
        channel.writeAndFlush(new DefaultFtpReply(FtpReply.REPLY_200_COMMAND_OKAY, sb
                .toString()));
    }
}
