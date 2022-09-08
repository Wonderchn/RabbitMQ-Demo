package com.imooc.food.rewardservicemanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.food.rewardservicemanager.dao.RewardDao;
import com.imooc.food.rewardservicemanager.dto.OrderMessageDTO;
import com.imooc.food.rewardservicemanager.enummeration.RewardStatus;
import com.imooc.food.rewardservicemanager.po.RewardPO;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class OrderMessageService {

    @Autowired
    RewardDao rewardDao;

    ObjectMapper objectMapper = new ObjectMapper();

    DeliverCallback deliverCallback = (consumerTag, message) -> {
        String messageBody = new String(message.getBody());
        log.info("deliverCallback:messageBody:{}", messageBody);
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        try {
            OrderMessageDTO orderMessageDTO = objectMapper.readValue(messageBody,
                    OrderMessageDTO.class);
            log.info("handleOrderService:orderSettlementDTO:{}", orderMessageDTO);
            RewardPO rewardPO = new RewardPO();
            rewardPO.setOrderId(orderMessageDTO.getOrderId());
            rewardPO.setStatus(RewardStatus.SUCCESS);
            rewardPO.setAmount(orderMessageDTO.getPrice());
            rewardPO.setDate(new Date());
            rewardDao.insert(rewardPO);
            orderMessageDTO.setRewardId(rewardPO.getId());
            log.info("handleOrderService:settlementOrderDTO:{}", orderMessageDTO);

            try (Connection connection = connectionFactory.newConnection();
                 Channel channel = connection.createChannel()) {
                String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
                channel.basicPublish("exchange.order.reward", "key.order", null, messageToSend.getBytes());
            }
        } catch (JsonProcessingException | TimeoutException e) {
            e.printStackTrace();
        }
    };

    @Async
    public void handleMessage() throws IOException, TimeoutException, InterruptedException {
        log.info("start linstening message");
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        connectionFactory.setHost("localhost");
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(
                    "exchange.order.reward",
                    BuiltinExchangeType.TOPIC,
                    true,
                    false,
                    null);

            channel.queueDeclare(
                    "queue.reward",
                    true,
                    false,
                    false,
                    null);

            channel.queueBind(
                    "queue.reward",
                    "exchange.order.reward",
                    "key.reward");


            channel.basicConsume("queue.reward", true, deliverCallback, consumerTag -> {
            });
            while (true) {
                Thread.sleep(100000);
            }
        }
    }
}

