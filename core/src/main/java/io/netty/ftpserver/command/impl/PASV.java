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
import io.netty.ftpserver.DataConnectionException;
import io.netty.ftpserver.ftplet.FtpException;
import io.netty.ftpserver.ftplet.FtpReply;
import io.netty.ftpserver.ftplet.FtpRequest;
import io.netty.ftpserver.impl.ServerDataConnectionFactory;
import io.netty.ftpserver.impl.reply.LocalizedFtpReply;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;
import io.netty.ftpserver.util.SocketAddressEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * <code>PASV &lt;CRLF&gt;</code><br>
 *
 * This command requests the server-DTP to "listen" on a data port (which is not
 * its default data port) and to wait for a connection rather than initiate one
 * upon receipt of a transfer command. The response to this command includes the
 * host and port address this server is listening on.
 *
 * FTPServer allows the user to configure an "external address" at the listener
 * level which will be the one reported by PASV response. This might solve some of
 * the issues with NATed addresses ( the reported IP is internal and not accesible by the
 * client) but not all of them - for example, if the FTPServer host has a dynamic IP
 * address. The solution for all these cases would be switching to the EPSV command,
 * which doesn't report any IP address back.
 *
 * In the case that EPSV command isn't available to the client, FTPServer integrators
 * can implement their own getPassiveExternalAddress to modify how the External IP
 * is resolved. One common approach would be retrieving this address from a webpage
 * which prints out the visitor's IP address. Another approach could be returning the
 * external IP address from  the USER configuration(as when a single server is mapped
 * into different addresses).
 *
 * Please note that PASV command is an internal classes and thus shouldn't be extended.
 * Integrators may decide to extend it at their own risk, but they should be aware that
 * the 'internal API' can be changed at any moment. Besides, in some environments
 * (OSGI) internal classes are not accesible and thus overriding won't work.
 * Still, the getPassiveExternalAddress method is provided for convenience so the
 * code to overwrite when reimplementing PASV command can be easily located.
 *
 *
 * @author Io Netty Project
 */
@Sharable
public class PASV extends AbstractCommand {
    private final Logger LOG = LoggerFactory.getLogger(PASV.class);

    @Override
    public void execute(ChannelHandlerContext context, FtpChannel channel, FtpRequest request) throws IOException, FtpException {
        // reset state variables
        channel.resetState();

        // set data connection
        ServerDataConnectionFactory dataCon = channel.getDataConnection();
        String externalPassiveAddress = getPassiveExternalAddress(channel);

        try {

            InetSocketAddress dataConAddress = dataCon.initPassiveDataConnection();

            // get connection info
            InetAddress servAddr;
            if (externalPassiveAddress != null) {
                servAddr = resolveAddress(externalPassiveAddress);
            } else {
                servAddr = dataConAddress.getAddress();
            }

            // send connection info to client
            InetSocketAddress externalDataConAddress = new InetSocketAddress(
                    servAddr, dataConAddress.getPort());

            String addrStr = SocketAddressEncoder.encode(externalDataConAddress);
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_227_ENTERING_PASSIVE_MODE, "PASV", addrStr));
        } catch (DataConnectionException e) {
            LOG.warn("Failed to open passive data connection", e);
            channel.writeAndFlush(LocalizedFtpReply.translate(channel, request,
                    FtpReply.REPLY_425_CANT_OPEN_DATA_CONNECTION,
                    "PASV", null));
            return;
        }
    }

    private InetAddress resolveAddress(String host) throws DataConnectionException {
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException ex) {
            throw new DataConnectionException(ex.getLocalizedMessage(), ex);
        }
    }

    /*
     * (non-Javadoc)
     * Returns the server's IP address which will be reported by the PASV response.
     *
     */
    protected String getPassiveExternalAddress(final FtpChannel channel) {
        return channel.getListener().getDataConnectionConfiguration().getPassiveExernalAddress();

    }
}
