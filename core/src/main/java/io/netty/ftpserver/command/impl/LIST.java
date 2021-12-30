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
import io.netty.ftpserver.command.impl.listing.DirectoryLister;
import io.netty.ftpserver.command.impl.listing.LISTFileFormater;
import io.netty.ftpserver.command.impl.listing.ListArgument;
import io.netty.ftpserver.command.impl.listing.ListArgumentParser;
import io.netty.ftpserver.ftplet.DataConnection;
import io.netty.ftpserver.ftplet.DataConnectionFactory;
import io.netty.ftpserver.ftplet.DefaultFtpReply;
import io.netty.ftpserver.ftplet.FtpException;
import io.netty.ftpserver.ftplet.FtpFile;
import io.netty.ftpserver.ftplet.FtpReply;
import io.netty.ftpserver.ftplet.FtpRequest;
import io.netty.ftpserver.impl.IODataConnectionFactory;
import io.netty.ftpserver.impl.reply.LocalizedDataTransferFtpReply;
import io.netty.ftpserver.impl.reply.LocalizedFtpReply;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * <code>LIST [&lt;SP&gt; &lt;pathname&gt;] &lt;CRLF&gt;</code><br>
 *
 * This command causes a list to be sent from the server to the passive DTP. If
 * the pathname specifies a directory or other group of files, the server should
 * transfer a list of files in the specified directory. If the pathname
 * specifies a file then the server should send current information on the file.
 * A null argument implies the user's current working or default directory. The
 * data transfer is over the data connection.
 *
 * @author Io Netty Project
 */
@Sharable
public class LIST extends AbstractCommand {
    private final Logger LOG = LoggerFactory.getLogger(LIST.class);

    private static final LISTFileFormater LIST_FILE_FORMATER = new LISTFileFormater();

    private final DirectoryLister directoryLister = new DirectoryLister();

    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        try {

            // reset state variables
            channel.resetState();

            // parse argument
            ListArgument parsedArg = ListArgumentParser.parse(request
                    .getArgument());

            // checl that the directory or file exists
            FtpFile file = channel.getFileSystemView().getFile(parsedArg.getFile());

            if(!file.doesExist()) {
                LOG.debug("Listing on a non-existing file");
                channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                        FtpReply.REPLY_450_REQUESTED_FILE_ACTION_NOT_TAKEN, "LIST",
                        null, file));
                return;
            }

            // 24-10-2007 - added check if PORT or PASV is issued, see
            // https://issues.apache.org/jira/browse/FTPSERVER-110
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
                    FtpReply.REPLY_150_FILE_STATUS_OKAY, "LIST", null));

            DataConnection dataConnection;
            try {
                dataConnection = channel.getDataConnection().openConnection();
            } catch (Exception e) {
                LOG.debug("Exception getting the output data stream", e);
                channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                        FtpReply.REPLY_425_CANT_OPEN_DATA_CONNECTION, "LIST",
                        null, file));
                return;
            }

            // transfer listing data
            boolean failure = false;
            String dirList = directoryLister.listFiles(parsedArg,
                    channel.getFileSystemView(), LIST_FILE_FORMATER);
            try {
                dataConnection.transferToClient(dirList);
            } catch (SocketException ex) {
                LOG.debug("Socket exception during list transfer", ex);
                failure = true;
                channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                        FtpReply.REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED,
                        "LIST", null, file));
            } catch (IOException ex) {
                LOG.debug("IOException during list transfer", ex);
                failure = true;
                channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                        FtpReply.REPLY_551_REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN,
                        "LIST", null, file));
            } catch (IllegalArgumentException e) {
                LOG.debug("Illegal list syntax: " + request.getArgument(), e);
                // if listing syntax error - send message
                channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                        FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                        "LIST", null, file));
            }

            // if data transfer ok - send transfer complete message
            if (!failure) {
                channel.writeAndFlush(LocalizedDataTransferFtpReply.translate(channel, request,
                        FtpReply.REPLY_226_CLOSING_DATA_CONNECTION, "LIST",
                        null, file, dirList.length()));
            }
        } finally {
            channel.getDataConnection().closeDataConnection();
        }
    }
}
