///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * The SQLData interface represents native SQL data.  A specific
 * implementation of this interface will implement a specific
 * type of SQL data. <br><br>
 * <p>
 * The implementation's constructor should not necessarily
 * initialize the data. That is done via the set() methods.
 */
interface SQLData extends Cloneable {
    /**
     * <i>NATIVE_ARRAY</i> is defined here for the array type received from zda.
     * But zda does not have a visible array type.
     * zda uses a bit that flags if the stream is an array.
     * So we just define it here as 10000.  This number is not important; it
     * just needs to be different from other native type numbers.
     * <p>This is used in {@code SQLDataFactory} and other array related classes.</p>
     */
    short NATIVE_ARRAY = 10000; //@array

    int UNDEFINED = 0;
    int BIGINT = 1;
    int BINARY = 2;
    int BLOB = 3;
    int BLOB_LOCATOR = 4;
    int CHAR = 5;
    int CHAR_FOR_BIT_DATA = 6;
    int CLOB = 7;
    int CLOB_LOCATOR = 8;
    int DATALINK = 9;
    int DATE = 10;
    int DBCLOB = 11;
    int DBCLOB_LOCATOR = 12;
    int DECIMAL = 13;
    int DECIMAL_USING_DOUBLE = 14;
    int DOUBLE = 15;
    int FLOAT = 16;
    int GRAPHIC = 17;
    int INTEGER = 18;
    int LONG_VARCHAR = 19;
    int LONG_VARCHAR_FOR_BIT_DATA = 20;
    int LONG_VARGRAPHIC = 21;
    int NUMERIC = 22;
    int NUMERIC_USING_DOUBLE = 23;
    int REAL = 24;
    int ROWID = 25;
    int SMALLINT = 26;
    int TIME = 27;
    int TIMESTAMP = 28;
    int VARBINARY = 29;
    int VARCHAR = 30;
    int VARCHAR_FOR_BIT_DATA = 31;
    int VARGRAPHIC = 32;
    int NCLOB = 33;         //@PDA jdbc40 (jdbc40 just added here for info)
    int NCLOB_LOCATOR = 34; //@PDA jdbc40
    int NCHAR = 35;         //@PDA jdbc40
    int NVARCHAR = 36;      //@PDA jdbc40
    int LONG_NVARCHAR = 37; //@pda jdbc40
    int DECFLOAT = 38;      //@DFA
    int ARRAY = 39;         //@array
    int XML_LOCATOR = 40;   //@xml3
    int BOOLEAN = 41;
    int ALL_READER_BYTES = -2;

