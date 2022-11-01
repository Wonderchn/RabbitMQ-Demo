package com.imooc.food.orderservicemanager.config;

import com.imooc.food.orderservicemanager.service.OrderMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
@Configuration
public class RabbitConfig {

    @Autowired
    OrderMessageService orderMessageService;

    //方法被@Autowired注解上，会自动执行相关方法
    @Autowired
    public void startListenMessage() throws IOException, TimeoutException, InterruptedException {
        orderMessageService.handleMessage();
    }

    /*---------------------restaurant---------------------*/
    @Bean
    public Exchange exchange1() {
        return new DirectExchange("exchange.order.restaurant");
    }

    @Bean
    public Queue queue1() {
        return new Queue("queue.order");
    }

    @Bean
    public Binding binding1() {
        return new Binding("queue.order",
                Binding.DestinationType.QUEUE,
                "exchange.order.restaurant",
                "key.order",
                null
        );
    }

    /*---------------------deliveryman---------------------*/
    @Bean
    public Exchange exchange2() {
        return new DirectExchange("exchange.order.deliveryman");
    }

    @Bean
    public Binding binding2() {
        return new Binding(
                "queue.order",
                Binding.DestinationType.QUEUE,
                "exchange.order.deliveryman",
                "key.order",
                null);
    }


    /*---------settlement---------*/
    @Bean
    public Exchange exchange3() {
        return new FanoutExchange("exchange.order.settlement");
    }

    @Bean
    public Exchange exchange4() {
        return new FanoutExchange("exchange.settlement.order");
    }

    @Bean
    public Binding binding3() {
        return new Binding(
                "queue.order",
                Binding.DestinationType.QUEUE,
                "exchange.settlement.order",
                "key.order",
                null);
    }

    /*--------------reward----------------*/
    @Bean
    public Exchange exchange5() {
        return new TopicExchange("exchange.order.reward");
    }

    @Bean
    public Binding binding4() {
        return new Binding(
                "queue.order",
                Binding.DestinationType.QUEUE,
                "exchange.order.reward",
                "key.order",
                null);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost("127.0.0.1");
        connectionFactory.setPort(5672);
        connectionFactory.setPassword("guest");
        connectionFactory.setUsername("guest");
        //需要使用过后，rabbitAdmin才能正式注入。
        connectionFactory.createConnection();
        return connectionFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        //rabbitAdmin设置自动执行
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

}
