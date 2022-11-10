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
 * $Id: ContactList.java,v 1.3 2003/11/16 17:30:49 popolony2k Exp $
 * $Author: popolony2k $
 * $Name:  $
 * $Revision: 1.3 $
 * $State: Exp $
 *
 */

package org.hn.sleek.jmml;

import java.util.*;


public class ContactList {

  private final static int    MSN_INTERNAL_FL = 1;
  private final static int    MSN_INTERNAL_AL = 2;
  private final static int    MSN_INTERNAL_BL = 4;
  private final static int    MSN_INTERNAL_RL = 8;
  
  final static String         RAW_NONE        = "NONE";
  final static String         RAW_AL          = "AL";
  final static String         RAW_BL          = "BL";
  final static String         RAW_RL          = "RL";
  final static String         RAW_FL          = "FL";

  public final static int     NONE            = -1;
  public final static int     ALLOW_LIST      =  0;
  public final static int     BLOCK_LIST      =  1;
  public final static int     REVERSE_LIST    =  2;
  public final static int     FORWARD_LIST    =  3;
  
  static HashMap              hContactListMap = new HashMap();
  
  HashMap                     hContacts;


  // Static initializations
  static {
    hContactListMap.put( ContactList.RAW_AL, new Integer( ContactList.ALLOW_LIST ) );
    hContactListMap.put( ContactList.RAW_BL, new Integer( ContactList.BLOCK_LIST ) );
    hContactListMap.put( ContactList.RAW_RL, new Integer( ContactList.REVERSE_LIST ) );
    hContactListMap.put( ContactList.RAW_FL, new Integer( ContactList.FORWARD_LIST ) );
  }

  
  /**
   * Create and initialize the contact list.
   */
  public ContactList() {

    hContacts = new HashMap();
  }
  
  /**
   * Add a contact to the contact list
   * @param contact The contact to add.
   */
  public synchronized void addToContactList( Contact contact )  {
    
    hContacts.put( contact.getUserName(), contact );
  }
  
  /**
   * Remove a contact from contact list
   * @param contact The contact to remove.
   */
  public synchronized void removeFromContactList( Contact contact )  {
    
    hContacts.remove( contact.getUserName() );
  }
  
  /**
   * Remove all users from contact list.
   */
  public synchronized void removeAll()  {
   
    hContacts.clear();
  }

  /**
   * Get a user from contact list
   * @param strUserName The username to retrieve
   */
  public Contact getContact( String strUserName )  {

    Contact      contact = ( Contact ) hContacts.get( strUserName );

    return contact;
  }

  /**
   * Parse the list representation (raw representation) 
   * to a Hashtable of all lists that user can be in.
   * @param strRawList The raw list to convert;
   */
  static synchronized Hashtable parseListType( String strRawList ) {

    Integer    iListType = ( Integer ) hContactListMap.get( strRawList.trim() );
    Hashtable  hLists    = new Hashtable();

    
    if( iListType != null ) {
      hLists.put( iListType, iListType );
    }
    else {
      
      int      nAllLists = Integer.parseInt( strRawList.trim() );
      
      if( ( nAllLists & MSN_INTERNAL_AL ) != 0 )  {
        Integer     iValue = ( Integer ) hContactListMap.get( RAW_AL );
        hLists.put( iValue, iValue );
      }
      
      if( ( nAllLists & MSN_INTERNAL_FL ) != 0 )  {
        Integer     iValue = ( Integer ) hContactListMap.get( RAW_FL );
        hLists.put( iValue, iValue );
      }
      
      if( ( nAllLists & MSN_INTERNAL_BL ) != 0 )  {
        Integer     iValue = ( Integer ) hContactListMap.get( RAW_BL );
        hLists.put( iValue, iValue );
      }
      
      if( ( nAllLists & MSN_INTERNAL_RL ) != 0 )  {
        Integer     iValue = ( Integer ) hContactListMap.get( RAW_RL );
        hLists.put( iValue, iValue );
      }
    }
    
    return hLists;
  }
  
  /**
   * Parse the list representation (int representation) 
   * to a string representation (raw representation).
   * @param nListType The list to convert;
   */
  static String parseListType( int nListType ) {
   
    switch( nListType )  {
      
      case ContactList.FORWARD_LIST : return ContactList.RAW_FL;
      
      case ContactList.BLOCK_LIST   : return ContactList.RAW_BL;
      
      case ContactList.ALLOW_LIST   : return ContactList.RAW_AL;
      
      case ContactList.REVERSE_LIST : return ContactList.RAW_RL;
      
      default : return ContactList.RAW_NONE;
    }
  }
  
  /**
   * Returns the Integer representation of list type.
   * @param strRawList The raw list to convert;
   */
  static Integer parseListTypeToInteger( String strRawList )  {
    
    return ( Integer ) hContactListMap.get( strRawList );
  }
}

// ContactList class