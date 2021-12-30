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
import io.netty.ftpserver.command.impl.listing.FileFormater;
import io.netty.ftpserver.command.impl.listing.ListArgument;
import io.netty.ftpserver.command.impl.listing.ListArgumentParser;
import io.netty.ftpserver.command.impl.listing.MLSTFileFormater;
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
 * <code>MLST &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * Returns info on the file over the control connection.
 *
 * @author Io Netty Project
 */
@Sharable
public class MLST extends AbstractCommand{
    private final Logger LOG = LoggerFactory.getLogger(MLST.class);

    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        // reset state variables
        channel.resetState();

        // parse argument
        ListArgument parsedArg = ListArgumentParser.parse(request.getArgument());

        FtpFile file = null;
        try {
            file = channel.getFileSystemView().getFile(parsedArg.getFile());
            if (file != null && file.doesExist()) {
                FileFormater formater = new MLSTFileFormater((String[]) channel
                        .getAttribute("MLST.types"));
                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        FtpReply.REPLY_250_REQUESTED_FILE_ACTION_OKAY, "MLST",
                        formater.format(file)));
            } else {
                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                        "MLST", null));
            }
        } catch (FtpException ex) {
            LOG.debug("Exception sending the file listing", ex);
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                    "MLST", null));
        }
    }
}
