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
 * $Id: MSNPListener.java,v 1.1.1.1 2003/10/27 01:14:47 popolony2k Exp $
 * $Author: popolony2k $
 * $Name:  $
 * $Revision: 1.1.1.1 $
 * $State: Exp $
 *
 */

package org.hn.sleek.jmml;


/**
 * Processes incoming MSNP messages.  ServerConnector object uses
 * this interface to notify the calling object of incoming messages,
 * and allows the calling object to deal with them appropriately.
 */
public interface MSNPListener {

  /**
   * Receives incoming MSNP message for handling by listener.
   *
   * @param incomingMessage The incoming MSNP message from the
   * server.
   */
  public void incomingMSNPMessage( IncomingMessage incomingMessage );

  /**
   * Proactively lets the listener know when the connection to
   * the server is disconnected.
   */
  public void serverDisconnected();

}
// MSNPListener interface