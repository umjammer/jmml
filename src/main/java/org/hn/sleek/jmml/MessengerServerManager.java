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
 * $Id: MessengerServerManager.java,v 1.3 2003/11/16 16:44:15 popolony2k Exp $
 * $Author: popolony2k $
 * $Name:  $
 * $Revision: 1.3 $
 * $State: Exp $
 *
 */

package org.hn.sleek.jmml;

import java.io.*;
import java.util.*;
import java.security.*;
import com.sun.net.ssl.*;


/**
 * MessengerServerManager is the entry point to MSN network.
 * MessengerServerManager class provides all methods to login/logout
 * and send messages and manage user status and other features of MSN
 * protocol.
 */
public class MessengerServerManager {

  static MessengerServerManager  instance;
  
  Vector                         clientListeners;
  NotificationServerConnector    notificationServer;

        
  /**
   * Initializes all class data.
   * Install a SSL provider if not installed.
   * For JDK1.3 SSL support. For JDK1.4 and higher this provider
   * is already present in JVM initialization.
   * The SSL package support MUST be installed in JDK 1.3 or lesser.
   */
  private MessengerServerManager() {
  
    clientListeners = new Vector();
          
    if( Security.getProvider( "SunJSSE" ) == null )  {
      Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() );
      java.lang.System.setProperty( "java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol" );
      //java.lang.System.setProperty( "javax.net.ssl.trustStore", "/usr/local/java/jdk1.3.1_06/jre/lib/security" );
    }

