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
import io.netty.ftpserver.ftplet.Authentication;
import io.netty.ftpserver.ftplet.AuthenticationFailedException;
import io.netty.ftpserver.ftplet.FileSystemFactory;
import io.netty.ftpserver.ftplet.FileSystemView;
import io.netty.ftpserver.ftplet.FtpException;
import io.netty.ftpserver.ftplet.FtpReply;
import io.netty.ftpserver.ftplet.FtpRequest;
import io.netty.ftpserver.ftplet.User;
import io.netty.ftpserver.ftplet.UserManager;
import io.netty.ftpserver.impl.ServerFtpStatistics;
import io.netty.ftpserver.impl.reply.LocalizedFtpReply;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;
import io.netty.ftpserver.usermanager.AnonymousAuthentication;
import io.netty.ftpserver.usermanager.UsernamePasswordAuthentication;
import io.netty.ftpserver.usermanager.impl.UserMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * <code>PASS &lt;SP&gt; <password> &lt;CRLF&gt;</code><br>
 *
 * The argument field is a Telnet string specifying the user's password. This
 * command must be immediately preceded by the user name command.
 *
 * @author Io Netty Project
 */
@Sharable
public class PASS extends AbstractCommand {
    private final Logger LOG = LoggerFactory.getLogger(PASS.class);

    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        boolean success = false;

        ServerFtpStatistics stat = (ServerFtpStatistics) channel.getContext()
                .getFtpStatistics();
        try {

            // reset state variables
            channel.resetState();

            // argument check
            String password = request.getArgument();


            // check user name
            String userName = channel.getUserArgument();

            if (userName == null && channel.getUser() == null) {
                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        FtpReply.REPLY_503_BAD_SEQUENCE_OF_COMMANDS, "PASS",
                        null));
                return;
            }

            // already logged-in
            if (channel.isLoggedIn()) {
                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        FtpReply.REPLY_202_COMMAND_NOT_IMPLEMENTED, "PASS",
                        null));
                return;
            }

            // anonymous login limit check

            boolean anonymous = userName != null
                    && userName.equals("anonymous");
            if (anonymous) {
                int currAnonLogin = stat.getCurrentAnonymousLoginNumber();
                int maxAnonLogin = channel.getContext().getConnectionConfig()
                        .getMaxAnonymousLogins();
                if(maxAnonLogin == 0) {
                    LOG.debug("Currently {} anonymous users logged in, unlimited allowed", currAnonLogin);
                } else {
                    LOG.debug("Currently {} out of {} anonymous users logged in", currAnonLogin, maxAnonLogin);
                }

                if (currAnonLogin >= maxAnonLogin) {
                    LOG.debug("Too many anonymous users logged in, user will be disconnected");
                    channel.writeAndFlush(LocalizedFtpReply.translate(
                            channel, request, FtpReply.REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
                            "PASS.anonymous", null));
                    return;
                }
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
                channel.writeAndFlush(LocalizedFtpReply.translate(
                        channel, request, FtpReply.REPLY_421_SERVICE_NOT_AVAILABLE_CLOSING_CONTROL_CONNECTION,
                        "PASS.login", null));
                return;
            }

            // authenticate user
            UserManager userManager = channel.getContext().getUserManager();
            User authenticatedUser = null;
            try {
                UserMetadata userMetadata = new UserMetadata();
                userMetadata.setInetAddress(channel.remoteAddress().getAddress());
                userMetadata.setCertificateChain(channel.getContext().getClientCertificates());

                Authentication auth;
                if (anonymous) {
                    auth = new AnonymousAuthentication(userMetadata);
                } else {
                    auth = new UsernamePasswordAuthentication(userName,
                            password, userMetadata);
                }

                authenticatedUser = userManager.authenticate(auth);
            } catch (AuthenticationFailedException e) {
                LOG.warn("User failed to log in");
            } catch (Exception e) {
                authenticatedUser = null;
                LOG.warn("PASS.execute()", e);
            }

            // first save old values so that we can reset them if Ftplets
            // tell us to fail
            User oldUser = channel.getUser();
            String oldUserArgument = channel.getUserArgument();

            if (authenticatedUser != null) {
                if(!authenticatedUser.getEnabled()) {
                    channel.writeAndFlush(LocalizedFtpReply.translate(
                            channel, request, FtpReply.REPLY_530_NOT_LOGGED_IN,
                            "PASS", null));
                    return;
                }


                channel.setUser(authenticatedUser);
                success = true;
            } else {
                channel.setUser(null);
                channel.setUserArgument(null);
            }

            if (!success) {
                // reset due to failure
                channel.setUser(oldUser);
                channel.setUserArgument(oldUserArgument);

                delayAfterLoginFailure(channel.getContext().getConnectionConfig()
                        .getLoginFailureDelay());

                LOG.warn("Login failure - " + userName);
                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        FtpReply.REPLY_530_NOT_LOGGED_IN, "PASS", userName));
                stat.setLoginFail(channel);

                channel.increaseFailedLogins();

                // kick the user if the max number of failed logins is reached
                int maxAllowedLoginFailues = channel.getContext().getConnectionConfig()
                        .getMaxLoginFailures();
                if (maxAllowedLoginFailues != 0
                        && channel.getFailedLogins() >= maxAllowedLoginFailues) {
                    LOG.warn("User exceeded the number of allowed failed logins, session will be closed");

                    channel.close().awaitUninterruptibly(10000);
                }

                return;
            }

            // update different objects
            FileSystemFactory fmanager = channel.getContext().getFileSystemManager();
            FileSystemView fsview = fmanager
                    .createFileSystemView(authenticatedUser);
            channel.setLogin(fsview);
            stat.setLogin(channel);

            // everything is fine - send login ok message
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_230_USER_LOGGED_IN, "PASS", userName));
            if (anonymous) {
                LOG.info("Anonymous login success - " + password);
            } else {
                LOG.info("Login success - " + userName);
            }

        } finally {

            // if login failed - reset user
            if (!success) {
                channel.reinitialize();
            }
        }
    }

    private void delayAfterLoginFailure(final int loginFailureDelay) {

        if (loginFailureDelay > 0) {
            LOG.debug("Waiting for " + loginFailureDelay
                    + " milliseconds due to login failure");

            try {
                Thread.sleep(loginFailureDelay);
            } catch (InterruptedException e) {
                // ignore and go on
            }
        }
    }
}
