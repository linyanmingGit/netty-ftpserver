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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * Client-Server listing negotation. Instruct the server what listing types to
 * include in machine directory/file listings.
 *
 * @author Io Netty Project
 */
@Sharable
public class OPTS_MLST extends AbstractCommand {

    private final static String[] AVAILABLE_TYPES = { "Size", "Modify", "Type",
            "Perm" };
    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
// reset state
        channel.resetState();

        // get the listing types
        String argument = request.getArgument();

        String listTypes;
        String types[];
        int spIndex = argument.indexOf(' ');
        if (spIndex == -1) {
            types = new String[0];
            listTypes = "";
        } else {
            listTypes = argument.substring(spIndex + 1);

            // parse all the type tokens
            StringTokenizer st = new StringTokenizer(listTypes, ";");
            types = new String[st.countTokens()];
            for (int i = 0; i < types.length; ++i) {
                types[i] = st.nextToken();
            }
        }
        // set the list types
        String[] validatedTypes = validateSelectedTypes(types);
        if (validatedTypes != null) {
            channel.setAttribute("MLST.types", validatedTypes);
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_200_COMMAND_OKAY, "OPTS.MLST", listTypes));
        } else {
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                    "OPTS.MLST", listTypes));
        }
    }

    private String[] validateSelectedTypes(final String types[]) {

        // ignore null types
        if (types == null) {
            return new String[0];
        }

        List<String> selectedTypes = new ArrayList<String>();
        // check all the types
        for (int i = 0; i < types.length; ++i) {
            for (int j = 0; j < AVAILABLE_TYPES.length; ++j) {
                if (AVAILABLE_TYPES[j].equalsIgnoreCase(types[i])) {
                    selectedTypes.add(AVAILABLE_TYPES[j]);
                    break;
                }
            }
        }

        return selectedTypes.toArray(new String[0]);
    }
}
