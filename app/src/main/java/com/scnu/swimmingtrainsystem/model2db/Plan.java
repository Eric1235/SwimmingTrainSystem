package com.scnu.swimmingtrainsystem.model2db;

import org.litepal.crud.DataSupport;

/**
 * 计划实体类
 * 
 * @author LittleByte
 * 
 */

public class Plan extends DataSupport {
	private long id;
	private int pid;

	private boolean reset;

	private int interval;

	/**
	 * 游泳的趟数
	 */
	private int time;

	/**
	 * 泳池大小
	 */
	private String pool;

	/**
	 * 该计划预计游泳进行的总距离
	 */
	private int distance;

	private String pdate;

	/**
	 * 该计划的备注，方便查阅并区分成绩
	 */
	private String extra;

	private int uid;

	@Override
	public String toString() {
		return "Plan{" +
				"id=" + id +
				", pid=" + pid +
				", reset=" + reset +
				", interval=" + interval +
				", time=" + time +
				", pool='" + pool + '\'' +
				", distance=" + distance +
				", pdate='" + pdate + '\'' +
				", extra='" + extra + '\'' +
				", uid=" + uid +
				'}';
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public boolean isReset() {
		return reset;
	}

	public void setReset(boolean reset) {
		this.reset = reset;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public String getPool() {
		return pool;
	}

	public void setPool(String pool) {
		this.pool = pool;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public String getPdate() {
		return pdate;
	}

	public void setPdate(String pdate) {
		this.pdate = pdate;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}
}
