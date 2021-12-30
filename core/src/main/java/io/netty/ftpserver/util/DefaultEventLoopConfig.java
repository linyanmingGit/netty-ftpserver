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

package io.netty.ftpserver.util;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;

import java.util.Locale;

/**
 * @author Io Netty Project
 */
public class DefaultEventLoopConfig {
    private final String eventLoopGroup;

    private final String socketChannel;

    public DefaultEventLoopConfig(String eventType){
        if("NIO".equals(eventType)){
            this.eventLoopGroup = "io.netty.channel.nio.NioEventLoopGroup";
            this.socketChannel = "io.netty.channel.socket.nio.NioServerSocketChannel";
        }else if("EPOLL".equals(eventType)){
            this.eventLoopGroup = "io.netty.channel.epoll.EpollEventLoopGroup";
            this.socketChannel = "io.netty.channel.epoll.EpollServerSocketChannel";
        }else if ("KQUEUE".equals(eventType)){
            this.eventLoopGroup = "io.netty.channel.kqueue.KQueueEventLoopGroup";
            this.socketChannel = "io.netty.channel.kqueue.KQueueServerSocketChannel";
        }else{
            String os = System.getProperty("os.name");
            if (os.toLowerCase(Locale.ROOT).startsWith("win")){
                this.eventLoopGroup = "io.netty.channel.nio.NioEventLoopGroup";
                this.socketChannel = "io.netty.channel.socket.nio.NioServerSocketChannel";
            }else {
                this.eventLoopGroup = "io.netty.channel.epoll.EpollEventLoopGroup";
                this.socketChannel = "io.netty.channel.epoll.EpollServerSocketChannel";
            }
        }
    }


    public EventLoopGroup getEventLoopGroup() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return (EventLoopGroup) Class.forName(eventLoopGroup).newInstance();
    }

    public Class<ServerChannel> getServerChannel() throws ClassNotFoundException {
        return (Class<ServerChannel>) Class.forName(socketChannel);
    }
}
