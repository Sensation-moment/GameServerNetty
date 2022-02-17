package org.tinygame.herostory.mq;

import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.rank.RankService;

import java.util.List;

/**
 * 消息队列消费者
 */
public final class MqConsumer {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(MqConsumer.class);

    /**
     * 私有化类默认构造器
     */
    private MqConsumer() {

    }

    /**
     * 初始化
     */
    static public void init() {
        // 创建消息队列消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("herostory");
        // 指定 nameSever 地址
        consumer.setNamesrvAddr("10.0.1.10:9876");
        try {
            // 订阅主题(*代表订阅该主题下的所有)
            consumer.subscribe("herostory_victor", "*");

            // 监听
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgExtList, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                    // 以下内容为收到消息后要干的事情
                    for (MessageExt msgExt : msgExtList) {
                        // 判空
                        if (null == msgExt)
                            // 跳过
                            continue;
                        // 将消息解析为胜利消息
                        VictorMsg victorMsg = JSONObject.parseObject(msgExt.getBody(), VictorMsg.class);

                        LOGGER.info("从消息队列中收到胜利消息, winnerId = {}, loserId = {}", victorMsg.winnerId, victorMsg.loserId);

                        // 刷新排行榜
                        RankService.getInstance().refreshRank(victorMsg.winnerId, victorMsg.loserId);
                    }
                    // 返回状态消息接收成功
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            consumer.start();
            LOGGER.info("消息队列 ( 消费者 ) 连接成功!");
        }catch (Exception ex) {
            // 记录错误日志
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}
