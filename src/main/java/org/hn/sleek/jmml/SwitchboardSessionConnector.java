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
 * $Id: SwitchboardSessionConnector.java,v 1.1.1.1 2003/10/27 01:15:13 popolony2k Exp $
 * $Author: popolony2k $
 * $Name:  $
 * $Revision: 1.1.1.1 $
 * $State: Exp $
 *
 */

package org.hn.sleek.jmml;


import java.io.*;
import java.util.*;

/**
 * Maintains the connection between the client and the switchboard
 * session server.  The switchboard session server acts as a gateway
 * for communications between two buddies on the MSN Messenger service.
 * One SwitchboardSessionConnector object is created for each buddy
 * on the user's buddy list.  When a message is to be sent to a buddy,
 * the message is placed in a queue, and a switchboard session server
 * connection is requested.  Once the server honours the request for
 * a switchboard session, the client connects to the switchboard server,
 * and sends the messages once the buddy also connects to the server
 * (and to the session).  Note that the implementation does not currently
 * support multi-way conversations.
 */
class SwitchboardSessionConnector implements MSNPListener {

  /** 
   * The MIME header is hardcoded as static, and does not specify either
   * a font or any modifications to the font.  (An extra header is required
   * to do this.)
   */
  final static String MIME_HEADER = "MIME-Version: 1.0\r\nContent-Type: text/plain; charset=UTF-8\r\n\r\n";

  /** The Passport name of the user who is signed in. */
  String              userName;

  /** The Passport name of the buddy. */
  String              buddyName;

  /** The challenge has for this session. This is valid only when the session is live. */
  String              challengeHash;

  /** The sessionID for this session. This is valid only when the session is live. */
  String              sessionID;

  /** IP address of the server. */
  String              serverIP;

  /** Port to be connected at on the server. */
  Integer             serverPort;

  /** The actual server connection */
  ServerConnection    sc;

  /** The number of participants in the session */
  int                 participants = 0;

  
  /** 
   * A queue of the messages that are queued to be sent to the buddy once
   * a switchboard session connection is established.
   */
  LinkedList          queuedMessages;

  /** Whether the user is making the call or answering the call */
  boolean             calling = false;

  /** Whether the buddy has joined the session. */
  boolean             joined  = false;

  int                 transactionID = 0;

  /** Reference to the notification server that was used to make this connection. */
  NotificationServerConnector notificationServer;

  
  /** 
   * Static queue of all the switchboard session objects that have requested a
   * switchboard session and are waiting.
   */
  protected static LinkedList queuedSessions = new LinkedList(); 

  
  /**
   * Constructor for a switchboard sesion.
   *
   * @param userName Passport name of the user.
   * @param buddyName Passport name of the buddy with whom this session is connected.
   * @param notificationServer Reference to the calling notification server.
   */
  public SwitchboardSessionConnector( String userName, String buddyName, NotificationServerConnector notificationServer ) {
    this.userName           = userName;
    this.buddyName          = buddyName;
    this.queuedMessages     = new LinkedList();
    this.notificationServer = notificationServer;
  }

  /**
   * Sets up the server information for this particular session.  This
   * information is culled from an XFR response from the server (the
   * response is sent to the notification server).
   *
   * @param serverIP The IP server.
   * @param sessionID The session ID for the requested session.
   * @param challengeHash The challengeHash ("password") for the session.
   */
  public void setSessionInfo( String serverIP, int serverPort, String sessionID, String challengeHash ) {
    
    this.serverIP      = serverIP;
    this.serverPort    = new Integer (serverPort);
    this.sessionID     = sessionID;
    this.challengeHash = challengeHash;
    this.joined        = false;
  }

  /**
   * Use this to set whether the client is making the call or answering
   * a call.
   *
   * @param calling Whether the client is making the call.
   */
  public void setCalling( boolean calling ) {
    
    this.calling = calling;
  }

