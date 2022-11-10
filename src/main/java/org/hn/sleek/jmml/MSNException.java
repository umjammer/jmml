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
 * $Id: MSNException.java,v 1.2 2003/11/01 14:04:32 popolony2k Exp $
 * $Author: popolony2k $
 * $Name:  $
 * $Revision: 1.2 $
 * $State: Exp $
 *
 */

package org.hn.sleek.jmml;


/**
 * Implements the exception manager to JMML library.
 */

public class MSNException extends java.lang.Exception {
  
  public static final int          MSN_EX_GENERIC    = 0;
  public static final int          MSN_EX_LOGIN_FAIL = 1;
  
  String                           strMessage;
  
  
  
  /** 
   * Creates a new instance of JMMLException 
   */
  public MSNException()  {
  
    this( MSN_EX_GENERIC );
  }
    
  /** 
   * Creates a new instance of JMMLException 
   * @param nExceptionType - The exception type;
   */
  public MSNException( int nExceptionType ) {

    super();
    
    switch( nExceptionType )  {
     
      case  MSN_EX_GENERIC    : strMessage = "MSN Exception"; break;
      
      case  MSN_EX_LOGIN_FAIL : strMessage = "Unable to login in MSN Network"; break;
    }
  }
  
  /**
   * Return the exception message
   */
  public String getMessage()  {
  
    return strMessage;
  }
  
}  // MSNException Class