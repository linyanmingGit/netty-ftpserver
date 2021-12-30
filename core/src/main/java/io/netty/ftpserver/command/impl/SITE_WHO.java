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
import io.netty.ftpserver.ftplet.UserManager;
import io.netty.ftpserver.impl.reply.LocalizedFtpReply;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;

import java.io.IOException;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * Sends the list of all the connected users.
 *
 * @author Io Netty Project
 */
@Sharable
public class SITE_WHO extends AbstractCommand{
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

//        // print all the connected user information
//        StringBuilder sb = new StringBuilder();
//
//        Map<Long, IoSession> sessions = session.getService()
//                .getManagedSessions();
//
//        sb.append('\n');
//        Iterator<IoSession> sessionIterator = sessions.values().iterator();
//
//        while (sessionIterator.hasNext()) {
//            FtpIoSession managedSession = new FtpIoSession(sessionIterator
//                    .next(), context);
//
//            if (!managedSession.isLoggedIn()) {
//                continue;
//            }
//
//            User tmpUsr = managedSession.getUser();
//            sb.append(StringUtils.pad(tmpUsr.getName(), ' ', true, 16));
//
//            if (managedSession.getRemoteAddress() instanceof InetSocketAddress) {
//                sb.append(StringUtils.pad(((InetSocketAddress) managedSession
//                                .getRemoteAddress()).getAddress().getHostAddress(),
//                        ' ', true, 16));
//            }
//            sb.append(StringUtils.pad(DateUtils.getISO8601Date(managedSession
//                    .getLoginTime().getTime()), ' ', true, 20));
//            sb.append(StringUtils.pad(DateUtils.getISO8601Date(managedSession
//                    .getLastAccessTime().getTime()), ' ', true, 20));
//            sb.append('\n');
//        }
//        sb.append('\n');
//        session.write(new DefaultFtpReply(FtpReply.REPLY_200_COMMAND_OKAY, sb
//                .toString()));
    }
}