  /**
   * Makes the actual socket connection between the client and server,
   * and then initiates the authentication/registration process.  If
   * calling, the client sends a USR command; if answering, the client
   * sends an ANS command (along with the sessionID and hash) to be added
   * to the conversation.
   */
  public void connectSession() {
    
    /* Create the connection to the server */
    sc = new ServerConnection( serverIP, serverPort.intValue(), this );

    if( calling ) {
      /* >>> USR 23 username@msn.com 293898198.29823982 */
      OutgoingMessage msg = new OutgoingMessage( Message.USR, getTransactionID() );
      msg.addArgument( userName ); 
      msg.addArgument( challengeHash );
      sc.sendMSNPMessage( msg );
    }
    else { /* Answering a call */
      /* >>> ANS 45 username@msn.com 9a98c78def8772 9083298 */
      OutgoingMessage msg = new OutgoingMessage( Message.ANS, getTransactionID() );
      
      msg.addArgument( userName );
      msg.addArgument( challengeHash );
      msg.addArgument( sessionID );
      sc.sendMSNPMessage( msg );
    }
  }

  /** 
   * If the server connection is made, and the buddy has joined the conversation,
   * then the message is immediately sent to the buddy.  If not, then the message
   * is placed in the queue, and another switchboard session is requested.  Messages
   * longer than about 1500 characters will fail (and the client will not be
   * notified).  Further, messages are sent so that there is no acknowledgement of
   * success or failure.
   *
   * @param message The message to be sent to the buddy.
   */
  public void sendMessage( String message ) {
    /* joined is true only when the buddy has joined a conversation with the user */
    if( joined ) {
      OutgoingMessage msg  = new OutgoingMessage( Message.MSG, getTransactionID() );
      String          body = MIME_HEADER + ( String ) message;
      
      /* NOTE: hardcoded "don't ack received messages" with the "U" below */
      msg.addArgument( "U" );
      msg.addArgument( ( new Integer( body.length() ) ).toString() );
      msg.setBody( body );
      sc.sendMSNPMessage( msg );
    }
    else {
      /* Add the message to the queue */
      synchronized( queuedMessages ) {
        queuedMessages.add( message );
      }
      
      synchronized( queuedSessions ) {
        queuedSessions.add( this );
      }
      
      /* Request another switchboard session */
      notificationServer.requestSwitchboardSession();
    }
  }

  /**
   * Dispatches the incoming message to the appropriate function.
   * @param incomingMessage The message from the server.
   */
  public void incomingMSNPMessage( IncomingMessage incomingMessage ) {
    
    switch( incomingMessage.getType() ) {
      case Message.BYE: receivedBYE( incomingMessage ); break;
      case Message.USR:	receivedUSR( incomingMessage ); break;
      case Message.CAL: receivedCAL( incomingMessage ); break;
      case Message.JOI: receivedJOI( incomingMessage ); break;
      case Message.IRO: receivedIRO( incomingMessage ); break;
      case Message.ANS: receivedANS( incomingMessage ); break;
      case Message.MSG: receivedMSG( incomingMessage ); break;
    }
  }

  /**
   * When it is clear that the session has ended, sets the "joined" flag
   * to be false.
   */
  public void serverDisconnected() {
    joined = false;
    sc     = null;
  }

  /**
   * Received when a buddy leaves a conversation.  Sets the "joined" flag
   * to be false.
   *
   * @param incomingMessage The incoming MSNP message from the server.
   */
  private void receivedBYE( IncomingMessage incomingMessage ) {
    
    /* Moving forward for multi-party conversation support, this should
       should be aware of who is leaving. */
    joined = false;
    participants--;
    
    if( participants == 0 ) {
      OutgoingMessage msg = new OutgoingMessage( Message.OUT, OutgoingMessage.NO_TRANSACTION_ID );
      
      sc.sendMSNPMessage( msg );
      sc = null;
    }
  }

  /**
   * Received as an authentication complete acknowledgement.  Used as a
   * place to send the "CAL" command, which "calls" a buddy into this
   * conversation.
   *
   * @param incomingMessage The incoming MSNP message from the server.
   */
  private void receivedUSR( IncomingMessage incomingMessage ) {
    
    /* >>> CAL 98 username2@msn.com */
    OutgoingMessage msg = new OutgoingMessage( Message.CAL, getTransactionID() );
    
    msg.addArgument( buddyName );
    sc.sendMSNPMessage( msg );
  }

  /**
   * When a CAL command is received, pulls out the session ID and places
   * it into the object field.
   *
   * @param incomingMessage The incoming MSNP message from the server.
   */
  private void receivedCAL( IncomingMessage incomingMessage ) {
    
    try {
      sessionID = incomingMessage.getSessionID();
    } catch( NoSuchFieldException e ) { 
      /* Shouldn't ever get here */
    }
  }

