///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLBigint.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;



class SQLBigint
implements SQLData
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


    // Private data.
    private int                 truncated_;
    private long                value_              = 0;



    public Object clone()
    {
        return new SQLBigint();
    }



//---------------------------------------------------------//
//                                                         //
// CONVERSION TO AND FROM RAW BYTES                        //
//                                                         //
//---------------------------------------------------------//



    public void convertFromRawBytes(byte[] rawBytes, int offset, ConverterImplRemote ccsidConverter)
        throws SQLException
    {
        value_ = BinaryConverter.byteArrayToLong(rawBytes, offset);
    }



    public void convertToRawBytes(byte[] rawBytes, int offset, ConverterImplRemote ccsidConverter)
        throws SQLException
    {
        BinaryConverter.longToByteArray(value_, rawBytes, offset);
    }



//---------------------------------------------------------//
//                                                         //
// SET METHODS                                             //
//                                                         //
//---------------------------------------------------------//



    public void set(Object object, Calendar calendar, int scale)
        throws SQLException
    {
      truncated_ = 0;

      if (object instanceof String) {
          try {
              value_ = Long.parseLong((String) object);
          }
          catch (NumberFormatException e) {
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
          }
      }

      else if (object instanceof Number)
      {
          // Compute truncation by getting the value as a double
          // and comparing it against MAX_VALUE/MIN_VALUE.
          double doubleValue = ((Number) object).doubleValue ();

          if (( doubleValue > Long.MAX_VALUE ) || ( doubleValue < Long.MIN_VALUE )) // @D9a
          {
              // Note:  Truncated here is set to 1 byte because the
              //        value has to be something positive in order
              //        for the code that checks it to do the right
              //        thing.
              truncated_ = 1;                                                       // @D9a
          }

          value_ = ((Number) object).longValue();

          // @D9d
          // Compute truncation. @Wz put the following three lines back in
          // double doubleValue = ((Number) object).doubleValue();
          // if (doubleValue != value_)
          //    truncated_ = Double.toString(doubleValue - value_).length() / 2;
      }

      else if (object instanceof Boolean)
          value_ = (((Boolean) object).booleanValue() == true) ? 1 : 0;

      else
          JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    }



//---------------------------------------------------------//
//                                                         //
// DESCRIPTION OF SQL TYPE                                 //
//                                                         //
//---------------------------------------------------------//



    public String getCreateParameters()
    {
        return null;
    }


    public int getDisplaySize()
    {
        return 20;
    }


    public String getLiteralPrefix ()
    {
        return null;
    }


    public String getLiteralSuffix ()
    {
        return null;
    }



    public String getLocalName ()
    {
        return "BIGINT";
    }


    public int getMaximumPrecision ()
    {
        return 19;
    }


    public int getMaximumScale ()
    {
        return 0;
    }


    public int getMinimumScale ()
    {
        return 0;
    }


    public int getNativeType ()
    {
        return 492;                 // @A1C
    }


    public int getPrecision ()
    {
        return 19;
    }


    public int getRadix ()
    {
        return 10;
    }



    public int getScale ()
    {
        return 0;
    }


	public int getType ()
	{
		return java.sql.Types.BIGINT;
	}


	public String getTypeName ()
	{
		return "BIGINT";
	}


    // @B1D public boolean isGraphic ()
    // @B1D {
    // @B1D     return false;
    // @B1D }



    public boolean isSigned ()
    {
        return true;
    }



    public boolean isText ()
    {
        return false;
    }



//---------------------------------------------------------//
//                                                         //
// CONVERSIONS TO JAVA TYPES                               //
//                                                         //
//---------------------------------------------------------//



    public int getActualSize ()
    {
        return 8;
    }



    public int getTruncated ()
    {
        return truncated_;
    }



	public InputStream toAsciiStream ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
	}



	public BigDecimal toBigDecimal (int scale)
	    throws SQLException
	{
        if (scale <= 0)                                                 
            return BigDecimal.valueOf (value_);                  
        else                                                            
            return BigDecimal.valueOf (value_).setScale (scale); 
	}



	public InputStream toBinaryStream ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
	}



	public Blob toBlob ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
	}



	public boolean toBoolean ()
	    throws SQLException
	{
	    return (value_ != 0);
	}



	public byte toByte ()
	    throws SQLException
	{
	    return (byte) value_;
	}



	public byte[] toBytes ()
	    throws SQLException
	{
	    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
	}



	public Reader toCharacterStream ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
	}



	public Clob toClob ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
	}



	public Date toDate (Calendar calendar)
	    throws SQLException
	{
	    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
	}



	public double toDouble ()
	    throws SQLException
	{
        return (double) value_;    
	}



	public float toFloat ()
	    throws SQLException
	{
        return (float) value_;
	}



	public int toInt ()
	    throws SQLException
	{
	    return (int) value_;
	}



	public long toLong ()
	    throws SQLException
	{
	    return value_;
	}



	public Object toObject ()
	{
	    return new Long (value_);
	}



	public short toShort ()
	    throws SQLException
	{
	    return (short) value_;
	}



	public String toString ()
	{
        return Long.toString (value_);
	}



	public Time toTime (Calendar calendar)
	    throws SQLException
	{
	    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
	}



	public Timestamp toTimestamp (Calendar calendar)
	    throws SQLException
	{
	    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
	}



	public InputStream	toUnicodeStream ()
	    throws SQLException
	{
	    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
	}



}

