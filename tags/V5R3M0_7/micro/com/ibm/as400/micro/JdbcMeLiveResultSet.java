///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JdbcMeLiveResultSet.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.micro;

import java.sql.*;
import java.io.*;

/**
 *  The JdbcMeLiveResultSet class provides access to a table
 *  of data generated by a database query.
 *
 *  <p>A result set maintains a cursor pointing to its current
 *  row of data.  Initially, the cursor is positioned before the
 *  first row.  If the result set is scrollable, use any of the
 *  cursor positioning methods to move the cursor within the result
 *  set.  If the result set is not scrollable, then only use next()
 *  to move the cursor.
 *
 *  <p>The get methods retrieve column values for the current row.
 *  Values can be retrieved using either the column index or the
 *  column name.  In general, using the column index is more efficient.
 *  Column indexes are numbered starting with 1.  Column names are
 *  not case sensitive.  If several columns have the same name,
 *  then the first matching column is used.  
 * 
 *  <p>Columns can have two names: a column name ("long name")
 *  and a system column name ("short name").  The get methods and
 *  findColumn() only support using the column name.
 * 
 *  <p>In each get method, the driver attempts to convert the
 *  underlying data to the specified Java type and returns a
 *  suitable Java value.  If such a conversion is not appropriate,
 *  a JdbcMeException is thrown.
 *
 *  <p>If the result set is updatable, the update methods modify
 *  column values for the current row in the result set, but not in the underlying
 *  database.  updateRow() causes all updates to the current row
 *  to be written to the database.  Use deleteRow() to delete the
 *  current row in the database.
 * 
 *  <p>For updatable result sets, there is also an insert row,
 *  which is used as a staging area for the contents of a new row.
 *  Use moveToInsertRow() to position the cursor to the insert row.
 *  Once all updates to the insert row have been made, use insertRow()
 *  to insert the row into the database.
 *  
 *  <p>In the following cases, result sets are always read only
 *  regardless of the concurrency set in the statement:
 *  <ul>
 *    <li>Stored procedure calls
 *    <li>DatabaseMetaData catalog methods
 *    <li>SELECT statements which do not specify FOR UPDATE.  This
 *        is only applicable when running to V4R5 or earlier.        
 *  </ul>
 *
 *  <p>In the following cases, result sets are always forward only
 *  regardless of the type set in the statement:
 *  <ul>
 *    <li>Stored procedure calls
 *  </ul>
 *
 *  <p>A result set is automatically closed by the statement that
 *  generated it when the statement is closed, run again, or used
 *  to retrieve the next result set from a sequence of multiple
 *  result sets.  To close the result set explicitly, call the
 *  close() method.
 *
 *  <p><b>Note:</b> Since Java 2 Micro-Edition does not include java.sql,
 *  JdbcMeLiveResultSet implements the java.sql package that is also part 
 *  of this driver.
 **/
public class JdbcMeLiveResultSet implements ResultSet 
{
    private JdbcMeConnection  connection_ = null;
    private JdbcMeStatement   stmt_ = null;
    private int rsId_ = -1;
    private byte onWhichRow_ = ROW_CURRENT;
    private Object currentRow_[] = null;
    private Object modifiedRowBuffer_[]  = null;

    public final static byte ROW_CURRENT = 0;
    public final static byte ROW_INSERT  = 1;
    public final static byte ROW_UPDATE  = 2;

    /**
     *  Construct the default JdbcMeLiveResultSet.
     **/
    private JdbcMeLiveResultSet()
    {
    }


    /**
     *  Construct a JdbcMeLiveResultSet for a statement.
     *
     *  @param stmt The SQL statement.
     *  @param rsId  The result set ID handle.
     *
     *  @exception JdbcMeResultSet If an error occurs
     **/
    JdbcMeLiveResultSet(JdbcMeStatement stmt, int rsId) throws JdbcMeException 
    {
        /**
         *  Line flows out:
         *    None
         * Line flows in:
         *    None
         **/
        rsId_ = rsId;
        stmt_ = stmt;
        connection_ = stmt.connection_;
    }

