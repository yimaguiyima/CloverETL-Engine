/*
*    jETeL/Clover - Java based ETL application framework.
*    Copyright (C) 2002-04  David Pavlis <david_pavlis@hotmail.com>
*    
*    This library is free software; you can redistribute it and/or
*    modify it under the terms of the GNU Lesser General Public
*    License as published by the Free Software Foundation; either
*    version 2.1 of the License, or (at your option) any later version.
*    
*    This library is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU    
*    Lesser General Public License for more details.
*    
*    You should have received a copy of the GNU Lesser General Public
*    License along with this library; if not, write to the Free Software
*    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
*/
package org.jetel.connection.jdbc.specific.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.jetel.connection.jdbc.DBConnection;
import org.jetel.connection.jdbc.SQLUtil;
import org.jetel.connection.jdbc.SQLCloverStatement.QueryType;
import org.jetel.connection.jdbc.specific.JdbcSpecific;
import org.jetel.exception.JetelException;
import org.jetel.metadata.DataFieldMetadata;
import org.jetel.util.string.StringUtils;

/**
 * Abstract implementation of JdbcSpecific, which is currently ancestor of all 
 * implementation of JdbcSpecific interface.
 * Contains a default result set optimization and a conversion table between sql types 
 * and clover field types.
 * 
 * @author Martin Zatopek (martin.zatopek@javlinconsulting.cz)
 *         (c) Javlin Consulting (www.javlinconsulting.cz)
 *
 * @created Jun 3, 2008
 */
abstract public class AbstractJdbcSpecific implements JdbcSpecific {

	/** the SQL comments pattern conforming to the SQL standard */
	private static final Pattern COMMENTS_PATTERN = Pattern.compile("--[^\r\n]*|/\\*.*?\\*/", Pattern.DOTALL);

	private static final String TYPES_CLASS_NAME = "java.sql.Types";

	private static final String RESULT_SET_PARAMETER_TYPE_FIELD = "OTHER";

	private final static int DEFAULT_FETCH_SIZE = 50;

	private AutoGeneratedKeysType autoGeneratedKeysType;
	
	protected AbstractJdbcSpecific() {
		this(AutoGeneratedKeysType.NONE);
	}

	public AbstractJdbcSpecific(AutoGeneratedKeysType autoGeneratedKeysType) {
		this.autoGeneratedKeysType = autoGeneratedKeysType;
	}
	
	/* (non-Javadoc)
	 * @see org.jetel.connection.jdbc.specific.JdbcSpecific#createSQLConnection(org.jetel.connection.jdbc.DBConnection, org.jetel.connection.jdbc.specific.JdbcSpecific.OperationType)
	 */
	abstract public Connection createSQLConnection(DBConnection dbConnection, OperationType operationType) throws JetelException;

	/* (non-Javadoc)
	 * @see org.jetel.connection.jdbc.specific.JdbcSpecific#getAutoKeyType()
	 */
	public AutoGeneratedKeysType getAutoKeyType() {
		return autoGeneratedKeysType;
	}

