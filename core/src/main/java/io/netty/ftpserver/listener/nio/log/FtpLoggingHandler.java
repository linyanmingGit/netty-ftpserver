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

package io.netty.ftpserver.listener.nio.log;

import java.net.SocketAddress;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.ftpserver.listener.nio.channel.FtpIoChannel;
import io.netty.handler.logging.ByteBufFormat;
import io.netty.handler.logging.LogLevel;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

/**
 * A {@link ChannelHandler} that logs all events using a logging framework.
 * By default, all events are logged at <tt>DEBUG</tt> level and full hex dumps are recorded for ByteBufs.
 *
 * @author Io Netty Project
 */
@ChannelHandler.Sharable
@SuppressWarnings({ "StringConcatenationInsideStringBufferAppend", "StringBufferReplaceableByString" })
public class FtpLoggingHandler extends ChannelDuplexHandler {

	private static final LogLevel DEFAULT_LEVEL = LogLevel.DEBUG;

	protected final InternalLogger logger;
	protected final InternalLogLevel internalLevel;

	private final boolean maskPassword;

	private final ByteBufFormat byteBufFormat;

	public FtpLoggingHandler(){
		this(DEFAULT_LEVEL,ByteBufFormat.HEX_DUMP,true);
	}

	public FtpLoggingHandler(LogLevel level){
		this(level,ByteBufFormat.HEX_DUMP,true);
	}

	public FtpLoggingHandler(LogLevel level, boolean maskPassword){
		this(level,ByteBufFormat.HEX_DUMP,maskPassword);
	}

	public FtpLoggingHandler(LogLevel level, ByteBufFormat byteBufFormat, boolean maskPassword){
		logger = InternalLoggerFactory.getInstance(getClass());
		internalLevel = level.toInternalLevel();
		this.byteBufFormat = ObjectUtil.checkNotNull(byteBufFormat, "byteBufFormat");
		this.maskPassword = maskPassword;
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelRegistered();
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelUnregistered();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (logger.isEnabled(internalLevel)) {
			logger.log(internalLevel, format(ctx, "ACTIVE"));
		}
		ctx.fireChannelActive();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (logger.isEnabled(internalLevel)) {
			logger.log(internalLevel, format(ctx, "INACTIVE"));
		}
		ctx.fireChannelInactive();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (logger.isEnabled(internalLevel)) {
			logger.log(internalLevel, format(ctx, "EXCEPTION", cause), cause);
		}
		ctx.fireExceptionCaught(cause);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		ctx.fireUserEventTriggered(evt);
	}

