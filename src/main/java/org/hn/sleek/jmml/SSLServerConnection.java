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
 * $Id: SSLServerConnection.java,v 1.2 2003/11/01 14:04:32 popolony2k Exp $
 * $Author: popolony2k $
 * $Name:  $
 * $Revision: 1.2 $
 * $State: Exp $
 *
 */

package org.hn.sleek.jmml;

import java.net.*;
import java.io.*;


/**
 * The SSLServerConnection maintain SSL negotiation with MSN Server
 * at login time.
 */

class SSLServerConnection {

  final String               DALOGIN                      = "DALogin=";
  final String               DASTATUS                     = "da-status=";
  final String               TICKET                       = "from-PP=";
  final String               SUCCESS                      = "success";
  final String               KEY_PASSPORT_URLS            = "PassportURLs";
  final String               KEY_LOCATION                 = "Location";
  final String               KEY_AUTHENTICATION_INFO      = "Authentication-Info";
  static final String        PASSPORT_LIST_SERVER_ADDRESS = "https://nexus.passport.com/rdr/pprdr.asp";

  HttpURLConnection          httpsConn   = null;
  
  
  /**
   * Creates a new instance of SSLServerConnection <br>
   * connecting with the specified server.
   *
   * @param strServerAddress The server address;
   * If connection fail the method throws IOException
   * exception.
   */
  SSLServerConnection( String strServerAddress ) throws IOException {
    
    try  {
      URL         url = new URL( strServerAddress );
      
      System.out.println( ">>> Negotiating challenge with " + PASSPORT_LIST_SERVER_ADDRESS );
      httpsConn = ( HttpURLConnection ) url.openConnection();
      
    } catch( UnknownHostException ue )  {
      throw ue;
    } catch( IOException e )  {
      throw e;
    }
  }
  
  /**
   * Returns the field value of returned 
   * server data.
   *
   * @param strKey The field name to retrieve
   * @oaram strField The complete field to search the key value
   */
  String getField( String strKey, String strField )  {
    
    try  {
      int       nIniPos        = strField.indexOf( strKey );
      int       nEndPos        = 0;
      
      
      if( nIniPos < 0 )
        return "";
      
      nIniPos+=strKey.length();
      nEndPos = strField.indexOf( ',', nIniPos );
      
      if( nEndPos < 0 )
        return "";
    
      return strField.substring( nIniPos, nEndPos );
    } catch( Exception e )  {
      return "";
    }
  }
  
  /**
   * Return the Passport Login server
   */
  String getPassportLoginServer()  {
    
    if( httpsConn != null )  {
      String    strPassportServer;
      String    strPassportURL = httpsConn.getHeaderField( KEY_PASSPORT_URLS );

      strPassportServer = "https://" + getField( DALOGIN, strPassportURL );
      System.out.println( "<<< Retrieving passport login server " + strPassportServer );
      httpsConn = null;

      return strPassportServer;
    }
    else
      return null;
  }
  
  /**
   * Request authorization ticket to login in passport
   * server.
   *
   * @param strLoginURL The login URL
   * @param strUserName The login user name
   * @param strPassword The user password
   * @strChallenge The challenge string sent by passport server
   */
  String requestAuthorizationTicket( String strLoginURL, String strUserName, String strPassword, String strChallenge )  {
    
    if( ( strLoginURL  != null ) && 
        ( strUserName  != null ) && 
        ( strPassword  != null ) &&
        ( strChallenge != null ) )  {
          
      String   strAuthString = "Passport1.4 OrgVerb=GET,OrgURL=http%3A%2F%2Fmessenger%2Emsn%2Ecom,sign-in=" + strUserName + ",pwd=" + strPassword + "," + strChallenge;

      try  {
        URL         url = new URL( strLoginURL );
        String      strLocation;
        String      strAuthInfo;
        String      strAuthTicket;

      
        System.out.println( ">>> Starting login" );
      
        httpsConn = ( HttpURLConnection ) url.openConnection();
        httpsConn.setDoOutput( true );      
        httpsConn.setRequestProperty( "Authorization", strAuthString );
        httpsConn.setUseCaches( false );
        strLocation = httpsConn.getHeaderField( KEY_LOCATION );
      
        // Server is redirecting login ???
        if( strLocation != null )  {
          System.out.println( "<<< Server request redirecting login " );
      
          url = new URL( strLocation );

          System.out.println( ">>> Redirecting login to " + strLocation );

          httpsConn = null;
          httpsConn = ( HttpURLConnection ) url.openConnection();
          httpsConn.setDoOutput( true );      
          httpsConn.setRequestProperty( "Authorization", strAuthString );
          httpsConn.setUseCaches( false );
        }
      
        strAuthInfo = httpsConn.getHeaderField( KEY_AUTHENTICATION_INFO );
        httpsConn   = null;
      
        if( getField( DASTATUS, strAuthInfo ).compareTo( SUCCESS ) != 0 )
          return null;
      
        strAuthTicket = getField( TICKET, strAuthInfo );
      
        if( strAuthTicket != null )
          strAuthTicket = strAuthTicket.substring( 1, strAuthTicket.length() - 1 );
      
        return strAuthTicket;
      
      } catch( UnknownHostException ue )  {
        return null;
      } catch( IOException e )  {
        return null;
      }
    }
    
    return null;
  }
}

// SSLServerConnection class