	/* (non-Javadoc)
	 * @see org.jetel.connection.jdbc.specific.JdbcSpecific#optimizeResultSet(java.sql.ResultSet, org.jetel.connection.jdbc.specific.JdbcSpecific.OperationType)
	 */
	public void optimizeResultSet(ResultSet resultSet, OperationType operationType) {
		switch (operationType){
		case READ:
			try {
				resultSet.setFetchDirection(ResultSet.FETCH_FORWARD);
				resultSet.setFetchSize(DEFAULT_FETCH_SIZE);
			} catch(SQLException ex) {
				//TODO: for now, do nothing
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.jetel.connection.jdbc.specific.JdbcSpecific#jetelType2sql(org.jetel.metadata.DataFieldMetadata)
	 */
	public int jetelType2sql(DataFieldMetadata field){
		switch (field.getType()) {
		case DataFieldMetadata.INTEGER_FIELD:
			return Types.INTEGER;
		case DataFieldMetadata.NUMERIC_FIELD:
			return Types.NUMERIC;
		case DataFieldMetadata.STRING_FIELD:
			return field.isFixed() ? Types.CHAR : Types.VARCHAR;
		case DataFieldMetadata.DATE_FIELD:
			boolean isDate = field.isDateFormat();
			boolean isTime = field.isTimeFormat();
			if (isDate && isTime || StringUtils.isEmpty(field.getFormatStr())) 
				return Types.TIMESTAMP;
			if (isDate)
				return Types.DATE;
			if (isTime)
				return Types.TIME;
			return Types.TIMESTAMP;
        case DataFieldMetadata.LONG_FIELD:
            return Types.BIGINT;
        case DataFieldMetadata.DECIMAL_FIELD:
            return Types.DECIMAL;
        case DataFieldMetadata.BYTE_FIELD:
        case DataFieldMetadata.BYTE_FIELD_COMPRESSED:
        	if (!StringUtils.isEmpty(field.getFormatStr())
					&& field.getFormatStr().equalsIgnoreCase(DataFieldMetadata.BLOB_FORMAT_STRING)) {
        		return Types.BLOB;
        	}
            return field.isFixed() ? Types.BINARY : Types.VARBINARY;
        case DataFieldMetadata.BOOLEAN_FIELD:
        	return Types.BOOLEAN;
		default:
			throw new IllegalArgumentException("Can't handle Clover's data type :"+field.getTypeAsString());
		}
	}
	
	
	public String jetelType2sqlDDL(DataFieldMetadata field) {
		int sqlType = jetelType2sql(field);
		
		switch(sqlType) {
		case Types.BINARY :
		case Types.VARBINARY :
		case Types.VARCHAR :
		case Types.CHAR :
			return sqlType2str(sqlType) + "(" + (field.isFixed() ? String.valueOf(field.getSize()) : "80") + ")";
		case Types.DECIMAL :
			String base = sqlType2str(sqlType);
			String prec = "";
			if (field.getProperty("length") != null) {
				if (field.getProperty("scale") != null) {
					prec = "(" + field.getProperty("length") + "," + field.getProperty("scale") + ")";
				} else {
					prec = "(" + field.getProperty("length") + ",0)";
				}
			}
			return base + prec;
		default :
			return sqlType2str(sqlType);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.jetel.connection.jdbc.specific.JdbcSpecific#sqlType2jetel(int)
	 */
	public char sqlType2jetel(int sqlType) {
		switch (sqlType) {
			case Types.INTEGER:
			case Types.SMALLINT:
			case Types.TINYINT:
			    return DataFieldMetadata.INTEGER_FIELD;
			//-------------------
			case Types.BIGINT:
			    return DataFieldMetadata.LONG_FIELD;
			//-------------------
			case Types.DECIMAL:
			case Types.NUMERIC:
				return DataFieldMetadata.DECIMAL_FIELD;
			case Types.DOUBLE:
			case Types.FLOAT:
			case Types.REAL:
				return DataFieldMetadata.NUMERIC_FIELD;
			//------------------
			case Types.CHAR:
			case Types.LONGVARCHAR:
			case Types.VARCHAR:
			case Types.CLOB:
				return DataFieldMetadata.STRING_FIELD;
			//------------------
			case Types.DATE:
			case Types.TIME:
			case Types.TIMESTAMP:
				return DataFieldMetadata.DATE_FIELD;
            //-----------------
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
			case Types.OTHER:
                return DataFieldMetadata.BYTE_FIELD;
			//-----------------
			case Types.BOOLEAN:
				return DataFieldMetadata.BOOLEAN_FIELD;
			// proximity assignment
			case Types.BIT:
			case Types.NULL:
				return DataFieldMetadata.STRING_FIELD;
			default:
				throw new IllegalArgumentException("Can't handle JDBC.Type :"+sqlType);
		}
	}

	/* (non-Javadoc)
	 * @see org.jetel.connection.jdbc.specific.JdbcSpecific#getResultSetParameterTypeField()
	 */
	public String getResultSetParameterTypeField() {
		return RESULT_SET_PARAMETER_TYPE_FIELD;
	}
	
	/* (non-Javadoc)
	 * @see org.jetel.connection.jdbc.specific.JdbcSpecific#getTypesClassName()
	 */
	public String getTypesClassName() {
		return TYPES_CLASS_NAME;
	}

	public Pattern getCommentsPattern() {
		return COMMENTS_PATTERN;
	}

	public String sqlType2str(int sqlType) {
		return SQLUtil.sqlType2str(sqlType);
	}

    public String quoteIdentifier(String identifier) {
        return identifier;
    }

    public String getValidateQuery(String query, QueryType queryType) throws SQLException {
		
		String q = null;
        String where = "WHERE";
        int indx;
        
        switch(queryType) {
		case INSERT:
			throw new SQLException("INSERT query cannot be validated");
		case UPDATE:
		case DELETE:
			
			q = query.toUpperCase();
			
			indx = q.indexOf(where);
            if (indx >= 0){
            	q = q.substring(0, indx + where.length()) + " 0=1 and " + q.substring(indx + where.length());
            }else{
            	q += " where 0=1";
            }
            break;
            
		case SELECT:
			
			query = SQLUtil.removeUnnamedFields(query, this);
			q = "SELECT wrapper_table.* FROM (" + query + ") wrapper_table where 1=0";
            break;
		}
	
        return q;
        
	}

	/**
	 * Default behavior for literal detection
	 */
	public boolean isLiteral(String s) {
		
		if (s == null) {
			return true;
		}
		
		s = s.trim();

		// numbers are literals
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {
		}
		try {
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException e) {
		}
		
		return s.startsWith("'");
		
	}

	/**
	 * A static method that retrieves schemas from dbMeta objects.
	 * Returns it as arraylist of strings in the format either <schema> or <catalog>.<schema>
	 * e.g.:
	 * mytable
	 * dbo.anothertable
	 * 
	 * @param dbMeta
	 * @return
	 * @throws SQLException
	 */
	protected static ArrayList<String> getMetaSchemas(DatabaseMetaData dbMeta) throws SQLException {
		ArrayList<String> ret = new ArrayList<String>();
		ResultSet result = dbMeta.getSchemas();
		String tmp;
		while (result.next()) {
			tmp = "";
			try {
				if (result.getString(2) != null) {
					tmp = result.getString(2) + dbMeta.getCatalogSeparator();
				}
			} catch (Exception e) {
				// -pnajvar
				// this is here deliberately
				// some dbms don't provide second column and that is not wrong, just have to ignore
			}
			tmp += result.getString(1);
			ret.add(tmp);
		}
		result.close();
		return ret;
	}
	
	protected static ArrayList<String> getMetaCatalogs(DatabaseMetaData dbMeta) throws SQLException {
		ArrayList<String> ret = new ArrayList<String>();
		ResultSet result = dbMeta.getCatalogs();
		String tmp;
		while (result.next()) {
			tmp = result.getString(1);
			ret.add(tmp);
		}
		result.close();
		return ret;
	}
	
	public ArrayList<String> getSchemas(java.sql.Connection connection)
			throws SQLException {

		ArrayList<String> tmp;
		
		ArrayList<String> schemas = new ArrayList<String>();

		DatabaseMetaData dbMeta = connection.getMetaData();
		
		// add schemas
		tmp = getMetaSchemas(dbMeta);
		if (tmp != null) {
			schemas.addAll(tmp);
		}

		// add catalogs
		tmp = getMetaCatalogs(dbMeta);
		if (tmp != null) {
			schemas.addAll(tmp);
		}
		
		return schemas;
	}

	public ResultSet getTables(java.sql.Connection connection, String dbName) throws SQLException {
		// by default, database `dbName` is considered a schema, sometimes it needs to be considered
		// as a catalog
		return connection.getMetaData().getTables(dbName, null, "%", new String[] {"TABLE", "VIEW" }/*tableTypes*/); //fix by kokon - show only tables and views
	}

    /* (non-Javadoc)
     * @see org.jetel.connection.jdbc.specific.JdbcSpecific#getColumns(java.sql.Connection, java.lang.String, java.lang.String)
     */
    public ResultSetMetaData getColumns(Connection connection, String schema, String table) throws SQLException {
		String sqlQuery = compileSelectQuery4Table(schema, table) + " where 0=1";
		ResultSet resultSet = connection.createStatement().executeQuery(sqlQuery);

		return resultSet.getMetaData();
    }
    
    /* (non-Javadoc)
     * @see org.jetel.connection.jdbc.specific.JdbcSpecific#compileSelectQuery4Table(java.lang.String, java.lang.String)
     */
    public String compileSelectQuery4Table(String schema, String table) {
//		some dbms don't support 
//    	return "select * from " + schema + "." + table
		return "select * from " + table;
    }
    
}
