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

package io.netty.ftpserver.listener.nio;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.ftpserver.DataConnectionConfiguration;
import io.netty.ftpserver.command.impl.RegisterCommand;
import io.netty.ftpserver.impl.FtpServerContext;
import io.netty.ftpserver.listener.Listener;
import io.netty.ftpserver.listener.ListenerFactory;
import io.netty.ftpserver.listener.nio.channel.FtpStatus;
import io.netty.ftpserver.listener.nio.codec.FtpRequestDecoder;
import io.netty.ftpserver.listener.nio.codec.FtpResponseEncoder;
import io.netty.ftpserver.listener.nio.log.FtpLoggingHandler;
import io.netty.ftpserver.ssl.ClientAuth;
import io.netty.ftpserver.ssl.SslConfiguration;
import io.netty.ftpserver.util.DefaultEventLoopConfig;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;
import java.util.concurrent.TimeUnit;

/**
 * <strong>Internal class, do not use directly.</strong>
 * 
 * The default {@link Listener} implementation.
 *
 * @author Io Netty Project
 */
public class NioListener extends AbstractListener {

    private final Logger LOG = LoggerFactory.getLogger(NioListener.class);

    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private ChannelFuture channelFuture;

    /**
     * @deprecated Use the constructor with IpFilter instead. 
     * Constructor for internal use, do not use directly. Instead use {@link ListenerFactory}
     */
    @Deprecated
    public NioListener(String serverAddress, int port,
                       boolean implicitSsl,
                       SslConfiguration sslConfiguration,
                       DataConnectionConfiguration dataConnectionConfig,
                       int idleTimeout,String channelType) {
        super(serverAddress, port, implicitSsl, sslConfiguration, dataConnectionConfig, 
                idleTimeout, channelType);
    }

    /**
     * @see Listener#start(FtpServerContext)
     */
    public synchronized void start(final FtpServerContext context) throws Exception {
        if(!isStopped()) {
            // listener already started, don't allow
            throw new IllegalStateException("Listener already started");
        }
        DefaultEventLoopConfig defaultEventLoopConfig = new DefaultEventLoopConfig(getChannelType());
        final EventLoopGroup bossGroup = defaultEventLoopConfig.getEventLoopGroup();
        final EventLoopGroup workGroup = defaultEventLoopConfig.getEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workGroup).channel(defaultEventLoopConfig.getServerChannel())
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .childAttr(AttributeKey.valueOf(FtpStatus.ATTRIBUTE_CONTEXT),context)
                    .childAttr(AttributeKey.valueOf(FtpStatus.ATTRIBUTE_LISTENER),this)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            if (isImplicitSsl()) {
                                SslConfiguration ssl = getSslConfiguration();
                                SSLEngine engine = ssl.getSSLContext().createSSLEngine();
                                engine.setUseClientMode(false);
                                if (ssl.getClientAuth() == ClientAuth.NEED) {
                                    engine.setNeedClientAuth(true);
                                } else if (ssl.getClientAuth() == ClientAuth.WANT) {
                                    engine.setWantClientAuth(true);
                                }
                                channel.pipeline().addLast("sslFilter", new SslHandler(engine));
                            }
                            channel.pipeline().addLast(new IdleStateHandler(60, 30, 0, TimeUnit.SECONDS));
                            channel.pipeline().addLast("decoder",new FtpRequestDecoder(2048));
                            channel.pipeline().addLast("encoder",new FtpResponseEncoder());
                            channel.pipeline().addLast(new RegisterCommand(channels));
                            channel.pipeline().addLast(new FtpLoggingHandler(LogLevel.INFO));
                        }
                    });
            channelFuture = serverBootstrap.bind(getPort()).sync();
            channelFuture.channel().closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    LOG.info("The service is exiting...");
                    bossGroup.shutdownGracefully();
                    workGroup.shutdownGracefully();
                }
            });
        } catch(Exception e) {
            // clean up if we fail to start
            LOG.error("The Service exceptions:"+e);
            stop();
        }
    }

    /**
     * @see Listener#stop()
     */
    public synchronized void stop() {
        // close server socket
        if(channelFuture !=null){
            channelFuture.channel().close();
        }
    }

    /**
     * @see Listener#isStopped()
     */
    public boolean isStopped() {
        return channelFuture ==null || !channelFuture.isDone();
    }

    /**
     * @see Listener#resume()
     */
    public synchronized void resume() {
        if(isStopped()){
            channelFuture.channel().notifyAll();
        }
    }

    /**
     * @see Listener#suspend()
     */
    public synchronized void suspend() {
        try {
            if(!isStopped()){
                channelFuture.channel().wait();
            }
        } catch (InterruptedException e) {
            LOG.error("serverChannel wait error:"+e);
        }
    }
    
//    /**
//     * @see Listener#getActiveSessions()
//     */
    public synchronized ChannelGroup getActiveChannel() {
        return channels;
    }
}
