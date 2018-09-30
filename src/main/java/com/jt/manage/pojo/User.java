package com.jt.manage.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

//通用Mapper实现不用写SQL语句，需要JPA支持
@Table(name="user")	//类和表创建关联映射
public class User implements Serializable{
	
	@Id		//主键，自增，strategy策略：IDENTITY自增
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
//	@Column(name="user_name")	一般也不用配置，可以设置一个mybatis全局的配置形成自动映射，驼峰规则
//	private String userName;
	
	private String name;
	private Date birthday;
	private String address;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getBirthday() {
		return birthday;
	}
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
}
