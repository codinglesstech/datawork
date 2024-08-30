package tech.codingless.standalone.datawork.util;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class SnowFlakeNumberUtil {

	// 2022-12-26 00:00:00 业务开始时间
	private static final long BEGIN_TIME_STAMP = 1671984000000L;
	private static AtomicInteger count = new AtomicInteger();
	private static long PRE_MILLIS = 0;
	private static long NODE = random(0, 1020);
	private static ReentrantLock LOCK = new ReentrantLock();

	public static long getNode() {
		return NODE;
	}

	public static long setNode(long node) {
		if (node > 0 && node < 1020) {
			NODE = node;
		}
		return NODE;
	}

	public static int random(int min, int max) {
		Random random = new Random();
		return random.nextInt(min + max) - min;
	}

	/**
	 * <pre>
	 * 通过雪花算法获得ID
	 * </pre>
	 * 
	 * 63Bit long value 高41比特为时间毫秒 + 中间 10bit为服务 + 尾 12bit为自增数
	 * 
	 * @return
	 */
	public static long nextId() {
		long t = System.currentTimeMillis();
		if (t != PRE_MILLIS) {
			try {
				LOCK.lock();
				if (t != PRE_MILLIS) {
					count.set(0);
					PRE_MILLIS = t;

					long id = (t - BEGIN_TIME_STAMP) << 22;
					id += (NODE << 12);
					id += count.incrementAndGet();
					return id;
				}
			} finally {
				LOCK.unlock();
			}
		}
		long id = (t - BEGIN_TIME_STAMP) << 22;
		id += (NODE << 12);
		id += count.incrementAndGet();
		return id;

	}

	private static AtomicInteger partitionCount = new AtomicInteger();
	private static ReentrantLock partitionLOCK = new ReentrantLock();
	private static long PATITION_PRE_MILLIS = 0;

	/**
	 * <pre>
	 * 雪花算法的一个变种，控制尾数0~9分区
	 * </pre>
	 * 
	 * 63Bit long value 高41比特为时间毫秒 + 中间 10bit为服务 + 尾 12bit为自增数
	 * 
	 * @param partition
	 * @return
	 */
	public static long nextId(long partition) {
		long t = System.currentTimeMillis();

		try {
			partitionLOCK.lock();
			if (t != PATITION_PRE_MILLIS) {
				partitionCount.set(0);
				PATITION_PRE_MILLIS = t;
			}
			long id = (t - BEGIN_TIME_STAMP) << 22;
			id += (NODE << 12);
			id += (partitionCount.incrementAndGet());

			long toPatition = partition % 10;
			long currentPatition = id % 10;
			if (toPatition == currentPatition) {
				return id;
			}
			if (toPatition < currentPatition) {
				toPatition += 10;
			}
			long diff = toPatition - currentPatition;
			partitionCount.addAndGet((int) diff);
			id += diff;
			return id;

		} finally {
			partitionLOCK.unlock();
		}

	}

}
