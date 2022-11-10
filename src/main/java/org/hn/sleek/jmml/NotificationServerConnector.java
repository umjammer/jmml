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
 * $Id: NotificationServerConnector.java,v 1.2 2003/11/16 16:44:15 popolony2k Exp $
 * $Author: popolony2k $
 * $Name:  $
 * $Revision: 1.2 $
 * $State: Exp $
 *
 */

package org.hn.sleek.jmml;


import java.security.*;
import java.net.*;
import java.util.*;

/**
 * Manages the connection against the Notification Server.  Handles authentication,
 * server-originated challenges, and requests for switchboard server sessions (both
 * originated by the client and originated externally.  Fires incomingMessage and 
 * contact property change events back to the MessengerServerManager object.  
 * Automatically joins switchboard session invites (as with the windows clients).
 */
public class NotificationServerConnector implements MSNPListener {

  /** Woooh... Symmetric key cryptography. Woooh... */
  final static String MESSENGER_CHALLENGE_STRING = "VT6PX?UQTM4WM%YR";
  final static String SDC_STRING                 = "0x0409 MSMSGS JMML X X";

  
  /** Notification Server IP address. */
  String           serverIP;
  
  /** Notifcation Server port */
  Integer          serverPort;

  /** Passport name of the person who is logged in. */
  String           userName;

  /** Password of the person who is logged in. */
  String           password;

  int              transactionID = 0;

  ServerConnection sc;

  boolean          bLoggedIn = false;
  String           strUserStatus;
  Hashtable        hGroups;
  ContactList      contactList;

  /** Maps between buddyNames (Passports) and switchboard sessions */
  HashMap          switchboardSessions;

  
  /**
   * Sets up the object, and gets it ready.  Does not connect to the service;
   * this is accomplished by calling the <pre>signIn()</pre> method.
   *
   * @param serverIP The IP of the Notification Server.
   * @param serverPort The port to connect to.
   * @param clientListsners The listener from MessengerServerManager class.
   */
  public NotificationServerConnector( String serverIP, Integer serverPort ) {
    
    this.serverIP            = serverIP;
    this.serverPort          = serverPort;
    this.switchboardSessions = new HashMap();
    this.contactList         = new ContactList();
    this.hGroups             = new Hashtable();
  }

  /**
   * Starts the sign in process against the MSN Messenger service with the given
   * username (Passport) and password pair.  The method
   * is actually a misnomer since it does not handle the entire sign in process;
   * instead, it only initates it (sign in is actually a five or six message exchange).
   * The call can block by choice.
   *
   * @param userName - User name to sign in with
   * @param password - The password for the user
   * @param strStatus - The initial user status
   * @param blocking - Whether to block or not
   */
  public synchronized void signIn( String userName, String password, String strStatus, boolean blocking ) {
     
    OutgoingMessage msg;

    this.strUserStatus = strStatus;
    this.userName      = userName;
    this.password      = password;

    /* Creates the server connection. */
    sc = new ServerConnection( serverIP, serverPort.intValue(), this );
    
    // >>> VER 0 MSNP8 only
    msg = new OutgoingMessage( Message.VER, getTransactionID() );
    msg.addArgument ( "MSNP8" );
    msg.addArgument ( "CVR0" );
    sc.sendMSNPMessage( msg );

    if( blocking == true ) {
      if( isConnected() )  {
        try {
          synchronized( this ) {
            wait();
          }
        } catch( InterruptedException e ) {
          
        }
      }
    }
  }

  /**
   * If a Switchboard Session has been created for the buddy, then the message is
   * passed to that Switchboard session and fired off.  If a session has not been
   * created for that buddy, then a new one is created, and the message is fired
   * off.  There is no requirement for this buddy to be on the user's contact list;
   * however by default, MSN Messenger clients reject messages from "non-buddies."
   * 
   * @param buddyName The buddy to whom the message is being sent.
   * @param message The message that is being sent to the buddy.
   */
  public void sendMessage( String buddyName, String message ) {
    
    Object o = switchboardSessions.get( buddyName );
    SwitchboardSessionConnector ssc;

    /* If the buddy hasn't been sent a message before, create a new Switchboard
       session for him/her. */
    if( o == null ) {
      ssc = new SwitchboardSessionConnector( userName, buddyName, this );
      switchboardSessions.put( buddyName, ssc );
    }
    else {
      ssc = ( SwitchboardSessionConnector ) o;
    }

    /* Send the message via the Switchboard Session. */
    ssc.sendMessage( message );
  }

