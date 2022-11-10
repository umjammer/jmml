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
 * $Id: IncomingMessage.java,v 1.2 2003/11/16 16:44:15 popolony2k Exp $
 * $Author: popolony2k $
 * $Name:  $
 * $Revision: 1.2 $
 * $State: Exp $
 *
 */

package org.hn.sleek.jmml;

import java.text.ParseException;
import java.util.*;

/**
 * Represents an incoming message from the server.  Encapsulates all
 * operations on an incoming message.  Abstracted operations throw 
 * exceptions for unsupported property retrievals for a given type.
 * Methods are smart enough to parse for the correct argument given 
 * the command type.
 * <p>
 * IncomingMessages are immutable.
 * <p>
 * Unimplemented methods (since servers don't do anything interesting
 * with them yet) include: getProtocols (from VER), getServerProtocol
 * (from INF), 
 * 
 */
class IncomingMessage extends Message {

  /**
    * Parses the command string from the server, and returns an IncomingMessage
    * object ready for use.
    *
    * @param incomingMessage String representation of the message.
    * @throws ParseException When the parsing of the message breaks.
    */
  static IncomingMessage parseMessage( String incomingMessage ) throws ParseException {
    
    if( incomingMessage == null )
      throw new ParseException( "null message received" , 1 );
      
    StringTokenizer   st      = new StringTokenizer( incomingMessage );
    IncomingMessage   message = new IncomingMessage();
    String            command, tid, argument;
    
    command = st.nextToken();
    message.arguments.add (command);
    message.type = Message.parseCommand (command);

    // Messages with transaction ID's must have that field filled in.
    switch( message.type ) {
      
      case Message.ADD:
      case Message.BLP: 
      case Message.BPR:
      case Message.CHG:
      case Message.CHL:
      case Message.GTC:
      case Message.ILN:
      case Message.LST:
      case Message.LSG:
      case Message.REM:
      case Message.SYN:
      case Message.USR:
      case Message.VER:
      case Message.SDC:
      case Message.XFR:
        tid = st.nextToken();
        message.arguments.add( tid );
                                
        try {
          message.transactionID = new Integer( tid );
        } catch( NumberFormatException e ) {
          // Commands like LST and BPR don't have a Transaction ID for SYN responses
          if( ( message.type != Message.LST ) && ( message.type != Message.BPR ) )  {
            throw new ParseException( "Invalid transaction ID", 1 );
          }
        } break;

      /* 
       * Only some error message have transaction ID's; parse the ones
       * that do, and continue for the ones that don't. 
       */
      case Message.ERROR:
        tid = st.nextToken();
        message.arguments.add( tid );
        
        try {
          message.transactionID = new Integer( tid );
        } catch( NumberFormatException e ) {
          /* 
           * If that error message didn't have a transaction ID, then
           * treat the token we pulled out as an argument (since we
           * can't put it back in the tokenizer. 
           */
          message.arguments.add( tid );
        }  break;
    }
    
    // All other tokens in the message are considered as arguments.
    while( st.hasMoreTokens() ) {
      argument = st.nextToken();
      message.arguments.add( argument );
    }
    
    return message;
  }

  /**
    * Returns the Protocol fields in VER commands.
    * <pre>VER TransactionID Protocol {, Protocol}* </pre>
    *
    * @return String of protocols (all of them concatenated).
    * @throws NoSuchFieldException When the message type doesn't have
    * a protocols field.
    */
  String getProtocols() throws NoSuchFieldException {
    
    switch( type ) {
      
      case Message.VER:
        
        String protocolList = "";
        
        for( int i = 0; i < arguments.size(); i++ ) {
          if( i < 2 ) 
            continue;
          else 
            protocolList += ( String ) arguments.get( i ) + " ";
        }
        
        return protocolList;

      default: throw new NoSuchFieldException( commandToString( type ) + " message types don't have a protocol list." );
    }
  }

  /**
   * Returns the security protocol embedded in the INF and USR
   * messages.
   * <pre>INF TransactionID SecurityProtocol
   * USR TransactionID SecurityProtocol S Hash</pre>
   *
   * @return String name of the security protocol.
   * @throws NoSuchFieldException When the message type doesn't have
   * a security protocol field.
   */
  String getSecurityProtocol() throws NoSuchFieldException {
    
    switch( type ) {
      //case Message.INF:
      case Message.USR: 
        /* BUG: This breaks for USR commands that are "OK" */
        return ( String ) arguments.get( 2 );
        
      default: throw new NoSuchFieldException( commandToString( type ) + " messages don't have security protocol." );
    }
  }


