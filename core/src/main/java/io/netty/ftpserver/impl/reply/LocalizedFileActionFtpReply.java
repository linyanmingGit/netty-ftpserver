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

import io.netty.ftpserver.ftplet.FileActionFtpReply;
import io.netty.ftpserver.ftplet.FtpFile;
import io.netty.ftpserver.ftplet.FtpRequest;
import io.netty.ftpserver.listener.nio.channel.FtpChannel;

/**
 * An implementation of <code>FileActionFtpReply</code>.
 * 
 * @author Io Netty Project
 * 
 */

public class LocalizedFileActionFtpReply extends LocalizedFtpReply implements
	FileActionFtpReply {

	/**
	 * The file or directory that data transfer is related to.
	 */
	private final FtpFile file;

	/**
	 * Creates a new instance of <code>LocalizedFileTransferReply</code>.
	 * 
	 * @param code
	 *            the reply code
	 * @param message
	 *            the detailed message
	 * @param file
	 *            the file or directory the data transfer is related to
	 */
	public LocalizedFileActionFtpReply(int code, String message, FtpFile file) {
		super(code, message);
		this.file = file;
	}

	public FtpFile getFile() {
		return file;
	}

	/**
	 * Returns the localized reply that contains all details about the data
	 * transfer.
	 * 
	 * @param channel
	 *            the FTP channel
	 * @param request
	 *            the FTP request
	 * @param code
	 *            the reply code
	 * @param subId
	 *            the sub message ID
	 * @param basicMsg
	 *            the basic message
	 * @param file
	 *            the file or directory that was transferred
	 * @return the localized reply
	 */
	public static LocalizedFileActionFtpReply translate(FtpChannel channel,
        FtpRequest request, int code, String subId,
        String basicMsg, FtpFile file) {
		String msg = FtpReplyTranslator.translateMessage(channel, request, code, subId, basicMsg);

		return new LocalizedFileActionFtpReply(code, msg, file);
	}
}