  /**
   * Fastrack method for sending MSNP packets directly to the Notification Server.
   * Most uses of this library will use the <pre>sendMessage(String,String)</pre>
   * method, which sends a message to a buddy.  In the even that lower level packets
   * need to be sent, then this method may be used.  Note that the responses to such
   * packets will not be sent back to the calling object.
   *
   * @param msg The MSNP packet to send to the Notification Server.
   */
  public void sendMessage( OutgoingMessage msg ) {
    sc.sendMSNPMessage( msg );
  }

  /**
   * Signs the user out of the MSN Messenger service by sending an OUT packet to
   * the service.
   */
  public void signOut() {
    
    /* >>> OUT */
    OutgoingMessage msg = new OutgoingMessage( Message.OUT, OutgoingMessage.NO_TRANSACTION_ID );
          
    sc.sendMSNPMessage( msg );
    contactList.removeAll();
  }
        
  /**
   * Return the loggedIn user name.
   */
  String getUserName()  {

    return userName;
  }
        
  /**
   * Attempts to set the status of the user at the notification server.
   * Response is asynchronous, and may come in the form of an REA ack or an
   * error code (209 - contains restricted word)
   *
   * @param newStatus - The new user status (See @link ContactStatus);
   */
  void setStatus( String strNewStatus )  {
          
    if( isConnected() && ( ContactStatus.parseStatus( strNewStatus ).compareTo( ContactStatus.UNKNOWN ) != 0 ) )  {
      OutgoingMessage outgoingMsg = new OutgoingMessage( Message.CHG, getTransactionID() );
          
      strUserStatus = strNewStatus;
          
      /* >>> CHG 23 NLN */
      outgoingMsg.addArgument( strUserStatus );
      sc.sendMSNPMessage( outgoingMsg );
    }
  }
        
  /**
   * Returns the user status.
   */
  String getStatus()  {
        
    return strUserStatus;
  }
        
  /**
   * Add user to the contact list.
   * @contact The user to add @see Contact
   */
  public synchronized void addToContactList( Contact contact )  {

    if( isConnected() && ( contact.getListType().get( ContactList.parseListTypeToInteger( ContactList.RAW_RL ) ) == null ) )  {
      OutgoingMessage outgoingMsg = new OutgoingMessage( Message.ADD, getTransactionID() );
      Integer         iListType   = ( Integer ) contact.getListType().values().toArray()[0];
          
      // >>> ADD nTID LISTTYPE UserHandle CustomUserHandle
      outgoingMsg.addArgument( ContactList.parseListType( iListType.intValue() ) );
      outgoingMsg.addArgument( contact.getUserName() );
      outgoingMsg.addArgument( ( contact.getFriendlyName().compareTo( "" ) == 0 ? contact.getUserName() : contact.getFriendlyName() ) );

      sc.sendMSNPMessage( outgoingMsg );
    }
  }
        
  /**
   * Remove a user from contact list.
   * @contact The user to remove @see Contact
   */
  public synchronized void removeFromContactList( Contact contact )  {
          
    if( isConnected() && ( contact.getListType().get( ContactList.parseListTypeToInteger( ContactList.RAW_RL ) ) == null ) )  {
      OutgoingMessage outgoingMsg = new OutgoingMessage( Message.REM, getTransactionID() );
      Integer         iListType   = ( Integer ) contact.getListType().values().toArray()[0];
          
      // >>> REM nTID LISTTYPE UserHandle CustomUserHandle
      outgoingMsg.addArgument( ContactList.parseListType( iListType.intValue() ) );
      outgoingMsg.addArgument( contact.getUserName() );

      sc.sendMSNPMessage( outgoingMsg );
    }
  }
        
  /**
   * Get user logged in the contactlist
   */
  ContactList getContactList()  {
         
    return contactList;
  }
        