  /**
   * Returns the friendly name (screen name) from the message.  The
   * friendly name that is returned is still UTF-8 encoded.
   *
   * <pre>NLN Status UserName FriendlyName
   * USR TransactionID OK UserName FriendlyName
   * ILN TransactionID Status UserName FriendlyName
   * LST TransactionID List SerialNumber ItemNumber TotalItems UserName FriendlyName
   * ADD TransactioNID List SerialNumber UserName FriendlyName
   * REA TransactionID SerialNumber UserName FriendlyName
   * RNG SessionID Server:Port CKI Hash UserName FriendlyName
   * MSG UserName FriendlyName BodyLength</pre>
   *
   * @return UTF-8-encoded friendly name.
   * @throws NoSuchFieldException When the message type doesn't have a friendly
   * name field.
   */
  String getFriendlyName() throws NoSuchFieldException {
    
    switch( type ) {
      
      case Message.MSG:  return ( String ) arguments.get( 2 );
      case Message.NLN:  return ( String ) arguments.get( 3 );
      case Message.USR: 
      case Message.ILN:
      case Message.REA:  return ( String ) arguments.get( 4 ); /* BUG: when USR is in S mode and not OK yet */
      case Message.ADD:  return ( String ) arguments.get( 5 );
      case Message.RNG:  return ( String ) arguments.get( 6 );
      case Message.LST:  {

        Object     objValue;
        
        /*
         * TODO: Microsoft is disabling (??) LST requests. Maybe in future
         * the, objValue = arguments.get( 7 ), will be removed.
         */
        try  {
          objValue = arguments.get( 7 );
        }  catch( Exception e )  {
          /*
           * If a exception is caught, this message can be a SYN response.
           * and then the order of argument is other.
           */
          objValue = arguments.get( 2 );
        }

        return ( objValue != null ? java.net.URLDecoder.decode( ( String ) objValue ) : "" );
      }
      
      default: throw new NoSuchFieldException( commandToString( type ) + " messages don't have friendlyname." );
    }
  }

  /**
   * Returns the Passport username from the message.
   * 
   * <pre>FLN UserName
   * NLN Status UserName FriendlyName
   * BPR TransactionID UserName Property [Value]
   * REA TransactionID SerialNumber UserName FriendlyName
   * USR TransactionID OK UserName FriendlyName
   * ILN TransactionID Status UserName FriendlyName
   * ADD TransactioNID List SerialNumber UserName FriendlyName
   * REM TransactionID List SerialNumber UserName
   * RNG SessionID Server:Port CKI Hash UserName FriendlyName
   * LST TransactionID List SerialNumber ItemNumber TotalItems UserName FriendlyName
   * MSG UserName FriendlyName BodyLength</pre>
   *
   * @return Passport user name.
   * @throws NoSuchFieldException When the message type doesn't have a
   * username field.
   */
  String getUserName() throws NoSuchFieldException {
    
    switch( type ) {
      case Message.FLN:
      case Message.MSG:  return ( String ) arguments.get( 1 );
      case Message.NLN:
      case Message.BPR:  return ( String ) arguments.get( 2 );
      case Message.USR: 
      case Message.ILN:
      case Message.REA:  return ( String ) arguments.get( 3 ); /* BUG: breaks when USR is in mode S */
      case Message.ADD:
      case Message.REM:  return ( String ) arguments.get( 4 );
      case Message.RNG:  return ( String ) arguments.get( 5 );
      case Message.LST:  {
        
        Object     objValue;
        
        /*
         * TODO: Microsoft is disabling (??) LST requests. Maybe in future
         * the, objValue = arguments.get( 6 ), will be removed.
         */
        try  {
          objValue = arguments.get( 6 );
        }  catch( Exception e )  {
          /*
           * If a exception is caught, this message can be a SYN response.
           * and then the order of argument is other.
           */
          objValue = arguments.get( 1 );
        }
        
        return ( objValue != null ? ( String ) objValue : "" );
      }
      
      default : throw new NoSuchFieldException( commandToString( type ) + " messages don't have username." );
    }
  }

