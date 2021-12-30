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
import io.netty.ftpserver.message.MessageResource;

import java.io.IOException;
import java.util.List;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * A new command "LANG" is added to the FTP command set to allow server-FTP
 * process to determine in which language to present server greetings and the
 * textual part of command responses.
 *
 * @author Io Netty Project
 */
@Sharable
public class LANG extends AbstractCommand {
    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        // reset state
        channel.resetState();

        // default language
        String language = request.getArgument();
        if (language == null) {
            channel.setLanguage(null);
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_200_COMMAND_OKAY, "LANG", null));
            return;
        }

        // check and set language
        language = language.toLowerCase();
        MessageResource msgResource = channel.getContext().getMessageResource();
        List<String> availableLanguages = msgResource.getAvailableLanguages();
        if (availableLanguages != null) {
            for (int i = 0; i < availableLanguages.size(); ++i) {
                if (availableLanguages.get(i).equals(language)) {
                    channel.setLanguage(language);
                    channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                            FtpReply.REPLY_200_COMMAND_OKAY, "LANG", null));
                    return;
                }
            }
        }

        // not found - send error message
        channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                FtpReply.REPLY_504_COMMAND_NOT_IMPLEMENTED_FOR_THAT_PARAMETER,
                "LANG", null));
    }
}