  /**
   * Synchronize the user properties used by a messenger
   * application.
   * The properties synchronized are, Forward List, 
   * Reverse List, Block List, Allow List, GTC setting,
   * BPL setting.
   */
  void synchronizeContactList()  {
        
    if( isConnected() )  {
      OutgoingMessage outgoingMsg = new OutgoingMessage( Message.SYN, getTransactionID() );
          
      // >>> SYN nTID 0
      outgoingMsg.addArgument( "0" );
      
      // Clear old contact list and group
      contactList.removeAll();
      hGroups.clear();
      
      sc.sendMSNPMessage( outgoingMsg );
    }
  }
  
  /**
   * Send a email to user with a subject.
   * @param strUserEmail The user email that message 
   * will be sent;
   */
  void sendEmailInvitation( String strUserEmail )  {
    
    if( isConnected() )  {
      OutgoingMessage outgoingMsg    = new OutgoingMessage( Message.SDC, getTransactionID() );
      String          utfEncodedUser = URLEncoder.encode( userName );
          
      outgoingMsg.addArgument( strUserEmail );
      outgoingMsg.addArgument( SDC_STRING );
      outgoingMsg.addArgument( utfEncodedUser );
      outgoingMsg.addArgument( "8" );  // ?????

      sc.sendMSNPMessage( outgoingMsg );
    }
  }
        
  /**
   * Get the user list from server.
   * @param nListType The list type @see ContactList<br>
   * Microsoft seems disabling this package after 18 Nov 2003. 
   * This method can be deprecated in future releases of JMML.
   * Call this method cause JMML's disconnect from MSN network.
   */
  void requestList( int nListType )  {
          
    if( isConnected() )  {
      OutgoingMessage outgoingMsg = new OutgoingMessage( Message.LST, getTransactionID() );
          
      // >>> LST nTID LISTTYPE
      outgoingMsg.addArgument( ContactList.parseListType( nListType ) );
      sc.sendMSNPMessage( outgoingMsg );
    }
  }
        
  /**
   * Return all groups for connected user
   */
  Hashtable getGroups()  {
          
    return hGroups;
  }
        
  /**
   * Return the user connection status.
   * true - connected;
   * false - disconnected;
   */
  boolean isConnected()  {
          
    return sc.isConnected();
  }
  
  /**
   * Attempts to set the friendly name of the user at the notification server.
   * Response is asynchronous, and may come in the form of an REA ack or an
   * error code (209 - contains restricted word)
   *
   * @param newFriendlyName UTF-8 encoded friendly name.
   */
  public void setFriendlyName( String newFriendlyName ) {

    /* >>> REA 325 user@msn.com I%20Rock */
    OutgoingMessage outgoingMsg    = new OutgoingMessage( Message.REA, getTransactionID() );
    String          utfEncodedName = URLEncoder.encode( newFriendlyName );

                
    outgoingMsg.addArgument( userName );
    outgoingMsg.addArgument( utfEncodedName );
    sc.sendMSNPMessage( outgoingMsg );
  }

  /**
   * Returns the current transaction ID to the
   * connected NS session.
   */
  private int getTransactionID() {
    
    transactionID++;
    return transactionID;
  }
	
  /**
   * Receives incoming MSNP messages from the server connection, and delegates
   * the responsibility of handling the message to the appropriate function.
   *
   * @param incomingMessage The MSNP message that is to be processed.
   */
  public void incomingMSNPMessage( IncomingMessage incomingMessage ) {
          
    switch( incomingMessage.getType() ) {
                  
      case Message.VER: receivedVER( incomingMessage );  break;
      case Message.CVR: receivedCVR( incomingMessage );  break;
      // Not needed in MSNP8
      //case Message.INF: receivedINF ( incomingMessage ); break;
      case Message.USR: receivedUSR( incomingMessage );  break;
      case Message.CHG: receivedCHG( incomingMessage );  break;
      case Message.CHL: receivedCHL( incomingMessage );  break;
      case Message.QRY: receivedQRY( incomingMessage );  break;
      case Message.XFR: receivedXFR( incomingMessage );  break;
      case Message.RNG: receivedRNG( incomingMessage );  break;
      case Message.REA: receivedREA( incomingMessage );  break;
      case Message.ILN: receivedILN( incomingMessage );  break;
      case Message.BPR: receivedBPR( incomingMessage );  break;
      case Message.NLN: receivedNLN( incomingMessage );  break;
      case Message.FLN: receivedFLN( incomingMessage );  break;
      case Message.LST: receivedLST( incomingMessage );  break;
      case Message.LSG: receivedLSG( incomingMessage );  break;
      case Message.ADD: receivedADD( incomingMessage );  break;
      case Message.REM: receivedREM( incomingMessage );  break;
      case Message.SDC: receivedSDC( incomingMessage );  break;
    }
  }

