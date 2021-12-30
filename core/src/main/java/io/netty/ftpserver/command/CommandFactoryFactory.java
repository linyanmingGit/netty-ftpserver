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

package io.netty.ftpserver.command;

//import io.netty.ftpserver.command.impl.ABOR;
//import io.netty.ftpserver.command.impl.ACCT;
//import io.netty.ftpserver.command.impl.APPE;
//import io.netty.ftpserver.command.impl.AUTH;
//import io.netty.ftpserver.command.impl.CDUP;
//import io.netty.ftpserver.command.impl.CWD;
//import io.netty.ftpserver.command.impl.DELE;

import io.netty.ftpserver.command.impl.ABOR;
import io.netty.ftpserver.command.impl.ACCT;
import io.netty.ftpserver.command.impl.APPE;
import io.netty.ftpserver.command.impl.AUTH;
import io.netty.ftpserver.command.impl.AbstractCommand;
import io.netty.ftpserver.command.impl.CDUP;
import io.netty.ftpserver.command.impl.CWD;
import io.netty.ftpserver.command.impl.DELE;
import io.netty.ftpserver.command.impl.DefaultCommandFactory;
import io.netty.ftpserver.command.impl.EPRT;
import io.netty.ftpserver.command.impl.EPSV;
import io.netty.ftpserver.command.impl.FEAT;
import io.netty.ftpserver.command.impl.HELP;
import io.netty.ftpserver.command.impl.LANG;
import io.netty.ftpserver.command.impl.LIST;
import io.netty.ftpserver.command.impl.MD5;
import io.netty.ftpserver.command.impl.MDTM;
import io.netty.ftpserver.command.impl.MFMT;
import io.netty.ftpserver.command.impl.MKD;
import io.netty.ftpserver.command.impl.MLSD;
import io.netty.ftpserver.command.impl.MLST;
import io.netty.ftpserver.command.impl.MODE;
import io.netty.ftpserver.command.impl.NLST;
import io.netty.ftpserver.command.impl.NOOP;
import io.netty.ftpserver.command.impl.OPTS;
import io.netty.ftpserver.command.impl.OPTS_MLST;
import io.netty.ftpserver.command.impl.OPTS_UTF8;
import io.netty.ftpserver.command.impl.PASS;
import io.netty.ftpserver.command.impl.PASV;
import io.netty.ftpserver.command.impl.PBSZ;
import io.netty.ftpserver.command.impl.PORT;
import io.netty.ftpserver.command.impl.PROT;
import io.netty.ftpserver.command.impl.PWD;
import io.netty.ftpserver.command.impl.QUIT;
import io.netty.ftpserver.command.impl.REIN;
import io.netty.ftpserver.command.impl.REST;
import io.netty.ftpserver.command.impl.RETR;
import io.netty.ftpserver.command.impl.RMD;
import io.netty.ftpserver.command.impl.RNFR;
import io.netty.ftpserver.command.impl.RNTO;
import io.netty.ftpserver.command.impl.SITE;
import io.netty.ftpserver.command.impl.SITE_DESCUSER;
import io.netty.ftpserver.command.impl.SITE_HELP;
import io.netty.ftpserver.command.impl.SITE_STAT;
import io.netty.ftpserver.command.impl.SITE_WHO;
import io.netty.ftpserver.command.impl.SITE_ZONE;
import io.netty.ftpserver.command.impl.SIZE;
import io.netty.ftpserver.command.impl.STAT;
import io.netty.ftpserver.command.impl.STOR;
import io.netty.ftpserver.command.impl.STOU;
import io.netty.ftpserver.command.impl.STRU;
import io.netty.ftpserver.command.impl.SYST;
import io.netty.ftpserver.command.impl.TYPE;
import io.netty.ftpserver.command.impl.USER;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