  /**
   * Returns the property name that has a new value.
   *
   * <pre>BPR TransactionID UserName Property [Value]
   * PRP TransactionID UserName Property [Value]</pre>
   *
   * @return The property name.
   * @throws NoSuchFieldException When the message type doesn't have a property
   * name field.
   */

   /* TODO: This needs to be renamed */
  String getProperty() throws NoSuchFieldException {
    
    switch( type ) {
      
      case Message.BPR:
      case Message.PRP:  return ( String ) arguments.get( 3 );
      default:  throw new NoSuchFieldException( commandToString( type ) + " messages don't have properties." );
    }
  }

  /**
   * Returns the new value for the property.
   *
   * <pre>BPR TransactionID UserName Property [Value]
   * PRP TransactionID UserName Property [Value]</pre>
   *
   * @return The new value for the property.
   * @throws NoSuchFieldException When the message type doesn't have a new value.
   */
  String getValue() throws NoSuchFieldException {
    
    switch( type ) {
      case Message.BPR:
      case Message.PRP:
        try {
          return ( String ) arguments.get( 4 );
        } catch( IndexOutOfBoundsException e ) {
          return null;
        }
        
        default: throw new NoSuchFieldException( commandToString( type ) + " message don't have property values." );
    }
  }

  /**
   * Gets the referral type for the XFR command.  Valid return
   * strings are NB and SB.  NB stands for Notification Server;
   * SB stands for switchboard server.
   *
   * <pre>XFR TransactionID ReferralType Address:PortNumber</pre>
   *
   * @return The referral type (SB or NB).
   * @throws NoSuchFieldException When the message type doesn't have a
   * referral type field.
   */
  String getReferralType() throws NoSuchFieldException {
    
    switch( type ) {
      case Message.XFR:  return( String ) arguments.get( 2 );
      default:  throw new NoSuchFieldException( commandToString( type ) + " messages don't have referraltype." );
    }
  }

  /**
   * Returns the server IP address.
   *
   * <pre>RNG SessionID Server:Port CKI Hash UserName FriendlyName
   * XFR TransactionID ReferralType Address[:PortNumber]</pre>
   *
   * @return The server's IP address.
   * @throws NoSuchFieldException When the message type doesn't have a
   * server IP field.
   */
  String getServerIP() throws NoSuchFieldException {
    
    String serverIP;
    
    switch( type ) {
      
      case Message.RNG:
        serverIP = ( String ) arguments.get( 2 );
        return ( serverIP.indexOf( ":" ) > -1 ) ? serverIP.substring( 0, serverIP.indexOf( ":" ) ) : serverIP;
        
      case Message.XFR: 
        serverIP = ( String ) arguments.get( 3 );
	return ( serverIP.indexOf( ":" ) > -1 ) ? serverIP.substring( 0, serverIP.indexOf( ":" ) ) : serverIP;
        
      default: throw new NoSuchFieldException( commandToString( type ) + " messages don't have an IP address." );
    }
  }

  /**
   * Returns the port to connect to on the server (or defaults to 1863
   * if there is a parse exception).
   *
   * <pre>RNG SessionID Server:Port CKI Hash UserName FriendlyName
   * XFR TransactionID ReferralType Address[:PortNumber]</pre>
   *
   * @return The port to connect to on the server.
   * @throws NoSuchFieldException When the message type doesn't have
   * a server port field.
   */
  int getServerPort() throws NoSuchFieldException {
    
    String serverIP;

    switch( type ) {
      
      case Message.RNG:
        serverIP = ( String ) arguments.get( 2 );
        
        if( serverIP.indexOf( ":" ) > -1 ) {
          try {
            return Integer.parseInt( serverIP.substring( serverIP.indexOf( ":" ) + 1 ) );
          } catch( NumberFormatException e ) {
            return 1863;
          }
        }
        else {
          return 1863;
        }
        
      case Message.XFR:
        serverIP = ( String ) arguments.get( 3 );

        if( serverIP.indexOf( ":" ) > -1 ) {
          try {
            return Integer.parseInt( serverIP.substring( serverIP.indexOf( ":" ) + 1 ) );
          } catch( NumberFormatException e ) {
            return 1863;
          }
        }
        else {
          return 1863;
        }
        
      default: throw new NoSuchFieldException( commandToString( type ) + " messages don't have port numbers." );
    }
  }
  
