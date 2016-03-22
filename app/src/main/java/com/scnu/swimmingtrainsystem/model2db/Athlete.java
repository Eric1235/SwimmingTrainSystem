package com.scnu.swimmingtrainsystem.model2db;

import org.litepal.crud.DataSupport;

/**
 * 运动员实体类
 * 
 * @author LittleByte
 * orm数据库映射类
 * 
 */

public class Athlete extends DataSupport {

	/**
	 * 运动员id
	 */
	private int id;

	private int aid;

	private int uid;

	/**
	 * 运动员名字
	 */
	private String name;
	/**
	 * 运动员年龄
	 */
	private int age;
	/**
	 * 运动员性别
	 */
	private String gender;
	/**
	 * 运动员电话
	 */
	private String phone;

	/**
	 * 运动员证件编号
	 */
	private String number;
	
	/**
	 * 运动员备注
	 */
	private String extras;
	/**
	 * 运动员所属的教练
	 */


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAid() {
		return aid;
	}

	public void setAid(int aid) {
		this.aid = aid;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getExtras() {
		return extras;
	}

	public void setExtras(String extras) {
		this.extras = extras;
	}

	@Override
	public String toString() {
		return "Athlete{" +
				"id=" + id +
				", aid=" + aid +
				", name='" + name + '\'' +
				", age=" + age +
				", gender='" + gender + '\'' +
				", phone='" + phone + '\'' +
				", number='" + number + '\'' +
				", extras='" + extras + '\'' +
				'}';
	}
}
