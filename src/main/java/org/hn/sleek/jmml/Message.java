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
 * $Id: Message.java,v 1.2 2003/11/16 16:44:15 popolony2k Exp $
 * $Author: popolony2k $
 * $Name:  $
 * $Revision: 1.2 $
 * $State: Exp $
 *
 */

package org.hn.sleek.jmml;

import java.util.*;
import java.text.ParseException;


/**
 * Represents a Message, encapsulating headers, arguments, body, and transaction id.
 * Provides the message type enumeration.
 */
abstract class Message {

  /** 
   * Represents the tokens in the messages.  For incoming messages, represents each 
   * token in the command stream--even the command and transaction id; for outgoing
   * messages, only represents the arguments to the command.
   */
  ArrayList arguments;

  /** Body of the message. */
  String    body;
  int       type = 0;
  Integer   transactionID = new Integer( -1 );

  static final String    CVR_STRING = "0x0409 winnt 5.1 i386 MSNMSGR 5.0.0540 MSMSGS ";

  
  // All constants values was remade
  final static int ACK   = 1;
  final static int ADD   = 2;
  final static int ADG   = 3;
  final static int ANS   = 4;
  final static int BLP   = 5;
  final static int BPR   = 6;
  final static int BYE   = 7;
  final static int CAL   = 8;
  final static int CHG   = 9;
  final static int CHL   = 10;
  final static int FLN   = 11;
  final static int GTC   = 12;
  final static int ILN   = 13;
  final static int TWN   = 14;
  final static int IRO   = 15;
  final static int JOI   = 16;
  final static int LSG   = 17;
  final static int LST   = 18;
  final static int MSG   = 19;
  final static int NAK   = 20;
  final static int NLN   = 21;
  final static int OUT   = 22;
  final static int PRP   = 23;
  final static int QRY   = 24;
  final static int REA   = 25;
  final static int REG   = 26;
  final static int REM   = 27;
  final static int RMG   = 28;
  final static int RNG   = 29;
  final static int SYN   = 30;
  final static int USR   = 31;
  final static int VER   = 32;
  final static int XFR   = 33;
  final static int CVR   = 34;
  final static int SDC   = 35;
  final static int ERROR = 36;  // Server error

  static HashMap commandMap;

  static {
    commandMap = new HashMap();
    commandMap.put( "ACK", new Integer( Message.ACK ) );
    commandMap.put( "ADD", new Integer( Message.ADD ) );
    commandMap.put( "ADG", new Integer( Message.ADG ) );
    commandMap.put( "ANS", new Integer( Message.ANS ) );
    commandMap.put( "BLP", new Integer( Message.BLP ) );
    commandMap.put( "BPR", new Integer( Message.BPR ) );
    commandMap.put( "BYE", new Integer( Message.BYE ) );
    commandMap.put( "CAL", new Integer( Message.CAL ) );
    commandMap.put( "CHG", new Integer( Message.CHG ) );
    commandMap.put( "CHL", new Integer( Message.CHL ) );
    commandMap.put( "FLN", new Integer( Message.FLN ) );
    commandMap.put( "GTC", new Integer( Message.GTC ) );
    commandMap.put( "ILN", new Integer( Message.ILN ) );
    commandMap.put( "TWN", new Integer( Message.TWN ) );
    commandMap.put( "IRO", new Integer( Message.IRO ) );
    commandMap.put( "JOI", new Integer( Message.JOI ) );
    commandMap.put( "LSG", new Integer( Message.LSG ) );
    commandMap.put( "LST", new Integer( Message.LST ) );
    commandMap.put( "MSG", new Integer( Message.MSG ) );
    commandMap.put( "NAK", new Integer( Message.NAK ) );
    commandMap.put( "NLN", new Integer( Message.NLN ) );
    commandMap.put( "OUT", new Integer( Message.OUT ) );
    commandMap.put( "PRP", new Integer( Message.PRP ) );
    commandMap.put( "QRY", new Integer( Message.QRY ) );
    commandMap.put( "REA", new Integer( Message.REA ) );
    commandMap.put( "REG", new Integer( Message.REG ) );
    commandMap.put( "REM", new Integer( Message.REM ) );
    commandMap.put( "RMG", new Integer( Message.RMG ) );
    commandMap.put( "RNG", new Integer( Message.RNG ) );
    commandMap.put( "SYN", new Integer( Message.SYN ) );
    commandMap.put( "USR", new Integer( Message.USR ) );
    commandMap.put( "VER", new Integer( Message.VER ) );
    commandMap.put( "XFR", new Integer( Message.XFR ) );
    commandMap.put( "CVR", new Integer( Message.CVR ) );
    commandMap.put( "SDC", new Integer( Message.SDC ) );
  }

  
  /**
   * Constructs a Message Object
   */
  protected Message() {
    
    arguments = new ArrayList();
  }