    /**
     *  Releases the result set's resources.
     *
     *  @exception JdbcMeException If an error occurs.
     **/
    public void close() throws JdbcMeException 
    {
        /**
         *  Line flows out:
         *    Function ID
         *    Result set handle ID
         * Line flows in:
         *    None
         **/
        try
        {
            connection_.system_.toServer_.writeInt(MEConstants.RS_CLOSE);
            connection_.system_.toServer_.writeInt(rsId_);
            connection_.system_.toServer_.flush();

            // Don't wait for ack from server.
            closeHard();
            return;
        }
        catch (IOException e)
        {
            // If an IOException occurs, our connection to the server
            // has been toasted. Lets reset it.
            connection_.disconnected();
            throw new JdbcMeException(e.toString(), null);
        }
    }

    /**
     *  Force the result set closed.
     **/
    void closeHard() 
    {
        /**
         *  Line flows out:
         *    None
         * Line flows in:
         *    None
         **/
        rsId_ = -1;
        stmt_ = null;
        currentRow_ = null;
        modifiedRowBuffer_ = null;
        return;
    }

    /**
     *  Deletes the current row from the result set and the database.
     *  After deleting a row, the cursor position is no longer valid,
     *  so it must be explicitly repositioned.
     *
     *  @exception JdbcMeException If the result set is not open,
     *                   the result set is not updatable,
     *                   the cursor is not positioned on a row,
     *                   the cursor is positioned on the insert row,
     *                   or an error occurs.
     **/
    public void deleteRow() throws JdbcMeException 
    {
        /**
         *  Line flows out:
         *    Function ID
         *    Result set handle ID
         * Line flows in:
         *    0  success
         *    -1 and exception information
         **/
        try
        {
            connection_.system_.toServer_.writeInt(MEConstants.RS_DELETE_ROW);
            connection_.system_.toServer_.writeInt(rsId_);
            connection_.system_.toServer_.flush();

            int results = connection_.system_.fromServer_.readInt();

            if (results == -1)
                JdbcMeDriver.processException(connection_);
        }
        catch (IOException e)
        {
            // If an IOException occurs, our connection to the server
            // has been toasted. Lets reset it.
            connection_.disconnected();
            throw new JdbcMeException(e.toString(), null);
        }
        return;
    }


    /**
     *  Returns the value of a column as a String object.
     *  This can be used to get values from columns with any SQL type.
     *
     *  @param  columnIndex   The column index (1-based).
     *  @return               The column value or null if the value is SQL NULL.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       the cursor is not positioned on a row,
     *                       the column index is not valid, or the
     *                       requested conversion is not valid.
     **/
    public String getString(int columnIndex) throws JdbcMeException 
    {
        if (currentRow_ == null)
            throw new JdbcMeException("RS Position", null);

        if (columnIndex < 1 || columnIndex > currentRow_.length)
            throw new JdbcMeException("RS Column " + columnIndex, null);

        if(onWhichRow_ == ROW_INSERT)  return modifiedRowBuffer_[columnIndex-1].toString();    //@A1A We want to get the values for the row we are inserting, not the value for the row the server cursor is on
        else return currentRow_[columnIndex-1].toString();
    }

    /**
     *  Returns the value of a column as a Java int value.
     *  This can be used to get values from columns with SQL
     *  types SMALLINT, INTEGER, BIGINT, REAL, FLOAT, DOUBLE, DECIMAL,
     *  NUMERIC, CHAR, and VARCHAR.
     *
     *  @param  columnIndex   The column index (1-based).
     *  @return               The column value or 0 if the value is SQL NULL.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       the cursor is not positioned on a row,
     *                       the column index is not valid, or the
     *                       requested conversion is not valid.
     **/
    public int getInt(int columnIndex) throws JdbcMeException 
    {
        if (currentRow_ == null)
            throw new JdbcMeException("RS Position", null);

        if (columnIndex < 1 || columnIndex > currentRow_.length)
            throw new JdbcMeException("RS Column " + columnIndex, null);

        if(onWhichRow_ == ROW_INSERT)                               //@A1A We want to get the values for the row we are inserting, not the value for the row the server cursor is on
        {
            if(stmt_.columnTypes_[columnIndex-1] == Types.INTEGER){ //@A1A
                return ((Integer)modifiedRowBuffer_[columnIndex-1]).intValue(); //@A1A
            }
            else return Integer.parseInt(modifiedRowBuffer_[columnIndex-1].toString());    //@A1A
        }

        // Optimize the getInt() of an Integer column
        if (stmt_.columnTypes_[columnIndex-1] == Types.INTEGER)
            return((Integer)currentRow_[columnIndex-1]).intValue();

        // Else, attempt to convert existing string or object
        // to an integer polymorphically.
        return Integer.parseInt(currentRow_[columnIndex-1].toString());
    }

