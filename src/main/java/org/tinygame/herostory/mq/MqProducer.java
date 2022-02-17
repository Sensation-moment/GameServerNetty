package org.tinygame.herostory.mq;

import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息队列生产者(工具类)
 */
public final class MqProducer {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(MqProducer.class);
    /**
     * 消息队列生产者
     */
    static private DefaultMQProducer _producer = null;

    /**
     * 私有化类默认构造器
     */
    private MqProducer() {

    }

    /**
     * 初始化
     */
    static public void init() {
        try {
            // 初始化消息队列
            DefaultMQProducer producer = new DefaultMQProducer("herostory");
            // 配置NameServer
            producer.setNamesrvAddr("10.0.1.10:9876");
            producer.start();
            // 重试
            producer.setRetryTimesWhenSendAsyncFailed(3);

            _producer = producer;

            LOGGER.info("消息队列 ( 生产者 ) 连接成功!");
        }catch (Exception ex) {
            // 记录错误日志
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * 发送消息
     *
     * @param topic 主题
     * @param msg   消息对象
     */
    static public void sendMsg(String topic, Object msg) {
        // 校验
        if (null == topic || null == msg) {
            return;
        }

        // 初始化消息
        Message newMsg = new Message();
        // 设置消息主题
        newMsg.setTopic(topic);
        newMsg.setBody(JSONObject.toJSONBytes(msg));

        try {
            // 发送消息
            _producer.send(newMsg);
        } catch (Exception ex) {
            // 记录错误日志
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}
