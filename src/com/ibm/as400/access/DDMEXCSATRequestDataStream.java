///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DDMEXCSATRequestDataStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream;

// Constructs the exchange attributes data stream request.
//
// Exchange attributes request:
// Term = EXCSAT.
// Parms = SRVCLSNM -> CHRSTRDR => Character string containing server class name in EBCDIC.
//                     EXTNAM   => Character string containing external name in EBCDIC.
// Size = 6 --> Header (0 - 5).
//        2 --> LL Length of EXCSAT term and parms (6, 7).
//        2 --> CP EXCSAT code point (8, 9).
//        2 --> LL Length of EXTNAM term and parms (10, 11).
//        2 --> CP EXTNAM code point (12, 13).
//        5 --> EXTNAM parm (JT400) (14 - 18).
//        2 --> LL Length of SRVCLSNM term and parm (19, 20).
//        2 --> CP SRVCLSNM code point (21, 22).
//        2 --> LL Length of CHRSTRDR term and parms (23, 24).
//        2 --> CP CHRSTRDR code point (25, 26).
//        3 --> SRVCLSNM parm (QAS) (27 - 29).
//        2 --> LL Length of MGRLVLLS term and parms.
//        2 --> CP MGRLVLLS code point.
//   4 * 23 --> Manager level parms and their values.
//       ----
//       126 --> Total length of the data stream.
class DDMEXCSATRequestDataStream extends DDMDataStream
{
    private static final int TOTAL_LENGTH = 126;
    private static final int HEADER_LENGTH = 6;

    DDMEXCSATRequestDataStream()
    {
        super(new byte[TOTAL_LENGTH]);

        // Initialize the header:  Don't continue on error, not chained, GDS id = D0, type = RQSDSS, no same request correlation.
        setGDSId((byte)0xD0);
        // setIsChained(false);
        // setContinueOnError(false);
        // setHasSameRequestCorrelation(false);
        setType(1);

        int offset = HEADER_LENGTH;  // start just after the 6-byte header

        set16bit(TOTAL_LENGTH - HEADER_LENGTH, offset);  // Set length of EXCSAT term and parms.
        offset += 2;
        set16bit(DDMTerm.EXCSAT, offset);  // Set code point for EXCSAT term.
        offset += 2;

        set16bit(9, offset);  // Set length of EXTNAM parm.
        offset += 2;
        set16bit(DDMTerm.EXTNAM, offset);  // Set code point for EXTNAM parm.
        offset += 2;
        // Set the external name (EXTNAM parm).
        data_[offset++] = (byte)0xD1; data_[offset++] = (byte)0xE3; data_[offset++] = (byte)0xF4; data_[offset++] = (byte)0xF0; data_[offset++] = (byte)0xF0; // EBCDIC "JT400"

        set16bit(11, offset);  // Set length of SRVCLSNM parm.
        offset += 2;
        set16bit(DDMTerm.SRVCLSNM, offset);  // Set code point for SRVCLSNM parm.
        offset += 2;

        // Note to maintenance programmer:
        // CHRSTRDR is ignored by the DDM Server, and is probably not needed.
        // We're not sure why it was added in the first place.
        set16bit(7, offset);  // Set length of CHRSTRDR parm.
        offset += 2;
        set16bit(DDMTerm.CHRSTRDR, offset);  // Set code point for CHRSTRDR parm.
        offset += 2;

        // Set the server class name (SRVCLSNM parm).
        data_[offset++] = (byte)0xD8; data_[offset++] = (byte)0xC1; data_[offset++] = (byte)0xE2; // EBCDIC "QAS".


        // Set the MGRLVLLS values.  Each parameter for the MGRLVLLS term is appended to the array as code point/value pairs.  No length bytes precede the parameters.


        set16bit(96, offset); // Set length of MGRLVLLS term and parms (4-byte LL/CP, plus 23 4-byte parms)
        offset += 2;
        set16bit(DDMTerm.MGRLVLLS, offset); // Set code point for MGRLVLLS.
        offset += 2;

        set16bit(DDMTerm.AGENT, offset);
        offset += 2;
        set16bit(3, offset);
        offset += 2;

        set16bit(DDMTerm.ALTINDF, offset);
        offset += 2;
        set16bit(3, offset);
        offset += 2;

        set16bit(DDMTerm.CMBACCAM, offset);
        offset += 2;
        set16bit(3, offset);
        offset += 2;

        set16bit(DDMTerm.CMBKEYAM, offset);
        offset += 2;
        set16bit(3, offset);
        offset += 2;

        set16bit(DDMTerm.CMBRNBAM, offset);
        offset += 2;
        set16bit(3, offset);
        offset += 2;

        // V4R2 system or later.
        set16bit(DDMTerm.CMNTCPIP, offset);  // Set CMNTCPIP code point. (V4R2 system or later)
        offset += 2;
        set16bit(5, offset);  // Set value for CMNTCPIP.  Must be 5.
        offset += 2;

        set16bit(DDMTerm.DICTIONARY, offset);
        offset += 2;
        set16bit(1, offset);
        offset += 2;

        set16bit(DDMTerm.DIRECTORY, offset);
        offset += 2;
        set16bit(3, offset);
        offset += 2;

        set16bit(DDMTerm.DIRFIL, offset);
        offset += 2;
        set16bit(3, offset);
        offset += 2;

        set16bit(DDMTerm.DRCAM, offset);
        offset += 2;
        set16bit(3, offset);
        offset += 2;

        set16bit(DDMTerm.KEYFIL, offset);
        offset += 2;
        set16bit(3, offset);
        offset += 2;

        set16bit(DDMTerm.LCKMGR, offset);
        offset += 2;
        set16bit(3, offset);
        offset += 2;

        set16bit(DDMTerm.RDB, offset);
        offset += 2;
        set16bit(3, offset);
        offset += 2;

        set16bit(DDMTerm.RELKEYAM, offset);
        offset += 2;
        set16bit(3, offset);
        offset += 2;

        set16bit(DDMTerm.RELRNBAM, offset);
        offset += 2;
        set16bit(3, offset);
        offset += 2;

        set16bit(DDMTerm.SECMGR, offset);
        offset += 2;
        set16bit(1, offset);
        offset += 2;

        set16bit(DDMTerm.SEQFIL, offset);
        offset += 2;
        set16bit(3, offset);
        offset += 2;

        set16bit(DDMTerm.SQLAM, offset);
        offset += 2;
        set16bit(3, offset);
        offset += 2;

        set16bit(DDMTerm.STRAM, offset);
        offset += 2;
        set16bit(3, offset);
        offset += 2;

        set16bit(DDMTerm.STRFIL, offset);
        offset += 2;
        set16bit(3, offset);
        offset += 2;

        set16bit(DDMTerm.SUPERVISOR, offset);
        offset += 2;
        set16bit(3, offset);
        offset += 2;

        set16bit(DDMTerm.SYSCMDMGR, offset);
        offset += 2;
        set16bit(4, offset);
        offset += 2;

        set16bit(DDMTerm.RSCRCVM, offset);
        offset += 2;
        set16bit(4, offset);
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending DDM EXCSAT request...");
        super.write(out);
    }
}