  /**
   * This connector does not do anything when the service is disconnected.
   */
  public void serverDisconnected() {
    
    System.out.println( "NotificationServerConnector.serverDisconnected() - Notification server disconnected..." );
    
    synchronized( this ) {
      notifyAll();
    }
                
    if( !bLoggedIn )
      MessengerServerManager.getInstance().fireLoginError();
    
      bLoggedIn = false;
                
      // Fire the client listener
      MessengerServerManager.getInstance().fireServerDisconnected();
  }
  
  /**
   * Responds to the VER response from the server.  Sends the INF request
   * without verifying the content of the VER response.
   *
   * @param incomingMessage The incoming MSNP message.
   */
  private void receivedVER( IncomingMessage incomingMessage ) {
    /* >>> CVR 2 */
    // New MSNP8 support
    OutgoingMessage outgoingMsg = new OutgoingMessage( Message.CVR, getTransactionID() ); 
    String          strArgument = Message.CVR_STRING + userName;
                
    outgoingMsg.addArgument( strArgument );
    sc.sendMSNPMessage( outgoingMsg );
  }
        
  /**
   * Called when an CVR message is
   * received.  The implementation does not check the form of the
   * message; instead, the message is used as a notification to send
   * the USR command.
   *
   * @param incomingMessage The message that was received.
   */
  private void receivedCVR( IncomingMessage incomingMessage ) {
          
    /* >>> USR [number] TWN I [userName] */
    OutgoingMessage outgoingMsg = new OutgoingMessage( Message.USR, getTransactionID() );
                
    outgoingMsg.addArgument( "TWN" );
    outgoingMsg.addArgument( "I" );
    outgoingMsg.addArgument( userName );
    sc.sendMSNPMessage( outgoingMsg );
  }

  // This package is not needed in the MSNP8 and now was changed by CVR response
  /**
   * Responds to the INF response from the server by sending and a USR
   * request.  This requests a hash value for the user given the hash
   * method specified by the server's response to INF.  (This is
   * hardcoded as MD5.)
   *
   * @param incomingMessage The incoming MSNP message.
   */
  //private void receivedINF (IncomingMessage incomingMessage) {
  /* >>> USR 23 MD5 I passport@msn.com */
  //	OutgoingMessage msg = new OutgoingMessage (Message.USR, getTransactionID());
  //	msg.addArgument ("MD5"); msg.addArgument ("I"); msg.addArgument (userName);
  //	sc.sendMSNPMessage (msg);
  //}

  /**
   * Responds to the USR messages from the server.  The first time, it sends back
   * the MD5 hash for authentication.  The second time the server sends a USR
   * message is to ACK the authentication; the client then sends a "set my status
   * as online" message.
   *
   * @param incomingMessage The incoming MSNP message.
   */
  private void receivedUSR( IncomingMessage incomingMessage ) {
    
    /* MD5 requires response; OK is an ACK */
    try {
      /* "SecurityProtocol" is something of a hack here. */
      String securityProtocol = incomingMessage.getSecurityProtocol();
                        
      if( securityProtocol.equals( Message.commandToString( Message.TWN ) ) )  {
                                
        SSLServerConnection sslConn     = new SSLServerConnection( SSLServerConnection.PASSPORT_LIST_SERVER_ADDRESS );
        String              strLoginSvr = sslConn.getPassportLoginServer();
        String              strTicket   = sslConn.requestAuthorizationTicket( strLoginSvr, userName, password, incomingMessage.getChallengeHash() );
                                  
        if( strTicket == null )  {
          sc.disconnect();
          return;
        }
        else  {
	  // >>> USR nn TICKET
          OutgoingMessage outgoingMsg = new OutgoingMessage( Message.USR, getTransactionID() );
                                    
          outgoingMsg.addArgument( Message.commandToString( Message.TWN ) );
          outgoingMsg.addArgument( "S" );
          outgoingMsg.addArgument( strTicket );
          sc.sendMSNPMessage( outgoingMsg );
        }
      }
      else 
        if( securityProtocol.equals( "OK" ) ) {
          
          synchronized( this ) {
            notifyAll();
          }
                            
          MessengerServerManager.getInstance().fireLoginAccepted();
          setStatus( strUserStatus );
          bLoggedIn = true;
        }
    } catch( Exception e ) {
      System.err.println( "NotificationServerConnector.receivedUSR() - " + e ); 
      sc.disconnect();
    }
  }

