package org.tinygame.herostory;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.msg.GameMsgProtocol;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息识别器(工具类)
 */
public class GameMsgRecognizer {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(GameMsgRecognizer.class);
    /**
     * 消息编号 -> 消息对象 的Map
     */
    static private final Map<Integer, GeneratedMessageV3> _msgCodeAndMsgObjMap = new HashMap<>();
    /**
     * 消息类 -> 消息编号 的Map
     */
    static private final Map<Class<?>, Integer> _clazzAndMsgCodeMap = new HashMap<>();

    /**
     * 私有化类默认构造器
     */
    private GameMsgRecognizer() {

    }

    /**
     * 初始化消息识别器
     */
    static public void init() {
        LOGGER.info("==== 完成消息类与消息编号的映射 ====");
        // 通过反射机制拿到GameMsgProtocol里面的所有内部类
        Class<?>[] innerClazzArray = GameMsgProtocol.class.getDeclaredClasses();

        // 遍历所有获取到的内部类
        for (Class<?> innerClazz : innerClazzArray) {
            // 若获取到的内部类为空(判空) 或者 获取到的这个类不是GeneratedMessageV3的子类(也即不是消息类，则这个类不是我们要找的)
            if (null == innerClazz || !GeneratedMessageV3.class.isAssignableFrom(innerClazz)) {
                // 就跳过
                continue;
            }
            // 获取类的名称
            String clazzName = innerClazz.getSimpleName();
            // 并转为小写
            clazzName = clazzName.toLowerCase();

            // 遍历所有的消息编码
            for (GameMsgProtocol.MsgCode msgCode : GameMsgProtocol.MsgCode.values()) {
                // 判空
                if (null == msgCode) {
                    // 跳过
                    continue;
                }
                // 获取消息编码
                String strMsgCode = msgCode.name();
                // 干掉所有的下划线
                strMsgCode = strMsgCode.replace("_", "");
                // 转成小写
                strMsgCode = strMsgCode.toLowerCase();

                // 如果前缀不同，那肯定对不上
                if (!strMsgCode.startsWith(clazzName)) {
                    // 跳过就行
                    continue;
                }

                try {
                    // 相当于调用 UserEntryCmd.getDefaultInstance() 获得对象实例
                    Object returnObj = innerClazz.getDeclaredMethod("getDefaultInstance").invoke(innerClazz);
                    // 打印一下日志，获取映射对应关系
                    LOGGER.info("{} <==> {}", innerClazz.getName(), msgCode.getNumber());
                    // 走到这里，说明msgCode和claszzName肯定是一个一一对应的关系了，塞到消息编号 -> 消息对象 的Map中去
                    _msgCodeAndMsgObjMap.put(msgCode.getNumber(), (GeneratedMessageV3) returnObj);
                    // 塞到消息类 -> 消息编号 的Map中
                    _clazzAndMsgCodeMap.put(innerClazz, msgCode.getNumber());
                }catch (Exception ex) {
                    // 记录错误日志
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * 根据消息编号获取消息构建器
     *
     * @param msgCode
     * @return
     */
    static public Message.Builder getBuilderByMsgCode(int msgCode) {
        // 校验合法性(消息编码不在范围内)
        if (msgCode < 0) {
            return null;
        }
        // 获取消息
        GeneratedMessageV3 defaultMsg = _msgCodeAndMsgObjMap.get(msgCode);
        // 判空，校验合法性
        if (null == defaultMsg) {
            return null;
        }else {
            // 返回消息构建器
            return defaultMsg.newBuilderForType();
        }
    }

    /**
     * 根据消息类获取消息编号
     *
     * @param msgClazz
     * @return
     */
    static public int getMsgCodeByClazz(Class<?> msgClazz) {
        // 校验合法性
        if (null == msgClazz) {// 若类为空值
            // 返回一个不存在的消息编号
            return -1;
        }
        // 获取对应的消息编号
        Integer msgCode = _clazzAndMsgCodeMap.get(msgClazz);
        // 若得到的编号为空
        if (msgCode == null) {
            // 返回一个不存在的消息编号
            return -1;
        }else {
            // 拆箱
            return msgCode.intValue();
        }
    }
}