  /**
   * Returns the serial number from the message.  If there is some sort of
   * parsing error, then -1 is returned.
   *
   * <pre>BLP TransactionID SerialNumber [AL|BL]
   * GTC TransactionID SerialNumber [A|N]
   * SYN TransactionID SerialNumber
   * LSG TransactionID SerialNumber ItemNumber TotalItems GroupID GroupName 0
   * BPR SerialNumber Property [Value]
   * LST TransactionID List SerialNumber ItemNumber TotalItems UserName FriendlyName
   * ADD TransactioNID List SerialNumber UserName FriendlyName
   * REM TransactionID List SerialNumber UserName
   * PRP TransactionID SerialNumber Property [Value]
   * REA TransactionID SerialNUmber UserName FriendlyName
   * REG TransactionID SerialNumber GroupID GroupName 0
   * RMG TransactionID SerialNumber GroupID</pre>
   * 
   * @return The serial number.
   * @throws NoSuchFieldException When the message type doesn't contain a
   * serial number field.
   */
  int getSerialNumber() throws NoSuchFieldException {
    
    switch( type ) {
      
      case Message.BPR:
        try {
          return Integer.parseInt( ( String ) arguments.get( 1 ) );
        } catch( NumberFormatException e ) {
          return -1;
        }
        
      case Message.BLP:
      case Message.GTC:
      case Message.SYN:
      case Message.LSG:
      case Message.PRP:
      case Message.REA:
      case Message.REG:
      case Message.RMG:
        try {
          return Integer.parseInt( ( String ) arguments.get( 2 ) );
        } catch( NumberFormatException e ) {
          return -1;
        }
      
      /*
       * TODO: Microsoft is disabling (??) LST requests. Maybe in future
       * the, case Message.LST:, will be removed.
       */
      case Message.LST:
      case Message.ADD:
      case Message.REM:
        try {
          return Integer.parseInt( ( String ) arguments.get( 3 ) );
        } catch( NumberFormatException e ) {
          return -1;
        }
        
      default:  throw new NoSuchFieldException( commandToString( type ) + " messages don't have a serial number." );
    }
  }

  /**
   * Returns the contact status contained in the message.
   * 
   * <pre>ILN TransactionID Status UserName FriendlyName
   * NLN Status UserName FriendlyName
   * CHG TransactionID Status</pre>
   *
   * @return A String representing the status in the message.
   * @throws NoSuchFieldException When the message doesn't contain a
   * status.
   */
  String getStatus() throws NoSuchFieldException {
    
    String rawStatus = "";
    
    
    switch( type ) {
      
      case Message.FLN:
        rawStatus = "FLN"; /* User is Offline */
        break;
        
      case Message.NLN:
        rawStatus = ( String ) arguments.get( 1 );
        break;
        
      case Message.CHG:
      case Message.ILN:
        rawStatus = ( String ) arguments.get( 2 );
        break;
        
      default:  throw new NoSuchFieldException( commandToString( type ) + " messages don't have a status." );
    }
    
    return rawStatus;
  }

  /**
   * Returns a Hashtable containing all types of contact list this message was referring
   * to.
   * 
   * <pre>LST TransactionID List SerialNumber ItemNumber TotalItems UserName FriendlyName
   * ADD TransactioNID List SerialNumber UserName FriendlyName
   * REM TransactionID List SerialNumber UserName</pre>
   *
   * @return A ContactList object representing the contact list type.
   * @throws NoSuchFieldException When the message type doesn't contain a
   * contact list type.
   */
  Hashtable getListType() throws NoSuchFieldException {
    
    switch( type ) {

      case Message.LST:  {
        /*
         * TODO: Microsoft is disabling (??) LST requests. Maybe in future
         * the, try block, will be removed.
         */
        /*
        try  {
          Integer.parseInt( ( String ) arguments.get( 3 ) );
        }  catch( NumberFormatException e )  {
          return ContactList.parseListType( ( String ) arguments.get( 2 ) );
        }
        */
        
        // Is a SYN response !!
        return ContactList.parseListType( ( String ) arguments.get( 3 ) );
      }
      case Message.ADD:
      case Message.REM:  return ContactList.parseListType( ( String ) arguments.get( 2 ) );
      default:  throw new NoSuchFieldException( commandToString( type ) + " messages don't have list type." );
    }
  }
  
