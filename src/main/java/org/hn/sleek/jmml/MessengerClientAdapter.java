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
 * $Id: MessengerClientAdapter.java,v 1.1.1.1 2003/10/27 01:14:36 popolony2k Exp $
 * $Author: popolony2k $
 * $Name:  $
 * $Revision: 1.1.1.1 $
 * $State: Exp $
 *
 */

package org.hn.sleek.jmml;

/**
 * Adapter implementation of the MessengerClientListener interface.
 * That is, it's a concrete implementation that doesn't do anything
 * with the callbacks.  And of course, that's perfectly acceptable.
 */
public class MessengerClientAdapter implements MessengerClientListener {

  public void contactPropertyChanged( ContactChangeEvent event ) {}
  public void incomingMessage( IncomingMessageEvent event ) {}
  public void reverseListChanged( String username ) {}
  public void serverDisconnected()  {}
  public void loginError() {}
  public void loginAccepted() {}
  public void groupReceived( String strGroupName )  {}
  public void contactReceived( Contact contact )  {}
  public void contactAdded( Contact contact ) {}
  public void contactRemoved( Contact contact ) {}
}

// MessengerClientAdapter class
