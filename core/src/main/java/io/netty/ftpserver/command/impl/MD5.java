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
import io.netty.ftpserver.util.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * <code>MD5 &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 * <code>MMD5 &lt;SP&gt; &lt;pathnames&gt; &lt;CRLF&gt;</code><br>
 *
 * Returns the MD5 value for a file or multiple files according to
 * draft-twine-ftpmd5-00.txt.
 *
 * @author Io Netty Project
 */
@Sharable
public class MD5 extends AbstractCommand {
    private final Logger LOG = LoggerFactory.getLogger(MD5.class);

    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        // reset state variables
        channel.resetState();

        boolean isMMD5 = false;

        if ("MMD5".equals(request.getCommand())) {
            isMMD5 = true;
        }

        // print file information
        String argument = request.getArgument();

        if (argument == null || argument.trim().length() == 0) {
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_504_COMMAND_NOT_IMPLEMENTED_FOR_THAT_PARAMETER,
                    "MD5.invalid", null));
            return;
        }

        String[] fileNames = null;
        if (isMMD5) {
            fileNames = argument.split(",");
        } else {
            fileNames = new String[] { argument };
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fileNames.length; i++) {
            String fileName = fileNames[i].trim();

            // get file object
            FtpFile file = null;

            try {
                file = channel.getFileSystemView().getFile(fileName);
            } catch (Exception ex) {
                LOG.debug("Exception getting the file object: " + fileName, ex);
            }

            if (file == null) {
                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        FtpReply.REPLY_504_COMMAND_NOT_IMPLEMENTED_FOR_THAT_PARAMETER,
                        "MD5.invalid", fileName));
                return;
            }

            // check file
            if (!file.isFile()) {
                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        FtpReply.REPLY_504_COMMAND_NOT_IMPLEMENTED_FOR_THAT_PARAMETER,
                        "MD5.invalid", fileName));
                return;
            }

            InputStream is = null;
            try {
                is = file.createInputStream(0);
                String md5Hash = md5(is);

                if (i > 0) {
                    sb.append(", ");
                }
                boolean nameHasSpaces = fileName.indexOf(' ') >= 0;
                if(nameHasSpaces) {
                    sb.append('"');
                }
                sb.append(fileName);
                if(nameHasSpaces) {
                    sb.append('"');
                }
                sb.append(' ');
                sb.append(md5Hash);

            } catch (NoSuchAlgorithmException e) {
                LOG.debug("MD5 algorithm not available", e);
                channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                        FtpReply.REPLY_502_COMMAND_NOT_IMPLEMENTED,
                        "MD5.notimplemened", null));
            } finally {
                if (isMMD5) {
                    channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                            252, "MMD5", sb.toString()));
                } else {
                    channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                            251, "MD5", sb.toString()));
                }
                IoUtils.close(is);
            }
        }
    }

    /**
     * @param is
     *            InputStream for which the MD5 hash is calculated
     * @return The hash of the content in the input stream
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private String md5(InputStream is) throws IOException,
            NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        DigestInputStream dis = new DigestInputStream(is, digest);

        byte[] buffer = new byte[1024];

        int read = dis.read(buffer);
        while (read > -1) {
            read = dis.read(buffer);
        }

        return new String(encodeHex(dis.getMessageDigest().digest()));
    }

    /**
     * Converts an array of bytes into an array of characters representing the
     * hexidecimal values of each byte in order. The returned array will be
     * double the length of the passed array, as it takes two characters to
     * represent any given byte.
     *
     * @param data
     *            a byte[] to convert to Hex characters
     * @return A char[] containing hexidecimal characters
     */
    public static char[] encodeHex(byte[] data) {

        int l = data.length;

        char[] out = new char[l << 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS[0x0F & data[i]];
        }

        return out;
    }

    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

}
