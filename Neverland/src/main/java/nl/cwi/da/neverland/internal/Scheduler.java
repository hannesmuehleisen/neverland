package nl.cwi.da.neverland.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class Scheduler {

	public static class SubquerySchedule extends
			HashMap<NeverlandNode, List<Subquery>> {
		private static final long serialVersionUID = 1L;
		private Query q;

		public SubquerySchedule(Query q) {
			this.q = q;
		}

		public Query getQuery() {
			return q;
		}

		public long getTimeoutMs() {
			// TODO : fixme
			return 3600 * 1000;
		}

		public SubquerySchedule schedule(NeverlandNode n, Subquery q) {
			if (!containsKey(n)) {
				put(n, new ArrayList<Subquery>());
			}
			get(n).add(q);
			return this;
		}

		public SubquerySchedule reschedule(NeverlandNode oldNode, Subquery q,
				NeverlandNode newNode) throws NeverlandException {
			if (oldNode == null || q == null || newNode == null) {
				throw new NeverlandException("null arguments, me no likey");
			}
			if (!containsKey(oldNode) || get(oldNode) == null) {
				throw new NeverlandException(oldNode
						+ "is not part of schedule " + toString());
			}
			if (!get(oldNode).contains(q)) {
				throw new NeverlandException(q
						+ " is not part of schedule for " + oldNode);
			}
			get(oldNode).remove(q);
			return schedule(newNode, q);
		}

		public NeverlandNode getNode(Subquery sq) {
			if (sq == null) {
				return null;
			}
			for (Map.Entry<NeverlandNode, List<Subquery>> e : entrySet()) {
				for (Subquery asq : e.getValue()) {
					if (sq.equals(asq)) {
						return e.getKey();
					}
				}
			}
			return null;
		}

		public long numSubqueries() {
			long sq = 0;
			for (Map.Entry<NeverlandNode, List<Subquery>> e : entrySet()) {
				sq += e.getValue().size();
			}
			return sq;
		}
	}

	public abstract SubquerySchedule schedule(Query q, List<NeverlandNode> nodes)
			throws NeverlandException;

	public static class StupidScheduler extends Scheduler {
		@Override
		public SubquerySchedule schedule(Query q, List<NeverlandNode> nodes)
				throws NeverlandException {

			if (nodes.size() < 1) {
				throw new NeverlandException(
						"Need at least one node to schedule queries to");
			}

			if (q.getSubqueries().size() < 1) {
				throw new NeverlandException(
						"Need at least one subquery to schedule");
			}

			NeverlandNode n1 = nodes.get(0);
			SubquerySchedule schedule = new SubquerySchedule(q);
			schedule.put(n1, q.getSubqueries());
			return schedule;
		}
	}

	public static class RoundRobinScheduler extends Scheduler {

		private int lastNodeIndex = 0;

		@Override
		public SubquerySchedule schedule(Query q, List<NeverlandNode> nodes)
				throws NeverlandException {
			SubquerySchedule schedule = new SubquerySchedule(q);

			for (Subquery sq : q.getSubqueries()) {
				lastNodeIndex = (lastNodeIndex + 1) % nodes.size();
				schedule.schedule(nodes.get(lastNodeIndex), sq);
			}
			return schedule;
		}

	}

	public static class StickyLoadBalancingScheduler extends Scheduler {

		@Override
		public SubquerySchedule schedule(Query q, List<NeverlandNode> nodes)
				throws NeverlandException {
			SubquerySchedule schedule = new SubquerySchedule(q);

			for (Subquery sq : q.getSubqueries()) {
				NeverlandNode n1 = nodes.get(sq.getSlice() % nodes.size());
				NeverlandNode n2 = nodes.get(sq.getSlice() + 1 % nodes.size());

				if (n1.getLoad() < n2.getLoad()) {
					schedule.schedule(n1, sq);
				} else {
					schedule.schedule(n2, sq);
				}

			}
			return schedule;
		}
	}

	public static class LoadBalancingScheduler extends Scheduler {

		private Random rnd = new Random(System.currentTimeMillis());

		@Override
		public SubquerySchedule schedule(Query q, List<NeverlandNode> nodes)
				throws NeverlandException {
			SubquerySchedule schedule = new SubquerySchedule(q);

			for (Subquery sq : q.getSubqueries()) {
				NeverlandNode n1 = nodes.get(rnd.nextInt(nodes.size()));
				NeverlandNode n2 = nodes.get(rnd.nextInt(nodes.size()));
				if (n1.getLoad() < n2.getLoad()) {
					schedule.schedule(n1, sq);
				} else {
					schedule.schedule(n2, sq);
				}
			}
			return schedule;
		}

	}

	public static class RandomScheduler extends Scheduler {

		private Random rnd = new Random(System.currentTimeMillis());

		@Override
		public SubquerySchedule schedule(Query q, List<NeverlandNode> nodes)
				throws NeverlandException {
			SubquerySchedule schedule = new SubquerySchedule(q);

			for (Subquery sq : q.getSubqueries()) {
				NeverlandNode n1 = nodes.get(rnd.nextInt(nodes.size()));
				schedule.schedule(n1, sq);
			}
			return schedule;
		}
	}

	public static class StickyScheduler extends Scheduler {

		@Override
		public SubquerySchedule schedule(Query q, List<NeverlandNode> nodes)
				throws NeverlandException {
			SubquerySchedule schedule = new SubquerySchedule(q);
			for (Subquery sq : q.getSubqueries()) {
				int node = sq.getSlice() % nodes.size();
				schedule.schedule(nodes.get(node), sq);
			}
			return schedule;
		}

	}
}