  // << LSG TransactionID SerialNumber ItemNumber TotalItems GroupID GroupName 0
  /**
   * Gets the item number from the message.
   *
   * <pre>LSG TransactionID SerialNumber ItemNumber TotalItems GroupID GroupName 0
   * LST TransactionID List SerialNumber ItemNumber TotalItems UserName FriendlyName</pre>
   *
   * @return The item number.
   * @throws NoSuchFieldException When the message type doesn't have an item number
   * field.
   */
  int getItemNumber() throws NoSuchFieldException {
    
    switch( type ) {
      case Message.LSG:
        try {
          return Integer.parseInt( ( String ) arguments.get( 3 ) );
        } catch( NumberFormatException e ) {
          return -1;
        }
        
      case Message.LST:
        try {
          return Integer.parseInt( ( String ) arguments.get( 4 ) );
        } catch( NumberFormatException e ) {
          return -1;
        }
        
      default:  throw new NoSuchFieldException( commandToString( type ) + " messages don't have item number." );
    }
  }

  /**
   * Returns the total number of items in the list (in LSG and LST message types).
   *
   * @return The total number of items in the list.
   * @throws NoSuchFieldException When the message type doesn't contain a
   * total items field.
   */
  int getTotalItems() throws NoSuchFieldException {
    
    switch( type ) {
      case Message.LSG:
        try {
          return Integer.parseInt( ( String ) arguments.get( 4 ) );
        } catch( NumberFormatException e ) {
          return -1;
        }

      case Message.LST:
        try {
          return Integer.parseInt( ( String ) arguments.get( 5 ) );
        } catch( NumberFormatException e ) {
          return -1;
        }
        
      default:  throw new NoSuchFieldException( commandToString( type ) + " messages don't have totalitems." );
    }
  }
        
  /**
   * Get the group ID for LSG messages (for SYN and LSG messages)
   */
  ArrayList getGroups()  {

    switch( type ) {

      case Message.LST :  {

        /*
         * TODO: Microsoft is disabling (??) LST requests. Maybe in future
         * the, try block, will be removed and only the catch block will remain.
         */
        try {
          /*
           * Only to know if we are in SYN response or is a LST response.
           * LST don't have a group id parameter.
           */
          Integer.parseInt( ( String ) arguments.get( 5 ) );
        } catch( Exception e ) {

          /*
           * If a exception is caught, this message can be a SYN response.
           * and then the order of argument is other.
           */
          
          if( arguments.size() < 4 )  {
            
            StringTokenizer   parser  = new StringTokenizer( ( String ) arguments.get( 4 ), "," );
            ArrayList         aGroups = new ArrayList();
            Integer           iValue;
        
            try  {
              while( parser.hasMoreTokens() )  {
                iValue = new Integer( Integer.parseInt( ( String ) parser.nextToken() ) );
                aGroups.add( iValue );
              }
            } catch( java.util.NoSuchElementException ex )  {
              System.err.println( "IncomingMessage.getGroups() - " + ex );
            }
          
            return aGroups;
          }
          else
            return null;
        }
      }

      case Message.LSG :  {
        
        /*
         * TODO: Microsoft is disabling (??) LSG requests. Maybe in future
         * the, try block, will be removed and only the catch block will remain.
         */
        try {
          ArrayList         aGroups = new ArrayList();
          Integer           iValue  = new Integer( Integer.parseInt( ( String ) arguments.get( 5 ) ) );
          
          aGroups.add( iValue );
          return aGroups;
          
        } catch( Exception e ) {
            
          /*
           * If a exception is caught, this message can be a SYN response.
           * and then the order of argument is other.
           */
          try  {
            ArrayList         aGroups = new ArrayList();
            Integer           iValue  = new Integer( Integer.parseInt( ( String ) arguments.get( 1 ) ) );
          
            aGroups.add( iValue );
            return aGroups;
          
          } catch( Exception ex )  {
            return null;
          }
        }
      }
            
      default : return null;
    }
  }

