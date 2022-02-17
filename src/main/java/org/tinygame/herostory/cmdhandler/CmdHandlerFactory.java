package org.tinygame.herostory.cmdhandler;

import com.google.protobuf.GeneratedMessageV3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.util.PackageUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 命令处理器工厂类
 */
public final class CmdHandlerFactory {
    /**
     * 日志对象
     */
    static private Logger LOGGER = LoggerFactory.getLogger(CmdHandlerFactory.class);
    /**
     * 命令处理器字典
     */
    static private final Map<Class<?>, ICmdHandler<? extends GeneratedMessageV3>> _handlerMap = new HashMap<>();
    /**
     * 私有化类默认构造器
     */
    private CmdHandlerFactory() {

    }

    /**
     * 初始化
     * 内部是一对一的映射关系，所以采用map
     */
    static public void init() {
        // 打印初始化日志
        LOGGER.info("==== 完成命令与处理器的关联 ====");
        // 获取包的名称
        String packageName = CmdHandlerFactory.class.getPackage().getName();
        // 扫描当前包，并获取到 ICmdHandler 所有的实现类
        Set<Class<?>> clazzSet = PackageUtil.listSubClazz(packageName, true, ICmdHandler.class);
        // 遍历所有的实现类
        for (Class<?> handlerClazz : clazzSet) {
            // 判空 或者 获取到的类是一个抽象类的话
            if (null == handlerClazz || 0 != (handlerClazz.getModifiers() & Modifier.ABSTRACT)) {
                // 跳过
                continue;
            }
            // 获取方法数组
            Method[] methodArray = handlerClazz.getDeclaredMethods();
            // 初始化消息类型
            Class<?> cmdClazz = null;

            // 遍历方法数组
            for (Method currMethod : methodArray) {
                // 判空 或者 通过方法名得知当前函数不是要找的handle函数
                if (null == currMethod || !currMethod.getName().equals("handle")) {
                    // 跳过
                    continue;
                }
                // 获取函数类型参数数组
                Class<?>[] parameterTypeArray = currMethod.getParameterTypes();
                // 如果参数个数小于2 或者 是V3的基类 或者 不是V3的派生类 则都不符合要寻找的条件
                if (parameterTypeArray.length < 2 || parameterTypeArray[1] == GeneratedMessageV3.class || !GeneratedMessageV3.class.isAssignableFrom(parameterTypeArray[1])) {
                    // 跳过
                    continue;
                }
                // 走到这里，拿到消息类型
                cmdClazz = parameterTypeArray[1];
                // 退出
                break;
            }
            // 如果还是没拿到这个消息类型
            if (null == cmdClazz) {
                // 跳过
                continue;
            }
            try {
                // 创建命令处理器实例
                ICmdHandler<?> newHandler = (ICmdHandler<?>) handlerClazz.newInstance();

                // 打印日志展示映射关系
                LOGGER.info("{} <==> {}", cmdClazz.getName(), handlerClazz.getName());

                // 塞到命令处理器Map中
                _handlerMap.put(cmdClazz, newHandler);
            }catch (Exception ex) {
                // 记录错误日志
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }

    /**
     * 创建命令处理器
     *
     * @param msgClazz
     * @return
     */
    static public ICmdHandler<? extends GeneratedMessageV3> create(Class<?> msgClazz) {
        if (null == msgClazz) {
            return null;
        }
        // 根据类去获取对应的命令处理器对象
        return _handlerMap.get(msgClazz);
    }
}
