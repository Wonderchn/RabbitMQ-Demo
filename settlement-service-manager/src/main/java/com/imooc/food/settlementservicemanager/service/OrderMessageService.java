package com.imooc.food.settlementservicemanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.food.settlementservicemanager.dao.SettlementDao;
import com.imooc.food.settlementservicemanager.dto.OrderMessageDTO;
import com.imooc.food.settlementservicemanager.enummeration.SettlementStatus;
import com.imooc.food.settlementservicemanager.po.SettlementPO;
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
    SettlementDao settlementDao;
    @Autowired
    SettlementService settlementService;

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
            SettlementPO settlementPO = new SettlementPO();
            settlementPO.setAmount(orderMessageDTO.getPrice());
            settlementPO.setDate(new Date());
            settlementPO.setOrderId(orderMessageDTO.getOrderId());
            settlementPO.setStatus(SettlementStatus.SUCCESS);
            settlementPO.setTransactionId(settlementService.settlement(orderMessageDTO.getAccountId(),
                    orderMessageDTO.getPrice()));
            settlementDao.insert(settlementPO);
            orderMessageDTO.setSettlementId(settlementPO.getId());
            log.info("handleOrderService:settlementOrderDTO:{}", orderMessageDTO);

            try (Connection connection = connectionFactory.newConnection();
                 Channel channel = connection.createChannel()) {
                String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
                channel.basicPublish("exchange.settlement.order", "key.order", null, messageToSend.getBytes());
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
                    "exchange.order.settlement",
                    BuiltinExchangeType.FANOUT,
                    true,
                    false,
                    null);


            channel.queueDeclare(
                    "queue.settlement",
                    true,
                    false,
                    false,
                    null);

            channel.queueBind(
                    "queue.settlement",
                    "exchange.order.settlement",
                    "key.settlement");


            channel.basicConsume("queue.settlement", true, deliverCallback, consumerTag -> {
            });
            while (true) {
                Thread.sleep(100000);
            }
        }
    }
}