  /**
   * Get the group name for LSG messages (for SYN and LSG messages)
   */
  String getGroupName()  {
          
    switch( type ) {

      case Message.LSG:  {
        /*
         * TODO: Microsoft is disabling (??) LSG requests. Maybe in future
         * the, try block, will be removed and only the catch block will remain.
         */
        try {
          return java.net.URLDecoder.decode( arguments.get( 6 ).toString() );
        } catch( Exception e ) {

          /*
           * If a exception is caught, this message can be a SYN response.
           * and then the order of argument is other.
           */
          try  {
            return java.net.URLDecoder.decode( arguments.get( 2 ).toString() );
          } catch( Exception ex )  {
            return "None";
          }
        }
      }

      default : return "None";
    }
  }

  /**
   * Gets the argument for the GTC message.
   * 
   * <pre>GTC TransactionID SerialNumber [A|N]</pre>
   *
   * @return The argument to the GTC message.
   * @throws NoSuchFieldException When the message type doesn't have a GTC argument.
   */
  String getGTCArgument() throws NoSuchFieldException {
    
    switch( type ) {
      case Message.GTC:
        return ( String ) arguments.get( 3 );
        
      default:  throw new NoSuchFieldException( commandToString( type ) + " messages don't have a GTC argument." );
    }
  }

  /**
   * Gets the argument for the BLP message.
   *
   * <pre>BLP TransactionID SerialNumber [AL|BL]</pre>
   *
   * @return The argument to the BLP message.
   * @throws NoSuchFieldException When the message type doesn't have a BLP argument.
   */
  String getBLPArgument() throws NoSuchFieldException {
    
    switch( type ) {
      
      case Message.BLP:  return ( String ) arguments.get( 3 );
      
      default:  throw new NoSuchFieldException( commandToString( type ) + " messages don't have a BLP argument." );
    }
  }

  /**
   * Returns the hash challenge issued by the server.
   * 
   * <pre>CHL TransactionID Hash
   * USR TransactionID SecurityProtocol S Hash
   * RNG SessionID Server:Port CKI Hash UserName FriendlyName</pre>
   * 
   * @return The challenge hash.
   * @throws NoSuchFieldException When the message type doesn't have a challenge hash.
   */
  String getChallengeHash() throws NoSuchFieldException {
    
    switch( type ) {

      case Message.CHL:  return ( String ) arguments.get( 2 );
      case Message.USR:
      case Message.RNG:  {
        Object    objValue = arguments.get( 4 );

        return ( String ) ( objValue != null ? ( String ) objValue : "" );
      }
      case Message.XFR:
        try {
          return ( String ) arguments.get( 5 );
        } catch( IndexOutOfBoundsException e ) { 
          /* TODO: This is ugly, but catchches XFR's without hash challenges */
          return null;
        }

      default: throw new NoSuchFieldException( commandToString( type ) + " messages don't have a challenge hash." );
    }
  }

  /**
   * Returns the session ID for the conversation.
   *
   * <pre>RNG SessionID ServerIP:Port "CKI" ChallengeHash UserName FriendlyName
   * CAL TransactionID "RINGING" SessionID</pre>
   *
   * @return The sessionID as a string.
   * @throws NoSuchFieldException When the message type doesn't have a
   * session ID.
   */
  String getSessionID() throws NoSuchFieldException {
    
    switch( type ) {
      
      case Message.RNG:  return ( String ) arguments.get( 1 );
      case Message.CAL:  return ( String ) arguments.get( 3 );
      default:  throw new NoSuchFieldException( commandToString( type ) + " messages don't have a session id." );
    }
  }

  /**
   * Returns the exit status code sent by the server.
   *
   * <pre>OUT StatusCode</pre>
   *
   * @return The exit status code.
   * @throws NoSuchFieldException When the message type doesn't have an exit
   * status.
   */
  String getExitStatus() throws NoSuchFieldException {
    
    switch( type ) {
      case Message.OUT:
        try {
          return ( String ) arguments.get( 1 );
        } catch( IndexOutOfBoundsException e ) { 
          /* TODO: no exit status */
          return "";
        }
        
      default:  throw new NoSuchFieldException( commandToString( type ) + " messages don't have exit status." );
    }
  }

