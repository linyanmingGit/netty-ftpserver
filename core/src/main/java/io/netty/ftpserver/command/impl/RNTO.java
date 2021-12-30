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
import io.netty.ftpserver.ftplet.FtpFile;
import io.netty.ftpserver.ftplet.FtpReply;
import io.netty.ftpserver.ftplet.FtpRequest;
import io.netty.ftpserver.impl.reply.LocalizedRenameFtpReply;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * <code>RNTO &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * This command specifies the new pathname of the file specified in the
 * immediately preceding "rename from" command. Together the two commands cause
 * a file to be renamed.
 *
 * @author Io Netty Project
 */
@Sharable
public class RNTO extends AbstractCommand {
    private final Logger LOG = LoggerFactory.getLogger(RNTO.class);

    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        try {

            // argument check
            String toFileStr = request.getArgument();
            if (toFileStr == null) {
                channel.writeAndFlush(LocalizedRenameFtpReply.translate(channel, request,
                        FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                        "RNTO", null, null, null));
                return;
            }

            // get the "rename from" file object
            FtpFile frFile = channel.getRenameFrom();
            if (frFile == null) {
                channel.writeAndFlush(LocalizedRenameFtpReply.translate(channel, request,
                        FtpReply.REPLY_503_BAD_SEQUENCE_OF_COMMANDS, "RNTO",
                        null, null, null));
                return;
            }

            // get target file
            FtpFile toFile = null;
            try {
                toFile = channel.getFileSystemView().getFile(toFileStr);
            } catch (Exception ex) {
                LOG.debug("Exception getting file object", ex);
            }
            if (toFile == null) {
                channel.writeAndFlush(LocalizedRenameFtpReply.translate(channel, request,
                        FtpReply.REPLY_553_REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED,
                        "RNTO.invalid", null, frFile, toFile));
                return;
            }
            toFileStr = toFile.getAbsolutePath();

            // check permission
            if (!toFile.isWritable()) {
                channel.writeAndFlush(LocalizedRenameFtpReply.translate(channel, request,
                        FtpReply.REPLY_553_REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED,
                        "RNTO.permission", null, frFile, toFile));
                return;
            }

            // check file existence
            if (!frFile.doesExist()) {
                channel.writeAndFlush(LocalizedRenameFtpReply.translate(channel, request,
                        FtpReply.REPLY_553_REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED,
                        "RNTO.missing", null, frFile, toFile));
                return;
            }

            // save away the old path
            String logFrFileAbsolutePath = frFile.getAbsolutePath();

            // now rename
            if (frFile.move(toFile)) {
                channel.writeAndFlush(LocalizedRenameFtpReply.translate(channel, request,
                        FtpReply.REPLY_250_REQUESTED_FILE_ACTION_OKAY, "RNTO",
                        toFileStr, frFile, toFile));

                LOG.info("File rename from \"{}\" to \"{}\"", logFrFileAbsolutePath,
                        toFile.getAbsolutePath());
            } else {
                channel.writeAndFlush(LocalizedRenameFtpReply.translate(channel, request,
                        FtpReply.REPLY_553_REQUESTED_ACTION_NOT_TAKEN_FILE_NAME_NOT_ALLOWED,
                        "RNTO", toFileStr, frFile, toFile));
            }

        } finally {
            channel.resetState();
        }
    }
}