    /**
     *  Inserts the contents of the insert row into the result set
     *  and the database.
     *  
     *  @exception JdbcMeException If the result set is not open,
     *                   the result set is not updatable,
     *                   the cursor is not positioned on the insert row,
     *                   a column that is not nullable was not specified,
     *                   or an error occurs.
     **/
    public void insertRow() throws JdbcMeException 
    {
        insertOrUpdateRow(MEConstants.RS_INSERT_ROW);
    }

    /**
     *  Positions the cursor to the insert row.
     *  If an InputStream from the current row is open, it is
     *  implicitly closed.  In addition, all warnings and pending updates
     *  are cleared.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       the result set is not scrollable,
     *                       the result set is not updatable,
     *                       or an error occurs.
     **/
    public void moveToInsertRow() throws JdbcMeException 
    {
        if (modifiedRowBuffer_ == null || modifiedRowBuffer_.length != stmt_.numColumns_)
        {
            modifiedRowBuffer_ = new Object[stmt_.numColumns_];
        }
        else
        {
            if (onWhichRow_ != ROW_INSERT)
            {
                for (int i=0; i<modifiedRowBuffer_.length; ++i)
                {
                    modifiedRowBuffer_[i] = null;
                }
            }
        }

        onWhichRow_ = ROW_INSERT;
        return;
    }


    /**
     *  Positions the cursor to the current row.  This is the row
     *  where the cursor was positioned before moving it to the insert
     *  row.  If the cursor is not on the insert row, then this
     *  has no effect.
     *
     *  <p>If an InputStream from the current row is open, it is
     *  implicitly closed.  In addition, all warnings and pending updates
     *  are cleared.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       the result set is not scrollable,
     *                       or an error occurs.
     **/
    public void moveToCurrentRow() throws JdbcMeException 
    {
        if (currentRow_ == null || currentRow_.length != stmt_.numColumns_)
            currentRow_ = new Object[stmt_.numColumns_];

        onWhichRow_ = ROW_CURRENT;
        return;
    }

    /**
     *  Read a single row of data resulting from a positioning method.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       the result set is not scrollable,
     *                       or an error occurs.
     **/
    private void readRow() throws IOException 
    {
        /**
         *  Line flow in:
         *    For a row, the data follows
         *     A value for each column
         *       Integer for INT columns
         *       String for all others
         **/
        if (currentRow_ == null || currentRow_.length != stmt_.numColumns_)
            currentRow_ = new Object[stmt_.numColumns_];

        for (int i=0; i<stmt_.numColumns_; ++i)
        {
            switch (stmt_.columnTypes_[i])
            {
            case Types.CHAR:   // Same as varchar
            case Types.VARCHAR:
                currentRow_[i] = connection_.system_.fromServer_.readUTF();
                break;
            case Types.INTEGER:
                currentRow_[i] = new Integer(connection_.system_.fromServer_.readInt());
                break;
            default :
                // The server sends a string for every value
                // other than the ones handled explicitly above.
                currentRow_[i] = connection_.system_.fromServer_.readUTF();
            }
        }
    }


    /**
     *  Positions the cursor to the next row.
     *  If an InputStream from the current row is open, it is
     *  implicitly closed.  In addition, all warnings and pending updates
     *  are cleared.
     *
     *  @return     true if the requested cursor position is valid; false
     *       if there are no more rows.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       or an error occurs.
     **/
    public boolean next() throws JdbcMeException 
    {
        return positioningMethod(MEConstants.RS_NEXT, -1);
    }

    /**
     *  Positions the cursor to the previous row.
     *  If an InputStream from the current row is open, it is implicitly
     *  closed.  In addition, all warnings and pending updates
     *  are cleared.
     *
     *  @return    true if the requested cursor position is
     *                  valid; false otherwise.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       the result set is not scrollable,
     *                       or an error occurs.
     **/
    public boolean previous() throws JdbcMeException 
    {
        return positioningMethod(MEConstants.RS_PREVIOUS, -1);
    }

