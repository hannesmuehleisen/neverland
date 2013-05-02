package nl.cwi.da.neverland.client;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NeverlandStatement implements Statement, PreparedStatement {

	private String sql = "";
	private NeverlandConnection conn;
	private ResultSet resultSet;

	public NeverlandStatement(NeverlandConnection conn) {
		this.conn = conn;
	}

	public NeverlandStatement(NeverlandConnection conn, String sql) {
		this.conn = conn;
		this.sql = sql;
	}

	private Map<Integer, String> parameters = new HashMap<Integer, String>();

	@Override
	public ResultSet executeQuery(String sql) throws SQLException {
		if (!conn.isValid(0)) {
			throw new SQLException("Connection invalid");
		}
		if (!sql.toUpperCase().trim().startsWith("SELECT")) {
			throw new SQLException("Only SELECT queries supported");
		}

		resultSet = new NeverlandResultSet(this, conn.request("EXEC " + sql));
		return resultSet;
	}

	@Override
	public int executeUpdate(String sql) throws SQLException {
		throw new SQLException("Unsupported, read-only connection");
	}

	@Override
	public void close() throws SQLException {
		// nop
	}

	@Override
	public int getMaxFieldSize() throws SQLException {
		return Integer.MAX_VALUE;
	}

	@Override
	public void setMaxFieldSize(int max) throws SQLException {
		throw new SQLException("Unsupported");
	}

	@Override
	public int getMaxRows() throws SQLException {
		return Integer.MAX_VALUE;
	}

	@Override
	public void setMaxRows(int max) throws SQLException {
		throw new SQLException(
				"Unsupported, use LIMIT in your SQL if you want to limit the result set size.");
	}

	@Override
	public void setEscapeProcessing(boolean enable) throws SQLException {
		// TODO check this

	}

	@Override
	public void cancel() throws SQLException {
		// TODO check this
	}

	@Override
	public int getQueryTimeout() throws SQLException {
		return Integer.MAX_VALUE;
	}

	@Override
	public void setQueryTimeout(int seconds) throws SQLException {
		throw new SQLException("Unsupported, we will wait for a loong time");

	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearWarnings() throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCursorName(String name) throws SQLException {
		throw new SQLException("Unsupported, no cursors");

	}

	@Override
	public boolean execute(String sql) throws SQLException {
		executeQuery(sql);
		return true;
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		if (resultSet == null) {
			throw new SQLException("No result set present");
		}
		return resultSet;
	}

	@Override
	public int getUpdateCount() throws SQLException {
		return 0;
	}

	@Override
	public boolean getMoreResults() throws SQLException {
		// we only have one result set
		return false;
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		if (direction != ResultSet.FETCH_FORWARD) {
			throw new SQLException("Only forward fetch supported.");
		}
	}

	@Override
	public int getFetchDirection() throws SQLException {
		return ResultSet.FETCH_FORWARD;
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		throw new SQLException("Setting fetch size is not supported.");
	}

	@Override
	public int getFetchSize() throws SQLException {
		return Integer.MAX_VALUE;
	}

	@Override
	public int getResultSetConcurrency() throws SQLException {
		return ResultSet.CONCUR_READ_ONLY;
	}

	@Override
	public int getResultSetType() throws SQLException {
		return ResultSet.TYPE_FORWARD_ONLY;
	}

	@Override
	public void addBatch(String sql) throws SQLException {
		throw new SQLException("Setting fetch size is not supported.");

	}

	@Override
	public void clearBatch() throws SQLException {
		throw new SQLException("Unsupported, no batches");
	}

	@Override
	public int[] executeBatch() throws SQLException {
		throw new SQLException("Unsupported, no batches");
	}

	@Override
	public Connection getConnection() throws SQLException {
		return conn;
	}

	@Override
	public boolean getMoreResults(int current) throws SQLException {
		close();
		return false;
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		throw new SQLException("Unsupported, read-only connection");
	}

	@Override
	public int executeUpdate(String sql, int autoGeneratedKeys)
			throws SQLException {
		throw new SQLException("Unsupported, read-only connection");
	}

	@Override
	public int executeUpdate(String sql, int[] columnIndexes)
			throws SQLException {
		throw new SQLException("Unsupported, read-only connection");
	}

	@Override
	public int executeUpdate(String sql, String[] columnNames)
			throws SQLException {
		throw new SQLException("Unsupported, read-only connection");
	}

	@Override
	public boolean execute(String sql, int autoGeneratedKeys)
			throws SQLException {
		return execute(sql);
	}

	@Override
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		return execute(sql);
	}

	@Override
	public boolean execute(String sql, String[] columnNames)
			throws SQLException {
		return execute(sql);
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		return ResultSet.HOLD_CURSORS_OVER_COMMIT; // whatever
	}

	@Override
	public boolean isClosed() throws SQLException {
		return false;
	}

	@Override
	public void setPoolable(boolean poolable) throws SQLException {
		throw new SQLException("Unsupported, no pooling");

	}

	@Override
	public boolean isPoolable() throws SQLException {
		throw new SQLException("Unsupported, no pooling");
	}

	@Override
	public ResultSet executeQuery() throws SQLException {
		if ("".equals(sql)) {
			throw new SQLException("No SQL set");
		}
		String replsql = bindParameters();
		return executeQuery(replsql);
	}

	private String bindParameters() {
		String boundSql = "";

		int i = 1;
		while (boundSql.contains("?") && parameters.containsKey(i)) {
			boundSql = boundSql.replaceFirst("?", parameters.get(i));
			i++;
		}

		return boundSql;
	}

	@Override
	public int executeUpdate() throws SQLException {
		throw new SQLException("Unsupported, read-only connection");
	}

	@Override
	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		parameters.put(parameterIndex, "NULL");

	}

	@Override
	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		parameters.put(parameterIndex, Boolean.toString(x));
	}

	@Override
	public void setByte(int parameterIndex, byte x) throws SQLException {
		parameters.put(parameterIndex, Byte.toString(x));
	}

	@Override
	public void setShort(int parameterIndex, short x) throws SQLException {
		parameters.put(parameterIndex, Short.toString(x));
	}

	@Override
	public void setInt(int parameterIndex, int x) throws SQLException {
		parameters.put(parameterIndex, Integer.toString(x));
	}

	@Override
	public void setLong(int parameterIndex, long x) throws SQLException {
		parameters.put(parameterIndex, Long.toString(x));
	}

	@Override
	public void setFloat(int parameterIndex, float x) throws SQLException {
		parameters.put(parameterIndex, Float.toString(x));
	}

	@Override
	public void setDouble(int parameterIndex, double x) throws SQLException {
		parameters.put(parameterIndex, Double.toString(x));
	}

	@Override
	public void setBigDecimal(int parameterIndex, BigDecimal x)
			throws SQLException {
		parameters.put(parameterIndex, x.toString());
	}

	private String quoteAndEscape(String s) {
		// TODO: escape "s in x, regex, later

		return "\"" + s + "\"";
	}

	@Override
	public void setString(int parameterIndex, String x) throws SQLException {
		parameters.put(parameterIndex, quoteAndEscape(x));

	}

	@Override
	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		throw new SQLException("Unsupported, no bytes");

	}

	@Override
	public void setDate(int parameterIndex, Date x) throws SQLException {
		parameters.put(parameterIndex, quoteAndEscape(x.toString()));
	}

	@Override
	public void setTime(int parameterIndex, Time x) throws SQLException {
		parameters.put(parameterIndex, quoteAndEscape(x.toString()));
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x)
			throws SQLException {
		parameters.put(parameterIndex, quoteAndEscape(x.toString()));
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		throw new SQLException("Unsupported, no streams");
	}

	@Override
	public void setUnicodeStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		throw new SQLException("Unsupported, no streams");
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		throw new SQLException("Unsupported, no streams");
	}

	@Override
	public void clearParameters() throws SQLException {
		parameters.clear();
	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType)
			throws SQLException {
		throw new SQLException("Unsupported, no objects");

	}

	@Override
	public void setObject(int parameterIndex, Object x) throws SQLException {
		throw new SQLException("Unsupported, no objects");
	}

	@Override
	public boolean execute() throws SQLException {
		executeQuery();
		return true;
	}

	@Override
	public void addBatch() throws SQLException {
		throw new SQLException("Unsupported, no batches");

	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, int length)
			throws SQLException {
		throw new SQLException("Unsupported, no streams");

	}

	@Override
	public void setRef(int parameterIndex, Ref x) throws SQLException {
		throw new SQLException("Unsupported, no refs");

	}

	@Override
	public void setBlob(int parameterIndex, Blob x) throws SQLException {
		throw new SQLException("Unsupported, no blobs");

	}

	@Override
	public void setClob(int parameterIndex, Clob x) throws SQLException {
		throw new SQLException("Unsupported, no clobs");
	}

	@Override
	public void setArray(int parameterIndex, Array x) throws SQLException {
		throw new SQLException("Unsupported, no arrays");
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		throw new SQLException(
				"Unsupported, we do not know result structure before execution.");

	}

	@Override
	public void setDate(int parameterIndex, Date x, Calendar cal)
			throws SQLException {

		// TODO ??

	}

	@Override
	public void setTime(int parameterIndex, Time x, Calendar cal)
			throws SQLException {
		// TODO ??

	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNull(int parameterIndex, int sqlType, String typeName)
			throws SQLException {
		parameters.put(parameterIndex, "NULL");

	}

	@Override
	public void setURL(int parameterIndex, URL x) throws SQLException {
		parameters.put(parameterIndex, quoteAndEscape(x.toString()));
	}

	@Override
	public ParameterMetaData getParameterMetaData() throws SQLException {
		// TODO do sth here
		return null;
	}

	@Override
	public void setRowId(int parameterIndex, RowId x) throws SQLException {
		throw new SQLException("Unsupported, no row ids");
	}

	@Override
	public void setNString(int parameterIndex, String value)
			throws SQLException {
		throw new SQLException("Unsupported, no N* types");
	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value,
			long length) throws SQLException {
		throw new SQLException("Unsupported, no N* types");
	}

	@Override
	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		throw new SQLException("Unsupported, no N* types");
	}

	@Override
	public void setClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		throw new SQLException("Unsupported, no clobs");
	}

	@Override
	public void setBlob(int parameterIndex, InputStream inputStream, long length)
			throws SQLException {
		throw new SQLException("Unsupported, no blobs");
	}

	@Override
	public void setNClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		throw new SQLException("Unsupported, no N* types");
	}

	@Override
	public void setSQLXML(int parameterIndex, SQLXML xmlObject)
			throws SQLException {
		throw new SQLException("Unsupported, no fricking XML");
	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType,
			int scaleOrLength) throws SQLException {
		throw new SQLException("Unsupported, no objects");
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		throw new SQLException("Unsupported, no streams");
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		throw new SQLException("Unsupported, no streams");
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader,
			long length) throws SQLException {
		throw new SQLException("Unsupported, no streams");
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x)
			throws SQLException {
		throw new SQLException("Unsupported, no streams");
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x)
			throws SQLException {
		throw new SQLException("Unsupported, no streams");
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader)
			throws SQLException {
		throw new SQLException("Unsupported, no streams");
	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value)
			throws SQLException {
		throw new SQLException("Unsupported, no streams");
	}

	@Override
	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		throw new SQLException("Unsupported, no clobs");

	}

	@Override
	public void setBlob(int parameterIndex, InputStream inputStream)
			throws SQLException {
		throw new SQLException("Unsupported, no blobs");
	}

	@Override
	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		throw new SQLException("Unsupported, no N* types");

	}

	@Override
	public void closeOnCompletion() throws SQLException {
		// nop
	}

	@Override
	public boolean isCloseOnCompletion() throws SQLException {
		// this result set does not hold much data and can simply be
		// garbage-collected.
		return true;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new SQLException("WTF");
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new SQLException("WTF");
	}

}
