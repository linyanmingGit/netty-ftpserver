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

import java.io.IOException;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * <code>ABOR &lt;CRLF&gt;</code><br>
 *
 * This command tells the server to abort the previous FTP service command and
 * any associated transfer of data. No action is to be taken if the previous
 * command has been completed (including data transfer). The control connection
 * is not to be closed by the server, but the data connection must be closed.
 * Current implementation does not do anything. As here data transfers are not
 * multi-threaded.
 *
 * @author Io Netty Project
 */
@Sharable
public class ABOR extends AbstractCommand {
    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        // reset state variables
        channel.resetState();

        // and abort any data connection
        channel.getDataConnection().closeDataConnection();
        channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                FtpReply.REPLY_226_CLOSING_DATA_CONNECTION, "ABOR", null));
    }
}
