package tech.codingless.standalone.datawork.v1.data;

import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Data;

@Data
public class JobStat {
	private static SimpleDateFormat SDF = new SimpleDateFormat("MM.dd HH:mm:ss");
	private int success;
	private int fail;
	private String gmtLastSuccess;
	private String gmtLastFail;
	private long successAvgCost;
	private long successMaxCost;
	private long successMinCost;
	private long failAvgCost;
	private long failMaxCost;
	private long failMinCost;
	// 是否处于运行当中, 对于JOB类型的有意义
	private boolean running;

	public void success(long cost) {
		this.success++;
		if (cost > this.successMaxCost) {
			this.successMaxCost = cost;
		}
		if (this.successMinCost <= 0 || cost < this.successMinCost) {
			this.successMinCost = cost;
		}
		this.successAvgCost = (this.successAvgCost * this.success + cost) / this.success;
		this.gmtLastSuccess = SDF.format(new Date());
	}

	public void fail(long cost) {
		this.fail++;
		if (cost > this.failMaxCost) {
			this.failMaxCost = cost;
		}
		if (this.failMinCost <= 0 || cost < this.failMinCost) {
			this.failMinCost = cost;
		}
		this.failAvgCost = (this.failAvgCost * this.fail + cost) / this.fail;
		this.gmtLastFail = SDF.format(new Date());

	}
}
