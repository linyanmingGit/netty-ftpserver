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

package io.netty.ftpserver.impl.reply;

import io.netty.ftpserver.ftplet.DefaultFtpReply;
import io.netty.ftpserver.ftplet.FtpRequest;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;

/**
 * <strong>Internal class, do not use directly.</strong>
 * 
 * FTP reply translator.
 *
 * @author Io Netty Project
 */
public class LocalizedFtpReply extends DefaultFtpReply {

    public static LocalizedFtpReply translate(FtpChannel channel, FtpRequest request, int code, String subId, String basicMsg) {
        String msg = FtpReplyTranslator.translateMessage(channel, request, code, subId,
                basicMsg);

        return new LocalizedFtpReply(code, msg);
    }

    /**
	 * Creates a new instance of <code>LocalizedFtpReply</code>.
	 * 
	 * @param code
	 *            the reply code
	 * @param message
	 *            the reply text
	 */
    public LocalizedFtpReply(int code, String message) {
        super(code, message);
    }
}