//import io.netty.ftpserver.command.impl.EPRT;
//import io.netty.ftpserver.command.impl.EPSV;
//import io.netty.ftpserver.command.impl.FEAT;
//import io.netty.ftpserver.command.impl.HELP;
//import io.netty.ftpserver.command.impl.LANG;
//import io.netty.ftpserver.command.impl.LIST;
//import io.netty.ftpserver.command.impl.MD5;
//import io.netty.ftpserver.command.impl.MDTM;
//import io.netty.ftpserver.command.impl.MFMT;
//import io.netty.ftpserver.command.impl.MKD;
//import io.netty.ftpserver.command.impl.MLSD;
//import io.netty.ftpserver.command.impl.MLST;
//import io.netty.ftpserver.command.impl.MODE;
//import io.netty.ftpserver.command.impl.NLST;
//import io.netty.ftpserver.command.impl.NOOP;
//import io.netty.ftpserver.command.impl.OPTS;
//import io.netty.ftpserver.command.impl.PASS;
//import io.netty.ftpserver.command.impl.PASV;
//import io.netty.ftpserver.command.impl.PBSZ;
//import io.netty.ftpserver.command.impl.PORT;
//import io.netty.ftpserver.command.impl.PROT;
//import io.netty.ftpserver.command.impl.PWD;
//import io.netty.ftpserver.command.impl.QUIT;
//import io.netty.ftpserver.command.impl.REIN;
//import io.netty.ftpserver.command.impl.REST;
//import io.netty.ftpserver.command.impl.RETR;
//import io.netty.ftpserver.command.impl.RMD;
//import io.netty.ftpserver.command.impl.RNFR;
//import io.netty.ftpserver.command.impl.RNTO;
//import io.netty.ftpserver.command.impl.SITE;
//import io.netty.ftpserver.command.impl.SITE_DESCUSER;
//import io.netty.ftpserver.command.impl.SITE_HELP;
//import io.netty.ftpserver.command.impl.SITE_STAT;
//import io.netty.ftpserver.command.impl.SITE_WHO;
//import io.netty.ftpserver.command.impl.SITE_ZONE;
//import io.netty.ftpserver.command.impl.SIZE;
//import io.netty.ftpserver.command.impl.STAT;
//import io.netty.ftpserver.command.impl.STOR;
//import io.netty.ftpserver.command.impl.STOU;
//import io.netty.ftpserver.command.impl.STRU;
//import io.netty.ftpserver.command.impl.SYST;
//import io.netty.ftpserver.command.impl.TYPE;

/**
 * Factory for {@link CommandFactory} instances
 *
 * @author Io Netty Project
 */
public class CommandFactoryFactory {

    private static final HashMap<String, AbstractCommand> DEFAULT_COMMAND_MAP = new HashMap<>();

