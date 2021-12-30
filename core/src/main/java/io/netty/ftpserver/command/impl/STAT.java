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
import io.netty.ftpserver.command.impl.listing.DirectoryLister;
import io.netty.ftpserver.command.impl.listing.LISTFileFormater;
import io.netty.ftpserver.command.impl.listing.ListArgument;
import io.netty.ftpserver.command.impl.listing.ListArgumentParser;
import io.netty.ftpserver.ftplet.FtpException;
import io.netty.ftpserver.ftplet.FtpFile;
import io.netty.ftpserver.ftplet.FtpReply;
import io.netty.ftpserver.ftplet.FtpRequest;
import io.netty.ftpserver.impl.reply.LocalizedDataTransferFtpReply;
import io.netty.ftpserver.impl.reply.LocalizedFileActionFtpReply;
import io.netty.ftpserver.impl.reply.LocalizedFtpReply;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;

import java.io.IOException;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * <code>STAT [&lt;SP&gt; &lt;pathname&gt;] &lt;CRLF&gt;</code><br>
 *
 * This command shall cause a status response to be sent over the control
 * connection in the form of a reply.
 *
 * @author Io Netty Project
 */
@Sharable
public class STAT extends AbstractCommand {
    private static final LISTFileFormater LIST_FILE_FORMATER = new LISTFileFormater();

    private final DirectoryLister directoryLister = new DirectoryLister();

    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        // reset state variables
        channel.resetState();

        if(request.getArgument() != null) {
            ListArgument parsedArg = ListArgumentParser.parse(request.getArgument());

            // check that the directory or file exists
            FtpFile file = null;
            try {
                file = channel.getFileSystemView().getFile(parsedArg.getFile());
                if(!file.doesExist()) {
                    channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                            FtpReply.REPLY_450_REQUESTED_FILE_ACTION_NOT_TAKEN, "LIST",
                            null, file));
                    return;
                }

                String dirList = directoryLister.listFiles(parsedArg,
                        channel.getFileSystemView(), LIST_FILE_FORMATER);

                int replyCode;
                if(file.isDirectory()) {
                    replyCode = FtpReply.REPLY_212_DIRECTORY_STATUS;
                } else {
                    replyCode = FtpReply.REPLY_213_FILE_STATUS;
                }

                channel.writeAndFlush(LocalizedFileActionFtpReply.translate(channel, request,
                        replyCode, "STAT",
                        dirList, file));

            } catch (FtpException e) {
                channel.writeAndFlush(LocalizedFileActionFtpReply.translate(channel, request,
                        FtpReply.REPLY_450_REQUESTED_FILE_ACTION_NOT_TAKEN,
                        "STAT", null, file));
            }

        } else {
            // write the status info
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_211_SYSTEM_STATUS_REPLY, "STAT", null));
        }
    }
}
