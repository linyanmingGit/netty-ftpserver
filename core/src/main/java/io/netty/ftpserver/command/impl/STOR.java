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
import io.netty.ftpserver.ftplet.DataConnection;
import io.netty.ftpserver.ftplet.DataConnectionFactory;
import io.netty.ftpserver.ftplet.DefaultFtpReply;
import io.netty.ftpserver.ftplet.FtpException;
import io.netty.ftpserver.ftplet.FtpFile;
import io.netty.ftpserver.ftplet.FtpReply;
import io.netty.ftpserver.ftplet.FtpRequest;
import io.netty.ftpserver.impl.IODataConnectionFactory;
import io.netty.ftpserver.impl.ServerFtpStatistics;
import io.netty.ftpserver.impl.reply.LocalizedDataTransferFtpReply;
import io.netty.ftpserver.impl.reply.LocalizedFtpReply;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;
import io.netty.ftpserver.util.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * <code>STOR &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * This command causes the server-DTP to accept the data transferred via the
 * data connection and to store the data as a file at the server site. If the
 * file specified in the pathname exists at the server site, then its contents
 * shall be replaced by the data being transferred. A new file is created at the
 * server site if the file specified in the pathname does not already exist.
 *
 * @author Io Netty Project
 */
@Sharable
public class STOR extends AbstractCommand {
    private final Logger LOG = LoggerFactory.getLogger(STOR.class);

    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        try {

            // get state variable
            long skipLen = channel.getFileOffset();

            // argument check
            String fileName = request.getArgument();
            if (fileName == null) {
                channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(
                        channel, request, FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                        "STOR", null, null));
                return;
            }

            // 24-10-2007 - added check if PORT or PASV is issued, see
            // https://issues.apache.org/jira/browse/FTPSERVER-110
            DataConnectionFactory connFactory = channel.getDataConnection();
            if (connFactory instanceof IODataConnectionFactory) {
                InetAddress address = ((IODataConnectionFactory) connFactory).getInetAddress();
                if (address == null) {
                    channel.writeAndFlush(new DefaultFtpReply(FtpReply.REPLY_503_BAD_SEQUENCE_OF_COMMANDS,
                            "PORT or PASV must be issued first"));
                    return;
                }
            }

            // get filename
            FtpFile file = null;
            try {
                file = channel.getFileSystemView().getFile(fileName);
            } catch (Exception ex) {
                LOG.debug("Exception getting file object", ex);
            }
            if (file == null) {
                channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                        FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
                        "STOR.invalid", fileName, file));
                return;
            }
            fileName = file.getAbsolutePath();

            // get permission
            if (!file.isWritable()) {
                channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                        FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
                        "STOR.permission", fileName, file));
                return;
            }

            // get data connection
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_150_FILE_STATUS_OKAY, "STOR",
                    fileName)).awaitUninterruptibly(10000);

            DataConnection dataConnection;
            try {
                dataConnection = channel.getDataConnection().openConnection();
            } catch (Exception e) {
                LOG.debug("Exception getting the input data stream", e);
                channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                        FtpReply.REPLY_425_CANT_OPEN_DATA_CONNECTION, "STOR",
                        fileName, file));
                return;
            }

            // transfer data
            boolean failure = false;
            OutputStream outStream = null;
            long transSz = 0L;
            try {
                outStream = file.createOutputStream(skipLen);
                transSz = dataConnection.transferFromClient(outStream);

                // attempt to close the output stream so that errors in
                // closing it will return an error to the client (FTPSERVER-119)
                if(outStream != null) {
                    outStream.close();
                }

                // notify the statistics component
                ServerFtpStatistics ftpStat = (ServerFtpStatistics) channel.getContext()
                        .getFtpStatistics();
                ftpStat.setUpload(channel, file, transSz);

            } catch (SocketException ex) {
                LOG.debug("Socket exception during data transfer", ex);
                failure = true;
                channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                        FtpReply.REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED,
                        "STOR", fileName, file));
            } catch (IOException ex) {
                LOG.debug("IOException during data transfer", ex);
                failure = true;
                channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                        FtpReply.REPLY_551_REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN,
                        "STOR", fileName, file));
            } finally {
                // make sure we really close the output stream
                // if data transfer ok - send transfer complete message
                if (!failure) {
                    channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                            FtpReply.REPLY_226_CLOSING_DATA_CONNECTION, "STOR",
                            fileName, file, transSz));
                }
                IoUtils.close(outStream);
            }
        } finally {
            channel.resetState();
            channel.getDataConnection().closeDataConnection();
        }
    }
}