  /**
   * Returns the error code.  If there's some sort of error, returns -1.
   *
   * @return The error code for the message.
   * @throws NoSuchFieldException When the message type doesn't have an error code.
   */
  int getErrorCode() throws NoSuchFieldException {
    
    if( type != Message.ERROR ) 
      throw new NoSuchFieldException( commandToString( type ) + " isn't an error code." );
    
    try {
      return Integer.parseInt( ( String ) arguments.get( 0 ) );
    }  catch( NumberFormatException e ) {
      return -1;
    }
  }
	
  /**
   * Returns the length of the body as reported in the command
   * string.  Returns -1 if there's some sort of problem.
   *
   * <pre>MSG TransactionID AckType BodyLength
   * MSG UserName FriendlyName BodyLength
   * MSG ServiceType ServiceType BodyLength</pre>
   *
   * @return The length of the body.
   * @throws NoSuchFieldException When the message type doesn't have
   * a body length field.
   */
  int getBodyLength() throws NoSuchFieldException {
    
    switch( type ) {
      case Message.MSG:
        try {
          return Integer.parseInt( ( String ) arguments.get( 3 ) );
        } catch( NumberFormatException e ) {
          // if you get here, you're doomed.
          return 0;
        }
        
      default:  throw new NoSuchFieldException( commandToString( type ) + " messages don't have a body length." );
    }
  }
    
