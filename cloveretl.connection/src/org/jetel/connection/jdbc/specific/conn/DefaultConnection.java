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
package org.jetel.connection.jdbc.specific.conn;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetel.connection.jdbc.DBConnection;
import org.jetel.connection.jdbc.driver.JdbcDriver;
import org.jetel.connection.jdbc.specific.JdbcSpecific.AutoGeneratedKeysType;
import org.jetel.connection.jdbc.specific.JdbcSpecific.OperationType;
import org.jetel.exception.JetelException;

/**
 * 
 * Default adapter for common java.sql.Connection class
 * It is directly used by the DefaultJdbcSpecific or as a ascendant of other Connection implementation.
 * 
 * @author Martin Zatopek (martin.zatopek@javlinconsulting.cz)
 *         (c) Javlin Consulting (www.javlinconsulting.cz)
 *
 * @created May 29, 2008
 */
public class DefaultConnection implements Connection {

	protected final static Log logger = LogFactory.getLog(DefaultConnection.class);

	protected final static int DEFAULT_FETCH_SIZE = 50;

	protected Connection connection;
	
	protected OperationType operationType;
	
	protected AutoGeneratedKeysType autoGeneratedKeysType;
	
	public DefaultConnection(DBConnection dbConnection, OperationType operationType, AutoGeneratedKeysType autoGeneratedKeysType) throws JetelException {
		this(dbConnection, operationType, autoGeneratedKeysType, false);
	}
	
	public DefaultConnection(DBConnection dbConnection, OperationType operationType, AutoGeneratedKeysType autoGeneratedKeysType, boolean conservative) throws JetelException {
		this.operationType = operationType;
		this.autoGeneratedKeysType = autoGeneratedKeysType;
		this.connection = connect(dbConnection);
		
		if (!conservative) {
			optimizeConnection(operationType);
			
			try {
				if (dbConnection.getHoldability() != null) {
					setHoldability(dbConnection.getHoldability());
				}
				if (dbConnection.getTransactionIsolation() != null) {
					setTransactionIsolation(dbConnection.getTransactionIsolation());
				}
			} catch (SQLException e) {
				throw new JetelException("Wrong connection configuration.", e);
			}
		}
	}

	/**
	 * @return the comprised inner java.sql.Connection
	 */
	public Connection getInnerConnection() {
		return connection;
	}
	
