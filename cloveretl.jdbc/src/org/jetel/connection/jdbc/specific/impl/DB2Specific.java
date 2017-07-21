/*
 * jETeL/CloverETL - Java based ETL application framework.
 * Copyright (c) Javlin, a.s. (info@cloveretl.com)
 *  
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jetel.connection.jdbc.specific.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.jetel.connection.jdbc.DBConnection;
import org.jetel.connection.jdbc.specific.conn.DefaultConnection;
import org.jetel.exception.JetelException;
import org.jetel.metadata.DataFieldMetadata;

/**
 * DB2 specific behaviour.
 * 
 * @author Martin Zatopek (martin.zatopek@javlinconsulting.cz)
 *         (c) Javlin Consulting (www.javlinconsulting.cz)
 *
 * @created Jun 3, 2008
 */
public class DB2Specific extends AbstractJdbcSpecific {

	private static final DB2Specific INSTANCE = new DB2Specific();

	protected DB2Specific() {
		super(AutoGeneratedKeysType.MULTI);
	}

	public static DB2Specific getInstance() {
		return INSTANCE;
	}

	/* (non-Javadoc)
	 * @see org.jetel.connection.jdbc.specific.impl.AbstractJdbcSpecific#createSQLConnection(org.jetel.connection.jdbc.DBConnection, org.jetel.connection.jdbc.specific.JdbcSpecific.OperationType)
	 */
	@Override
	public Connection createSQLConnection(DBConnection dbConnection, OperationType operationType) throws JetelException {
		return new DefaultConnection(dbConnection, operationType, getAutoKeyType());
	}
	
	@Override
	public String quoteIdentifier(String identifier) {
        return ('"' + identifier + '"');
    }

	@Override
	public String sqlType2str(int sqlType) {
		switch(sqlType) {
		case Types.BOOLEAN :
			return "SMALLINT";
		case Types.NUMERIC :
			return "DOUBLE";
		}
		return super.sqlType2str(sqlType);
	}
	@Override
	public String jetelType2sqlDDL(DataFieldMetadata field) {
		int sqlType = jetelType2sql(field);
		String ddlKeyword;
		switch(sqlType) {
		case Types.BINARY:
			ddlKeyword = "CHAR";
			break;
		case Types.VARBINARY:
			ddlKeyword = "VARCHAR";
			break;
		default: 
			return super.jetelType2sqlDDL(field);
		}
		return  ddlKeyword + "(" + (field.isFixed() ? String.valueOf(field.getSize()) : "80") + ")" + " FOR BIT DATA";
	}
	@Override
	public int jetelType2sql(DataFieldMetadata field) {
		switch (field.getType()) {
		case DataFieldMetadata.BOOLEAN_FIELD:
        	return Types.SMALLINT;
		case DataFieldMetadata.NUMERIC_FIELD:
			return Types.DOUBLE;
        default: 
        	return super.jetelType2sql(field);
		}
	}
	
	@Override
	public char sqlType2jetel(int sqlType) {
		switch (sqlType) {
		case Types.SMALLINT:
			return DataFieldMetadata.INTEGER_FIELD;
		default:
			return super.sqlType2jetel(sqlType);
		}
	}
	
	@Override
	public ResultSet getTables(java.sql.Connection connection, String dbName) throws SQLException {
		return connection.getMetaData().getTables(null, dbName, "%", new String[] {"TABLE", "VIEW" }/*tableTypes*/);
	}

	@Override
	public boolean isSchemaRequired() {
		return true;
	}
}
