package com.leyou.item.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tb_category")
public class Category {
	@TableId(type = IdType.AUTO)
	private Long id;
	private String name;
	private Long parentId;
	private Boolean isParent;
	private Integer sort;
	private Date createTime;
	private Date updateTime;
}