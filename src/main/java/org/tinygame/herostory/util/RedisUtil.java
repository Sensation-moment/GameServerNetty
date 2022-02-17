package org.tinygame.herostory.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Redis工具类
 */
public final class RedisUtil {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(RedisUtil.class);
    /**
     * Redis连接池
     */
    static private JedisPool _jedisPool = null;

    /**
     * 私有化类默认构造器
     */
    private RedisUtil() {

    }

    /**
     * 初始化
     */
    static public void init() {
        try {
            _jedisPool = new JedisPool("1.117.191.23", 6379);
            LOGGER.info("Redis连接成功");
        }catch (Exception ex) {
            // 记录错误日志
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * 获取Redis实例
     *
     * @return Redis实例
     */
    static public Jedis getJedis() {
        if (null == _jedisPool) {
            throw new RuntimeException("_jedisPool尚未初始化");
        }
        Jedis jedis = _jedisPool.getResource();
        //jedis.auth("root");
        return jedis;
    }
}
