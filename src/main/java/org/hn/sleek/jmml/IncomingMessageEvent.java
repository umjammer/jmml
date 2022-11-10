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
 * $Id: IncomingMessageEvent.java,v 1.1.1.1 2003/10/27 01:14:36 popolony2k Exp $
 * $Author: popolony2k $
 * $Name:  $
 * $Revision: 1.1.1.1 $
 * $State: Exp $
 *
 */

package org.hn.sleek.jmml;

import java.net.URLDecoder;

/**
 * Represents and incoming message event, containing the Passport name, friendly
 * and the actual message from the buddy.
 * <p>
 * In the future, this may include a timestamp and/or message type information.
 */
public class IncomingMessageEvent {

  /** Passport username of the sending buddy. */
  String userName;
	
  /** Friendly Name of the buddy */
  String friendlyName;

  /** Body of the actual message */
  String message;

  /**
   * Creates an IncomingMessageEvent out of the arguments.  Very WYSIWYG.
   * Exception is that we try to decode the friendlyName, which is MIME-
   * encoded.
   *
   * @param userName Passport name of the sending buddy.
   * @param friendlyName Screen name of the sending buddy.
   * @param message Actual message sent by the buddy.
   */
  public IncomingMessageEvent( String userName, String friendlyName, String message ) {
    
    this.userName = userName;
    this.message  = message;

    /* As a backup, just use the undecoded string */
    String decodedName = friendlyName;

    try {
      // JDK 1.3 Backward compatibility
      //decodedName = URLDecoder.decode (friendlyName, "UTF-8");
      decodedName = URLDecoder.decode (friendlyName);
    } catch( Exception e ) { 
      /* Big woop */ 
    }
    finally {
      this.friendlyName = decodedName;
    }
  }

  /**
   * Returns the passport name of the sending party.
   *
   * @return Passport name of the sending party.
   */
  public String getUserName() {
    return userName;
  }

  /**
   * Returns the screen name of the sending party.
   *
   * @return Screen name of the sending party.
   */
  public String getFriendlyName() {
    
    return friendlyName;
  }

  /**
   * Returns the body of the instant message.
   *
   * @return Body of the instant message.
   */
  public String getMessage() {
    
    return message;
  }
}
// IncomingMessageEvent Class