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
 * $Id: ServerConnection.java,v 1.2 2003/11/16 16:44:15 popolony2k Exp $
 * $Author: popolony2k $
 * $Name:  $
 * $Revision: 1.2 $
 * $State: Exp $
 *
 */

package org.hn.sleek.jmml;

import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.util.*;


/**
 * ServerConnection is the object that actually maintains the socket-level
 * connection between the client and the server.  It is unintelligent in
 * the sense that it acts merely as a network layer; sending and receiving
 * messages blindly as requested.  It calls the originating object through
 * the MSNPListener interface.
 */
public class ServerConnection {

  /** Actual socket between server and client. */
  Socket       connection;

  /** Reference to the caller; caller is implicitly implied to be the listener. */
  MSNPListener listener;

  /** Whether the connection is live. */
  boolean      connected = false;

  
  
  /** 
   * Creates the server connection, and starts the "listener" that listens
   * on the socket for activity by the server.  If during connection, there
   * is an error, the listener is notified through the MSNPListener interface.
   *
   * @param serverAddress The IP address of the server.
   * @param port The port on the server to connect to.
   * @param listener The caller that listens for MSNP packets.
   */
  public ServerConnection (String serverAddress, int port, MSNPListener listener) {
    this.listener = listener;
		
    try {
      this.connection = new Socket (serverAddress, port);
      this.connected  = true;
    } catch( IOException e ) {
      disconnect();
      return;
    }
    
    System.out.println( "Connecting... " + serverAddress + ":" + port );
    readLoop();
  }

  /**
   * Returns whether the server connection object is actually connected 
   * to the Messenger server.
   *
   * @return Whether the client is connected to the server.
   */
  public synchronized boolean isConnected() {
          
    return connected;
  }
        
  /**
   * Disconnect server object. This method ends the readLoop Thread.
   */
  synchronized void disconnect()  {
    
    try  {
      connected = false;
      listener.serverDisconnected();
                
      if( connection != null )
        connection.close();
            
    } catch( IOException e )  {
      System.err.println( "ServerConnection.disconnect() - " + e );
    }
  }

  /**
   * Sends the specified MSNP packet to the server.  If sending fails for some
   * reason, the client is not notified.
   *
   * @param msg The message that is to be sent.
   */
  public void sendMSNPMessage( OutgoingMessage msg ) {
    
    try {
      PrintWriter pw = new PrintWriter( new OutputStreamWriter( connection.getOutputStream(), "UTF-8" ) );

      pw.write( msg.getMessageString() );
      pw.flush();

      System.out.println( ">>> " + msg.toString().trim() );
    }  catch( Exception e ) {
      System.out.println( e );
    }
  }

  /**
   * Creates a network "reading" thread that listens for MSNP packets from
   * the server.  Each packet is encased inside an IncomingMessage object, 
   * and then and then a new thread is created to pass the command back to
   * the listening object. A new thread is spawned to handle the command so 
   * that communications are not blocked.
   * <p>
   * When the connection is dropped, the loop stops, and notifies the listener
   * via the MSNPListener interface.
   */
  private void readLoop() {
    
    Thread readThread = new Thread() {
                  
      public void run() {
        
        BufferedReader br;

        try {
          br = new BufferedReader( new InputStreamReader( connection.getInputStream(), "UTF-8" ) );
        } catch( IOException e ) {
          disconnect();
	  return;
        }

        String            inputLine;
        IncomingMessage   msg;

        while( connected ) {
          try {

            /* Read in the command */
            inputLine = br.readLine();
            System.out.println( "<<< " + inputLine );
            
            /* A connection that has been dropped returns a null */
            if( inputLine == null ) {
              disconnect();
              break;
            }

            /* Parse the message */
            msg = IncomingMessage.parseMessage( inputLine );
            
          }  catch( IOException e ) {
            /* If the message was malformed or something else goes wrong, we just
             ignore it and continue the loop. */
            System.out.println( e );
            continue;
          }  catch( ParseException e ) {
            System.out.println( e );
            continue;
          }

	  /* If the message has a body (i.e. if it is of type Message.MSG), then
	     interpret the line to determine how many bytes to read in from the
             stream. */
          if( msg.hasBody() ) {
            try {
              int     length = msg.getBodyLength();
              char[]  body   = new char[length];
              
              
              br.read( body, 0, length );
              msg.setBody( new String( body ) );
            } catch( NoSuchFieldException e ) {
              /* If we were duped somehow into thinking that it had a body, and
		 it really didn't, or something went wrong, then don't bother
		 doing anything. */
              System.err.println( "ServerConnection.readLoop() - " + e );
            } catch( IOException e ) {
              System.err.println( "ServerConnection.readLoop() - " + e );
            }
          }
          
          /* Start a new thread and pass the message back to the listener. */
          ( new HandleMessage( msg ) ).run();
        }

        // Set the status of this ServerConnection Object
        disconnect();
      }
    };

    /* Start the read thread. */
    readThread.start();
  }
  
  /**
   * Handles contacting the listener with the MSNP packet.  This class
   * is used so that there is an alternative, independent reference to the 
   * MSNP packet.  If it were not created, the msg would be changed in the 
   * next iteration of the read loop.
   */
  class HandleMessage extends Thread {

    /** The "independent" reference to the MNSP packet. */
    IncomingMessage msg;

        
    /**
     * Stores a reference to the incoming message packet.
     *
     * @param msg The MSNP packet to be handled.
     */
    public HandleMessage( IncomingMessage msg ) {
      
      this.msg = msg;
    }

    
    /** 
     * Simply calls the listener through the MSNPListener interface, and
     * passes the listener the new packet.  Once that thread has completed
     * its execution, then this thread dies.
     */
    public void run() {
      listener.incomingMSNPMessage( msg );
    }
  }
}
// ServerConnection class