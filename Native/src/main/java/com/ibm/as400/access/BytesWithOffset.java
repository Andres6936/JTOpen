///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: BytesWithOffset.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


// Class that represents some data and an offset into it.
class BytesWithOffset
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  byte[] data_;
  int    offset_ = 0;

  BytesWithOffset(byte[] data)
  {
    this(data, 0);
  }

  BytesWithOffset(byte[] data,
                  int offset)
  {
    data_ = data;
    offset_ = offset;
  }
}