    // Create a trust manager that does not validate certificate chains
    // Thanks to www.javaalmanac.com
    TrustManager[] trustAllCerts = new TrustManager[] {
      
      new X509TrustManager() {
          
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          
          return null;
        }
            
        public boolean isClientTrusted( java.security.cert.X509Certificate[] chain ) {
          
          return true;
        }
            
        public boolean isServerTrusted( java.security.cert.X509Certificate[] chain ) {
          
          return true;
        }
      }
    };
    
    // Install the all-trusting trust manager
    try {
      SSLContext sc = SSLContext.getInstance( "SSL" );
      sc.init( null, trustAllCerts, new java.security.SecureRandom() );
      HttpsURLConnection.setDefaultSSLSocketFactory( sc.getSocketFactory() );
    } catch( Exception e ) {
      System.err.println( "MessengerServerManger.MessengerServerManger() - " + e );
    }
  }

  /**
   * Returns one active MessengerServerManager instance.
   * If non is created, getInstance creates a new MessengerServerManager 
   * instance.
   */
  public static MessengerServerManager getInstance() {
        
    if( instance == null ) {
      instance = new MessengerServerManager();
    }
    
    return instance;
  }

  /**
   * Signs the user into the service.  This calls the other signIn
   * and blocks the thread of execution until the sign in is complete.
   * The function returns true to connection succesfull or false if
   * could not connect into MSN server.
   *
   * @param userName - Users's sign in name
   * @param password - User's password
   * @param strStatus - The initial user status
   */
   public void signIn( String userName, String password, String strStatus ) throws MSNException {
          
     signIn( userName, password, strStatus, true );
                
     if( !isConnected() )  {
       throw new MSNException( MSNException.MSN_EX_LOGIN_FAIL );
     }
   }

  /**
   * Signs the user into the service.  Allows the client to specify whether
   * the signIn occurs asynchronously (non-blocking), or whether the function
   * blocks until sign in is complete.
   *
   * @param userName - User's sign in name
   * @param password - User's password
   * @param strStatus - The initial user status
   * @param blocking - Whether the function should block the thread until the
   * sign in is complete.
   */
  public void signIn( String userName, String password, String strStatus, boolean blocking ) {

    DispatchServerConnector ds         = new DispatchServerConnector();
    ArrayList               serverInfo = ds.getNSServer( userName );
    
    
    /**
     * We need check if NS is available.
     * If a list of NS Servers is null (for any reason)
     * DS server could be connected.
     */
    if( serverInfo == null )  {
      fireLoginError();
      return;
    }
                
    notificationServer = new NotificationServerConnector( ( String ) serverInfo.get( 0 ), ( Integer ) serverInfo.get( 1 ) );
    notificationServer.signIn( userName, password, strStatus, blocking );
  }

  /**
   * signout from MSN network.
   */
  public void signOut() {
    
    notificationServer.signOut();
  }

  /**
   * Return the user connection status.
   * true - connected;
   * false - disconnected;
   */
  public boolean isConnected()  {
          
    if( notificationServer != null )
      return notificationServer.isConnected();
    else
      return false;
  }
        
  /**
   * Return the loggedIn user name.
   */
  public String getUserName()  {

    return notificationServer.getUserName();
  }
        
  /**
   * Attempts to set the status of the user at the notification server.
   * Response is asynchronous, and may come in the form of an REA ack or an
   * error code (209 - contains restricted word)
   *
   * @param newStatus - The new user status (See @link ContactStatus);
   */
  public void setStatus( String strNewStatus )  {
          
    notificationServer.setStatus( strNewStatus );
  }
        
  /**
   * Returns the user status.
   */
  public String getStatus()  {
         
    return notificationServer.getStatus();
  }
  
  /**
   * Add user to the contact list.
   * @contact The user to add @see Contact
   */
  public void addToContactList( Contact contact )  {
    
    notificationServer.addToContactList( contact );
  }
  
  /**
   * Remove a user frin contact list.
   * @contact The user to remove @see Contact
   */
  public synchronized void removeFromContactList( Contact contact )  {
   
    notificationServer.removeFromContactList( contact );
  }
  
  /**
   * Get user logged in the contactlist
   */
  public ContactList getContactList()  {
         
    return notificationServer.getContactList();
  }
        
  /**
   * Synchronize the user properties used by a messenger
   * application.
   * The properties synchronized are, Forward List, 
   * Reverse List, Block List, Allow List, GTC setting,
   * BPL setting.
   */
  public void synchronizeContactList()  {
        
    notificationServer.synchronizeContactList();
  }
  
  /**
   * Send a email to user with a subject.
   * @param strUserEmail The user email that message 
   * will be sent;
   */
  void sendEmailInvitation( String strUserEmail )  {
    
    notificationServer.sendEmailInvitation( strUserEmail );
  }
  
  /**
   * Get the user list from server.
   * @param nListType The list type @see ContactList
   */
  public void requestList( int nListType )  {
   
    notificationServer.requestList( nListType );
  }
        
  /**
   * Return all groups for connected user
   */
  public Hashtable getGroups()  {

    return notificationServer.getGroups();
  }

  /*
  public void addContact( String userName ) {
    
  }
  
  public void removeContact( String userName ) {
    
  }
  
  public void blockContact( String userName ) {
    
  }
  */

  /**
   * Send a message to a buddy;
   * 
   * @param buddyName The buddy to whom the message is being sent.
   * @param message The message that is being sent to the buddy.
   */
  public void sendMessage( String userName, String message ) {
    
    notificationServer.sendMessage( userName, message );
  }

  /**
   * Attempts to set the friendly name of the user at the notification server.
   * Response is asynchronous, and may come in the form of an REA ack or an
   * error code (209 - contains restricted word)
   *
   * @param newFriendlyName UTF-8 encoded friendly name.
   */
  public void setFriendlyName( String friendlyName ) {
    
    notificationServer.setFriendlyName( friendlyName );
  }

  /* Fast-track method for sending down MSNP packets. May be removed later. */
  public void sendMessage( OutgoingMessage msg ) {
    
    notificationServer.sendMessage( msg );
  }

  /**
   * Add a MSN event listener to MessengerServerMaager 
   * created instance.
   * @param newListener The listener to add;
   */
  public synchronized void addMessengerClientListener( MessengerClientListener newListener ) {
    
    clientListeners.add( newListener );
  }

  /**
   * Remove a MSN event listener from MessengerServerMaager 
   * created instance.
   * @param newListener The listener to remove;
   */
  public synchronized void removeMessengerClientListener( MessengerClientListener listener ) {
    
    clientListeners.remove( listener );
  }

  /**
   * Event fire things -- if any of these fail, the listener is removed from the listener 
   * list -- will also dump a debug message
   */
  protected void fireIncomingMessageEvent( String userName, String friendlyName, String message ) {
  
    Vector         cloneListener;
    
    synchronized( this )  {
      cloneListener = ( Vector ) clientListeners.clone();
    }
    
    IncomingMessageEvent   event     = new IncomingMessageEvent( userName, friendlyName, message );
    ListIterator           listeners = cloneListener.listIterator();
    
    while( listeners.hasNext() ) {
      try {
        MessengerClientListener listener = ( MessengerClientListener ) listeners.next();
        listener.incomingMessage( event );
      } catch( Exception e ) {
        System.err.println( "MessengerServerManager.fireIncomingMessageEvent() - " + e );
      }
    }
  }

  protected void fireContactChangeEvent( String userName, int property, Object newValue, String sourceCommand ) {
    
    Vector         cloneListener;
    
    synchronized( this )  {
      cloneListener = ( Vector ) clientListeners.clone();
    }
    
    ContactChangeEvent  event     = new ContactChangeEvent( userName, property, newValue, sourceCommand );
    ListIterator        listeners = cloneListener.listIterator();
    
    while( listeners.hasNext() ) {
      try {
        MessengerClientListener listener = ( MessengerClientListener ) listeners.next();
        listener.contactPropertyChanged( event );
      } catch( Exception e ) {
        System.err.println( "MessengerServerManager.fireContactChangeEvent() - " + e );
      }
    }
  }
  
  protected void fireReverseListChangedEvent( String userName ) {
    
  }
        
  /**
   * Fire the serverDisconnected event listeners.
   */
  protected void fireServerDisconnected() {

    Vector         cloneListener;
    
    synchronized( this )  {
      cloneListener = ( Vector ) clientListeners.clone();
    }
    
    ListIterator listeners = cloneListener.listIterator();
                
    while( listeners.hasNext() ) {
      try {
        MessengerClientListener listener = ( MessengerClientListener ) listeners.next();
        listener.serverDisconnected();
      } catch( Exception e ) {
        System.err.println( "MessengerServerManager.fireServerDisconnected() - " + e );
      }
    }
  }
        
  /**
   * Fire the loginError event listeners.
   */
  protected void fireLoginError() {

    Vector         cloneListener;
    
    synchronized( this )  {
      cloneListener = ( Vector ) clientListeners.clone();
    }
    
    ListIterator listeners = cloneListener.listIterator();
                
    while( listeners.hasNext() ) {
      try {
        MessengerClientListener listener = ( MessengerClientListener ) listeners.next();
        listener.loginError();
      } catch( Exception e ) {
        System.err.println( "MessengerServerManager.fireLoginError() - " + e );
      }
    }
  }
        
  /**
   * Fire the loginAccepted event listeners.
   */
  protected void fireLoginAccepted() {

    Vector         cloneListener;
    
    synchronized( this )  {
      cloneListener = ( Vector ) clientListeners.clone();
    }
    
    ListIterator listeners = cloneListener.listIterator();
                
    while( listeners.hasNext() ) {
      try {
        MessengerClientListener listener = ( MessengerClientListener ) listeners.next();
        listener.loginAccepted();
      } catch( Exception e ) {
        System.err.println( "MessengerServerManager.fireLoginAccepted() - " + e );
      }
    }
  }
        
  /**
   * Fire the groupReceived event listeners.
   * @param strGroupName The group name received
   * from server
   */
  protected void fireGroupReceived( String strGroupName ) {

    Vector         cloneListener;
    
    synchronized( this )  {
      cloneListener = ( Vector ) clientListeners.clone();
    }
    
    ListIterator listeners = cloneListener.listIterator();
                
    while( listeners.hasNext() ) {
      try {
        MessengerClientListener listener = ( MessengerClientListener ) listeners.next();
        listener.groupReceived( strGroupName );
      } catch( Exception e ) {
        System.err.println( "MessengerServerManager.fireGroupReceived() - " + e );
      }
    }
  }
        
  /**
   * Fire the contactReceived event listeners.
   * @param contact The contact received from server.
   */
  protected void fireContactReceived( Contact contact ) {

    Vector         cloneListener;
    
    synchronized( this )  {
      cloneListener = ( Vector ) clientListeners.clone();
    }
    
    ListIterator listeners = cloneListener.listIterator();
                
    while( listeners.hasNext() ) {
      try {
        MessengerClientListener listener = ( MessengerClientListener ) listeners.next();
        listener.contactReceived( contact );
      } catch( Exception e ) {
        System.err.println( "MessengerServerManager.fireContactReceived() - " + e );
      }
    }
  }
  
  /**
   * Fire the contactAdded event listeners.
   * @param contact The contact added to contact list.
   */
  protected void fireContactAdded( Contact contact ) {

    Vector         cloneListener;
    
    synchronized( this )  {
      cloneListener = ( Vector ) clientListeners.clone();
    }
    
    ListIterator listeners = cloneListener.listIterator();
                
    while( listeners.hasNext() ) {
      try {
        MessengerClientListener listener = ( MessengerClientListener ) listeners.next();
        listener.contactAdded( contact );
      } catch( Exception e ) {
        System.err.println( "MessengerServerManager.fireContactAdded() - " + e );
      }
    }
  }
  
  /**
   * Fire the contactRemoved event listeners.
   * @param contact The contact removed from contact list.
   */
  protected void fireContactRemoved( Contact contact ) {

    Vector         cloneListener;
    
    synchronized( this )  {
      cloneListener = ( Vector ) clientListeners.clone();
    }
    
    ListIterator listeners = cloneListener.listIterator();
                
    while( listeners.hasNext() ) {
      try {
        MessengerClientListener listener = ( MessengerClientListener ) listeners.next();
        listener.contactRemoved( contact );
      } catch( Exception e ) {
        System.err.println( "MessengerServerManager.fireContactRemoved() - " + e );
      }
    }
  }

  
  /* Sets up a fun little bot
     args[0] = user-name
     args[1] = password
  */
  public static void main( final String[] args ) {

    final MessengerServerManager msn = MessengerServerManager.getInstance();
    final String[]   cleverResponses = { "InfoBot forever!", "Tony kicks ass!",
                                         "MSN Messenger is cool!", "What's up?",
                                         "Halo is cool, but Quake is better!",
                                         "I beat Darren in Halo",
                                         "My name is MobileJane." 
                                       };

    
    msn.addMessengerClientListener( new MessengerClientAdapter() {
      
      public void incomingMessage (IncomingMessageEvent e) {
        
        System.out.println( e.getMessage() );
        
        int    clever = e.getMessage().length() % 7;
        String cleverResponse = "Clever response...";

        try {
          BufferedReader br = new BufferedReader( new InputStreamReader( ( Runtime.getRuntime().exec( "/usr/games/fortune" ) ).getInputStream() ) );
          StringBuffer   sb = new StringBuffer();
          String         line;
          
          
          line = br.readLine();
          
          while( line != null ) {
            sb.append( line );
            line = br.readLine();
          }
          
          cleverResponse = sb.toString();
        } catch( Exception e1 ) {
          System.out.println( e1 );
        }
				
        msn.sendMessage( e.getUserName(), cleverResponse );
      }

      public void contactPropertyChanged( ContactChangeEvent event ) {
	System.out.println( "Contact Change: " + event );
      }
      
      public void serverDisconnected()  {
        System.out.println( "Server disconnected" );
      }
    } );
		
    try  {
      msn.signIn( args[0], args[1], ContactStatus.ONLINE );
    } catch( MSNException e )  {
      System.out.println( "main() - " + e ); 
    }
    
    if( msn.isConnected() )  {
      /** Test sending a message */
      Thread t = new Thread() {
                    
        public void run() {
        
          try {
            Thread.sleep( 3000 );
          } catch( InterruptedException e ) {
                                
          }

          msn.setFriendlyName( "TestBot+++" );
        }
      };
                  
      t.start();
    }
  }
}

// MessengerServerManager class