    /**
     *  Positions the cursor to a relative row number.
     *  
     *  <p>Attempting to move beyond the first row positions the
     *  cursor before the first row. Attempting to move beyond the last
     *  row positions the cursor after the last row.
     *
     *  <p>If an InputStream from the current row is open, it is
     *  implicitly closed.  In addition, all warnings and pending updates
     *  are cleared.
     *
     *  @param  rowNumber   The relative row number.  If the relative row
     *               number is positive, this positions the cursor
     *               after the current position.  If the relative
     *               row number is negative, this positions the
     *               cursor before the current position.  If the
     *               relative row number is 0, then the cursor
     *               position does not change.
     *
     *  @return             true if the requested cursor position is valid, false otherwise.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       the result set is not scrollable,
     *                       the cursor is not positioned on a valid row,
     *                       or an error occurs.
     **/
    public boolean relative(int rowNumber) throws JdbcMeException 
    {
        return positioningMethod(MEConstants.RS_RELATIVE, rowNumber);
    }


    /**
     *  Positions the cursor to the first row.
     *  If an InputStream from the current row is open, it is
     *  implicitly closed.  In addition, all warnings and pending updates
     *  are cleared.
     *
     *  @return             true if the requested cursor position is
     *               valid; false otherwise.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       the result set is not scrollable,
     *                       or an error occurs.
     **/
    public boolean first() throws JdbcMeException 
    {
        return positioningMethod(MEConstants.RS_FIRST, -1);
    }

    /**
     *  Positions the cursor to the last row.
     *  If an InputStream from the current row is open, it is
     *  implicitly closed.  In addition, all warnings and pending updates
     *  are cleared.
     *
     *  @return             true if the requested cursor position is
     *               valid; false otherwise.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       the result set is not scrollable,
     *                       or an error occurs.
     **/
    public boolean last() throws JdbcMeException 
    {
        return positioningMethod(MEConstants.RS_LAST, -1);
    }

    /**
     *  Positions the cursor to an absolute row number.
     *
     *  <p>Attempting to move any number of positions before
     *  the first row positions the cursor to before the first row. 
     *  Attempting to move beyond the last
     *  row positions the cursor after the last row.
     *
     *  <p>If an InputStream from the current row is open, it is
     *  implicitly closed.  In addition, all warnings and pending updates
     *  are cleared.
     *
     *  @param  rowNumber   The absolute row number.  If the absolute row
     *               number is positive, this positions the cursor
     *               with respect to the beginning of the result set.
     *               If the absolute row number is negative, this
     *               positions the cursor with respect to the end
     *               of result set.
     *
     *  @return             true if the requested cursor position is
     *               valid; false otherwise.
     *
     *  @exception JdbcMeException  If the result set is not open,
     *                    the result set is not scrollable,
     *                    the row number is 0,
     *                    or an error occurs.
     **/
    public boolean absolute(int rowNumber) throws JdbcMeException 
    {
        return positioningMethod(MEConstants.RS_ABSOLUTE, rowNumber);
    }

    /**
     * The previous() and next() methods both have the
     * same line flows.
     * This function implements them.
     *
     **/
    private boolean positioningMethod(int what, int parameter) throws JdbcMeException 
    {
        /**
         *  Line flows out
         *    Function ID
         *    Result set handle ID
         *    Optional positioning parameter
         * Line flows in:
         *    -1 and exception data
         *     OR
         *    0 indicating no row to be positioned at
         *     OR
         *    1 indicating positioned on a row, data follows
         *      Value for each column
         *       Integer for INT columns
         *       String for all others
         **/
        try
        {
            onWhichRow_ = ROW_CURRENT;
            connection_.system_.toServer_.writeInt(what);
            connection_.system_.toServer_.writeInt(rsId_);

            if (parameter != -1)
                connection_.system_.toServer_.writeInt(parameter);

            connection_.system_.toServer_.flush();
            int   moreData = connection_.system_.fromServer_.readInt();

            if (moreData == -1)
                JdbcMeDriver.processException(connection_);

            if (moreData == 0)
                return false;

            readRow();
            return true;
        }
        catch (IOException e)
        {
            // If an IOException occurs, our connection to the server
            // has been toasted. Lets reset it.
            connection_.disconnected();
            throw new JdbcMeException(e.toString(), null);
        }
    }


