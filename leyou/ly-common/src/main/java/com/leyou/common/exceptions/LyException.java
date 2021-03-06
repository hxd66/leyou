package com.leyou.common.exceptions;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Data;
import lombok.Getter;

@Getter
public class LyException extends RuntimeException{
    private int status;
    public LyException(ExceptionEnum em){
        super(em.getMessage());
        this.status = em.getStatus();
    }

    public LyException(ExceptionEnum em,String msg){
        super(msg);
        this.status = em.getStatus();
    }
}