  /**
   * Returns whether the message type has a body.
   *
   * @return Whether the message type has a body.
   */
  boolean hasBody() {
    
    /* Only MSG types have bodies. */
    if( type == Message.MSG ) 
      return true;
    else 
      return false;
  }

  /**
   * Sets the body of the message.
   *
   * @param body The new body of the message.
   */
  void setBody( String body ) {
    
    this.body = body;
  }

  /**
   * Returns the body of the message.
   *
   * @return The body of the message.
   */
  String getBody() {
    
    return body;
  }

  /**
   * Returns the transaction ID of the message.
   *
   * @return The transaction ID of the message.
   */
  int getTransactionID() {
    
    return transactionID.intValue();
  }

  /**
   * Returns the type of message that this is.
   *
   * @return The message type.
   */
  int getType() {
    
    return type;
  }

  /**
   * Returns the message type (as an int) depending on the command
   * string.
   *
   * @return The command type.
   * @throws ParseException When the command type is invalid.
   */
  static int parseCommand( String rawCommand ) throws ParseException {
    
    String trimmedCommand = rawCommand.trim();

    /* If it's a valid message type, then it's in the commandMap */
    if( commandMap.containsKey( trimmedCommand ) ) {
      return ( ( Integer ) commandMap.get( trimmedCommand ) ).intValue();
    }

    try {
      /* Error types */
      Integer.parseInt( trimmedCommand );
      return Message.ERROR;
    } catch( NumberFormatException e ) {
      /* What IS this thing?! */
      throw new ParseException( trimmedCommand + " is not a valid command.", 0 );
    }
  }

  /**
   * Returns the String representation of a command type.  If the command
   * type is invalid, returns "NOT_A_COMMAND" as a string.
   *
   * @param commandType A message command type.
   * @return The string representation of the command.
   */
  static String commandToString( int commandType ) {
    
    switch( commandType ) {
      
      case Message.ACK: return "ACK"; 
      case Message.ADD: return "ADD"; 
      case Message.ADG: return "ADG"; 
      case Message.ANS: return "ANS"; 
      case Message.BLP: return "BLP"; 
      case Message.BPR: return "BPR"; 
      case Message.BYE: return "BYE"; 
      case Message.CAL: return "CAL"; 
      case Message.CHG: return "CHG"; 
      case Message.CHL: return "CHL"; 
      case Message.FLN: return "FLN"; 
      case Message.GTC: return "GTC"; 
      case Message.ILN: return "ILN"; 
      case Message.TWN: return "TWN";
      case Message.IRO: return "IRO"; 
      case Message.JOI: return "JOI"; 
      case Message.LSG: return "LSG"; 
      case Message.LST: return "LST"; 
      case Message.MSG: return "MSG";
      case Message.NAK: return "NAK"; 
      case Message.NLN: return "NLN"; 
      case Message.OUT: return "OUT"; 
      case Message.PRP: return "PRP"; 
      case Message.QRY: return "QRY";
      case Message.REA: return "REA";
      case Message.REG: return "REG"; 
      case Message.REM: return "REM"; 
      case Message.RMG: return "RMG"; 
      case Message.RNG: return "RNG"; 
      case Message.SYN: return "SYN"; 
      case Message.USR: return "USR"; 
      case Message.VER: return "VER"; 
      case Message.XFR: return "XFR"; 
      case Message.CVR: return "CVR";
      case Message.SDC: return "SDC";
      case Message.ERROR: return "ERROR";
      default: return "NOT_A_COMMAND";
    }
  }

  /**
   * Returns a string representation of the Message.  Note that this isn't
   * the string representation that is sent over the wire to the Messenger
   * servers; instead, it is a debug-style string representation.
   *
   * @return A debug string representation of the Message.
   */
  public String toString() {
    
    String    commandString;
    String    transactionIDString;
    String    argumentsString = "";
    String    bodyString      = "";
		
    if( getType() == 0 ) {
      commandString = "[No Command] ";
    }
    else {
      commandString = commandToString( type );
    }

    if( transactionID.intValue() == -1 ) {
      transactionIDString = "[No Transaction ID] ";
    }
    else {
      transactionIDString = transactionID.toString();
    }

    if( arguments == null ) {
      argumentsString = "[No Arguments] ";
    }
    else {
      argumentsString = arguments.toString();
    }

    if( hasBody() ) {
      bodyString = body;
    }
    
    return commandString + transactionIDString  + argumentsString  + "\n" + bodyString;
  }
}
// Message Class