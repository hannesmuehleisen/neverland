package nl.cwi.da.neverland.internal;

import java.io.Serializable;

public class NeverlandNode implements Serializable {

	@Override
	public String toString() {
		return "NeverlandNode [hostname=" + hostname + ", sessionId="
				+ sessionId + ", jdbcDriver=" + jdbcDriver + ", jdbcUrl="
				+ jdbcUrl + ", load=" + load + "]";
	}

	private static final long serialVersionUID = 3L;

	private String jdbcUrl;
	private String jdbcUser;
	private String jdbcPass;
	private String jdbcDriver;
	private String sessionId;
	private String hostname;
	private double load;

	public NeverlandNode(String hostname, String sessionId, String jdbcDriver,
			String jdbcUrl, String jdbcUser, String jdbcPass, double load) {
		this.jdbcDriver = jdbcDriver;
		this.jdbcUrl = jdbcUrl;
		this.jdbcUser = jdbcUser;
		this.jdbcPass = jdbcPass;
		this.sessionId = sessionId;
		this.load = load;
		this.hostname = hostname;
	}

	public String getId() {
		return sessionId;
	}

	public String getJdbcDriver() {
		return jdbcDriver;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public String getJdbcUser() {
		return jdbcUser;
	}

	public String getJdbcPass() {
		return jdbcPass;
	}

	public String getHostname() {
		return hostname;
	}

	public double getLoad() {
		return load;
	}

	public void setLoad(double systemLoadAverage) {
		this.load = systemLoadAverage;
	}

	public void setId(String sessionId2) {
		this.sessionId = sessionId2;
	}

	public void setHostname(String hostName2) {
		this.hostname = hostName2;

	}

}
