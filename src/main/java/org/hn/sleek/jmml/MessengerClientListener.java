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
 * $Id: MessengerClientListener.java,v 1.1.1.1 2003/10/27 01:14:36 popolony2k Exp $
 * $Author: popolony2k $
 * $Name:  $
 * $Revision: 1.1.1.1 $
 * $State: Exp $
 *
 */

package org.hn.sleek.jmml;

/**
 * MSN Messenger clients using the library should implement this interface,
 * and register against the MessengerServerManager object.  Events fired through
 * this interface include: (1) incoming IM messages, (2) property changes, and
 * (3) reverseList changes.
 */
public interface MessengerClientListener {

  /**
   * Someone's status changes (online to busy, etc.), or friendly
   * name changes or something.
   */
  public void contactPropertyChanged( ContactChangeEvent event );

  /**
   * message is decoded and in local strings
   */
  public void incomingMessage( IncomingMessageEvent event );

  /**
   * Someone else adds you to their list.
   */
  public void reverseListChanged( String username );
        
  /**
   * The user is disconnected from MSN server
   */
  public void serverDisconnected();
      
  /**
   * Login fail
   */
  public void loginError();
        
  /**
   * Login successfull
   */
  public void loginAccepted();
        
  /**
   * Receive group
   */
  public void groupReceived( String strGroupName );
  
  /**
   * Receive contact
   */
  public void contactReceived( Contact contact );
  
  /**
   * User added to the contact list
   */
  public void contactAdded( Contact contact );
  
  /**
   * User removed from contact list
   */
  public void contactRemoved( Contact contact );
  
}

// MessengerClientListener interface