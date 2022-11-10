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
 * $Id: Contact.java,v 1.2 2003/11/16 16:44:15 popolony2k Exp $
 * $Author: popolony2k $
 * $Name:  $
 * $Revision: 1.2 $
 * $State: Exp $
 *
 */

package org.hn.sleek.jmml;

import java.util.*;


/**
 * Represents a contact in the contact list.  Encapsulates all of the
 * MSN Messenger Contact properties, and supports an extra "my name for
 * this person" property.
 */
public class Contact {

  public final static int HOME_PHONE_NUMBER   = 1;
  public final static int WORK_PHONE_NUMBER   = 2;
  public final static int MOBILE_PHONE_NUMBER = 3;
  public final static int MOBILE_ENABLED      = 4;
  public final static int CONTACTLIST_TYPE    = 5;
  public final static int STATUS              = 6;
  public final static int FRIENDLY_NAME       = 7;

  /** User's email login name. */
  String         userName;

  /** User's friendly name. */
  String         friendlyName;

  /** Alternative name that this user is known by. */
  String         realName;
  ContactStatus  status;
  ArrayList      aGroups;
  Hashtable      hListType;

  String         homePhoneNumber;
  String         workPhoneNumber;
  String         mobilePhoneNumber;
  boolean        mobileEnabled;


  public Contact( String userName ) {
    
    Integer      iListType = ContactList.parseListTypeToInteger( ContactList.RAW_FL );
        
    this.userName          = userName;
    this.friendlyName      = "";
    this.realName          = "";
    this.status            = new ContactStatus( ContactStatus.UNKNOWN );
    this.homePhoneNumber   = "";
    this.workPhoneNumber   = "";
    this.mobilePhoneNumber = "";
    this.mobileEnabled     = false;
    this.aGroups           = new ArrayList();
    this.hListType         = new Hashtable();
    this.hListType.put( iListType, iListType );
  }

  void setFriendlyName( String friendlyName ) {
    
    this.friendlyName = friendlyName;
  }

  public String getFriendlyName() {
    
    return friendlyName;
  }

  void setRealName( String realName ) {
    
    this.realName = realName;
  }

  public String getRealName() {
    
    return realName;
  }
  
  void setUserName( String strUserName )  {
    
    this.userName = strUserName;
  }
  
  public String getUserName() {
    
    return userName;
  }

  void setStatus( String rawStatus ) {
    
    status = new ContactStatus( rawStatus );
  }
  
  public ContactStatus getStatus() {
    
    return status;
  }

  public String getPhoneNumber( int which ) {
    
    switch( which ) {
      
      case HOME_PHONE_NUMBER   :  return homePhoneNumber;
      
      case WORK_PHONE_NUMBER   :  return workPhoneNumber;
      
      case MOBILE_PHONE_NUMBER :  return mobilePhoneNumber;
      
      default:  return "";
    }
  }

  void setPhoneNumber( int which, String phoneNumber ) {
    
    switch( which ) {
      
      case HOME_PHONE_NUMBER   : homePhoneNumber = phoneNumber; break;
      
      case WORK_PHONE_NUMBER   : workPhoneNumber = phoneNumber; break;
      
      case MOBILE_PHONE_NUMBER : mobilePhoneNumber = phoneNumber; break;
      
    }
  }

  void setMobileEnabled( boolean mobileEnabled ) {
    
    this.mobileEnabled = mobileEnabled;
  }

  public boolean getMobileEnabled() {
    
    return mobileEnabled;
  }

  /**
   * Set the contact's group id
   * @param aGroups The list of user groups
   */
  void setGroups( ArrayList aGroups ) {
   
    this.aGroups = aGroups;
  }

  /**
   * Return the contact groups
   */
  public ArrayList getGroups()  {
    
    return aGroups;
  }
  
  /**
   * Set the list type based on value of ContactList
   * class;
   * @param hListType The Hashtable with all ContactList Type @see ContactList
   */
  void setListType( Hashtable hListType )  {
    
    this.hListType = hListType;
  }
  
  /**
   * Return the list type.
   */
  public Hashtable getListType()  {
   
    return hListType;
  }

  /**
   * Convert a integer contact property representation
   * to string format.
   * @param property The property to convert.
   */
  public static String propertyToString( int property ) {
    
    switch( property ) {
      
      case STATUS :  return "status";
      
      case FRIENDLY_NAME :  return "friendly name";
      
      case HOME_PHONE_NUMBER : return "home phone number";
      
      case WORK_PHONE_NUMBER : return "work phone number";
      
      case MOBILE_PHONE_NUMBER : return "mobile phone number";
      
      case MOBILE_ENABLED : return "mobile enabled";
      
      default:  return "unknown";
    }
  }

  /**
   * Return the contact details
   */
  public String getDetails() {
    
    StringBuffer sb = new StringBuffer();
    
    
    sb.append( "UserName: " + getUserName() );
    sb.append( "\nFriendlyName: " + getFriendlyName() );
    sb.append( "\nStatus: " + getStatus() );
    
    return sb.toString();
  }

  /**
   * Return the string representation of this object
   * acctually, the contact friendly name.
   */
  public String toString() {
    
    if( friendlyName != null ) {
      return friendlyName;
    }
    else {
      return userName;
    }
  }
}

// Contact class