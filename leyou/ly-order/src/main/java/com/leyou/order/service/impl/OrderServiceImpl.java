package com.leyou.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.threadlocals.UserHolder;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SkuDTO;
import com.leyou.order.config.PayProperties;
import com.leyou.order.dto.CartDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.entity.Order;
import com.leyou.order.entity.OrderDetail;
import com.leyou.order.entity.OrderLogistics;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderLogisticsMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.service.OrderService;
import com.leyou.order.vo.OrderDetailVO;
import com.leyou.order.vo.OrderLogisticsVO;
import com.leyou.order.vo.OrderVO;
import com.leyou.user.client.UserClient;
import com.leyou.user.dto.AddressDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PayProperties payProp;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderLogisticsMapper logisticsMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private UserClient userClient;

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Long createOrder(OrderDTO orderDTO) {
        //创建订单
        Order order = new Order();
        //设置订单编号
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);

        //设置订单用户
        String userId = UserHolder.getUserId();
        order.setUserId(Long.parseLong(userId));

        //金额相关信息
        List<CartDTO> carts = orderDTO.getCarts();
        //获得所有sku的id
        List<Long> idList = carts.stream().map(CartDTO::getSkuId).collect(Collectors.toList());
        //处理cartDto为一个map，其key是skuId，值是num
        Map<Long, Integer> numMap = carts.stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        //查询sku
        List<SkuDTO> skuList = itemClient.querySkusByIds(idList);
        //计算金额的和
        long total = 0;
        int size = 0;
        for (SkuDTO sku : skuList) {
            int num = numMap.get(sku.getId());
            //计算总金额
            total += sku.getPrice() * num;
            //组装OrderDetail
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(orderId);
            //StringUtils.substringBefore(sku.getImages(), ",")
            detail.setImage(sku.getImages());
            detail.setNum(num);
            detail.setSkuId(sku.getId());
            detail.setOwnSpec(sku.getOwnSpec());
            detail.setPrice(sku.getPrice());
            detail.setTitle(sku.getTitle());
            //将orderDetail保存数据库
            size = size + orderDetailMapper.insert(detail);
        }
        //判断新增是否正常
        if(size != skuList.size()){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        // 1.3.3 填写金额数据
        order.setTotalFee(total);
        order.setPaymentType(orderDTO.getPaymentType());
        order.setPostFee(0L);     //数据库限制不能为null
        order.setActualFee(total /* + 邮费 - 优惠金额*/);

        // 1.4 订单状态初始化
        order.setStatus(OrderStatusEnum.INIT.value());

        // 1.5 写order到数据库
        int count = orderMapper.insert(order);
        if(count != 1){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

        // 3 写orderLogistics
        // 3.1.查询收货地址
        AddressDTO addr = userClient.queryAddressById(Long.parseLong(userId), orderDTO.getAddressId());
        // 3.2.填写物流信息
        OrderLogistics logistics = BeanHelper.copyProperties(addr, OrderLogistics.class);
        logistics.setOrderId(orderId);

        count = logisticsMapper.insert(logistics);
        if(count != 1){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

        // 4 减库存
        itemClient.minusStock(numMap);

        return orderId;
    }

    @Override
    public OrderVO queryOrderById(Long id) {
        // 1.查询订单
        Order order = orderMapper.selectById(id);
        if (order == null) {
            // 不存在
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        // 2. 查询订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(id);
        List<OrderDetail> details = orderDetailMapper.selectList(new QueryWrapper<>(detail));
        if(CollectionUtils.isEmpty(details)){
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        // 3.查询物流信息
        OrderLogistics logistics = logisticsMapper.selectById(id);
        if (logistics == null) {
            // 不存在
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }

        // 4.封装数据
        OrderVO orderVO = BeanHelper.copyProperties(order, OrderVO.class);
        orderVO.setDetailList(BeanHelper.copyWithCollection(details, OrderDetailVO.class));
        orderVO.setLogistics(BeanHelper.copyProperties(logistics, OrderLogisticsVO.class));
        return orderVO;
    }
    private static final String PAY_ID = "ly:pay";
    //支付地址的接口
    private String PAY_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    @Override
    public String createPayUrl(Long id) {
        try {
            Order order = orderMapper.selectById(id);

            //拼接发送请求内容
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put("appid", payProp.getAppID());
            requestMap.put("mch_id", payProp.getMchID());
            requestMap.put("nonce_str", WXPayUtil.generateUUID()); //随机值
            requestMap.put("body", "商场商品");
            requestMap.put("out_trade_no", id.toString());  //订单号
            requestMap.put("total_fee", "1"); //注意转成string类型
            requestMap.put("spbill_create_ip", "127.0.0.1");
            requestMap.put("notify_url", payProp.getNotifyurl()); //通知地址
            requestMap.put("trade_type", "NATIVE ");   //支付类型

            //1.发送的内容 2.签名
            String signedXml = WXPayUtil.generateSignedXml(requestMap, payProp.getKey());

            //通过restTemplate发送http请求
            //1.地址，2请求的内容 3.返回类型

            String responseStr = restTemplate.postForObject(PAY_URL, signedXml, String.class);
            //将微信返回的内容转成map对象，通过工具类
            Map<String, String> map = WXPayUtil.xmlToMap(responseStr);

            //将微信支付地址存入redis,支付时间为2个小时
            redisTemplate.boundValueOps(PAY_ID + id).set(map.get("code_url"), 2, TimeUnit.HOURS);

            return map.get("code_url");
        } catch (Exception e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.PAY_ERROR);
        }

    }

    @Override
    public Integer queryPayStatus(Long id) {
        try {
            //拼接请求内容
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put("appid",payProp.getAppID() );
            requestMap.put("mch_id", payProp.getMchID());
            requestMap.put("out_trade_no", id.toString());
            requestMap.put("nonce_str",WXPayUtil.generateUUID() );

            //通过微信工具类转成xml
            String signedXml = WXPayUtil.generateSignedXml(requestMap, payProp.getKey());

            String url = "https://api.mch.weixin.qq.com/pay/orderquery";

            //查询订单支付状态
            String responseStr = restTemplate.postForObject(url, signedXml, String.class);

            //转成map方便取值
            Map<String, String> map = WXPayUtil.xmlToMap(responseStr);
            String trade_state = map.get("trade_state");


            //如果支付成功，更新订单状态
            if(StringUtils.equals(trade_state, "SUCCESS")){
                Order order = new Order();
                order.setOrderId(id);
                order.setStatus(OrderStatusEnum.PAY_UP.value()); //已付款未发货状态码为2
                order.setPayTime(new Date());  //支付时间
                orderMapper.updateById(order);
            }

            return "SUCCESS".equals(trade_state)?1:0;
        } catch (Exception e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.PAY_ERROR);
        }
    }
}
