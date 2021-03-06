package nl.cwi.da.neverland.client;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import nl.cwi.da.neverland.csv.CSVEntry;
import nl.cwi.da.neverland.csv.TestResults;
import nl.cwi.da.neverland.internal.Constants;
import nl.cwi.da.neverland.internal.SSBM;
import nl.cwi.da.neverland.internal.StatisticalDescription;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;

public class NeverlandTestDriver {

	private static class ThreadResult {
		private Map<String, StatisticalDescription> timings;
		private double totalDuration;
		private long queriesRun;
		private double qps;
	}

	public static class QueryResult extends TestResults {
		@CSVEntry
		public Integer queryset;
		@CSVEntry
		public String query;
		@CSVEntry
		public Double time;
	}

	public static void main(String[] args) throws JSAPException {

		JSAP jsap = new JSAP();

		jsap.registerParameter(new FlaggedOption("host").setShortFlag('h')
				.setLongFlag("host").setStringParser(JSAP.STRING_PARSER)
				.setRequired(false).setDefault("localhost")
				.setHelp("Hostname of the Neverland Coordinator"));

		jsap.registerParameter(new FlaggedOption("csv").setShortFlag('c')
				.setLongFlag("csv").setStringParser(JSAP.STRING_PARSER)
				.setRequired(false).setDefault("/dev/null")
				.setHelp("CSV File to write results to"));

		jsap.registerParameter(new FlaggedOption("port").setShortFlag('p')
				.setLongFlag("port").setStringParser(JSAP.INTEGER_PARSER)
				.setRequired(false)
				.setDefault(Integer.toString(Constants.JDBC_PORT))
				.setHelp("JDBC port on the Neverland Coordinator"));

		jsap.registerParameter(new FlaggedOption("threads").setShortFlag('t')
				.setLongFlag("threads").setStringParser(JSAP.INTEGER_PARSER)
				.setRequired(false).setDefault("1")
				.setHelp("Number of threads"));

		jsap.registerParameter(new FlaggedOption("warmup").setShortFlag('w')
				.setLongFlag("warmup").setStringParser(JSAP.INTEGER_PARSER)
				.setRequired(false).setDefault("0")
				.setHelp("Number of warmup runs through the query set"));

		jsap.registerParameter(new FlaggedOption("runs").setShortFlag('r')
				.setLongFlag("runs").setStringParser(JSAP.INTEGER_PARSER)
				.setRequired(false).setDefault("1")
				.setHelp("Number of test runs through the query set"));

		jsap.registerParameter(new FlaggedOption("set").setShortFlag('s')
				.setLongFlag("set").setStringParser(JSAP.STRING_PARSER)
				.setRequired(false).setDefault("SSBM")
				.setHelp("Test query set, can be SSBM or SSBM-FAST"));

		JSAPResult res = jsap.parse(args);

		if (!res.success()) {
			@SuppressWarnings("rawtypes")
			Iterator errs = res.getErrorMessageIterator();
			while (errs.hasNext()) {
				System.err.println(errs.next());
			}

			System.err.println("Usage: " + jsap.getUsage() + "\nParameters: "
					+ jsap.getHelp());
			System.exit(-1);
		}

		try {
			Class.forName("nl.cwi.da.neverland.client.NeverlandDriver");
		} catch (ClassNotFoundException e) {
			System.exit(-1);
		}

		final String jdbc = "jdbc:neverland://" + res.getString("host") + ":"
				+ res.getInt("port") + "/db";
		final int warmup = res.getInt("warmup");
		final int runs = res.getInt("runs");
		final int threads = res.getInt("threads");

		final File csvFile = new File(res.getString("csv"));
		new QueryResult().writeHeader(csvFile);

		System.out.println(NeverlandTestDriver.class.getSimpleName() + " "
				+ threads + " thread(s)");

		List<Future<ThreadResult>> resultStats = new ArrayList<Future<ThreadResult>>();

		ExecutorService executorService = Executors.newFixedThreadPool(res
				.getInt("threads"));

		final Map<String, String> tq = SSBM.QUERIES;
		if (res.getString("set").toLowerCase().equals("ssbm-fast")) {
			tq.remove("Q07");
			tq.remove("Q11");
			tq.remove("Q12");
			tq.remove("Q13");
		}

		for (int i = 0; i < threads; i++) {
			resultStats.add(executorService
					.submit(new Callable<ThreadResult>() {
						@Override
						public ThreadResult call() throws Exception {
							Map<String, StatisticalDescription> timings = new HashMap<String, StatisticalDescription>();

							Connection c = DriverManager.getConnection(jdbc);
							Statement s = c.createStatement();
							for (int i = 0; i < warmup; i++) {
								System.out.println("Running warmup set "
										+ (i + 1) + " of " + warmup);
								for (Entry<String, String> e : tq.entrySet()) {
									double time = executeQuery(e.getValue(), s);
									System.out.println(e.getKey() + ": " + time);
								}
							}
							long start = System.currentTimeMillis();

							for (int i = 0; i < runs; i++) {
								System.out.println("Running test set "
										+ (i + 1) + " of " + runs);
								for (Entry<String, String> e : tq.entrySet()) {
									if (!timings.containsKey(e.getKey())) {
										timings.put(e.getKey(),
												new StatisticalDescription());
									}
									double time = executeQuery(e.getValue(), s);
									timings.get(e.getKey()).addValue(time);
									System.out.println(e.getKey() + ": " + time);

									QueryResult qr = new QueryResult();
									qr.query = e.getKey();
									qr.queryset = i;
									qr.time = time;
									qr.writeToFile(csvFile);
								}
							}

							s.close();
							c.close();

							double durationSecs = (System.currentTimeMillis() - start) / 1000.0;

							ThreadResult tr = new ThreadResult();
							tr.queriesRun = runs * SSBM.QUERIES.size();
							tr.totalDuration = durationSecs;
							tr.timings = timings;
							tr.qps = tr.queriesRun / tr.totalDuration;
							return tr;
						}

						private double executeQuery(String query, Statement s)
								throws SQLException {
							long start = System.currentTimeMillis();
							s.executeQuery(query);
							return (System.currentTimeMillis() - start) / 1000.0;
						}
					}));

		}
		executorService.shutdown();

		double totalQps = 0;
		Map<String, StatisticalDescription> timings = new TreeMap<String, StatisticalDescription>();

		for (Future<ThreadResult> rf : resultStats) {
			try {
				ThreadResult d = rf.get();
				for (Entry<String, String> e : SSBM.QUERIES.entrySet()) {
					if (!timings.containsKey(e.getKey())) {
						timings.put(e.getKey(), new StatisticalDescription());
					}
					timings.get(e.getKey()).merge(d.timings.get(e.getKey()));
				}
				totalQps += d.qps;

			} catch (Exception e) {
				e.printStackTrace(System.err);
			}

		}

		for (Entry<String, StatisticalDescription> e : timings.entrySet()) {
			System.out.println(e.getKey() + ":\t" + e.getValue());
		}

		System.out.println("QPS: " + totalQps);

	}
}