	//*************** java.sql.Connection interface **************//
	/* (non-Javadoc)
	 * @see java.sql.Connection#clearWarnings()
	 */
	public void clearWarnings() throws SQLException {
		connection.clearWarnings();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#close()
	 */
	public void close() throws SQLException {
		connection.close();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#commit()
	 */
	public void commit() throws SQLException {
		connection.commit();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#createStatement()
	 */
	public Statement createStatement() throws SQLException {
		Statement statement;

		switch (operationType) {
		case READ:
			try {
				statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
			} catch (SQLException e) {
				logger.warn(e.getMessage());
				statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			}catch (UnsupportedOperationException e) {
				logger.warn(e.getMessage());
				statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			}
			break;
		default:
			statement = connection.createStatement();
		}
		
		return optimizeStatement(statement);
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#createStatement(int, int, int)
	 */
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return optimizeStatement(connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#createStatement(int, int)
	 */
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return optimizeStatement(connection.createStatement(resultSetType, resultSetConcurrency));
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#getAutoCommit()
	 */
	public boolean getAutoCommit() throws SQLException {
		return connection.getAutoCommit();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#getCatalog()
	 */
	public String getCatalog() throws SQLException {
		return connection.getCatalog();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#getHoldability()
	 */
	public int getHoldability() throws SQLException {
		return connection.getHoldability();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#getMetaData()
	 */
	public DatabaseMetaData getMetaData() throws SQLException {
		return connection.getMetaData();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#getTransactionIsolation()
	 */
	public int getTransactionIsolation() throws SQLException {
		return connection.getTransactionIsolation();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#getTypeMap()
	 */
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return connection.getTypeMap();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#getWarnings()
	 */
	public SQLWarning getWarnings() throws SQLException {
		return connection.getWarnings();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#isClosed()
	 */
	public boolean isClosed() throws SQLException {
		return connection.isClosed();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#isReadOnly()
	 */
	public boolean isReadOnly() throws SQLException {
		return connection.isReadOnly();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#nativeSQL(java.lang.String)
	 */
	public String nativeSQL(String sql) throws SQLException {
		return connection.nativeSQL(sql);
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
	 */
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return (CallableStatement) optimizeStatement(connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
	 */
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return (CallableStatement) optimizeStatement(connection.prepareCall(sql, resultSetType, resultSetConcurrency));
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareCall(java.lang.String)
	 */
	public CallableStatement prepareCall(String sql) throws SQLException {
		return (CallableStatement) optimizeStatement(connection.prepareCall(sql));
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int, int)
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return (PreparedStatement) optimizeStatement(connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return (PreparedStatement) optimizeStatement(connection.prepareStatement(sql, resultSetType, resultSetConcurrency));
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int)
	 */
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return (PreparedStatement) optimizeStatement(connection.prepareStatement(sql, autoGeneratedKeys));
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
	 */
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		PreparedStatement statement;
		if(autoGeneratedKeysType == AutoGeneratedKeysType.SINGLE) {
			if (columnIndexes != null) {
				statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			}else{
				logger.warn("Columns are null");
				logger.info("Getting generated keys switched off !");
				statement = connection.prepareStatement(sql);
			}
		}else{
			statement = connection.prepareStatement(sql, columnIndexes);
		}
		optimizeStatement(statement);
		return statement;
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String, java.lang.String[])
	 */
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		PreparedStatement statement;
		if(autoGeneratedKeysType == AutoGeneratedKeysType.SINGLE) {
			if (columnNames != null) {
				statement =  connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			}else{
				logger.warn("Columns are null");
				logger.info("Getting generated keys switched off !");
				statement =  connection.prepareStatement(sql);
			}
		}else{
			statement =  connection.prepareStatement(sql, columnNames);
		}
		optimizeStatement(statement);
		return statement;
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#prepareStatement(java.lang.String)
	 */
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		PreparedStatement statement;
		switch (operationType) {
		case READ:
			try {
				statement = connection.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
			} catch (SQLException e) {
				logger.warn(e.getMessage());
				statement = connection.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			}catch (UnsupportedOperationException e) {
				logger.warn(e.getMessage());
				statement = connection.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			}
			break;
		default:
			statement = connection.prepareStatement(sql);
		}
		optimizeStatement(statement);
		return statement;
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
	 */
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		connection.releaseSavepoint(savepoint);
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#rollback()
	 */
	public void rollback() throws SQLException {
		connection.rollback();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	public void rollback(Savepoint savepoint) throws SQLException {
		connection.rollback(savepoint);
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#setAutoCommit(boolean)
	 */
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		connection.setAutoCommit(autoCommit);
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#setCatalog(java.lang.String)
	 */
	public void setCatalog(String catalog) throws SQLException {
		connection.setCatalog(catalog);
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#setHoldability(int)
	 */
	public void setHoldability(int holdability) throws SQLException {
		connection.setHoldability(holdability);
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#setReadOnly(boolean)
	 */
	public void setReadOnly(boolean readOnly) throws SQLException {
		connection.setReadOnly(readOnly);
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#setSavepoint()
	 */
	public Savepoint setSavepoint() throws SQLException {
		return connection.setSavepoint();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#setSavepoint(java.lang.String)
	 */
	public Savepoint setSavepoint(String name) throws SQLException {
		return connection.setSavepoint(name);
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#setTransactionIsolation(int)
	 */
	public void setTransactionIsolation(int level) throws SQLException {
		connection.setTransactionIsolation(level);
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#setTypeMap(java.util.Map)
	 */
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		connection.setTypeMap(map);
	}

	//*************** END of java.sql.Connection INTERFACE ******************//
	
	/**
	 * This method optimizes all java.sql.Statements returned by this interface (createStatement(?)).
	 * @param statement
	 * @return
	 * @throws SQLException
	 */
	protected Statement optimizeStatement(Statement statement) throws SQLException {
		switch (operationType) {
			case READ:
				try{
					statement.setFetchDirection(ResultSet.FETCH_FORWARD);
				}catch(SQLException ex){
					//TODO: for now, do nothing;
				}
				break;
		}
		
		return statement;
	}

	/**
	 * Sets up a java.sql.Connection according given DBConnection object.
	 * @param dbConnection
	 * @return
	 * @throws JetelException
	 */
	protected Connection connect(DBConnection dbConnection) throws JetelException {
		JdbcDriver jdbcDriver = dbConnection.getJdbcDriver();
		Driver driver = jdbcDriver.getDriver();
		Connection connection;
		Properties connectionProperties = new Properties(jdbcDriver.getProperties());
		connectionProperties.putAll(dbConnection.createConnectionProperties());
		
        try {
            connection = driver.connect(dbConnection.getDbUrl(), connectionProperties);
        } catch (SQLException ex) {
            throw new JetelException("Can't connect to DB.", ex);
        }
        if (dbConnection == null) {
            throw new JetelException("Not suitable driver for specified DB URL (" + driver + " / " + dbConnection.getDbUrl());
        }
//        // try to set Transaction isolation level, it it was specified
//        if (config.containsKey(TRANSACTION_ISOLATION_PROPERTY_NAME)) {
//            int trLevel;
//            String isolationLevel = config.getProperty(TRANSACTION_ISOLATION_PROPERTY_NAME);
//            if (isolationLevel.equalsIgnoreCase("READ_UNCOMMITTED")) {
//                trLevel = Connection.TRANSACTION_READ_UNCOMMITTED;
//            } else if (isolationLevel.equalsIgnoreCase("READ_COMMITTED")) {
//                trLevel = Connection.TRANSACTION_READ_COMMITTED;
//            } else if (isolationLevel.equalsIgnoreCase("REPEATABLE_READ")) {
//                trLevel = Connection.TRANSACTION_REPEATABLE_READ;
//            } else if (isolationLevel.equalsIgnoreCase("SERIALIZABLE")) {
//                trLevel = Connection.TRANSACTION_SERIALIZABLE;
//            } else {
//                trLevel = Connection.TRANSACTION_NONE;
//            }
//            try {
//                connection.setTransactionIsolation(trLevel);
//            } catch (SQLException ex) {
//                // we do nothing, if anything goes wrong, we just
//                // leave whatever was the default
//            }
//        }
        // DEBUG logger.debug("DBConenction (" + getId() +") finishes connect function to the database at " + simpleDateFormat.format(new Date()));
        
        return connection;
	}

	/**
	 * Optimizes inner java.sql.Connection according given operation type.
	 * @param operationType
	 */
	protected void optimizeConnection(OperationType operationType) {
		switch (operationType) {
		case READ:
			try {
				connection.setAutoCommit(false);
				connection.setReadOnly(true);
				connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
				connection.setHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT);
			} catch (SQLException ex) {
				logger.warn("Optimizing connection failed: " + ex.getMessage());
				logger.warn("Try to use another jdbc specific");
			} catch (UnsupportedOperationException ex) {
				logger.warn("Optimizing connection failed: " + ex.getMessage());
				logger.warn("Try to use another jdbc specific");
			}
			break;
		case WRITE:
		case CALL:
			try {
				connection.setAutoCommit(false);
				connection.setReadOnly(false);
				connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
				connection.setHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT);
			} catch (SQLException ex) {
				logger.warn("Optimizing connection failed: " + ex.getMessage());
				logger.warn("Try to use another jdbc specific");
			} catch (UnsupportedOperationException ex) {
				logger.warn("Optimizing connection failed: " + ex.getMessage());
				logger.warn("Try to use another jdbc specific");
			}
			break;

		case TRANSACTION:
			try {
				connection.setAutoCommit(true);
				connection.setReadOnly(false);
				connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
				connection.setHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT);
			} catch (SQLException ex) {
				logger.warn("Optimizing connection failed: " + ex.getMessage());
				logger.warn("Try to use another jdbc specific");
			} catch (UnsupportedOperationException ex) {
				logger.warn("Optimizing connection failed: " + ex.getMessage());
				logger.warn("Try to use another jdbc specific");
			}
			break;
		}
	}

}
