package com.leyou.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyou.common.constants.RegexPatterns;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;

@TableName("tb_user")
@Data
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    @Pattern(regexp = RegexPatterns.USERNAME_REGEX,message = "用户名格式不正确")
    private String username;
    @Length(min = 4,max = 30,message = "密码格式不正确")
    private String password;
    @Pattern(regexp = RegexPatterns.PHONE_REGEX,message = "手机号格式不正确")
    private String phone;
    private Date createTime;
    private Date updateTime;
}
