package com.leyou.user.client;

import com.leyou.user.dto.AddressDTO;
import com.leyou.user.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("user-service")
public interface UserClient {
    /**
     * 根据用户名和密码查询用户
     */
    @GetMapping("query")
    UserDTO queryUserByUsernameAndPassword(
            @RequestParam("username")String username, @RequestParam("password")String password
    );

    /**
     * 根据
     * @param userId 用户id
     * @param id 地址id
     * @return 地址信息
     */
    @GetMapping("address")
    AddressDTO queryAddressById(@RequestParam("userId") Long userId, @RequestParam("id") Long id);
}
