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
 * $Id: ContactChangeEvent.java,v 1.1.1.1 2003/10/27 01:14:12 popolony2k Exp $
 * $Author: popolony2k $
 * $Name:  $
 * $Revision: 1.1.1.1 $
 * $State: Exp $
 *
 */

package org.hn.sleek.jmml;

import java.net.URLDecoder;


public class ContactChangeEvent {
  
  String       sourceCommand;
  String       userName;
  int          property;
  Object       newValue;

  
  public ContactChangeEvent( String userName, int property, Object newValue, String sourceCommand ) {
    
    this.userName      = userName;
    this.sourceCommand = sourceCommand;
    this.property      = property;
    this.newValue      = newValue;

    switch( property ) {
      
      case Contact.FRIENDLY_NAME:
        
      case Contact.HOME_PHONE_NUMBER:
        
      case Contact.WORK_PHONE_NUMBER:
        
      case Contact.MOBILE_PHONE_NUMBER:
        String decodedName = ( String ) newValue;

        try {
          decodedName = URLDecoder.decode( decodedName );
        } catch( Exception e ) { 
          /* Big woop */ 
        }
        finally {
          this.newValue = decodedName;
        }
    }
  }

  public String getSourceCommand() {
    
    return sourceCommand;
  }

  public String getUserName() {
    
    return userName;
  }

  public int getProperty() {
    
    return property;
  }

  public Object getNewValue() {
    
    return newValue;
  }

  public String toString() {
    
    return userName + " " + Contact.propertyToString( property ) + ": " + newValue;
  }
}
// ContactChangeEvent class