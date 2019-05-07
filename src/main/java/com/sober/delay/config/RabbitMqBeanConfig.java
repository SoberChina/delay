package com.sober.delay.config;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author liweigao
 * @date 2018/12/8 上午10:52
 */
@Slf4j
@Component
public class RabbitMqBeanConfig {

    /**
     * ttl Time To Live queue
     * delay.ttl.queue.%s -> %s :不同过期的消息放到不同的队列中
     * RabbitMQ只对处于队头的消息判断是否过期（即不会扫描队列），所以，很可能队列中已存在死消息，但是队列并不知情。这会影响队列统计数据的正确性，妨碍队列及时释放资源。
     *
     * @see <a href="https://www.rabbitmq.com/ttl.html">ttl</a>
     */
    public static final String DELAY_TTL_QUEUE = "delay.ttl.queue_%s";

    /**
     * delay.ttl.direct.exchange
     */
    public static final String DELAY_TTL_DIRECT_EXCHANGE = "delay.ttl.direct.exchange";


    public static final String DELAY_TTL_ROOTING_KEY = "delay_ttl_rooting_key_%s";

    /**
     *
     */
    public static final String DELAY_CONSUMER_DLX_QUEUE = "delay.consumer.queue";
    /**
     * dlx Dead Letter Exchange
     */
    public static final String DELAY_CONSUMER_DLX_DIRECT_EXCHANGE = "delay.consumer.direct.exchange";
    public static final String DEFAULT_ROUTING_KEY = "default";


    private static Map<String, Object> arguments = Maps.newConcurrentMap();

    static {
        arguments.put("x-dead-letter-exchange", DELAY_CONSUMER_DLX_DIRECT_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", DEFAULT_ROUTING_KEY);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DELAY_CONSUMER_DLX_DIRECT_EXCHANGE);
    }

    @Bean
    public Queue dlxQueue() {

        return QueueBuilder.durable(DELAY_CONSUMER_DLX_QUEUE).build();
    }

    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with(DEFAULT_ROUTING_KEY);
    }


    @Bean
    public DirectExchange ttlExchange() {
        return new DirectExchange(DELAY_TTL_DIRECT_EXCHANGE);
    }

    @Autowired
    private RabbitAdmin rabbitAdmin;

    /**
     * 动态创建组件
     *
     * @param queueName  queue名称
     * @param rootingKey rooting-key
     * @param expireTime 毫秒
     */
    public void createAssembly(String queueName, String rootingKey, Long expireTime) {
        try {
            arguments.put("x-message-ttl", expireTime);
            Queue queue = QueueBuilder.durable(queueName).withArguments(arguments).build();
            rabbitAdmin.declareQueue(queue);
            rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(ttlExchange()).with(rootingKey));
        } catch (Exception e) {
            log.warn("rebuild assembly is error:{}", e.getMessage());
        }

    }
}
