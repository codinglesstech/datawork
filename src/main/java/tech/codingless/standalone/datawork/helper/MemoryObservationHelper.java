package tech.codingless.standalone.datawork.helper;

import org.openjdk.jol.info.GraphLayout;

import tech.codingless.standalone.datawork.util.BooleanUtil;

public class MemoryObservationHelper {

	private static ThreadLocal<Long> SIZE = new ThreadLocal<>();
	private static ThreadLocal<Boolean> ENABLED = new ThreadLocal<>();

	/**
	 * 初始化内存统计代码
	 */
	public static void init() {
		SIZE.set(0L);
		ENABLED.set(true);
	}

	public static void clear() {
		SIZE.set(0L);
		ENABLED.set(false);
	}

	public static void increment(Object o) {
		if (o == null) {
			return;
		}
		if (BooleanUtil.isNotTrue(ENABLED.get())) {
			return;
		}
		Long totalSize = SIZE.get();
		if (totalSize == null) {
			SIZE.set(GraphLayout.parseInstance(o).totalSize());
		} else {
			SIZE.set(GraphLayout.parseInstance(o).totalSize() + totalSize);
		}
	}

	public static long size() {
		Long sz = SIZE.get();
		return sz == null ? 0 : sz;
	}
}
