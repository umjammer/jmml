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
 * $Id: DispatchServerConnector.java,v 1.2 2003/11/16 16:44:15 popolony2k Exp $
 * $Author: popolony2k $
 * $Name:  $
 * $Revision: 1.2 $
 * $State: Exp $
 *
 */

package org.hn.sleek.jmml;

import java.util.*;

/**
 * Makes the first connection the Dispatch server.  Connecting to this server
 * negotiates for a Notification Server, and is the first step in
 * authentication.  Communication is asynchronous, and so the object provides
 * callbacks for the ServerConnection object, which negotiates the actual
 * communications.
 */
public class DispatchServerConnector implements MSNPListener {

  final static String MESSENGER_DS_SERVER = "messenger.hotmail.com";
  final static int    MESSENGER_DS_PORT   = 1863;

  ServerConnection    sc;
  int                 transactionID       =  0;
  int                 port                = -1;
  String              serverIP;
  String              userName;
	

  /**
   * Negotiates against a Dispatch server for a Notification Server.
   *
   * @param userName User's Passport ID
   * @return An array list whose elements are [(String) NotificationServerIP, (int) NotificationServerPort]
   */
  public ArrayList getNSServer( String userName ) {
    
    OutgoingMessage msg;

    this.userName = userName;

    /* Creates a new server connection */
    sc = new ServerConnection( MESSENGER_DS_SERVER, MESSENGER_DS_PORT, this );

    /**
     * We need check if DS connection was OK to continue
     * negotiating with NS server.
     * If connection with DS could be stablished a null
     * object is returned indicating fail to connect
     * with DS Server.
     */
    if( !sc.isConnected() )
      return null;
                
    // >>> VER MSNP8 support only
    msg = new OutgoingMessage( Message.VER, getTransactionID() );
    msg.addArgument( "MSNP8" );
    msg.addArgument( "CVR0" );
    sc.sendMSNPMessage( msg );
    
    /* Wait until all asynchronous communication is complete */
    try {
      synchronized( this ) {
        wait();
      }
    } catch( InterruptedException e ) { }
      /* Create the return value [ServerIP, port] */
      if( port != -1 )  {
	ArrayList serverInfo = new ArrayList();
                
	serverInfo.add( serverIP );
        serverInfo.add( new Integer( port ) );
        
        return serverInfo;
      }
                
      return null;
  }

  /**
   * Returns the current DispatchServer 
   * transaction ID.
   */
  private int getTransactionID() {
    transactionID++;
    return transactionID;
  }
	
  /**
   * When an MSNP packet arrives from the socket, we are notified, and
   * must handle it accordingly.
   *
   * @param incomingMessage The MSNP packet from the wire.
   */
  public void incomingMSNPMessage( IncomingMessage incomingMessage ) {
    
    switch( incomingMessage.getType() ) {
      case Message.VER:  receivedVER( incomingMessage );  break;
      // Not needed in MSNP8
      //case Message.INF:  receivedINF (incomingMessage);  break;
      case Message.XFR:  receivedXFR( incomingMessage );  break;
      case Message.CVR:  receivedCVR( incomingMessage );  break;
    }
  }

  /**
   * When the server disconnects, it lets us know so we can take care
   * of any housecleaning that's necessary.
   */
  public void serverDisconnected() {
          
    System.out.println( "DispatchServerConnector.serverDisconnected() - Dispatch server has disconnected..." );
          
    MessengerServerManager.getInstance().fireLoginError();
    MessengerServerManager.getInstance().fireServerDisconnected();
          
    synchronized( this )  {
      notifyAll();
    }
  }

  /**
   * Called when a VER message is 
   * received for this object.  This implementation does no checking 
   * to ensure proper form; instead, the message is used as a
   * notification to send the INF command.
   *
   * @param incomingMessage The message that was received.
   */
  private void receivedVER( IncomingMessage incomingMessage ) {
  
    // New MSNP8 support
    OutgoingMessage outgoingMsg = new OutgoingMessage( Message.CVR, getTransactionID() ); 
    String          strArgument = Message.CVR_STRING + userName;
                
    outgoingMsg.addArgument( strArgument );
    sc.sendMSNPMessage( outgoingMsg );
  }

  // This package is not needed in the MSNP8 and now was changed by CVR response
  /**
   * Called when an INF message is
   * received.  The implementation does not check the form of the
   * message; instead, the message is used as a notification to send
   * the USR command.
   *
   * @param incomingMessage The message that was received.
   */
  //private void receivedINF (IncomingMessage incomingMessage) {
  /* >>> USR [number] MD5 I [userName] */
  //  OutgoingMessage msg = new OutgoingMessage (Message.USR, getTransactionID());
  //  msg.addArgument ("MD5"); msg.addArgument ("I"); msg.addArgument (userName);
  //  sc.sendMSNPMessage (msg);
  //}

  /**
   * Called when an XFR message is
   * received.  Implementation notifies the object that all the
   * necessary information has been received from the Dispatch
   * Server, and signals so.
   *
   * @param incomingMessage The message that was received.
   */
  private void receivedXFR( IncomingMessage incomingMessage ) {
    
    try {
      serverIP = incomingMessage.getServerIP();
      port     = incomingMessage.getServerPort();
    } catch( Exception e ) {
      serverIP = "64.4.12.93";
      port     = 1863;
    }
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
                
    outgoingMsg.addArgument( Message.commandToString( Message.TWN ) );
    outgoingMsg.addArgument( "I" );
    outgoingMsg.addArgument( userName );
    sc.sendMSNPMessage( outgoingMsg );
  }

  /**
   * Tests connecting to the Dispatch Server, and dumping out the
   * information that was collected.
   */
  public static void main( String[] args ) {

    DispatchServerConnector ds = new DispatchServerConnector();
    
    System.out.println( ds.getNSServer( args[0] ) );

  }
}
// DispatchServerConnector Class