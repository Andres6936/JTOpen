///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBColumnDescriptorsDataFormat.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;



/**
The DBColumnDescriptorsDataFormat describes the 
data in the variable length column descriptor.
**/
class DBColumnDescriptorsDataFormat
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


    // Store these as byte arrays so we can 
    private byte[]                 baseColumnName_;
    private byte[]                 baseTableName_;
    private byte[]                 baseTableSchemaName_;

    // Column label is a string because we are given the CCSID in the field if we receive
    // a column label from the server.
    private String                 columnLabel_;  


    /**
    Constructs a DBColumnDescriptorsDataFormat object.  Use this when overlaying
    on a reply datastream.  The cached data will be set when overlay()
    is called.
    **/
    DBColumnDescriptorsDataFormat()                              
    {                                            
    }


    /**
    Positions the overlay structure.  This reads the cached data only
    when it was not previously set by the constructor.
    **/
    void overlay (byte[] rawBytes, int offset, int variableColumnInfoLength)
    throws SQLException
    {
        int codePoint;
        int lengthOfVariablePart;

        // Parse through how ever many of the 3900, 3901, 3902, and 3904 there are (can be 0 
        // to 4).

        // Make sure variableColumnInfoLength is greater than 0.  If it is 0, 
        // that means the query did not return us variable column information.
        while (variableColumnInfoLength > 0)
        {
            codePoint = BinaryConverter.byteArrayToShort (rawBytes, offset + 4);
            lengthOfVariablePart = BinaryConverter.byteArrayToShort (rawBytes, offset);
            switch (codePoint)
            {
            case 3900:
                // base column name

                System.arraycopy(rawBytes, offset + 6, baseColumnName_, 0, lengthOfVariablePart);
                break;

            case 3901:
                // base table name
                System.arraycopy(rawBytes, offset + 6, baseTableName_, 0, lengthOfVariablePart);
                break;

            case 3902:
                // column label (carries its own CCSID, so make it a String, not a byte array)
                int ccsid = BinaryConverter.byteArrayToShort (rawBytes, offset + 6); 
                try
                {
                    columnLabel_ = (ConvTable.getTable(ccsid, null)).byteArrayToString(rawBytes, 
                                                                                       offset + 8, 
                                                                                       lengthOfVariablePart);
                }
                catch (UnsupportedEncodingException e)
                {
                    JDError.throwSQLException (JDError.EXC_INTERNAL, e);
                }
                break;

            case 3904:
                // schema name
                System.arraycopy(rawBytes, offset + 6, baseTableSchemaName_, 0, lengthOfVariablePart);
                break;
            }
            //Subtract off the length what we took off the datastream.
            variableColumnInfoLength = variableColumnInfoLength - (6 + lengthOfVariablePart);
            //Move the offset to the next code point.
            offset = offset + (6 + lengthOfVariablePart);
        }
    }



    String getBaseColumnName(ConvTable convTable)
    {
        //We don't have to be returned a baseColumnName by the server, depending on the query
        if (baseColumnName_ != null)
        {
            return convTable.byteArrayToString (baseColumnName_, 0, baseColumnName_.length);
        }
        else
            return null;
    }



    String getBaseTableName(ConvTable convTable)
    {
        //We don't have to be returned a baseTableName by the server, depending on the query
        if (baseColumnName_ != null)
        {
            return convTable.byteArrayToString (baseTableName_, 0, baseTableName_.length);
        }
        else
            return null;
    }



    String getBaseTableSchemaName(ConvTable convTable)
    {
        //We don't have to be returned a baseTableSchemaName by the server, depending on the query
        if (baseTableSchemaName_ != null)
        {
            return convTable.byteArrayToString (baseTableSchemaName_, 0, 
                                                baseTableSchemaName_.length);
        }
        else
            return null;
    }



    String getColumnLabel(ConvTable convTable)
    {
        //We don't have to be returned a column label by the server, depending on the query.
        //In fact, if the column label is the same as the base column name, we will not be returned
        //it.  If we get a base column name and not a column label, we can assume that the column
        //label is the same as the base column name and return that.
        if (columnLabel_ != null)
        {
            // If we have a column label, we already converted it based on the CCSID provided 
            // by the database, so don't use the converter the user passed in.
            return columnLabel_;
        }
        // We weren't returned a column label, so try to return the base column name.
        else if (baseColumnName_ != null)
        {
            return convTable.byteArrayToString (baseColumnName_, 0, baseColumnName_.length);
        }
        // We weren't returned a column label or a base column name, so return null.
        else
            return null;
    }
}


