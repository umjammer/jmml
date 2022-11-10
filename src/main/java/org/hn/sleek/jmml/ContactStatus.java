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
 * $Id: ContactStatus.java,v 1.1.1.1 2003/10/27 01:14:13 popolony2k Exp $
 * $Author: popolony2k $
 * $Name:  $
 * $Revision: 1.1.1.1 $
 * $State: Exp $
 *
 */

package org.hn.sleek.jmml;

import java.util.*;

public class ContactStatus {

  String status;

  // All protected static data was made public 
  public final static String UNKNOWN        = "UNKNOWN";
  public final static String ONLINE         = "NLN";
  public final static String OFFLINE        = "FLN";
  public final static String APPEAR_OFFLINE = "HDN";
  public final static String IDLE           = "IDL";
  public final static String AWAY           = "AWY";
  public final static String BUSY           = "BSY";
  public final static String BE_RIGHT_BACK  = "BRB";
  public final static String ON_THE_PHONE   = "PHN";
  public final static String OUT_TO_LUNCH   = "LUN";
  
  static HashMap statusMap = new HashMap();
  
  static {
    statusMap.put( "NLN", ContactStatus.ONLINE );
    statusMap.put( "FLN", ContactStatus.OFFLINE );
    statusMap.put( "HDN", ContactStatus.APPEAR_OFFLINE );
    statusMap.put( "IDL", ContactStatus.IDLE );
    statusMap.put( "AWY", ContactStatus.AWAY );
    statusMap.put( "BSY", ContactStatus.BUSY );
    statusMap.put( "BRB", ContactStatus.BE_RIGHT_BACK );
    statusMap.put( "PHN", ContactStatus.ON_THE_PHONE );
    statusMap.put( "LUN", ContactStatus.OUT_TO_LUNCH );
  }

  
  public ContactStatus( String rawStatus ) {
    
    this.status = parseStatus( rawStatus );
  }

  /** 
   * Parses a status string, and returns a "clean" status term.
   */
  public static String parseStatus( String rawStatus ) {
    
    String status = ( String ) statusMap.get( rawStatus.trim() );
    
    if( status != null ) {
      return status;
    }
    else {
      return ContactStatus.UNKNOWN; // BUG: This isn't a good way to handle it.
    }
  }

  public String toString() {
    
    return status;
  }
}
// ContactStatus class