  /**
   * Server responds to status changes by sending a CHG back to the
   * client.
   *
   * @param incomingMessage The incoming MSNP message.
   */
  private synchronized void receivedCHG( IncomingMessage incomingMessage ) {
    
  }

  /**
   * On occassion, the server will send challenge messages (CHL) to the
   * client.  This is symmetric key crypto; the passphrase to return is
   * the MD5 hash on the challenge + the key.
   *
   * @param incomingMessage The incoming MSNP message.
   */
  private void receivedCHL( IncomingMessage incomingMessage ) {
    
    try {
      /* >>> QRY 42 PROD0038W!61ZTF9 32 098c9d8ef890ab8 */
      
      OutgoingMessage msg = new OutgoingMessage( Message.QRY, getTransactionID() );
     
      msg.addArgument( "PROD0038W!61ZTF9" );
      msg.addArgument( "32" ); 
      msg.addArgument( "\r\n" );
      msg.addArgument( MD5sum( incomingMessage.getChallengeHash() + MESSENGER_CHALLENGE_STRING ) );
      sc.sendMSNPMessage( msg );
    } catch( NoSuchFieldException e ) { /* Shouldn't ever get here */ 
    }
  }

  /**
   * The server sends a QRY message in response to a valid QRY message
   * from the client.  This QRY is merely an ACK from the server.
   *
   * @param incomingMessage The incoming MSNP message.
   */
  private void receivedQRY( IncomingMessage incomingMessage ) {
    
  }

  /**
   * The Notification Server sends XFR messages to the client in two
   * different cases: (1) a Switchboard Session is requested, and
   * (2) when the user is asked to change notification servers.
   * In the first case, the next waiting switchboard session request
   * (by the client) is honoured.  In the second case, nothing happens
   * (right now).
   *
   * @param incomingMessage The incoming MSNP message.
   */
  private void receivedXFR (IncomingMessage incomingMessage) {
    try {
      String referralType = incomingMessage.getReferralType();

      if( referralType.equals( "SB" ) ) {
        Object o;

        /* Get the next session request from the queue. */
        synchronized( SwitchboardSessionConnector.queuedSessions ) {
          o = SwitchboardSessionConnector.queuedSessions.removeLast();
        }

        /* If it's no longer there, then return. */
        if( o == null ) 
          return;
        
        SwitchboardSessionConnector ssc = ( SwitchboardSessionConnector ) o;
        String serverIP      = incomingMessage.getServerIP();
        int    serverPort    = incomingMessage.getServerPort();
        String challengeHash = incomingMessage.getChallengeHash();
        String sessionID     = "None";

        /* Otherwise, tell the switchboard session to connect. */
        ssc.setSessionInfo( serverIP, serverPort, sessionID, challengeHash );

	/* Ensure that it knows that we are the calling party. */
	ssc.setCalling( true );
        ssc.connectSession();
      }
      else { 
        /* TODO: it's a "This server is going down XFR", so 
           flush the sc, and close it down.  make a new SC.
         */
      }
    }
    catch( NoSuchFieldException e ) { 
      /* Shouldn't ever get here */ 
    }
  }

