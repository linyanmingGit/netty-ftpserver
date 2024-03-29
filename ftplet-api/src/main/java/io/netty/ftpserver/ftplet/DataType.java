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

package io.netty.ftpserver.ftplet;

/**
 * Type safe enum for describing the data type
 *
 * @author Io Netty Project
 */
public enum DataType {

    /**
     * Binary data type
     */
    BINARY,

    /**
     * ASCII data type
     */
    ASCII;

    /**
     * Parses the argument value from the TYPE command into the type safe class
     * 
     * @param argument
     *            The argument value from the TYPE command. Not case sensitive
     * @return The appropriate data type
     * @throws IllegalArgumentException
     *             If the data type is unknown
     */
    public static DataType parseArgument(char argument) {
        switch (argument) {
        case 'A':
        case 'a':
            return ASCII;
        case 'I':
        case 'i':
            return BINARY;
        default:
            throw new IllegalArgumentException("Unknown data type: " + argument);
        }
    }
}
