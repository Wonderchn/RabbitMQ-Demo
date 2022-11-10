package com.imooc.food.orderservicemanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.food.orderservicemanager.dao.OrderDetailDao;
import com.imooc.food.orderservicemanager.dto.OrderMessageDTO;
import com.imooc.food.orderservicemanager.enummeration.OrderStatus;
import com.imooc.food.orderservicemanager.po.OrderDetailPO;
import com.imooc.food.orderservicemanager.vo.OrderCreateVO;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderDetailDao orderDetailDao;
    @Autowired
    RabbitTemplate rabbitTemplate;

    ObjectMapper objectMapper = new ObjectMapper();


//    public void createOrder(OrderCreateVO orderCreateVO) throws IOException, TimeoutException, InterruptedException {
//        log.info("createOrder:orderCreateVO:{}", orderCreateVO);
//        OrderDetailPO orderPO = new OrderDetailPO();
//        orderPO.setAddress(orderCreateVO.getAddress());
//        orderPO.setAccountId(orderCreateVO.getAccountId());
//        orderPO.setProductId(orderCreateVO.getProductId());
//        orderPO.setStatus(OrderStatus.ORDER_CREATING);
//        orderPO.setDate(new Date());
//        orderDetailDao.insert(orderPO);
//
//        OrderMessageDTO orderMessageDTO = new OrderMessageDTO();
//        orderMessageDTO.setOrderId(orderPO.getId());
//        orderMessageDTO.setProductId(orderPO.getProductId());
//        orderMessageDTO.setAccountId(orderCreateVO.getAccountId());
//
//        ConnectionFactory connectionFactory = new ConnectionFactory();
//        connectionFactory.setHost("localhost");
//
//        try (Connection connection = connectionFactory.newConnection();
//             Channel channel = connection.createChannel()) {
////           channel.confirmSelect();
////           String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
////            log.info("message sent");
////           if (channel.waitForConfirms()){
////               log.info("RabbitMq");
////           }
//
//            //           channel.confirmSelect();
////           String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
////           //多条同步消息确认
////           for (int i = 0; i<10; i++){
////               channel.basicPublish("exchange.order.restaurant", "key.restaurant",null, messageToSend.getBytes());
////           }
////            log.info("message sent");
////           if (channel.waitForConfirms()){
////               log.info("RabbitMq");
////           }
//
//            //异步确认方法
//            channel.confirmSelect();
//            ConfirmListener confirmListener = new ConfirmListener() {
//                @Override
//                public void handleAck(long deliveryTag, boolean multiple) throws IOException {
//                    log.info("ACK,deliveryTag:{},multiple:{}", deliveryTag, multiple);
//                }
//
//                @Override
//                public void handleNack(long deliveryTag, boolean multiple) throws IOException {
//                    log.info("NACK,deliveryTag:{},multiple:{}", deliveryTag, multiple);
//
//                }
//            };
//            channel.addConfirmListener(confirmListener);
//            String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
//            for (int i = 0; i < 10; i++) {
//                //设置单条消息的过期时间
//                AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().expiration("15000").build();
//                channel.basicPublish("exchange.order.restaurant", "key.restaurant", properties, messageToSend.getBytes());
//            }
//            Thread.sleep(10000);
//        }
//    }


    // 创建订单
    public void createOrder(OrderCreateVO orderCreateVO) throws IOException, TimeoutException, InterruptedException {
        log.info("createOrder:orderCreateVO:{}", orderCreateVO);
        OrderDetailPO orderPO = new OrderDetailPO();
        orderPO.setAddress(orderCreateVO.getAddress());
        orderPO.setAccountId(orderCreateVO.getAccountId());
        orderPO.setProductId(orderCreateVO.getProductId());
        orderPO.setStatus(OrderStatus.ORDER_CREATING);
        orderPO.setDate(new Date());
        // 会返回数据库自动生成的数据
        orderDetailDao.insert(orderPO);

        OrderMessageDTO orderMessageDTO = new OrderMessageDTO();
        orderMessageDTO.setOrderId(orderPO.getId());
        orderMessageDTO.setProductId(orderPO.getProductId());
        orderMessageDTO.setAccountId(orderCreateVO.getAccountId());
        String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);

        MessageProperties messageProperties = new MessageProperties();
        // 设置过期时间等
        messageProperties.setExpiration("15000");
        /* -------------- 使用RabbitTemplate发送消息 ----------------- */
        Message message = new Message(messageToSend.getBytes(), messageProperties);
        // 设置发送消息的对应关系信息
        CorrelationData correlationData = new CorrelationData();
        correlationData.setId(orderPO.getId().toString());
        rabbitTemplate.send(
                "exchange.order.restaurant",
                "key.restaurant",
                message,
                correlationData
        );

        // 消息转换并发送
        /*rabbitTemplate.convertAndSend(
                "exchange.order.restaurant",
                "key.restaurant",
                messageToSend
        );*/

        // 也可以使用原生
        /*rabbitTemplate.execute(channel -> {
            // 使用channel做原生的操作(@FunctionalInterface函数式编程)
        });*/

        log.info("message send");
        Thread.sleep(1000);
    }
}