  /**
   * When someone contacts this client, the server sends an RNG message
   * that contains the switchboard session information.  To communicate
   * with the buddy, the client must connect to the switchboard server.
   * Upon receipt of the message, a new switchboard session object is
   * created for the buddy, and the switchboard session is initiated.
   *
   * @param incomingMessage The incoming MSNP message.
   */
  private void receivedRNG( IncomingMessage incomingMessage ) {
    try {
      String sessionID     = incomingMessage.getSessionID();
      String buddyName     = incomingMessage.getUserName();
      String challengeHash = incomingMessage.getChallengeHash();
      String serverIP      = incomingMessage.getServerIP();
      int serverPort       = incomingMessage.getServerPort();

      /* If the buddy has a switchboard session connector, then use that
         since it will contain a queue of messages. */
      SwitchboardSessionConnector ssc = ( SwitchboardSessionConnector ) switchboardSessions.get( buddyName );
     
      if( ssc == null ) {
        /* If one does not exist, then create one. */
        ssc = new SwitchboardSessionConnector( userName, buddyName, this );
        switchboardSessions.put( buddyName, ssc );
      }

      ssc.setSessionInfo( serverIP, serverPort, sessionID, challengeHash );

      /* Tell the switchboard session connector that it is answering a call */
      ssc.setCalling( false );

      /* Connect to the switchboard session server. */
      ssc.connectSession();
    } catch( NoSuchFieldException e ) { 
      /* Should never get here. */
    }
  }
  
  private void receivedREA( IncomingMessage incomingMessage ) {
    
    try {
      MessengerServerManager.getInstance().fireContactChangeEvent( incomingMessage.getUserName(), Contact.FRIENDLY_NAME, incomingMessage.getFriendlyName(), incomingMessage.toString() );
    } catch( NoSuchFieldException e ) { 
      /* Should never get here. */
    }
  }

  private void receivedILN( IncomingMessage incomingMessage ) {
    
    try {
      /* Friendly Name */
      MessengerServerManager.getInstance().fireContactChangeEvent( incomingMessage.getUserName(), Contact.FRIENDLY_NAME, incomingMessage.getFriendlyName(), incomingMessage.toString() );

      /* Status */
      MessengerServerManager.getInstance().fireContactChangeEvent( incomingMessage.getUserName(), Contact.STATUS, incomingMessage.getStatus(), incomingMessage.toString() );
    } catch( NoSuchFieldException e ) { 
      /* Should never get here. */
    }
  }

  private void receivedFLN( IncomingMessage incomingMessage ) {
    
    try {
      MessengerServerManager.getInstance().fireContactChangeEvent (incomingMessage.getUserName(), Contact.STATUS, incomingMessage.getStatus(), incomingMessage.toString());
    } catch( NoSuchFieldException e ) { 
      /* Should never get here. */
    }
  }

  private void receivedNLN( IncomingMessage incomingMessage ) {
    
    try {
      /* Friendly Name */
      MessengerServerManager.getInstance().fireContactChangeEvent( incomingMessage.getUserName(), Contact.FRIENDLY_NAME, incomingMessage.getFriendlyName(), incomingMessage.toString() );

      /* Status */
      MessengerServerManager.getInstance().fireContactChangeEvent (incomingMessage.getUserName(), Contact.STATUS, incomingMessage.getStatus(), incomingMessage.toString());
    } catch( NoSuchFieldException e ) { 
      /* Should never get here. */
    }
  }

  private void receivedBPR( IncomingMessage incomingMessage ) {
    
    String newValue;
    String property;
    
    
    try {
      newValue = incomingMessage.getValue();

      if( newValue == null ) 
        return;
			
      /* Property Name */
      property = incomingMessage.getProperty();

      if( property.equals( "PHH" ) ) {
        MessengerServerManager.getInstance().fireContactChangeEvent( incomingMessage.getUserName(), Contact.HOME_PHONE_NUMBER, newValue, incomingMessage.toString() );
      }
      else 
        if( property.equals( "PHW" ) ) {
	  MessengerServerManager.getInstance().fireContactChangeEvent( incomingMessage.getUserName(), Contact.WORK_PHONE_NUMBER, newValue, incomingMessage.toString() );
        }
        else 
          if( property.equals( "PHM" ) ) {
            MessengerServerManager.getInstance().fireContactChangeEvent( incomingMessage.getUserName(), Contact.MOBILE_PHONE_NUMBER, newValue, incomingMessage.toString() );
          }
          else 
            if( property.equals( "MOB" ) ) {
              MessengerServerManager.getInstance().fireContactChangeEvent( incomingMessage.getUserName(), Contact.MOBILE_ENABLED, newValue, incomingMessage.toString() );
            }
    } catch( NoSuchFieldException e ) { 
      /* Should never get here. */
    }
  }
  
