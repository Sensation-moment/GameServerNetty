package org.tinygame.herostory;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 广播员(工具类)
 */
public final class Broadcaster {
    /**
     * 信道组，这里一定要用static，否则无法实现群发
     */
    static private final ChannelGroup _channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 私有化类默认构造器
     */
    private Broadcaster() {

    }

    /**
     * 添加信道
     *
     * @param ch
     */
    static public void addChannel(Channel ch) {
        // 如果信道不为空
        if (null != ch) {
            // 添加到信道组中
            _channelGroup.add(ch);
        }
    }

    /**
     * 移除信道
     *
     * @param ch
     */
    static public void removeChannel(Channel ch) {
        // 若信道不为空
        if (null != ch) {
            // 从信道组中移除信道
            _channelGroup.remove(ch);
        }
    }

    /**
     * 广播消息
     *
     * @param msg
     */
    static public void broadcast(Object msg) {
        // 若消息不为空
        if (null != msg) {
            // 将消息广播出去给其他信道
            _channelGroup.writeAndFlush(msg);
        }
    }
}
