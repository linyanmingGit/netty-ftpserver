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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * <code>RNFR &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * This command specifies the old pathname of the file which is to be renamed.
 * This command must be immediately followed by a "rename to" command specifying
 * the new file pathname.
 *
 * @author Io Netty Project
 */
@Sharable
public class RNFR extends AbstractCommand {
    private final Logger LOG = LoggerFactory.getLogger(RNFR.class);

    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        // reset state variable
        channel.resetState();

        // argument check
        String fileName = request.getArgument();
        if (fileName == null) {
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                    "RNFR", null));
            return;
        }

        // get filename
        FtpFile renFr = null;
        try {
            renFr = channel.getFileSystemView().getFile(fileName);
        } catch (Exception ex) {
            LOG.debug("Exception getting file object", ex);
        }

        // check file
        if (renFr == null) {
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN, "RNFR",
                    fileName));
        } else {
            channel.setRenameFrom(renFr);
            fileName = renFr.getAbsolutePath();
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_350_REQUESTED_FILE_ACTION_PENDING_FURTHER_INFORMATION,
                    "RNFR", fileName));
        }
    }
}
