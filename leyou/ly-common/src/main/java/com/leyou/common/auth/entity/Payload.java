package com.leyou.common.auth.entity;

import lombok.Data;

import java.util.Date;

/**
 * 载荷对象
 * @param <T>
 */
@Data
public class Payload<T> {

    private String id;
    private T userInfo;
    private Date expiration;
}
