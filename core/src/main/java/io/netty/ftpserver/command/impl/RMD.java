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
import io.netty.ftpserver.impl.ServerFtpStatistics;
import io.netty.ftpserver.impl.reply.LocalizedFileActionFtpReply;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * <code>RMD  &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * This command causes the directory specified in the pathname to be removed as
 * a directory (if the pathname is absolute) or as a subdirectory of the current
 * working directory (if the pathname is relative).
 *
 * @author Io Netty Project
 */
@Sharable
public class RMD extends AbstractCommand {
    private final Logger LOG = LoggerFactory.getLogger(RMD.class);

    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        // reset state variables
        channel.resetState();

        // argument check
        String fileName = request.getArgument();
        if (fileName == null) {
            channel.writeAndFlush(LocalizedFileActionFtpReply.translate(channel, request,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                    "RMD", null, null));
            return;
        }

        // get file object
        FtpFile file = null;
        try {
            file = channel.getFileSystemView().getFile(fileName);
        } catch (Exception ex) {
            LOG.debug("Exception getting file object", ex);
        }
        if (file == null) {
            channel.writeAndFlush(LocalizedFileActionFtpReply.translate(channel, request,
                    FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
                    "RMD.permission", fileName, file));
            return;
        }

        fileName = file.getAbsolutePath();

        // first let's make sure the path is a directory
        if (!file.isDirectory()) {
            channel.writeAndFlush(LocalizedFileActionFtpReply.translate(channel, request,
                    FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
                    "RMD.invalid", fileName, file));
            return;
        }

        // then make sure that the client did not request the deletion of
        // current working directory.
        FtpFile cwd = channel.getFileSystemView().getWorkingDirectory();
        if(file.equals(cwd)) {
            channel.writeAndFlush(LocalizedFileActionFtpReply.translate(channel, request,
                    FtpReply.REPLY_450_REQUESTED_FILE_ACTION_NOT_TAKEN, "RMD.busy",
                    fileName, file));
            return;
        }

        // now check to see if the user have permission to delete this directory
        if (!file.isRemovable()) {
            channel.writeAndFlush(LocalizedFileActionFtpReply.translate(channel, request,
                    FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
                    "RMD.permission", fileName, file));
            return;
        }

        // now delete directory
        if (file.delete()) {
            channel.writeAndFlush(LocalizedFileActionFtpReply.translate(channel, request,
                    FtpReply.REPLY_250_REQUESTED_FILE_ACTION_OKAY, "RMD",
                    fileName, file));

            // write log message
            String userName = channel.getUser().getName();
            LOG.info("Directory remove : " + userName + " - " + fileName);

            // notify statistics object
            ServerFtpStatistics ftpStat = (ServerFtpStatistics) channel.getContext()
                    .getFtpStatistics();
            ftpStat.setRmdir(channel, file);

        } else {

            channel.writeAndFlush(LocalizedFileActionFtpReply.translate(channel, request,
                    FtpReply.REPLY_450_REQUESTED_FILE_ACTION_NOT_TAKEN, "RMD",
                    fileName, file));
        }
    }
}
