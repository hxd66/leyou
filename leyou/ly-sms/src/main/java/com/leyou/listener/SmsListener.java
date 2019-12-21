package com.leyou.listener;

import com.leyou.sms.utils.SmsUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.leyou.common.constants.MQConstants.Exchange.SMS_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.Queue.SMS_VERIFY_CODE_QUEUE;
import static com.leyou.common.constants.MQConstants.RoutingKey.VERIFY_CODE_KEY;

@Component
@Slf4j
public class SmsListener {

    @Autowired
    private SmsUtils smsUtils;
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = SMS_VERIFY_CODE_QUEUE),
                    exchange = @Exchange(name = SMS_EXCHANGE_NAME),
                    key = VERIFY_CODE_KEY
            )
    )
    public void listenVerifyCode(Map<String,String> msg){
        if (msg == null){
            return;
        }
        //移动手机数据，剩下的只是短信参数
        String phone = msg.get("phone");
        String code = msg.get("code");
        if (StringUtils.isBlank(phone)){
            return;
        }
        try {
            smsUtils.sendSms(phone,code);
        } catch (Exception e) {
            //短信验证码失败后不重发，所以需要捕获异常
            log.error("【SMS服务】短信验证码发送失败", e);
        }
    }
}
