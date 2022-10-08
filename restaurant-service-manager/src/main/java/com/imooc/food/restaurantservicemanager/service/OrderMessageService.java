package com.imooc.food.restaurantservicemanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.food.restaurantservicemanager.dao.ProductDao;
import com.imooc.food.restaurantservicemanager.dao.RestaurantDao;
import com.imooc.food.restaurantservicemanager.dto.OrderMessageDTO;
import com.imooc.food.restaurantservicemanager.enummeration.ProductStatus;
import com.imooc.food.restaurantservicemanager.enummeration.RestaurantStatus;
import com.imooc.food.restaurantservicemanager.po.ProductPO;
import com.imooc.food.restaurantservicemanager.po.RestaurantPO;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class OrderMessageService {

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    ProductDao productDao;
    @Autowired
    RestaurantDao restaurantDao;

    @Autowired
    Channel channel;

    @Async
    public void handleMessage() throws IOException, TimeoutException, InterruptedException {
        log.info("start linstening message");

        channel.exchangeDeclare(
                "exchange.order.restaurant",
                BuiltinExchangeType.DIRECT,
                true,
                false,
                null);

        //重新声明队列需要把原先的队列进行一个删除操作
        Map<String,Object> args = new HashMap<String,Object>(16);
        args.put("x-message-ttl", 150000);

        // 声明队列
        channel.queueDeclare(
                "queue.restaurant",
                true,
                false,
                false,
                args);


        channel.queueBind(
                "queue.restaurant",
                "exchange.order.restaurant",
                "key.restaurant");


        //关闭自动ACK
        channel.basicQos(2);
        channel.basicConsume("queue.restaurant", false, deliverCallback, consumerTag -> {
        });
        while (true) {
            Thread.sleep(100000);
        }
    }


    DeliverCallback deliverCallback = (consumerTag, message) -> {
        String messageBody = new String(message.getBody());
        log.info("deliverCallback:messageBody:{}", messageBody);
        try {
            OrderMessageDTO orderMessageDTO = objectMapper.readValue(messageBody,
                    OrderMessageDTO.class);

            ProductPO productPO = productDao.selsctProduct(orderMessageDTO.getProductId());
            log.info("onMessage:productPO:{}", productPO);
            RestaurantPO restaurantPO = restaurantDao.selsctRestaurant(productPO.getRestaurantId());
            log.info("onMessage:restaurantPO:{}", restaurantPO);
            if (ProductStatus.AVALIABLE == productPO.getStatus() && RestaurantStatus.OPEN == restaurantPO.getStatus()) {
                orderMessageDTO.setConfirmed(true);
                orderMessageDTO.setPrice(productPO.getPrice());
            } else {
                orderMessageDTO.setConfirmed(false);
            }
            log.info("sendMessage:restaurantOrderMessageDTO:{}", orderMessageDTO);
            //AutoClosable


//                channel.addReturnListener(new ReturnListener() {
//                    @Override
//                    public void handleReturn(int replyCode,
//                                             String replyText,
//                                             String exchange,
//                                             String routingKey,
//                                             AMQP.BasicProperties properties,
//                                             byte[] body
//                    ) throws IOException {
//                        log.info("Message Return: " +
//                                "replyCode:{}, replyText:{}, exchange:{}, routingKey:{}, properties:{}, body:{}",
//                                replyCode, replyText, exchange, routingKey, properties, new String(body));
//                        //除了打印log，可以加别的业务操作
//                    }
//                });

            channel.addReturnListener(new ReturnCallback() {
                @Override
                public void handle(Return returnMessage) {
                    log.info("Message Return: returnMessage{}", returnMessage);

                    //除了打印log，可以加别的业务操作
                }
            });
            //multiple为false,表明为单条手动ACK
            //multiple为true，则为多条手动ACK
            //推荐使用单条ACK
            // 多条消息手动签收(5条消息全部签收一次)
            if (message.getEnvelope().getDeliveryTag() % 5 == 0) {
                channel.basicAck(message.getEnvelope().getDeliveryTag(), true);
            }
//            channel.basicAck(message.getEnvelope().getDeliveryTag(),false);
            //实现重回队列
            channel.basicNack(message.getEnvelope().getDeliveryTag(), false, true);

            String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
            //此处mandatory 设置为true，意思为如果发送失败，则会调用发送端的ReturnListener相关方法
            channel.basicPublish("exchange.order.restaurant", "key.order", true, null, messageToSend.getBytes());
            Thread.sleep(1000);

        } catch (JsonProcessingException | InterruptedException e) {
            e.printStackTrace();
        }
    };
}