    /**
     * Returns a clone of the SQLData object.  Use this sparingly
     * so that we minimize the number of copies.
     *
     * @return The clone.
     **/
    Object clone();

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    /**
     * Loads the contents of the data from raw bytes, as returned
     * in a reply from the system.
     *
     * @param rawBytes               raw bytes from the system.
     * @param offset                 offset.
     * @param converter              the converter.
     * @param ignoreConversionErrors Should conversion errors be ignored
     *                               Used when converting packed decimal arrays
     * @throws SQLException If the raw bytes are not in
     *                      the expected format.
     **/
    void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable converter, boolean ignoreConversionErrors)
            throws SQLException;

    void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable converter)
            throws SQLException;

    /**
     * Converts the contents of the data in raw bytes, as needed
     * in a request to the system.
     *
     * @param rawBytes       the raw bytes for the system.
     * @param offset         the offset into the byte array.
     * @param ccsidConverter the converter.
     * @throws SQLException If a database error occurs.
     **/
    void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
            throws SQLException;


    /**
     * validates that raw truncated data is correct.  The data is corrected if is not correct.
     * This is only used when converting to MIXED CCSID and UTF-8.
     *
     * @param rawBytes       the raw bytes for the system.
     * @param offset         the offset into the byte array.
     * @param ccsidConverter the converter.
     * @throws SQLException If a database error occurs.
     **/
    void validateRawTruncatedData(byte[] rawBytes, int offset, ConvTable ccsidConverter)
            throws SQLException;


    //---------------------------------------------------------//
    //                                                         //
    // SET METHODS                                             //
    //                                                         //
    // The set methods initialize the data in a uniform way    //
    // across all types.  If a specific initialization is      //
    // needed based on a Java type, then add other flavors     //
    // of set() methods.                                       //
    //                                                         //
    //---------------------------------------------------------//

    /**
     * Sets the contents of the data based on a Java object.
     * This performs all conversions described in Table 6
     * of the JDBC specification.
     *
     * @param object   a Java object.
     * @param calendar The calendar.
     * @param scale    The scale.
     * @throws SQLException If the Java object is not an
     *                      appropriate type.
     **/
    void set(Object object, Calendar calendar, int scale)
            throws SQLException;

    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    // These methods describe information about the actual     //
    // type of data.                                           //
    //                                                         //
    /*---------------------------------------------------------*/

    /**
     * Returns the SQL type constant for the implementing class.
     *
     * @return the SQL type constant.
     **/
    int getSQLType();

    /**
     * Returns the parameters used in creating the
     * type.
     *
     * @return the parameters, separated by commas,
     * or null if none.
     **/
    String getCreateParameters();

    /**
     * Returns the display size.  This is defined in Appendix
     * D of the ODBC 2.0 Programmer's Reference.
     *
     * @return the display size (in characters).
     **/
    int getDisplaySize();

    //@F1A JDBC 3.0

    /**
     * Returns the Java class name for ParameterMetaData.getParameterClassName().
     *
     * @return the Java class name.
     **/
    String getJavaClassName();

    /**
     * Returns the prefix used to quote a literal.
     *
     * @return the prefix, or null if none.
     **/
    String getLiteralPrefix();

    /**
     * Returns the suffix used to quote a literal.
     *
     * @return the suffix, or null if none.
     **/
    String getLiteralSuffix();

    /**
     * Returns the localized version of the name of the
     * data type.
     *
     * @return the name, or null.
     **/
    String getLocalName();

    /**
     * Returns the maximum precision of the type. This is
     * defined in Appendix D of the ODBC 2.0 Programmer's
     * Reference.
     *
     * @return the maximum precision.
     **/
    int getMaximumPrecision();

    /**
     * Returns the maximum scale of the type.  This is
     * defined in Appendix D of the ODBC 2.0 Programmer's
     * Reference.
     *
     * @return the maximum scale.
     **/
    int getMaximumScale();

    /**
     * Returns the minimum scale of the type.  This is
     * defined in Appendix D of the ODBC 2.0 Programmer's
     * Reference.
     *
     * @return the minimum scale.
     **/
    int getMinimumScale();

    /**
     * Returns the native IBM i identifier for the type.
     *
     * @return the native type.
     **/
    int getNativeType();

    /**
     * Returns the precision of the type. This is
     * defined in Appendix D of the ODBC 2.0 Programmer's
     * Reference.
     *
     * @return the precision.
     **/
    int getPrecision();

    /**
     * Returns the radix for the type.
     *
     * @return the radix.
     **/
    int getRadix();

    /**
     * Returns the scale of the type. This is
     * defined in Appendix D of the ODBC 2.0 Programmer's
     * Reference.
     *
     * @return the scale.
     **/
    int getScale();

    /**
     * Returns the type constant associated with the type.
     *
     * @return SQL type code defined in java.sql.Types.
     **/
    int getType();

    /**
     * Returns the name of the data type.
     *
     * @return the name.
     **/
    String getTypeName();

    /**
     * Indicates whether the type is signed.
     *
     * @return true or false
     **/
    boolean isSigned();

    /**
     * Indicates whether the type is text.  This also
     * indicates that the associated data needs to be
     * converted.
     *
     * @return true or false
     **/
    boolean isText();

    /**
     * Returns the actual size of this piece of data in bytes.
     *
     * @return the actual size of this piece of data in bytes.
     **/
    int getActualSize();

    /**
     * Returns the number of bytes truncated by the last conversion
     * of this piece of data.
     *
     * @return the number of bytes truncated by the last conversion
     **/
    int getTruncated();

    /**
     * Clears the truncated information
     */
    void clearTruncated();

    /**
     * Returns true if the last conversion of this piece of
     * data was out of bounds of the range of the requested
     * datatype.  This will only happen when requesting
     * conversion to a numeric type.
     *
     * @return out of bounds indicator
     */
    boolean getOutOfBounds();

    /**
     * Clear the out of bounds flag
     */
    void clearOutOfBounds();

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSIONS TO JAVA TYPES                               //
    //                                                         //
    // These methods convert the data to a specific Java       //
    // type.  These conversions should be provided per         //
    // section 7, table 1 ("Use of ResultSet.getXxx methods    //
    // to retrieve common SQL data types") of the JDBC 1.10    //
    // specification.  If a conversion is not required or is   //
    // not possible given the data, then the method should     //
    // throw an exception.                                     //
    //                                                         //
    /*---------------------------------------------------------*/

    /**
     * Converts the data to a stream of ASCII characters.
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    InputStream getAsciiStream()
            throws SQLException;

    /**
     * Converts the data to a Java BigDecimal object.
     *
     * @param scale scale, or -1 to use full scale.
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    BigDecimal getBigDecimal(int scale)
            throws SQLException;

    /**
     * Converts the data to a stream of uninterpreted bytes.
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    InputStream getBinaryStream()
            throws SQLException;

    /**
     * Converts the data to a java.sql.Blob object.
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    Blob getBlob()
            throws SQLException;

    /**
     * Converts the data to a Java boolean.
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    boolean getBoolean()
            throws SQLException;

    /**
     * Converts the data to a Java byte.
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    byte getByte()
            throws SQLException;

    /**
     * Converts the data to a Java byte array containing
     * uninterpreted bytes.
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    byte[] getBytes()
            throws SQLException;

    /**
     * Converts the data to a java.io.Reader object.
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    Reader getCharacterStream()
            throws SQLException;

    /**
     * Converts the data to a java.sql.Clob object.
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    Clob getClob()
            throws SQLException;

    /**
     * Converts the data to a java.sql.Date object.
     *
     * @param calendar The calendar.
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    Date getDate(Calendar calendar)
            throws SQLException;

    /**
     * Converts the data to a Java double.
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    double getDouble()
            throws SQLException;

    /**
     * Converts the data to a Java float.
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    float getFloat()
            throws SQLException;

    /**
     * Converts the data to a Java int.
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    int getInt()
            throws SQLException;

    /**
     * Converts the data to a Java long.
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    long getLong()
            throws SQLException;

    /**
     * Converts the data to a Java object.  The actual type
     * of the Java object is dictated per section 8,
     * table 2 ("Standard mapping from SQL types to Java types")
     * of the JDBC 1.10 specification
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    Object getObject()
            throws SQLException;


    /**
     * Converts the data to a Java object that will be used
     * later when processing a batch.  In most cases, this
     * will return the same object as getObject().
     * In the timestamp case, we return an AS400FieldedTimstamp.
     * This is need to permit timestamps that do not exist in the
     * current timezone to be sent to the server.
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    Object getBatchableObject()
            throws SQLException;


    /**
     * Converts the data to a Java short.
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    short getShort()
            throws SQLException;

    /**
     * Converts the data to a Java String object.  This
     * conversion must be provided by the implementation.
     *
     * @return the result of the conversion.
     * @throws SQLException If a database error occurs.
     **/
    String getString()
            throws SQLException;

    /**
     * Converts the data to a java.sql.Time object.
     *
     * @param calendar The calendar.
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    Time getTime(Calendar calendar)
            throws SQLException;

    /**
     * Converts the data to a java.sql.Timestamp object.
     *
     * @param calendar The calendar.
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    Timestamp getTimestamp(Calendar calendar)
            throws SQLException;

    /**
     * Converts the data to a stream of Unicdoe characters.
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    InputStream getUnicodeStream()
            throws SQLException;

    //@PDA jdbc40

    /**
     * Converts the data to a java.io.Reader object.
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    Reader getNCharacterStream()
            throws SQLException;

    //@PDA jdbc40

    /**
     * Converts the data to a java.sql.NClob object
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    NClob getNClob()
            throws SQLException;

    //@PDA jdbc40

    /**
     * Converts the data to String object.
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     *                      void clearOutOfBounds();
     **/
    String getNString()
            throws SQLException;

    //@PDA jdbc40

    /**
     * Converts the data to a java.sql.SQLXML object.
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    SQLXML getSQLXML()
            throws SQLException;

    //@PDA jdbc40

    /**
     * Converts the data to a java.sql.RowId object.
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    RowId getRowId()
            throws SQLException;

    //@array

    /**
     * Converts (returns) the data to a java.sql.Array object.
     *
     * @return the result of the conversion.
     * @throws SQLException If the conversion is not
     *                      required or not possible.
     **/
    Array getArray()
            throws SQLException;

    void updateSettings(SQLConversionSettings settings);

    /**
     * Save the current value.  Called before the statement is executed so that
     * the previous value can be restored if the statement needs to be
     * seamlessly re-executed
     */
    void saveValue() throws SQLException;

    /**
     * Obtain the save value.  All values are "wrapped" to the corresponding Java type
     */
    Object getSavedValue();

}
