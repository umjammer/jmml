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
 * $Id: SimpleMSNResponder.java,v 1.2 2003/10/30 01:32:23 popolony2k Exp $
 * $Author: popolony2k $
 * $Name:  $
 * $Revision: 1.2 $
 * $State: Exp $
 *
 */

import org.hn.sleek.jmml.*;

class SimpleMSNResponder {

	public static void main (final String[] args) {
		final MessengerServerManager msn = MessengerServerManager.getInstance();

                msn.addMessengerClientListener (new MessengerClientAdapter() {
                        public void incomingMessage (IncomingMessageEvent event) {
                                msn.sendMessage (event.getUserName(), "Your message was: " + event.getMessage());
                        }
                });

                /* msn.signIn (userName, password */
                try  {
		  msn.signIn (args[0], args[1], ContactStatus.ONLINE );
                } catch( MSNException e )  {
                  System.err.println( e );
                }
	}

}
