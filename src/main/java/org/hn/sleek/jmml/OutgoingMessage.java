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
 * $Id: OutgoingMessage.java,v 1.2 2003/11/16 16:44:15 popolony2k Exp $
 * $Author: popolony2k $
 * $Name:  $
 * $Revision: 1.2 $
 * $State: Exp $
 *
 */

package org.hn.sleek.jmml;

/**
 * Represents an outgoing message.  Exposes a List-like interface 
 * for adding arguments, and interacting with the arguments; 
 * providing no verification of whether the message is constructed
 * properly.  The exceptions to the List metaphor are command type
 * and transaction id, which must be specified at creation, and body.
 *
 */
class OutgoingMessage extends Message {

  /** Constant used for messages without a transaction ID */
  final static int NO_TRANSACTION_ID = -1;

  
  
  /**
   * Creates an OutgoingMessage with the specified transaction ID
   * and as the specified type.  Assumes that the type is valid.
   *
   * @param type Type of message.
   * @param transactionID TransactionID for the message.
   */
  public OutgoingMessage( int type, int transactionID ) {
    
    this.type = type;
    this.transactionID = new Integer( transactionID );
  }

  /**
   * Creates an OutgoingMessage as follows: <pre>OutgoingMessage (type, NO_TRANSACTION_ID)</pre>.
   *
   * @param type Type of message.
   */
  public OutgoingMessage( int type ) {
    
    this( type, NO_TRANSACTION_ID );
  }

  /**
   * Sets the body of the message to be as specified.  Note that
   * the command does (effectively) nothing for messages other
   * than the MSG type.
   *
   * @param body Body of the outgoing message.
   */
  public void setBody( String body ) {
    
    this.body = body;
  }

  /**
   * Returns the MSNP formatted string that represents this message.  For
   * MSG messages, adds the body, and for QRY messages, does something
   * special.
   *
   * @return MSNP formatted string representing this message.
   */
  public String getMessageString() {
    
    String commandString = Message.commandToString( type ) + " ";

    
    /* If the message has a transactionID, include it */
    String transactionIDString = "";
    
    
    if( transactionID.intValue() != NO_TRANSACTION_ID ) 
      transactionIDString = transactionID.toString();
		
    String message = commandString + transactionIDString;

    switch( type ) {
      case Message.QRY:  {
        message = message + " " + arguments.get( 0 ) + " " + arguments.get( 1 ) + arguments.get( 2 ) + arguments.get( 3 );
	return message;
      }
      
      case Message.MSG:  {
        for( int i = 0; i < arguments.size(); i++ ) {
          message = message + " " + ( String ) arguments.get( i );
        }
        
	return message.trim() + "\r\n" + body;
      }
      
      default:  {
        for( int i = 0; i < arguments.size(); i++ ) {
          message = message + " " + ( String ) arguments.get( i );
        }
        
        return message.trim() + "\r\n";
      }
    }
  }

  /**
   * Adds the next argument in the command string.  This interface does
   * not check for validity of the arguments, and accepts them blindly.
   *
   * @param argument The argument that is added the command string.
   */
  public void addArgument( String argument ) {
    
    arguments.add( argument );
  }

  /**
   * Returns the string representation of the message.  Calls the
   * <pre>getMessageString()</pre> method.
   *
   * @return An MSNP string representing the message.
   */
  public String toString() {
    return getMessageString();
  }

  /**
   * Test function.  Creates a bunch of random messages and checks
   * to make sure that the toString() function returns a valid
   * string.
   */
  public static void main( String[] args ) {
    
    OutgoingMessage msg = new OutgoingMessage( Message.ADD, 10 );
    
    msg.addArgument( "AL" ); 
    msg.addArgument( "example@passport.com" ); 
    msg.addArgument( "FirendlyName" ); 
    System.out.println ( msg );

    msg = new OutgoingMessage( Message.ADG, 23 );
    msg.addArgument( "New%20Group" );
    msg.addArgument( "0" );
    System.out.println( msg );
    
    msg = new OutgoingMessage( Message.TWN, 1 );
    System.out.println( msg );
    
    msg = new OutgoingMessage( Message.OUT, NO_TRANSACTION_ID );
    System.out.println( msg );
  }
}
// OutgoingMessage Class
  
/*
>>> ADD 10 AL example@passport.com example@passport.com
>>> ADG 23 New%20Group 0
>>> CHG 9 NLN
>>> INF 1
>>> LST 11 BL
>>> PRP 56 PHW 555%20555-0691
>>> QRY 10 msmsgs@msnmsgr.com 32 (Newline) \n8f2f5a91b72102cd28355e9fc9000d6e (No Newline)
>>> REA 25 example@passport.com My%20New%20Name
>>> REG 25 15 New%20Name 0
>>> REM 12 AL example@passport.com
>>> RMG 24 15
>>> SYN 54 0
>>> USR 2 MD5 I example@passport.com
>>> USR 6 MD5 S 23e54a439a6a17d15025f4c6cbd0f6b5
>>> VER 0 MSNP7 MSNP6 MSNP5 MSNP4 CVR0
>>> XFR 10 SB
>>> CAL 2 name_123@hotmail.com
*/


/*
ADD TransactionID List UserName FriendlyName
ADG TransactionID GroupName GroupNumber?
CHG TransactionID Status
INF TransactionID
LST TransactionID List
PRP TransactionID Property Value
QRY TransactionID msmsgs@msnmsgr.com 32\n
HashedValue
REA TransactionID UserName NewFriendlyName
REG TransactionID GroupID? NewGroupName 0?
REM TransactionID List UserName
RMG TransactionID GroupNumber
SYN TransactionID SerialNumber
USR TransactionID SecurityProtocol I UserName
USR TransactionID SecurityProtocol S Hash
VER TransactionID Protocol {Protocol}*
XFR TransactionID Type
CAL TransactionID UserName
*/