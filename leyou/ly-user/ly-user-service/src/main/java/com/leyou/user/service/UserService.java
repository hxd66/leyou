package com.leyou.user.service;

import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.User;

public interface UserService {

    Boolean checkData(String param, Integer type);

    void sendCode(String phone);

    void register(User user, String code);

    UserDTO queryUserByUsernameAndPassword(String username, String password);

}