	@Override
	public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
		ctx.bind(localAddress, promise);
	}

	@Override
	public void connect(
			ChannelHandlerContext ctx,
			SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
		ctx.connect(remoteAddress, localAddress, promise);
	}

	@Override
	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		ctx.disconnect(promise);
	}

	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		if (logger.isEnabled(internalLevel)) {
			logger.log(internalLevel, format(ctx, "CLOSE"));
		}
		ctx.close(promise);
	}

	@Override
	public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		ctx.deregister(promise);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelReadComplete();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (logger.isEnabled(internalLevel)) {
			logger.log(internalLevel, format(ctx, "RECEIVED", msg));
		}
		ctx.fireChannelRead(msg);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (logger.isEnabled(internalLevel)) {
			logger.log(internalLevel, format(ctx, "SEND", msg));
		}
		ctx.write(msg, promise);
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		ctx.fireChannelWritabilityChanged();
	}

	@Override
	public void flush(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}


	/**
	 * Formats an event and returns the formatted message.
	 *
	 * @param eventName the name of the event
	 */
	protected String format(ChannelHandlerContext ctx, String eventName) {
		FtpIoChannel ftpIoChannel = new FtpIoChannel(ctx.channel());
		String userName = ftpIoChannel.getUserArgument();
		String remote = ftpIoChannel.remoteAddress().getAddress().getHostAddress();
		return new StringBuilder(userName.length() + 3 +remote.length() + 3 + eventName.length())
				.append("[").append(userName).append("]").append(' ')
				.append("[").append(remote).append("]").append(' ')
				.append(eventName)
				.toString();
	}


	/**
	 * Formats an event and returns the formatted message.
	 *
	 * @param eventName the name of the event
	 * @param arg       the argument of the event
	 */
	protected String format(ChannelHandlerContext ctx, String eventName, Object arg) {
		if (arg instanceof ByteBuf) {
			return formatByteBuf(ctx, eventName, (ByteBuf) arg);
		} else if (arg instanceof ByteBufHolder) {
			return formatByteBufHolder(ctx, eventName, (ByteBufHolder) arg);
		} else {
			return formatSimple(ctx, eventName, arg);
		}
	}


	/**
	 * Generates the default log message of the specified event whose argument is a {@link ByteBuf}.
	 */
	private String formatByteBuf(ChannelHandlerContext ctx, String eventName, ByteBuf msg) {
		FtpIoChannel ftpIoChannel = new FtpIoChannel(ctx.channel());
		String userName = ftpIoChannel.getUserArgument();
		String remote = ftpIoChannel.remoteAddress().getAddress().getHostAddress();
		int length = msg.readableBytes();
		if (length == 0) {
			StringBuilder buf = new StringBuilder(userName.length() + 3 +
					remote.length() + 3 + eventName.length() + 4);
			buf.append("[").append(userName).append("]").append(' ').append("[").append(remote)
					.append("]").append(' ').append(eventName).append(": 0B");
			return buf.toString();
		} else {
			int outputLength = userName.length() + 3 +
					remote.length() + 3 + eventName.length() + 2 + 10 + 1;
			if (byteBufFormat == ByteBufFormat.HEX_DUMP) {
				int rows = length / 16 + (length % 15 == 0? 0 : 1) + 4;
				int hexDumpLength = 2 + rows * 80;
				outputLength += hexDumpLength;
			}
			StringBuilder buf = new StringBuilder(outputLength);
			buf.append("[").append(userName).append("]").append(' ').append("[").append(remote)
					.append("]").append(' ').append(eventName).append(": ").append(length).append('B');
			if (byteBufFormat == ByteBufFormat.HEX_DUMP) {
				buf.append(NEWLINE);
				appendPrettyHexDump(buf, msg);
			}

			return buf.toString();
		}
	}

	/**
	 * Generates the default log message of the specified event whose argument is a {@link ByteBufHolder}.
	 */
	private String formatByteBufHolder(ChannelHandlerContext ctx, String eventName, ByteBufHolder msg) {
		FtpIoChannel ftpIoChannel = new FtpIoChannel(ctx.channel());
		String userName = ftpIoChannel.getUserArgument();
		String remote = ftpIoChannel.remoteAddress().getAddress().getHostAddress();
		String msgStr = msg.toString();
		ByteBuf content = msg.content();
		int length = content.readableBytes();
		if (length == 0) {
			StringBuilder buf = new StringBuilder(userName.length() + 3 +
					remote.length() + 3 + eventName.length() + 2 + msgStr.length() + 4);
			buf.append("[").append(userName).append("]").append(' ').append("[").append(remote)
					.append("]").append(' ').append(eventName).append(", ").append(msgStr).append(", 0B");
			return buf.toString();
		} else {
			int outputLength = userName.length() + 3 +
					remote.length() + 3 + eventName.length() + 2 + msgStr.length() + 2 + 10 + 1;
			if (byteBufFormat == ByteBufFormat.HEX_DUMP) {
				int rows = length / 16 + (length % 15 == 0? 0 : 1) + 4;
				int hexDumpLength = 2 + rows * 80;
				outputLength += hexDumpLength;
			}
			StringBuilder buf = new StringBuilder(outputLength);
			buf.append("[").append(userName).append("]").append(' ').append("[").append(remote)
					.append("]").append(' ').append(eventName).append(": ")
					.append(msgStr).append(", ").append(length).append('B');
			if (byteBufFormat == ByteBufFormat.HEX_DUMP) {
				buf.append(NEWLINE);
				appendPrettyHexDump(buf, content);
			}

			return buf.toString();
		}
	}

	/**
	 * Generates the default log message of the specified event whose argument is an arbitrary object.
	 */
	private String formatSimple(ChannelHandlerContext ctx, String eventName, Object msg) {
		FtpIoChannel ftpIoChannel = new FtpIoChannel(ctx.channel());
		String userName = ftpIoChannel.getUserArgument();
		String remote = ftpIoChannel.remoteAddress().getAddress().getHostAddress();
		String msgStr = String.valueOf(msg);
		if (maskPassword && msgStr.startsWith("PASS")) {
			msgStr = "PASS *****";
		}
		StringBuilder buf = new StringBuilder(userName.length() + 3 +
				remote.length() + 3 + eventName.length() + 2 + msgStr.length());
		return buf.append("[").append(userName).append("]").append(' ')
				.append("[").append(remote).append("]").append(' ')
				.append(eventName).append(": ").append(msgStr).toString();
	}
}