  /**
   * Just a smattering of test cases.
   */
  public static void main( String[] args ) {
    
    if( args.length > 0 ) {
      System.out.println( "Test the arg for validity." );
    }
    
    String[] cmds = {   "ADD 0 RL 105 example@passport.com Mike",
                        "ADD 0 RL 105 example@passport.com Mike", 
                        "BLP 54 12182 AL", 
                        "BPR 12182 example@passport.com MOB N",
                        "BPR 12182 example@passport.com MOB N", 
                        "BPR 12182 example@passport.com PHH", 
                        "BPR 12182 example@passport.com PHH", 
                        "BPR 12182 example@passport.com PHM I%20Dont%20Have%20One", 
                        "BPR 12182 example@passport.com PHM I%20Dont%20Have%20One", 
                        "BPR 12182 example@passport.com PHW 555%20555-1234", 
                        "BPR 12182 example@passport.com PHW 555%20555-1234", 
                        "BPR 12182 myname@msn.com MOB N", 
                        "BPR 12182 myname@msn.com MOB Y", 
                        "BPR 12182 myname@msn.com MOB Y", 
                        "BPR 12182 myname@msn.com PHH", 
                        "BPR 12182 myname@msn.com PHH 555%20555%204321", 
                        "BPR 12182 myname@msn.com PHH 555%20555%204321", 
                        "BPR 12182 myname@msn.com PHM", 
                        "BPR 12182 myname@msn.com PHM", 
                        "BPR 12182 myname@msn.com PHM", 
                        "BPR 12182 myname@msn.com PHW", 
                        "BPR 12182 myname@msn.com PHW I%20AM%20DUMB", 
                        "BPR 12182 myname@msn.com PHW I%20AM%20DUMB", 
                        "CHG 10 HDN", 
                        "CHG 7 NLN", 
                        "CHG 8 AWY", 
                        "CHG 9 NLN", 
                        "CHL 0 15570131571988941333", 
                        "FLN name_123@hotmail.com", 
                        "GTC 54 12182 A", 
                        "ILN 7 AWY example@passport.com Mike", 
                        "ILN 7 BSY myname@msn.com My%20Name", 
                        "ILN 7 NLN name_123@hotmail.com Name_123", 
                        "INF 1 MD5", 
                        "INF 4 MD5", 
                        "LSG 54 12182 1 3 0 Other%20Contacts 0", 
                        "LSG 54 12182 1 3 0 Other%20Contacts 0", 
                        "LSG 54 12182 2 3 2 Group1 0", 
                        "LSG 54 12182 2 3 2 Group1 0", 
                        "LSG 54 12182 3 3 5 Group2 0", 
                        "LSG 54 12182 3 3 5 Group2 0", 
                        "LST 10 FL 21 1 3 example@passport.com Mike 0", 
                        "LST 10 FL 21 2 3 name_123@hotmail.com Name_123 2", 
                        "LST 10 FL 21 2 3 name_123@hotmail.com Name_123 2", 
                        "LST 10 FL 21 3 3 myname@msn.com My%20Name 0", 
                        "LST 11 BL 3 0 0", 
                        "LST 54 AL 12182 1 3 myname@msn.com My%20Name", 
                        "LST 54 AL 12182 2 3 example@passport.com Mike", 
                        "LST 54 AL 12182 3 3 name_123@hotmail.com Name_123", 
                        "LST 54 BL 12182 0 0", 
                        "LST 54 FL 12182 1 2 example@passport.com Mike 0", 
                        "LST 54 FL 12182 1 2 example@passport.com Mike 0", 
                        "LST 54 FL 12182 2 2 myname@msn.com My%20Name 2", 
                        "LST 54 FL 12182 2 2 myname@msn.com Name_123 2", 
                        "LST 54 RL 12182 1 2 myname@msn.com My%20Name", 
                        "NLN AWY example@passport.com Mike", 
                        "NLN NLN myname@msn.com My%20Name", 
                        "OUT",
                        "OUT OTH",
                        "OUT SSD",
                        "PRP 54 12182 MBE N", 
                        "PRP 54 12182 MBE N", 
                        "PRP 54 12182 MOB Y", 
                        "PRP 54 12182 MOB Y", 
                        "PRP 54 12182 PHH 555%20555-0690", 
                        "PRP 54 12182 PHH 555%20555-0690", 
                        "PRP 54 12182 PHM", 
                        "PRP 54 12182 PHM", 
                        "PRP 54 12182 PHW", 
                        "PRP 54 12182 PHW", 
                        "PRP 55 12183 PHH 555%20555-0690", 
                        "PRP 56 12184 PHW 555%20555-0691", 
                        "QRY 10", 
                        "REA 25 115 example@passport.com My%20New%20Name", 
                        "REG 25 12066 15 New%20Name 0", 
                        "REM 0 RL 106 example@passport.com", 
                        "RMG 24 12065 15", 
                        "RNG 11752099 64.4.12.193:1863 CKI 849102291.520491932 myname@msn.com My%20Name", 
                        "SYN 54 12182", 
                        "USR 5 MD5 S 1013928519.693957190", 
                        "USR 6 OK example@passport.com My%20Screen%20Name 1", 
                        "VER 0 MSNP7 MSNP6 MSNP5 MSNP4 CVR0", 
                        "VER 3 MSNP7 MSNP6 MSNP5 MSNP4 CVR0", 
                        "XFR 10 SB 64.4.12.193:1863 CKI 16925950.1016955577.17693", 
                        "XFR 2 NS 64.4.12.132:1862 0",
                        "XFR 2 NS 64.4.12.133 0",
                        "911 It's over dude",
                        "220 Bad Call"
                    };
                    
                  
    System.out.println( "*-*-*-*-*-*-*-*-*-*-*" );
    
    for( int i = 0; i < cmds.length; i++ ) {
      System.out.println( "----------------" );
      
      try {
        System.out.println( "Original: " + cmds[i] );
        IncomingMessage msg = IncomingMessage.parseMessage( cmds[i] );
        
        //System.out.println (">> getProtocols: " + msg.getProtocols());
	//System.out.println (">> getSecurityProtocol: " + msg.getSecurityProtocol());
	//System.out.println (">> getFriendlyName: " + msg.getFriendlyName());
	//System.out.println (">> getUserName: " + msg.getUserName());
	System.out.println( ">> getProperty: " + msg.getProperty() );
        System.out.println( ">> getValue: " + msg.getValue() );
        //System.out.println (">> getReferralType: " + msg.getReferralType());
	//System.out.println (">> serverIP: " + msg.getServerIP());
	//System.out.println (">> serverPort: " + msg.getServerPort());
	//System.out.println (">> getSerialNumber: " + msg.getSerialNumber());
	//System.out.println (">> getStatus: " + msg.getStatus());
	//System.out.println (">> getListType: " + msg.getListType());
	//System.out.println (">> getItemNumber: " + msg.getItemNumber());
	//System.out.println (">> getTotalItems: " + msg.getTotalItems());
	//System.out.println (">> getGTCArgument: " + msg.getGTCArgument());
	//System.out.println (">> getBLPArgument: " + msg.getBLPArgument());
	//System.out.println (">> getChallengeHash: " + msg.getChallengeHash());
	//System.out.println (">> getExitStatus(): " + msg.getExitStatus());
      } catch( Exception e ) {
        System.out.println( "Error: " + e );
      }
    }
  }
}

// IncomingMessage class