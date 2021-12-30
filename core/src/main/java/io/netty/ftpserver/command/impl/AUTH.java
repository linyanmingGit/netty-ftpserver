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
import io.netty.ftpserver.ssl.ClientAuth;
import io.netty.ftpserver.ssl.SslConfiguration;
import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * This server supports explicit SSL support.
 *
 * @author Io Netty Project
 */
@Sharable
public class AUTH extends AbstractCommand {

    private final Logger LOG = LoggerFactory.getLogger(AUTH.class);

    private static final List<String> VALID_AUTH_TYPES = Arrays.asList("SSL", "TLS", "TLS-C", "TLS-P");

    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        // reset state variables
        channel.resetState();

        // argument check
        if (!request.hasArgument()) {
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                    "AUTH", null));
            return;
        }

        // check SSL configuration
        if (channel.getListener().getSslConfiguration() == null) {
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    431, "AUTH", null));
            return;
        }

        // check parameter
        String authType = request.getArgument().toUpperCase();
        if (VALID_AUTH_TYPES.contains(authType)) {
            if(authType.equals("TLS-C")) {
                authType = "TLS";
            } else if(authType.equals("TLS-P")) {
                authType = "SSL";
            }

            try {
                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        234, "AUTH." + authType, null));
                secureSession(channel, authType);
            } catch (FtpException ex) {
                throw ex;
            } catch (Exception ex) {
                LOG.warn("AUTH.execute()", ex);
                throw new FtpException("AUTH.execute()", ex);
            }
        } else {
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_502_COMMAND_NOT_IMPLEMENTED, "AUTH", null));
        }
    }

    private void secureSession(final FtpChannel channel, final String type)
            throws GeneralSecurityException, FtpException {
        SslConfiguration ssl = channel.getListener().getSslConfiguration();

        if (ssl != null) {
            SSLEngine engine = ssl.getSSLContext().createSSLEngine();
            engine.setUseClientMode(false);
            if (ssl.getClientAuth() == ClientAuth.NEED) {
                engine.setNeedClientAuth(true);
            } else if (ssl.getClientAuth() == ClientAuth.WANT) {
                engine.setWantClientAuth(true);
            }
            channel.pipeline().addFirst("sslFilter", new SslHandler(engine));
            if("SSL".equals(type)) {
                channel.getDataConnection().setSecure(true);
            }
        } else {
            throw new FtpException("Socket factory SSL not configured");
        }
    }
}