  /**
   * When the buddy joins the conversation, set the "joined" flag to be
   * true, and send the queue of messages to the buddy.
   *
   * @param incomingMessage The incoming MSNP message from the server.
   */
  private void receivedJOI( IncomingMessage incomingMessage ) {
    
    /* Currently only used to send queue of messages
       If someone enters OTHER than the buddyName, then this should fire an
       event up to the previous level...
     
         1. buddyName = "Multiway Conversation"
	 2. switchboardSessions.remove (buddyName);
	 3. switchboardSEssion.add (sessionID, ssc);

	 Will also need a way to say "don't give me any more messages from
	 these punks." 
		
	 There will also be some added complexity in the BYE command since
	 conversations where everyone but two people leave become the
	 conversation for that one user. 
     */

    joined = true;
    sendQueuedMessages();
    participants++;
  }

  /**
   * When answering a call, the switchboard session will already have people
   * in the conversation.  IRO messages tell the client who is already in
   * the conversation (switchboard session).
   * <p>
   * Since this library does not currently support multi-way conversations,
   * IRO messages don't trigger anything.
   *
   * @param incomingMessage The incoming MSNP message from the server.
   */
  private void receivedIRO( IncomingMessage incomingMessage ) {
    
    /* Should probably do something similar as with the JOI */
    participants++;
  }

  /**
   * When answering a call, the switchboard server acknowledges when you have
   * joined the conversation.  The ANS message is thus just an ACK.
   *
   * @param incomingMessage The incoming MSNP message from the server.
   */
  private void receivedANS( IncomingMessage incomingMessage ) {
    
    /* Might be good to sendQueuedMessages */
    joined = true;
  }

  /**
   * Handles incoming messages from the switchboard session server (instant 
   * messages and "user is typing" messages).  Instant messages are fired back
   * to the notification server; "user is typing" messages are currently dropped
   * on the floor.  When errrors occur, the message is dropped on the floor.
   * Font information from the instant messages is discarded.
   *
   * @param incomingMessage The incoming MSNP message from the server.
   */
  private void receivedMSG( IncomingMessage incomingMessage ) {
    
    try {
      BufferedReader reader = new BufferedReader( new StringReader( incomingMessage.getBody() ) );
      
      /* Reads in the first MIME header "MIME-Version: 1.0" and discards it */
      reader.readLine();
      
      /* Reads in the Content-Type of the MIME header */
      String type = reader.readLine();
      
      /* "Content-Type: text/plain" is an instant message */
      if( type.indexOf( "plain" ) != -1 ) {
        StringBuffer bodyBuffer = new StringBuffer();
        String parameters       = reader.readLine();  /* Font information; this is discarded. */
        String line             = reader.readLine(); /* Begins reads the body of the message into the buffer */
        
        
        while( line != null ) {
          bodyBuffer.append( line );
          line = reader.readLine();
        }

	/* TODO: URLDecode on the friendly name? */
        notificationServer.fireIncomingMessageEvent( buddyName, incomingMessage.getFriendlyName(), bodyBuffer.toString() );
      }
    } catch( IOException e ) {
      System.out.println( e );
    } catch( NoSuchFieldException e2 ) { 
      /* Should never happen */	
    }
  }

  /**
   * Pulls out each message from the message queue, and individually
   * sending the messages to the buddy.  Uses the sendMessage method.
   */
  private void sendQueuedMessages() {
    
    /* TODO: Is this absolutely necessary?  There is a possibility
       of deadlock here.
       1. Buddy JOIns, so sendQueuedMessages is called.
       2. sendQueuedMessages locks queuedMessages
       3. sendMessage is called for first message
       4. after message is sent, buddy leaves the conversation
       5. joined becomes false
       6. on next loop, the next queued message is sent to sendMessage
       7. sendMessage tries to put it in the queue; except
 	  queuedMessages is synchronized....
    */
    
    synchronized( queuedMessages ) {
      int messageCount = queuedMessages.size();
      
      for( int i = 0; i < messageCount; i++ ) {
        sendMessage( ( String ) queuedMessages.removeFirst() );
      }
    }
  }

  /**
   * Returns the current SwitchboardSession 
   * transaction ID.
   */
  private int getTransactionID() {
    transactionID++;
    return transactionID;
  }
}
// SwitchboardSessionConnector class