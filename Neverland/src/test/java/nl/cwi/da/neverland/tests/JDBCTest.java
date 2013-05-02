package nl.cwi.da.neverland.tests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import nl.cwi.da.neverland.Coordinator;
import nl.cwi.da.neverland.Worker;
import nl.cwi.da.neverland.client.NeverlandDriver;
import nl.cwi.da.neverland.client.NeverlandResultSet;

import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class JDBCTest {

	private static Logger log = Logger.getLogger(JDBCTest.class);

	@Test
	public void testConnection() throws InterruptedException,
			ClassNotFoundException, SQLException {

		// bring up coordinator
		(new Thread() {
			public void run() {
				Coordinator.main(new String[0]);
			}
		}).start();

		// bring up a worker
		(new Thread() {
			public void run() {
				Worker.main(new String[0]);
			}
		}).start();

		Thread.sleep(5000);

		Class.forName("nl.cwi.da.neverland.client.NeverlandDriver");
		Connection conn = DriverManager
				.getConnection("jdbc:neverland://localhost:50002/db");

		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery("SELECT * FROM lineitem LIMIT 100");

	}

	@Test
	public void testResultSet() throws SQLException {
		Statement s = mock(Statement.class);
		ResultSet rs = new NeverlandResultSet(s,
				"l_orderkey\tl_extendedprice\tl_returnflag\n"
						+ "INTEGER\tDECIMAL\tVARCHAR(100)\n"
						+ "42\t42.2\t\"asdf\"\n" + "21\t21.1\t\"fdsa\"\n");

		rs.next();
		assertEquals(rs.getInt(1), 42);
		assertEquals(rs.getInt("l_orderkey"), 42);
		assertEquals(rs.getDouble(2), 42.2, 0.001);
		assertEquals(rs.getDouble("l_extendedprice"), 42.2, 0.001);
		assertEquals(rs.getString("l_returnflag"), "asdf");


		// check indices that are out of bounds
		try {
			rs.getInt(0);
			fail();
		} catch (Exception e) {
		}
		try {
			rs.getInt(-111);
			fail();
		} catch (Exception e) {
		}

		try {
			rs.getInt(4);
			fail();
		} catch (Exception e) {
		}

		// test non-existing names
		try {
			rs.getInt("foobar");
			fail();
		} catch (Exception e) {
		}

		// test result set metadata
		ResultSetMetaData rsm = rs.getMetaData();
		assertEquals(rsm.getColumnName(1), "l_orderkey");
		assertEquals(rsm.getColumnTypeName(1), "integer");
		assertEquals(rsm.getColumnType(1), 4);

		// test iteration
		rs.beforeFirst();
		int count = 0;
		while (rs.next()) {
			assertTrue(rs.getInt(1) == 42 || rs.getInt(1) == 21);
			count++;
		}
		assertEquals(count, 2);

		rs.close();
	}
}