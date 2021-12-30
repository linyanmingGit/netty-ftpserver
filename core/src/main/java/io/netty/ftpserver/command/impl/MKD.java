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
 * <code>MKD  &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * This command causes the directory specified in the pathname to be created as
 * a directory (if the pathname is absolute) or as a subdirectory of the current
 * working directory (if the pathname is relative).
 *
 * @author Io Netty Project
 */
@Sharable
public class MKD extends AbstractCommand {
    private final Logger LOG = LoggerFactory.getLogger(MKD.class);

    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        // reset state
        channel.resetState();

        // argument check
        String fileName = request.getArgument();
        if (fileName == null) {
            channel.writeAndFlush(LocalizedFileActionFtpReply.translate(channel, request,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                    "MKD", null, null));
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
                    "MKD.invalid", fileName, file));
            return;
        }

        // check permission
        fileName = file.getAbsolutePath();
        if (!file.isWritable()) {
            channel.writeAndFlush(LocalizedFileActionFtpReply.translate(channel, request,
                    FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
                    "MKD.permission", fileName, file));
            return;
        }

        // check file existance
        if (file.doesExist()) {
            channel.writeAndFlush(LocalizedFileActionFtpReply.translate(channel, request,
                    FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
                    "MKD.exists", fileName, file));
            return;
        }

        // now create directory
        if (file.mkdir()) {
            channel.writeAndFlush(LocalizedFileActionFtpReply.translate(channel, request,
                    FtpReply.REPLY_257_PATHNAME_CREATED, "MKD", fileName, file));

            // write log message
            String userName = channel.getUser().getName();
            LOG.info("Directory create : " + userName + " - " + fileName);

            // notify statistics object
            ServerFtpStatistics ftpStat = (ServerFtpStatistics) channel.getContext()
                    .getFtpStatistics();
            ftpStat.setMkdir(channel, file);

        } else {
            channel.writeAndFlush(LocalizedFileActionFtpReply.translate(channel, request,
                    FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "MKD",
                    fileName, file));
        }
    }
}
