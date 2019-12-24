package com.leyou.common.threadlocals;

/**
 * 存储用户数据
 */
public class UserHolder {
    private static final ThreadLocal<String> TL = new ThreadLocal<>();

    public static void setUser(String userId){
        TL.set(userId);
    }

    public static String getUserId(){
        return TL.get();
    }

    public static void removeUserId(){
        TL.remove();
    }
}
