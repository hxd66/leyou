package com.leyou.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static com.leyou.common.constants.MQConstants.Exchange.SMS_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.RoutingKey.VERIFY_CODE_KEY;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SmsTest {
    @Autowired
    AmqpTemplate amqpTemplate;

    @Test
    public void testSendMessage(){
        Map<String, String> map = new HashMap<>();
        map.put("phone","13663592309");
        map.put("code","856495");
        amqpTemplate.convertAndSend(SMS_EXCHANGE_NAME,VERIFY_CODE_KEY,map);

    }
}