  private void receivedLST( IncomingMessage incomingMessage ) {
          
    try  {
      Contact    contact = new Contact( incomingMessage.getUserName() );
            
      contact.setFriendlyName( incomingMessage.getFriendlyName() );
      contact.setGroups( incomingMessage.getGroups() );
      contact.setListType( incomingMessage.getListType() );
      contactList.addToContactList( contact );
      MessengerServerManager.getInstance().fireContactReceived( contact );
          
      System.out.println( "NotificationServerConnector.receivedLST() - " + contact.toString() );
    }  catch( NoSuchFieldException e )  {
      System.err.println( "NotificationServerConnector.receivedLST() - " + e );
    }
  }
        
  /**
   * When SYN event is sent, some configurations are retrieved
   * as LSG (List Groups)
   */
  private void receivedLSG( IncomingMessage incomingMessage ) {
          
    ArrayList         aGroups  = incomingMessage.getGroups();
    int               nGroupID = -1;
          
    if( aGroups != null )  {
      Integer      iGroupID = ( Integer ) aGroups.get( 0 );
      
      nGroupID = iGroupID.intValue();
      hGroups.put( iGroupID, incomingMessage.getGroupName() );
      MessengerServerManager.getInstance().fireGroupReceived( incomingMessage.getGroupName() );
    }
          
    System.out.println( "NotificationServerConnector.receivedLSG() - " + incomingMessage.getGroupName() + " " + nGroupID );
  }
        
  private void receivedADD( IncomingMessage incomingMessage ) {
          
    try  {
      Contact      contact = new Contact( incomingMessage.getUserName() );

      contact.setFriendlyName( incomingMessage.getFriendlyName() );
      contact.setListType( incomingMessage.getListType() );
      contactList.addToContactList( contact );
      MessengerServerManager.getInstance().fireContactAdded( contact );
          
      System.out.println( "NotificationServerConnector.receivedADD() - " + contact );
    } catch( java.lang.NoSuchFieldException e )  {
      System.err.println( "NotificationServerConnector.receivedADD() - " + e ); 
    }
  }
        
  private void receivedREM( IncomingMessage incomingMessage ) {

    try  {
      Contact      contact = contactList.getContact( incomingMessage.getUserName() );

      contactList.removeFromContactList( contact );
      MessengerServerManager.getInstance().fireContactRemoved( contact );

      System.out.println( "NotificationServerConnector.receivedREM() - " + contact );
    } catch( java.lang.NoSuchFieldException e )  {
      System.err.println( "NotificationServerConnector.receivedREM() - " + e ); 
    }
  }

  private void receivedPRP( IncomingMessage incomingMessage ) {
  }
  
  private void receivedMSG( IncomingMessage incomingMessage ) {
  }
  
  private void receivedBLP( IncomingMessage incomingMessage ) {
  }
  
  private void receivedGTC( IncomingMessage incomingMessage ) {
  }
  
  private void receivedADG( IncomingMessage incomingMessage ) {
  }
  
  private void receivedREG( IncomingMessage incomingMessage ) {
  }
  
  private void receivedRMG( IncomingMessage incomingMessage ) {
  }
  
  private void receivedSDC( IncomingMessage incomingMessage ) {
  }

  /**
   * Acknowledgement for a SYNc.  Not interesting.
   * @param incomingMessage The incoming MSNP message.
   */
  private void receivedSYN( IncomingMessage incomingMessage ) {
  }

  /**
   * Sends a request to the Notification Server for a Switchboard
   * Session.  This is sent when the client wants to initiate communication
   * with another buddy.
   */
  protected void requestSwitchboardSession() {
    
    /* >>> XFR 87 SB */
    OutgoingMessage msg = new OutgoingMessage( Message.XFR, getTransactionID() );
    
    msg.addArgument( "SB" );
    sc.sendMSNPMessage( msg );
  }

