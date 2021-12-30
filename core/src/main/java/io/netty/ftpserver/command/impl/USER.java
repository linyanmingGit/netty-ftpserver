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
import io.netty.ftpserver.ftplet.User;
import io.netty.ftpserver.impl.ServerFtpStatistics;
import io.netty.ftpserver.impl.reply.LocalizedFtpReply;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;
import io.netty.ftpserver.usermanager.impl.ConcurrentLoginRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * <code>USER &lt;SP&gt; &lt;username&gt; &lt;CRLF&gt;</code><br>
 *
 * The argument field is a Telnet string identifying the user. The user
 * identification is that which is required by the server for access to its file
 * system. This command will normally be the first command transmitted by the
 * user after the control connections are made.
 *
 * @author Io Netty Project
 */
@Sharable
public class USER extends AbstractCommand {
    private final Logger LOG = LoggerFactory.getLogger(USER.class);

    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        boolean success = false;
        ServerFtpStatistics stat = (ServerFtpStatistics) channel.getContext()
                .getFtpStatistics();
        try {

            // reset state variables
            channel.resetState();

            // argument check
            String userName = request.getArgument();
            if (userName == null) {
                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                        "USER", null));
                return;
            }

            // already logged-in
            User user = channel.getUser();
            if (channel.isLoggedIn()) {
                if (userName.equals(user.getName())) {
                    channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                            FtpReply.REPLY_230_USER_LOGGED_IN, "USER",null));
                    success = true;
                } else {
                    channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                            530, "USER.invalid", null));
                }
                return;
            }

            // anonymous login is not enabled
            boolean anonymous = userName.equals("anonymous");
            if (anonymous && (!channel.getContext().getConnectionConfig()
                    .isAnonymousLoginEnabled())) {
                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        FtpReply.REPLY_530_NOT_LOGGED_IN, "USER.anonymous",
                        null));
                return;
            }

            // anonymous login limit check
            int currAnonLogin = stat.getCurrentAnonymousLoginNumber();
            int maxAnonLogin = channel.getContext().getConnectionConfig()
                    .getMaxAnonymousLogins();
            if(maxAnonLogin == 0) {
                LOG.debug("Currently {} anonymous users logged in, unlimited allowed", currAnonLogin);
            } else {
                LOG.debug("Currently {} out of {} anonymous users logged in", currAnonLogin, maxAnonLogin);
            }
            if (anonymous && (currAnonLogin >= maxAnonLogin)) {
                LOG.debug("Too many anonymous users logged in, user will be disconnected");

                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        FtpReply.REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
                        "USER.anonymous", null));
                return;
            }

            // login limit check
            int currLogin = stat.getCurrentLoginNumber();
            int maxLogin = channel.getContext().getConnectionConfig().getMaxLogins();

            if(maxLogin == 0) {
                LOG.debug("Currently {} users logged in, unlimited allowed", currLogin);
            } else {
                LOG.debug("Currently {} out of {} users logged in", currLogin, maxLogin);
            }

            if (maxLogin != 0 && currLogin >= maxLogin) {
                LOG.debug("Too many users logged in, user will be disconnected");

                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        FtpReply.REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
                        "USER.login", null));
                return;
            }

            User configUser = channel.getContext().getUserManager().getUserByName(userName);
            if (configUser != null) {
                // user login limit check

                ConcurrentLoginRequest loginRequest = new ConcurrentLoginRequest(
                        stat.getCurrentUserLoginNumber(configUser) + 1,
                        stat.getCurrentUserLoginNumber(configUser, channel.remoteAddress().getAddress()) + 1);

                if (configUser.authorize(loginRequest) == null) {
                    LOG.debug("User logged in too many sessions, user will be disconnected");
                    channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                            FtpReply.REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
                            "USER.login", null));
                    return;
                }
            }

            // finally set the user name
            success = true;
            channel.setUserArgument(userName);
            if (anonymous) {
                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        FtpReply.REPLY_331_USER_NAME_OKAY_NEED_PASSWORD,
                        "USER.anonymous", userName));
            } else {
                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        FtpReply.REPLY_331_USER_NAME_OKAY_NEED_PASSWORD,
                        "USER", userName));
            }
        } finally {

            // if not ok - close connection
            if (!success) {
                LOG.debug("User failed to login, session will be closed");
                channel.close().awaitUninterruptibly(10000);
            }
        }
    }
}
