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
import io.netty.ftpserver.ftplet.FileSystemView;
import io.netty.ftpserver.ftplet.FtpException;
import io.netty.ftpserver.ftplet.FtpFile;
import io.netty.ftpserver.ftplet.FtpReply;
import io.netty.ftpserver.ftplet.FtpRequest;
import io.netty.ftpserver.impl.reply.LocalizedFileActionFtpReply;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * <code>CWD  &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * This command allows the user to work with a different directory for file
 * storage or retrieval without altering his login or accounting information.
 * Transfer parameters are similarly unchanged. The argument is a pathname
 * specifying a directory.
 *
 * @author Io Netty Project
 */
@Sharable
public class CWD extends AbstractCommand {
    private final Logger LOG = LoggerFactory.getLogger(CWD.class);

    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        // reset state variables
        channel.resetState();

        // get new directory name
        String dirName = "/";
        if (request.hasArgument()) {
            dirName = request.getArgument();
        }

        // change directory
        FileSystemView fsview = channel.getFileSystemView();
        boolean success = false;
        try {
            success = fsview.changeWorkingDirectory(dirName);
        } catch (Exception ex) {
            LOG.debug("Failed to change directory in file system", ex);
        }
        FtpFile cwd = fsview.getWorkingDirectory();
        if (success) {
            dirName = cwd.getAbsolutePath();
            channel.writeAndFlush(LocalizedFileActionFtpReply.translate(channel, request,
                    FtpReply.REPLY_250_REQUESTED_FILE_ACTION_OKAY, "CWD",
                    dirName, cwd));
        } else {
            channel.writeAndFlush(LocalizedFileActionFtpReply.translate(channel, request,
                            FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
                            "CWD", null, cwd));
        }
    }
}
