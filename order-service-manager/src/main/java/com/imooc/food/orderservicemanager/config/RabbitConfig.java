package com.imooc.food.orderservicemanager.config;

import com.imooc.food.orderservicemanager.service.OrderMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
@Configuration
public class RabbitConfig {

    @Autowired
    OrderMessageService orderMessageService;

    @Autowired
    public void startListenMessage() throws IOException, TimeoutException, InterruptedException {
        orderMessageService.handleMessage();
    }

    @Autowired
    public void initRabbit() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost("127.0.0.1");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);


        /*---------------------restaurant---------------------*/
        Exchange exchange = new DirectExchange("exchange.order.restaurant");
        rabbitAdmin.declareExchange(exchange);

        Queue queue = new Queue("queue.order");
        rabbitAdmin.declareQueue(queue);

        Binding binding = new Binding("queue.order",
                //绑定目的地类型
                Binding.DestinationType.QUEUE,
                "exchange.order.restaurant",
                "key.order",
                null);
        rabbitAdmin.declareBinding(binding);

        /*---------------------deliveryman---------------------*/
        exchange = new DirectExchange("exchange.order.deliveryman");
        rabbitAdmin.declareExchange(exchange);

        binding = new Binding("queue.order",
                //绑定目的地类型
                Binding.DestinationType.QUEUE,
                "exchange.order.deliveryman",
                "key.order",
                null);
        rabbitAdmin.declareBinding(binding);


        /*---------------------settlement---------------------*/

        exchange = new FanoutExchange("exchange.order.settlement");
        rabbitAdmin.declareExchange(exchange);

        exchange = new FanoutExchange("exchange.settlement.order");
        rabbitAdmin.declareExchange(exchange);

        binding = new Binding("queue.order",
                //绑定目的地类型
                Binding.DestinationType.QUEUE,
                "exchange.settlement.order",
                "key.order",
                null);
        rabbitAdmin.declareBinding(binding);


        /*---------------------reward---------------------*/

        exchange = new TopicExchange("exchange.order.reward");
        rabbitAdmin.declareExchange(exchange);

        binding = new Binding("queue.order",
                //绑定目的地类型
                Binding.DestinationType.QUEUE,
                "exchange.order.reward",
                "key.order",
                null);
        rabbitAdmin.declareBinding(binding);

    }
}
