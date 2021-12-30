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
import io.netty.ftpserver.ftplet.DataType;
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * <code>RETR &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * This command causes the server-DTP to transfer a copy of the file, specified
 * in the pathname, to the server- or user-DTP at the other end of the data
 * connection. The status and contents of the file at the server site shall be
 * unaffected.
 *
 * @author Io Netty Project
 */
@Sharable
public class RETR extends AbstractCommand {
    private final Logger LOG = LoggerFactory.getLogger(RETR.class);

    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        try {

            // get state variable
            long skipLen = channel.getFileOffset();

            // argument check
            String fileName = request.getArgument();
            if (fileName == null) {
                channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                        FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                        "RETR", null, null));
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
                channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                        FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
                        "RETR.missing", fileName, file));
                return;
            }
            fileName = file.getAbsolutePath();

            // check file existance
            if (!file.doesExist()) {
                channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                        FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
                        "RETR.missing", fileName, file));
                return;
            }

            // check valid file
            if (!file.isFile()) {
                channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                        FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
                        "RETR.invalid", fileName, file));
                return;
            }

            // check permission
            if (!file.isReadable()) {
                channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                        FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
                        "RETR.permission", fileName, file));
                return;
            }

            // 24-10-2007 - added check if PORT or PASV is issued, see
            // https://issues.apache.org/jira/browse/FTPSERVER-110
            //TODO move this block of code into the super class. Also, it makes
            //sense to have this as the first check before checking everything
            //else such as the file and its permissions.
            DataConnectionFactory connFactory = channel.getDataConnection();
            if (connFactory instanceof IODataConnectionFactory) {
                InetAddress address = ((IODataConnectionFactory) connFactory)
                        .getInetAddress();
                if (address == null) {
                    channel.writeAndFlush(new DefaultFtpReply(
                            FtpReply.REPLY_503_BAD_SEQUENCE_OF_COMMANDS,
                            "PORT or PASV must be issued first"));
                    return;
                }
            }

            // get data connection
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_150_FILE_STATUS_OKAY, "RETR", null));

            // send file data to client
            boolean failure = false;
            InputStream is = null;

            DataConnection dataConnection;
            try {
                dataConnection = channel.getDataConnection().openConnection();
            } catch (Exception e) {
                LOG.debug("Exception getting the output data stream", e);
                channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                        FtpReply.REPLY_425_CANT_OPEN_DATA_CONNECTION, "RETR",
                        null, file));
                return;
            }

            long transSz = 0L;
            try {

                // open streams
                is = openInputStream(channel, file, skipLen);

                // transfer data
                transSz = dataConnection.transferToClient(is);
                // attempt to close the input stream so that errors in
                // closing it will return an error to the client (FTPSERVER-119)
                if(is != null) {
                    is.close();
                }

                // notify the statistics component
                ServerFtpStatistics ftpStat = (ServerFtpStatistics) channel.getContext()
                        .getFtpStatistics();
                if (ftpStat != null) {
                    ftpStat.setDownload(channel, file, transSz);
                }

            } catch (SocketException ex) {
                LOG.debug("Socket exception during data transfer", ex);
                failure = true;
                channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                        FtpReply.REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED,
                        "RETR", fileName, file, transSz));
            } catch (IOException ex) {
                LOG.debug("IOException during data transfer", ex);
                failure = true;
                channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                        FtpReply.REPLY_551_REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN,
                        "RETR", fileName, file, transSz));
            } finally {
                // if data transfer ok - send transfer complete message
                if (!failure) {
                    channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                            FtpReply.REPLY_226_CLOSING_DATA_CONNECTION, "RETR",
                            fileName, file, transSz));

                }

                // make sure we really close the input stream
                IoUtils.close(is);
            }
        } finally {
            channel.resetState();
            channel.getDataConnection().closeDataConnection();
        }
    }

    /**
     * Skip length and open input stream.
     */
    public InputStream openInputStream(FtpChannel channel, FtpFile file,
                                       long skipLen) throws IOException {
        InputStream in;
        if (channel.getDataType() == DataType.ASCII) {
            int c;
            long offset = 0L;
            in = new BufferedInputStream(file.createInputStream(0L));
            while (offset++ < skipLen) {
                if ((c = in.read()) == -1) {
                    throw new IOException("Cannot skip");
                }
                if (c == '\n') {
                    offset++;
                }
            }
        } else {
            in = file.createInputStream(skipLen);
        }
        return in;
    }
}
