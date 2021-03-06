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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetel.connection.jdbc.specific.conn.MySQLConnection;
import org.jetel.database.sql.DBConnection;
import org.jetel.database.sql.SqlConnection;
import org.jetel.exception.JetelException;
import org.jetel.metadata.DataFieldMetadata;
import org.jetel.metadata.DataFieldType;
import org.jetel.util.string.StringUtils;

/**
 * My SQL specific behaviour.
 * 
 * @author Martin Zatopek (martin.zatopek@javlinconsulting.cz)
 *         (c) Javlin Consulting (www.javlinconsulting.cz)
 *
 * @created Jun 3, 2008
 */
public class MySQLSpecific extends AbstractJdbcSpecific {

	/** the SQL comments pattern specific for MySQL */
	private static final Pattern COMMENTS_PATTERN = Pattern.compile("(#|-- )[^\r\n]*|/\\*(?!!).*?\\*/", Pattern.DOTALL);

	private static final MySQLSpecific INSTANCE = new MySQLSpecific();
	
	public static MySQLSpecific getInstance() {
		return INSTANCE;
	}

	protected MySQLSpecific() {
		super();
	}

	@Override
	public AutoGeneratedKeysType getAutoKeyType() {
		return AutoGeneratedKeysType.SINGLE;
	}
	
	@Override
	public Pattern getCommentsPattern() {
		return COMMENTS_PATTERN;
	}
	
	@Override
	public boolean isBackslashEscaping() {
		return true;
	}

	@Override
	public SqlConnection createSQLConnection(DBConnection dbConnection, Connection connection, OperationType operationType) throws JetelException {
		return new MySQLConnection(dbConnection, connection, operationType);
	}

	/* (non-Javadoc)
	 * @see org.jetel.connection.jdbc.specific.impl.AbstractJdbcSpecific#optimizeResultSet(java.sql.ResultSet, org.jetel.connection.jdbc.specific.JdbcSpecific.OperationType)
	 */
	@Override
	public void optimizeResultSet(ResultSet res,OperationType operType){
		if (operType == OperationType.READ) {
			try {
				res.setFetchDirection(ResultSet.FETCH_FORWARD);
			}catch(SQLException ex){
				//TODO: for now, do nothing
			}
		}
	}

	@Override
    public String quoteIdentifier(String identifier) {
        return ("`" + identifier + "`");
    }

	@Override
	public String compileSelectQuery4Table(String schema, String owner, String table) {
    	if (isSchemaRequired() && !StringUtils.isEmpty(schema)) {
    		return "select * from `" + schema + "`.`" + table+"`";
    	} else {
    		return "select * from `" + table+"`";
    	}
	}

	@Override
	public String sqlType2str(int sqlType) {
		switch(sqlType) {
		case Types.TIMESTAMP :
			return "DATETIME";
		case Types.BOOLEAN :
			return "TINYINT";
		case Types.INTEGER :
			return "INT";
		case Types.NUMERIC :
			return "DOUBLE";
		}
		return super.sqlType2str(sqlType);
	}

	@Override
	public String jetelType2sqlDDL(DataFieldMetadata field) {
		switch (jetelType2sql(field)) {
		case Types.BOOLEAN:
		case Types.BIT:
			return "TINYINT(1)";
		case Types.DATE:
			if (field.hasFormat()) {
				Pattern p = Pattern.compile("[y]{1,4}");
				Matcher m = p.matcher(field.getFormat());
				if (m.matches()) {
					return "YEAR";
				}
			}
			return super.jetelType2sqlDDL(field);
		}
		return super.jetelType2sqlDDL(field);
	}
	
	@Override
	public int jetelType2sql(DataFieldMetadata field) {
		switch (field.getDataType()) {
		case BOOLEAN:
			return Types.BIT;
		case NUMBER:
			return Types.DOUBLE;
		default:
			return super.jetelType2sql(field);
		}
	}
	
	@Override
	public char sqlType2jetel(int sqlType) {
		switch (sqlType) {
		case Types.BIT:
			return DataFieldType.BOOLEAN.getShortName();
		default:
			return super.sqlType2jetel(sqlType);
		}
	}
    
	@Override
	public boolean isSchemaRequired() {
		return true;
	}
}