    /**
     *  Updates a column in the current row using a String value.
     *  The driver converts this to an SQL VARCHAR value.
     *
     *  <p>This does not update the database directly.  Instead, it updates
     *  a copy of the data in memory.  Call updateRow() or insertRow() to
     *  update the database.
     *
     *  @param  columnIndex   The column index (1-based).
     *  @param  value   The column value or null to update
     *                             the value to SQL NULL.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       the result set is not updatable,
     *                       the cursor is not positioned on a row,
     *                       the column index is not valid,
     *                       or the requested conversion is not valid.
     **/
    public void updateString(int columnIndex, String value) throws JdbcMeException 
    {
        if (stmt_.getResultSetConcurrency() != ResultSet.CONCUR_UPDATABLE)  //A1C changed to call getResultSetConcurrency();
            throw new JdbcMeException("Cursor state invalid", null);

        if (currentRow_ == null || currentRow_.length != stmt_.numColumns_)
            currentRow_ = new Object[stmt_.numColumns_];

        if (modifiedRowBuffer_ == null || modifiedRowBuffer_.length != currentRow_.length)
            modifiedRowBuffer_ = new Object[currentRow_.length];

        //@A1C changed to ROW_INSERT from ROW_UPDATE.  If we are inserting a new row, we do not want to copy the data
        //from the current row on the server to it.
        if(onWhichRow_ != ROW_INSERT)    {   //@A1A
            System.arraycopy(currentRow_, 0, modifiedRowBuffer_, 0, currentRow_.length);      //@A1A
            onWhichRow_ = ROW_UPDATE;  //@A1A we are updating a current row in the result set, not inserting a new row
            currentRow_[columnIndex-1] = value; //@A1A set to new value so when a getXxx is called we can retrieve the new value
        }
        
        if (columnIndex < 1 || columnIndex > modifiedRowBuffer_.length)
            throw new JdbcMeException("RS Column " + columnIndex, null);

        switch (stmt_.columnTypes_[columnIndex-1])
        {
        case Types.CHAR:   // same as varchar
        case Types.VARCHAR:
            modifiedRowBuffer_[columnIndex-1] = value;
            break;
        case Types.INTEGER:
            modifiedRowBuffer_[columnIndex-1] = new Integer(Integer.parseInt(value));
            break;
        default :
            // The server sends a string for every value
            // other than the ones handled explicitly above.
            modifiedRowBuffer_[columnIndex-1] = value;
        }
        return;
    }

    /**
     *  Updates a column in the current row using a Java int value.
     *  The driver converts this to an SQL INTEGER value.
     *  
     *  <p>This does not update the database directly.  Instead, it updates
     *  a copy of the data in memory.  Call updateRow() or insertRow() to
     *  update the database.
     * 
     *  @param  columnIndex   The column index (1-based).
     *  @param  value   The column value.
     *
     *  @exception  JdbcMeException    If the result set is not open,
     *                       the result set is not updatable,
     *                       the cursor is not positioned on a row,
     *                       the column index is not valid, or the
     *                       requested conversion is not valid.
     **/
    public void updateInt(int columnIndex, int value) throws JdbcMeException 
    {
        if (stmt_.getResultSetConcurrency() != ResultSet.CONCUR_UPDATABLE) //@A1C Changed to getResultSetConcurrency
            throw new JdbcMeException("Cursor state invalid", null);

        if (currentRow_ == null || currentRow_.length != stmt_.numColumns_)     //@A1A Be consistent with updateString()
            currentRow_ = new Object[stmt_.numColumns_];                        //@A1A

        if (modifiedRowBuffer_ == null || modifiedRowBuffer_.length != currentRow_.length)
            modifiedRowBuffer_ = new Object[currentRow_.length];

        //@A1C changed to ROW_INSERT from ROW_UPDATE.  If we are inserting a new row, we do not want to copy the data
        //from the current row on the server to it.
        if (onWhichRow_ != ROW_INSERT)               //@A1C
        {
            System.arraycopy(currentRow_, 0, modifiedRowBuffer_, 0, currentRow_.length);
            onWhichRow_ = ROW_UPDATE;      //We are updating a current row in the result set, not inserting a new row
            currentRow_[columnIndex-1] = new Integer(value); //@A1A Want to set the current rows value to the new value
        }

        if (columnIndex < 1 || columnIndex > modifiedRowBuffer_.length)
            throw new JdbcMeException("RS Column " + columnIndex, null);

        switch (stmt_.columnTypes_[columnIndex-1])
        {
        case Types.CHAR:   // same as varchar
        case Types.VARCHAR:
            modifiedRowBuffer_[columnIndex-1] = Integer.toString(value);
            break;
        case Types.INTEGER:
            modifiedRowBuffer_[columnIndex-1] = new Integer(value);
            break;
        default :
            // The server sends a string for every value
            // other than the ones handled explicitly above.
            modifiedRowBuffer_[columnIndex-1] = Integer.toString(value);
        }
        return;
    }