  /**
   * When a message is received by the switchboard session connector, the 
   * object notifies the notification server connector.  The notification 
   * server connector notifies all registered MessengerClientListeners
   * that a message was received.
   *
   * @param userName The Passport name of the sending buddy.
   * @param friendlyName The friendly name of the sending buddy.
   * @param message The body of the message.
   */
  protected synchronized void fireIncomingMessageEvent( String userName, String friendlyName, String message ) {
          
    MessengerServerManager.getInstance().fireIncomingMessageEvent( userName, friendlyName, message );
  }

  /**
   * Calculates the MD5 checksum of the string, and returns a
   * string of its hexadecimal representation.
   *
   * @param toHash The string that is to be checksummed.
   * @return String representation of the hexadecimal MD5 checksum of the string.
   */
  private static String MD5sum( String toHash ) {
    
    try {
      MessageDigest md = MessageDigest.getInstance( "MD5" );
      
      return byteArrayToHexString( md.digest( toHash.getBytes() ) );
    } catch( NoSuchAlgorithmException e ) {
      
    }
    
    return ""; /* You're doomed */
  }

  /**
   * Converts a byte array into a hexadecimal string.
   *
   * @param bytes The byte array to convert.
   * @return String representation of the hexadecimal representation of the byte array.
   */
  private static String byteArrayToHexString( byte[] bytes ) {
    
    String hexString = "";

    for( int i = 0; i < bytes.length; i++ ) {
      byte b    = bytes[i];
      hexString = hexString + byteToHexString( b & 0xf, ( b >> 4 ) & 0xf );
    }	
		
    return hexString;
  }

  /** 
   * Converts a single byte into a hexadecimal string.
   *
   * @param nib1 The first nibble of the byte.
   * @param nib2 The second nibble of the byte.
   * @return String representation of the hexadecimal representation of a byte.
   */
  private static String byteToHexString( int nib1, int nib2 ) {
   
    char   char1, char2;
    char[] chars = new char[2];
    
    char1    = nibbleToChar( nib1 );
    char2    = nibbleToChar( nib2 );
    chars[0] = char2;
    chars[1] = char1;

    return ( new String ( chars ) );
  }

  /**
   * Converts a nibble into a character.
   *
   * @param nibble The nibble.
   * @return A character representation of the hexadecimal nibble.
   */
  private static char nibbleToChar( int nibble ) {
    
    if( nibble < 10 ) {
      return ( Integer.toString( nibble ) ).charAt( 0 );
    }
    else {
      int nib = nibble - 10;
      
      return ( char )( ( ( char ) nib ) + 'a' );
    }
  }

  /**
   * Test case.  Connects against the dispatch server, and then
   * authenticates against the notification server.  Then sends
   * four messages.
   *
   * args[0] = username
   * args[1] = password
   * args[2] = another-user-to-send-message-to
   * args[3] = yet-another-user
   */
  public static void main( final String[] args ) {
    
    DispatchServerConnector ds = new DispatchServerConnector();
    ArrayList  serverInfo = ds.getNSServer( args[0] );
    
    final NotificationServerConnector nsc = new NotificationServerConnector( ( String ) serverInfo.get( 0 ), ( Integer ) serverInfo.get( 1 ) );

    nsc.signIn( args[0], args[1], ContactStatus.ONLINE, false );
    
    /** Test sending a message */
    Thread t = new Thread() {
      public void run() {
        try {
          Thread.sleep( 3000 );
        } catch ( InterruptedException e ) {}
        
        nsc.sendMessage( args[2], "what's up?" );
        nsc.sendMessage( args[3], "i rock house" );
      }
    };
    
    t.start();

    Thread t2 = new Thread() {
      public void run() {
        try {
          Thread.sleep( 6000 );
        } catch( InterruptedException e ) {}
        
        nsc.sendMessage( args[2], "the second message" );
        nsc.sendMessage( args[3], "you got nothin'" );
      }
    };
    
    t2.start();
  }
}

// NotificationServerConnector class