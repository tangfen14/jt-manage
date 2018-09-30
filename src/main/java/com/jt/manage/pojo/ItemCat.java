package com.jt.manage.pojo;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.jt.common.po.BasePojo;

@Table(name="tb_item_cat") //类和表的映射
public class ItemCat extends BasePojo{
	@Id	//主键，自增策略
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	//都用包装类
	private Long id;   //Long对应bigint
	private Long parentId;//Long对应bigint
	private String name;
	private Integer status;
	private Integer sortOrder;
	private Boolean isParent;//Boolean对应tinyint(1)
/*	create table tb_item_cat
	(
	   id                   bigint not null auto_increment,
	   parent_id            bigint comment '父ID=0时，代表一级类目',
	   name                 varchar(150),
	   status               int(1) default 1 comment '默认值为1，可选值：1正常，2删除',
	   sort_order           int(4) not null,
	   is_parent            tinyint(1),
	   created              datetime,
	   updated              datetime,
	   primary key (id)
	);*/

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getParentId() {
		return parentId;
	}
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getSortOrder() {
		return sortOrder;
	}
	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}
	public Boolean getIsParent() {
		return isParent;
	}
	public void setIsParent(Boolean isParent) {
		this.isParent = isParent;
	}
	
}
