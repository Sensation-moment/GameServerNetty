package org.tinygame.herostory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.mq.MqConsumer;
import org.tinygame.herostory.util.RedisUtil;

/**
 * 排行榜应用程序
 */
public class RankApp {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(RankApp.class);

    /**
     * 应用主函数
     *
     * @param args
     */
    public static void main(String[] args) {
        // 初始化Redis
        RedisUtil.init();
        // 初始化消息队列消费者
        MqConsumer.init();
    }
}
