package com.leyou.user.controller;

import com.leyou.user.dto.AddressDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AddressController {
    @GetMapping
    public ResponseEntity<AddressDTO> queryAddressById(@RequestParam("userId")Long userId,
                                                       @RequestParam("id")Long id){
        AddressDTO address = new AddressDTO();
        address.setId(1L);
        address.setStreet("京低");
        address.setCity("北京");
        address.setDistrict("昌平区");
        address.setAddressee("小迪");
        address.setPhone("15800000000");
        address.setProvince("北京");
        address.setPostcode("100000");
        address.setIsDefault(true);
        return ResponseEntity.ok(address);
    }
}