    /**
     *  Updates the database with the new contents of the current row.
     *
     *  @exception JdbcMeException If the result set is not open,
     *                   the result set is not updatable,
     *                   the cursor is not positioned on a row,
     *                   the cursor is positioned on the insert row,
     *                   or an error occurs.
     **/
    public void updateRow() throws JdbcMeException 
    {
        insertOrUpdateRow(MEConstants.RS_UPDATE_ROW);
    }

    /**
     *  Returns the ResultSetMetaData object that describes the result set's columns.
     * 
     *  @return     The metadata object.
     *
     *  @exception  JdbcMeException    If an error occurs.
     **/
    public ResultSetMetaData getMetaData() throws JdbcMeException 
    {
        // The statement object records meta data about
        // this result set.
        return new JdbcMeResultSetMetaData(stmt_.numColumns_, stmt_.columnTypes_);
    }

    /**
     *  Returns the statement for this result set.
     *
     *  @return The statement for this result set, or null if the
     *          result set was returned by a DatabaseMetaData
     *          catalog method.
     *
     *  @exception JdbcMeException If an error occurs.
     **/
    public Statement getStatement() throws JdbcMeException
    {
        return stmt_;
    }

    /**
     *  Perform an insert or update row.
     *
     *  @exception JdbcMeException if an error occurs.
     **/
    private void insertOrUpdateRow(int which) throws JdbcMeException 
    {
        /**
         * Line flows out
         *    Function ID
         *    Result set handle ID
         * Line flows in
         *    -1 and exception data
         *     OR
         *    0 indicating the row was inserted/updated
         **/
        try
        {
            int mustBeOnRow = ROW_INSERT;

            if (which == MEConstants.RS_UPDATE_ROW)
                mustBeOnRow = ROW_UPDATE;

            if (modifiedRowBuffer_ == null || !(onWhichRow_ == mustBeOnRow))
                throw new IllegalArgumentException("RS no in/upd row");

            for (int i=0; i<stmt_.numColumns_; ++i)
            {
                if (modifiedRowBuffer_[i] == null)
                    throw new IllegalArgumentException("RS col not set: " + i);
            }

            connection_.system_.toServer_.writeInt(which);
            connection_.system_.toServer_.writeInt(rsId_);

            // All the data follows.
            for (int i=0; i<stmt_.numColumns_; ++i)
            {
                switch (stmt_.columnTypes_[i])
                {
                case Types.CHAR:
                case Types.VARCHAR:
                    connection_.system_.toServer_.writeUTF((String)modifiedRowBuffer_[i]);
                    break;
                case Types.INTEGER:
                    connection_.system_.toServer_.writeInt(((Integer)modifiedRowBuffer_[i]).intValue());
                    break;
                default :
                    // The server sends a string for every value
                    // other than the ones handled explicitly above.
                    connection_.system_.toServer_.writeUTF(modifiedRowBuffer_[i].toString());
                }
            }
            connection_.system_.toServer_.flush();
            int results = connection_.system_.fromServer_.readInt();

            if (results == -1)
                JdbcMeDriver.processException(connection_);

            return;
        }
        catch (IOException e)
        {
            // If an IOException occurs, our connection to the server
            // has been toasted. Lets reset it.
            connection_.disconnected();
            throw new JdbcMeException(e.toString(), null);
        }
    }

    /**
     *  Returns the result set type.
     *
     *  @return The result set type. Valid values are:
     *                           <ul>
     *                             <li>TYPE_FORWARD_ONLY
     *                             <li>TYPE_SCROLL_INSENSITIVE
     *                             <li>TYPE_SCROLL_SENSITIVE
     *                           </ul>
     *
     *  @exception JdbcMeException If the result set is not open.
     **/
    public int getType() throws JdbcMeException 
    {
        return stmt_.type_;
    }

    /**
     *  Returns the result set concurrency.
     *
     *  @return The result set concurrency. Valid values are:
     *                           <ul>
     *                             <li>CONCUR_READ_ONLY
     *                             <li>CONCUR_UPDATABLE
     *                           </ul>
     *
     *  @exception JdbcMeException If the result set is not open.
     **/
    public int getConcurrency() throws JdbcMeException 
    {
        return stmt_.concurrency_;
    }
}