    static {
        // first populate the default command list
        DEFAULT_COMMAND_MAP.put("ABOR", new ABOR());
        DEFAULT_COMMAND_MAP.put("ACCT", new ACCT());
        DEFAULT_COMMAND_MAP.put("APPE", new APPE());
        DEFAULT_COMMAND_MAP.put("AUTH", new AUTH());
        DEFAULT_COMMAND_MAP.put("CDUP", new CDUP());
        DEFAULT_COMMAND_MAP.put("CWD", new CWD());
        DEFAULT_COMMAND_MAP.put("DELE", new DELE());
        DEFAULT_COMMAND_MAP.put("EPRT", new EPRT());
        DEFAULT_COMMAND_MAP.put("EPSV", new EPSV());
        DEFAULT_COMMAND_MAP.put("FEAT", new FEAT());
        DEFAULT_COMMAND_MAP.put("HELP", new HELP());
        DEFAULT_COMMAND_MAP.put("LANG", new LANG());
        DEFAULT_COMMAND_MAP.put("LIST", new LIST());
        DEFAULT_COMMAND_MAP.put("MD5", new MD5());
        DEFAULT_COMMAND_MAP.put("MFMT", new MFMT());
        DEFAULT_COMMAND_MAP.put("MMD5", new MD5());
        DEFAULT_COMMAND_MAP.put("MDTM", new MDTM());
        DEFAULT_COMMAND_MAP.put("MLST", new MLST());
        DEFAULT_COMMAND_MAP.put("MKD", new MKD());
        DEFAULT_COMMAND_MAP.put("MLSD", new MLSD());
        DEFAULT_COMMAND_MAP.put("MODE", new MODE());
        DEFAULT_COMMAND_MAP.put("NLST", new NLST());
        DEFAULT_COMMAND_MAP.put("NOOP", new NOOP());
        DEFAULT_COMMAND_MAP.put("OPTS", new OPTS());
        DEFAULT_COMMAND_MAP.put("OPTS_MLST", new OPTS_MLST());
        DEFAULT_COMMAND_MAP.put("OPTS_UTF8", new OPTS_UTF8());
        DEFAULT_COMMAND_MAP.put("PASS", new PASS());
        DEFAULT_COMMAND_MAP.put("PASV", new PASV());
        DEFAULT_COMMAND_MAP.put("PBSZ", new PBSZ());
        DEFAULT_COMMAND_MAP.put("PORT", new PORT());
        DEFAULT_COMMAND_MAP.put("PROT", new PROT());
        DEFAULT_COMMAND_MAP.put("PWD", new PWD());
        DEFAULT_COMMAND_MAP.put("QUIT", new QUIT());
        DEFAULT_COMMAND_MAP.put("REIN", new REIN());
        DEFAULT_COMMAND_MAP.put("REST", new REST());
        DEFAULT_COMMAND_MAP.put("RETR", new RETR());
        DEFAULT_COMMAND_MAP.put("RMD", new RMD());
        DEFAULT_COMMAND_MAP.put("RNFR", new RNFR());
        DEFAULT_COMMAND_MAP.put("RNTO", new RNTO());
        DEFAULT_COMMAND_MAP.put("SITE", new SITE());
        DEFAULT_COMMAND_MAP.put("SIZE", new SIZE());
        DEFAULT_COMMAND_MAP.put("SITE_DESCUSER", new SITE_DESCUSER());
        DEFAULT_COMMAND_MAP.put("SITE_HELP", new SITE_HELP());
        DEFAULT_COMMAND_MAP.put("SITE_STAT", new SITE_STAT());
        DEFAULT_COMMAND_MAP.put("SITE_WHO", new SITE_WHO());
        DEFAULT_COMMAND_MAP.put("SITE_ZONE", new SITE_ZONE());
        DEFAULT_COMMAND_MAP.put("STAT", new STAT());
        DEFAULT_COMMAND_MAP.put("STOR", new STOR());
        DEFAULT_COMMAND_MAP.put("STOU", new STOU());
        DEFAULT_COMMAND_MAP.put("STRU", new STRU());
        DEFAULT_COMMAND_MAP.put("SYST", new SYST());
        DEFAULT_COMMAND_MAP.put("TYPE", new TYPE());
        DEFAULT_COMMAND_MAP.put("USER", new USER());
    }

    private Map<String, AbstractCommand> commandMap = new HashMap<>();

    private boolean useDefaultCommands = true;

    /**
     * Create an {@link CommandFactory} based on the configuration on the factory.
     * @return The {@link CommandFactory}
     */
    public CommandFactory createCommandFactory() {
        
        Map<String, AbstractCommand> mergedCommands = new HashMap<String, AbstractCommand>();
        if(useDefaultCommands) {
            mergedCommands.putAll(DEFAULT_COMMAND_MAP);
        }
        
        mergedCommands.putAll(commandMap);
        
        return new DefaultCommandFactory(mergedCommands);
    }
    
    /**
     * Are default commands used?
     * 
     * @return true if default commands are used
     */
    public boolean isUseDefaultCommands() {
        return useDefaultCommands;
    }

    /**
     * Sets whether the default commands will be used.
     * 
     * @param useDefaultCommands
     *            true if default commands should be used
     */
    public void setUseDefaultCommands(final boolean useDefaultCommands) {
        this.useDefaultCommands = useDefaultCommands;
    }

    /**
     * Get the installed commands
     * 
     * @return The installed commands
     */
    public Map<String, AbstractCommand> getCommandMap() {
        return commandMap;
    }

    /**
     * Add or override a command.
     * @param commandName The command name, e.g. STOR
     * @param command The command
     */
    public void addCommand(String commandName, AbstractCommand command) {
        if(commandName == null) {
            throw new NullPointerException("commandName can not be null");
        }
        if(command == null) {
            throw new NullPointerException("command can not be null");
        }
        
        commandMap.put(commandName.toUpperCase(), command);
    }
    
    /**
     * Set commands to add or override to the default commands
     * 
     * @param commandMap
     *            The map of commands, the key will be used to map to requests.
     */
    public void setCommandMap(final Map<String, AbstractCommand> commandMap) {
        if (commandMap == null) {
            throw new NullPointerException("commandMap can not be null");
        }

        this.commandMap.clear();

        for (Entry<String, AbstractCommand> entry : commandMap.entrySet()) {
            this.commandMap.put(entry.getKey().toUpperCase(), entry.getValue());
        }
    }
}
