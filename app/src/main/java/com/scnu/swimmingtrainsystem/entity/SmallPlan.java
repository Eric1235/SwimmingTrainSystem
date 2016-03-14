package com.scnu.swimmingtrainsystem.entity;

public class SmallPlan {
	private int distance;
	private String pool;
	private String extra;
	private String pdate;
	private boolean reset;
	private int time;//游泳圈数

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public String getPool() {
		return pool;
	}

	public String getPdate() {
		return pdate;
	}

	public void setPdate(String pdate) {
		this.pdate = pdate;
	}

	public void setPool(String pool) {
		this.pool = pool;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public boolean isReset() {
		return reset;
	}

	public void setReset(boolean reset) {
		this.reset = reset;
	}

	@Override
	public String toString() {
		return "SmallPlan{" +
				"distance=" + distance +
				", pool='" + pool + '\'' +
				", extra='" + extra + '\'' +
				", pdate='" + pdate + '\'' +
				", reset=" + reset +
				", time=" + time +
				'}';
	}
}
