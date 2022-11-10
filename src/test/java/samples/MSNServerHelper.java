/*
 * ===========================================================================
 *  Copyright (C) 2002-2003  Tony Tang
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * ===========================================================================
 */

/**
 *
 * $Id: MSNServerHelper.java,v 1.2 2003/10/30 01:32:23 popolony2k Exp $
 * $Author: popolony2k $
 * $Name:  $
 * $Revision: 1.2 $
 * $State: Exp $
 *
 */

import org.hn.sleek.jmml.*;
import java.io.*;
import java.util.*;

class MSNServerHelper {

	static Hashtable commands = new Hashtable();

	static {
		commands.put ("cal", "/usr/bin/cal");
		commands.put ("finger", "/usr/bin/finger");
		commands.put ("fortune", "/usr/games/fortune");
		commands.put ("ifconfig", "/sbin/ifconfig");
		commands.put ("uptime", "/usr/bin/uptime");
		commands.put ("who", "/usr/bin/who");
	}


	public static void main (final String[] args) {
		final MessengerServerManager msn = MessengerServerManager.getInstance();

		msn.addMessengerClientListener (new MessengerClientAdapter() {
			public void incomingMessage (IncomingMessageEvent event) {

				StringTokenizer t = new StringTokenizer (event.getMessage());
				Vector command = new Vector();
				while (t.hasMoreTokens()) {
					command.add (t.nextToken());
				}

				if (!commands.containsKey(command.get(0))) {
					msn.sendMessage (event.getUserName(), "Sorry, I don't understand " + event.getMessage() + "... try one of these: cal, finger, fortune, ifconfig, uptime, or who");
				}

				/* Has the command */
				String cleverResponse = "Clever response...";
				String[] commandArray = new String[command.size()];

				for (int i = 0; i < command.size(); i++) {
					commandArray[i] = (String) command.get(i);
				}
				commandArray[0] = (String) commands.get (command.get(0));

				try {
					BufferedReader br = new BufferedReader (new InputStreamReader ((Runtime.getRuntime().exec (commandArray)).getInputStream()));
					StringBuffer sb = new StringBuffer();
					String line;
					line = br.readLine();
					while (line != null) {
						sb.append (line);
						line = br.readLine();
					}
					cleverResponse = sb.toString();
				}
				catch (Exception e1) {
					System.out.println (e1);
				}

				msn.sendMessage (event.getUserName(), cleverResponse);


			}
		});

                try  {
		  msn.signIn (args[0], args[1], ContactStatus.ONLINE );
                } catch( MSNException e )  {
                  System.err.println( e );
                }
	}

}
