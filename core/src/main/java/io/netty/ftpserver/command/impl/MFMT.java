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
import io.netty.ftpserver.impl.reply.LocalizedFtpReply;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;
import io.netty.ftpserver.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

/**
 * Command for changing the modified time of a file.
 * <p>
 * Specified in the following document:
 * http://www.omz13.com/downloads/draft-somers-ftp-mfxx-00.html#anchor8
 * </p>
 *
 * @author Io Netty Project
 */
@Sharable
public class MFMT extends AbstractCommand {
    private final Logger LOG = LoggerFactory.getLogger(MFMT.class);

    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        // reset state variables
        channel.resetState();

        String argument = request.getArgument();

        if (argument == null || argument.trim().length() == 0) {
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                    "MFMT.invalid", null));
            return;
        }

        String[] arguments = argument.split(" ",2);

        if(arguments.length != 2) {
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                    "MFMT.invalid", null));
            return;
        }

        String timestamp = arguments[0].trim();

        try {

            Date time = DateUtils.parseFTPDate(timestamp);

            String fileName = arguments[1].trim();

            // get file object
            FtpFile file = null;

            try {
                file = channel.getFileSystemView().getFile(fileName);
            } catch (Exception ex) {
                LOG.debug("Exception getting the file object: " + fileName, ex);
            }

            if (file == null || !file.doesExist()) {
                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
                        "MFMT.filemissing", fileName));
                return;
            }

            // check file
            if (!file.isFile()) {
                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                        "MFMT.invalid", null));
                return;
            }

            // check if we can set date and retrieve the actual date stored for the file.
            if (!file.setLastModified(time.getTime())) {
                // we couldn't set the date, possibly the file was locked
                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        FtpReply.REPLY_450_REQUESTED_FILE_ACTION_NOT_TAKEN, "MFMT",
                        fileName));
                return;
            }

            // all checks okay, lets go
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_213_FILE_STATUS,
                    "MFMT", "ModifyTime=" + timestamp + "; " + fileName));
            return;

        } catch (ParseException e) {
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                    "MFMT.invalid", null));
            return;
        }
    }
}
