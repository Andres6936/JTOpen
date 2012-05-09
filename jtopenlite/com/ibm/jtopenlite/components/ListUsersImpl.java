///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  ListUsersImpl.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.components;

import com.ibm.jtopenlite.*;
import com.ibm.jtopenlite.ddm.*;
import java.io.*;
import java.util.*;

final class ListUsersImpl implements DDMReadCallback
{
  private DDMRecordFormat rf_;

  ListUsersImpl()
  {
  }

  private final Vector users_ = new Vector();
  private boolean done_ = false;

  public void newRecord(DDMCallbackEvent event, DDMDataBuffer dataBuffer) throws IOException
  {
    final byte[] data = dataBuffer.getRecordDataBuffer();
    String userName = rf_.getField("UPUPRF").getString(data);
    String userClass = rf_.getField("UPUSCL").getString(data);
    String passwordExpired = rf_.getField("UPPWEX").getString(data);
    long maxStorage = rf_.getField("UPMXST").getLong(data);
    long storageUsed = rf_.getField("UPMXSU").getLong(data);
    String description = rf_.getField("UPTEXT").getString(data);
    String locked = rf_.getField("UPUPLK").getString(data);
    String damaged = rf_.getField("UPUPDM").getString(data);
    String status = rf_.getField("UPSTAT").getString(data);
    long uid = rf_.getField("UPUID").getLong(data);
    long gid = rf_.getField("UPGID").getLong(data);
    UserInfo ui = new UserInfo(userName, userClass, passwordExpired, maxStorage, storageUsed, description,
                               locked, damaged, status, uid, gid);
    users_.addElement(ui);
  }

  public void recordNotFound(DDMCallbackEvent event)
  {
    done_ = true;
  }

  public void endOfFile(DDMCallbackEvent event)
  {
    done_ = true;
  }

  private boolean done()
  {
    return done_;
  }

  public UserInfo[] getUsers(final DDMConnection ddmConn) throws IOException
  {
    Message[] messages = ddmConn.execute("DSPUSRPRF USRPRF(*ALL) TYPE(*BASIC) OUTPUT(*OUTFILE) OUTFILE(QTEMP/TBALLUSERS)");
    if (messages.length > 0)
    {
      if (messages.length != 1 && !messages[0].getID().equals("CPF9861")) // Output file created.
      {
        throw new MessageException("Error retrieving users: ", messages);
      }
    }

    users_.removeAllElements();
    if (rf_ == null)
    {
      rf_ = ddmConn.getRecordFormat("QTEMP", "TBALLUSERS");
      rf_.getField("UPUSCL").setCacheStrings(true);
      rf_.getField("UPPWEX").setCacheStrings(true);
      rf_.getField("UPUPLK").setCacheStrings(true);
      rf_.getField("UPUPDM").setCacheStrings(true);
      rf_.getField("UPSTAT").setCacheStrings(true);
    }

    done_ = false;

    DDMFile file = ddmConn.open("QTEMP", "TBALLUSERS", "TBALLUSERS", "QSYDSUPB", DDMFile.READ_ONLY, false, 160, 1);
    while (!done())
    {
      ddmConn.readNext(file, this);
    }
    ddmConn.close(file);

    UserInfo[] arr = new UserInfo[users_.size()];
    return (UserInfo[])users_.toArray(arr);
  }
}
