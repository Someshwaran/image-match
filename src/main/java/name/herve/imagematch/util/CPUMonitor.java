/*
 * Copyright 2013 Nicolas HERVE.
 * 
 * This file is part of image-match
 * 
 * image-match is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * image-match is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with image-match. If not, see <http://www.gnu.org/licenses/>.
 */
package name.herve.imagematch.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Nicolas HERVE - n.herve@laposte.net
 */
public class CPUMonitor {
	private class CPUTime {
		private long startTime;
		private long stopTime;
		private long startUserTime;
		private long startCPUTime;
		private long stopUserTime;
		private long stopCPUTime;

		public CPUTime() {
			super();

			startUserTime = 0;
			startCPUTime = 0;
			stopUserTime = 0;
			stopCPUTime = 0;
			startTime = 0;
			stopTime = 0;
		}

		public long getCPUElapsedTimeNano() {
			return stopCPUTime - startCPUTime;
		}

		public long getElapsedTimeMilli() {
			return stopTime - startTime;
		}

		public long getStartUserTime() {
			return startUserTime;
		}

		public long getStopUserTime() {
			return stopUserTime;
		}

		public long getUserElapsedTimeNano() {
			return stopUserTime - startUserTime;
		}

		public void setStartCPUTime(long startCPUTime) {
			this.startCPUTime = startCPUTime;
			setStopCPUTime(startCPUTime);
		}

		public void setStartTime(long startTime) {
			this.startTime = startTime;
			setStopTime(startTime);
		}

		public void setStartUserTime(long startUserTime) {
			this.startUserTime = startUserTime;
			setStopUserTime(startUserTime);
		}

		public void setStopCPUTime(long stopCPUTime) {
			this.stopCPUTime = stopCPUTime;
		}

		public void setStopTime(long stopTime) {
			this.stopTime = stopTime;
		}

		public void setStopUserTime(long stopUserTime) {
			this.stopUserTime = stopUserTime;
		}
	}

	public final static int MONITOR_CURRENT_THREAD = 0;
	// public final static int MONITOR_ALL_THREAD_ROUGHLY = 1;
	public final static int MONITOR_ALL_THREAD_FINELY = 2;

	private static final double NANO_TO_MILLI = 1d / 1000000d;
	private static final double MILLI_TO_SEC = 1d / 1000d;
	private static final double NANO_TO_SEC = NANO_TO_MILLI * MILLI_TO_SEC;

	public static int getAvailableProcessors() {
		return Runtime.getRuntime().availableProcessors();
	}

	private CPUTime time;

	private Map<Long, CPUTime> threadTimes;

	private ThreadMXBean bean;
	// private OperatingSystemMXBean osBean;

	private int monitorType;

	public CPUMonitor() {
		this(MONITOR_CURRENT_THREAD);
	}

	public CPUMonitor(int type) {
		super();

		this.monitorType = type;

		time = new CPUTime();

		bean = ManagementFactory.getThreadMXBean();
		// osBean = ManagementFactory.getOperatingSystemMXBean();
	}

	public long getCPUElapsedTimeMilli() {
		return nanoToMilli(time.getCPUElapsedTimeNano());
	}

	public double getCPUElapsedTimeSec() {
		return nanoToSec(time.getCPUElapsedTimeNano());
	}

	public long getElapsedTimeMilli() {
		return time.getElapsedTimeMilli();
	}

	public double getElapsedTimeSec() {
		return milliToSec(time.getElapsedTimeMilli());
	}

	public int getThreadCount() {
		return bean.getThreadCount();
	}

	public long getUserElapsedTimeMilli() {
		return nanoToMilli(time.getUserElapsedTimeNano());
	}

	public double getUserElapsedTimeSec() {
		return nanoToSec(time.getUserElapsedTimeNano());
	}

	private double milliToSec(long milli) {
		return milli * MILLI_TO_SEC;
	}

	private long nanoToMilli(long nano) {
		return Math.round(nano * NANO_TO_MILLI);
	}

	private double nanoToSec(long nano) {
		return nano * NANO_TO_SEC;
	}

	public void start() throws IllegalAccessError {
		if (!bean.isCurrentThreadCpuTimeSupported()) {
			throw new IllegalAccessError("This JVM does not support time benchmarking");
		}

		switch (monitorType) {
		case MONITOR_CURRENT_THREAD:
			time.setStartUserTime(bean.getCurrentThreadUserTime());
			time.setStartCPUTime(bean.getCurrentThreadCpuTime());
			break;
		// case MONITOR_ALL_THREAD_ROUGHLY:
		// if (!(osBean instanceof com.sun.management.OperatingSystemMXBean))
		// {
		// throw new IllegalAccessError(
		// "This JVM does not support this version of multiple threads time benchmarking");
		// }
		//
		// time.setStartUserTime(((com.sun.management.OperatingSystemMXBean)
		// osBean).getProcessCpuTime());
		// time.setStartCPUTime(time.getStartUserTime());
		// break;
		case MONITOR_ALL_THREAD_FINELY:
			threadTimes = new HashMap<Long, CPUTime>();
			time.setStartUserTime(0);
			time.setStartCPUTime(0);
			long[] tids = bean.getAllThreadIds();
			for (long id : tids) {
				CPUTime cput = new CPUTime();
				cput.setStartCPUTime(bean.getThreadCpuTime(id));
				cput.setStartUserTime(bean.getThreadUserTime(id));
				threadTimes.put(id, cput);
			}

			break;
		}

		time.setStartTime(System.currentTimeMillis());
	}

	public void stop() {
		switch (monitorType) {
		case MONITOR_CURRENT_THREAD:
			time.setStopUserTime(bean.getCurrentThreadUserTime());
			time.setStopCPUTime(bean.getCurrentThreadCpuTime());
			break;
		// case MONITOR_ALL_THREAD_ROUGHLY:
		// time.setStopUserTime(((com.sun.management.OperatingSystemMXBean)
		// osBean).getProcessCpuTime());
		// time.setStopCPUTime(time.getStopUserTime());
		// break;
		case MONITOR_ALL_THREAD_FINELY:
			// Ignores threads that died during the monitoring
			long[] tids = bean.getAllThreadIds();
			long c = 0;
			long u = 0;
			for (long id : tids) {
				CPUTime cput = threadTimes.get(id);
				if (cput == null) {
					cput = new CPUTime();
				}
				cput.setStopCPUTime(bean.getThreadCpuTime(id));
				cput.setStopUserTime(bean.getThreadUserTime(id));

				c += cput.getCPUElapsedTimeNano();
				u += cput.getUserElapsedTimeNano();
			}
			time.setStopCPUTime(c);
			time.setStopUserTime(u);
			break;
		}
		time.setStopTime(System.currentTimeMillis());
	}
}
