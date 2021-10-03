///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrintObjectImpl.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;

/**
 * The PrintObjectImpl interface defines a set of methods
 * needed for a full implementation of the PrintObject class.
 **/

interface PrintObjectImpl {

    /**
     * The getAttrValue (package scope) method is introduced to allow the
     * propagation of any changes made to attrs (by updateAttrs) to the ImplRemote
     * object.
     *
     * @return attr value
     **/
    NPCPAttribute getAttrValue();


    Integer getIntegerAttribute(int attributeID)
            throws
            AS400SecurityException,
            ErrorCompletingRequestException,
            IOException,
            InterruptedException,
            RequestNotSupportedException;


    Float getFloatAttribute(int attributeID)
            throws
            AS400SecurityException,
            ErrorCompletingRequestException,
            IOException,
            InterruptedException,
            RequestNotSupportedException;


    String getStringAttribute(int attributeID)
            throws
            AS400SecurityException,
            ErrorCompletingRequestException,
            IOException,
            InterruptedException,
            RequestNotSupportedException;


    Integer getSingleIntegerAttribute(int attributeID)
            throws
            AS400SecurityException,
            ErrorCompletingRequestException,
            IOException,
            InterruptedException,
            RequestNotSupportedException;


    Float getSingleFloatAttribute(int attributeID)
            throws
            AS400SecurityException,
            ErrorCompletingRequestException,
            IOException,
            InterruptedException,
            RequestNotSupportedException;


    String getSingleStringAttribute(int attributeID)
            throws
            AS400SecurityException,
            ErrorCompletingRequestException,
            IOException,
            InterruptedException,
            RequestNotSupportedException;

    /**
     * The setPrintObjectAttrs (package scope) method is introduced to allow
     * the propagation of PrintObject property changes to the ImplRemote object.
     *
     * @param idCodePoint
     * @param cpAttrs
     * @param type
     **/
    void setPrintObjectAttrs(NPCPID idCodePoint,
                             NPCPAttribute cpAttrs,
                             int type);


    void setSystem(AS400Impl system);  // @A1C


    void update()
            throws
            AS400SecurityException,
            ErrorCompletingRequestException,
            IOException,
            InterruptedException,
            RequestNotSupportedException;

}
