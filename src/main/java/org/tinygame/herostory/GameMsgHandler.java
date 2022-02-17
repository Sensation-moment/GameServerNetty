package org.tinygame.herostory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.model.UserManager;
import org.tinygame.herostory.msg.GameMsgProtocol;

/**
 * 自定义的消息处理器
 */
public class GameMsgHandler extends SimpleChannelInboundHandler<Object> {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(GameMsgHandler.class);

    /**
     * 处理客户端刚连接上事件
     *
     * @param ctx
     * @throws Exception
     */
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 校验合法性
        if (ctx == null) {
            return;
        }

        try {
            super.channelActive(ctx);
            // 添加信道
            Broadcaster.addChannel(ctx.channel());
        }catch (Exception ex) {
            // 记录错误日志
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * 处理玩家退场逻辑
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // 校验合法性
        if (null == ctx) {
            return;
        }
        try {
            super.handlerRemoved(ctx);
            // 移除信道
            Broadcaster.removeChannel(ctx.channel());

            // 获取Session中保存的用户ID
            Integer userId = (Integer)ctx.channel().attr(AttributeKey.valueOf("userId")).get();
            // 校验用户ID的合法性(如果为空，则直接返回无需执行后续内容，避免空值错误导致服务器雪崩效应)
            if (null == userId) {
                return;
            }
            // 从用户字典中移除当前用户
            UserManager.removeByUserId(userId);

            // 构造返回对象
            GameMsgProtocol.UserQuitResult.Builder resultBuilder = GameMsgProtocol.UserQuitResult.newBuilder();
            // 填充
            resultBuilder.setQuitUserId(userId);
            GameMsgProtocol.UserQuitResult newResult = resultBuilder.build();
            // 广播出去
            Broadcaster.broadcast(newResult);
        }catch (Exception ex) {
            // 记录错误日志
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * 处理接收到的消息
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (null == ctx || null == msg) {
            return;
        }
        //LOGGER.info("收到客户端消息, msg = {}", msg);

        // 跳转到主消息处理器
        MainMsgProcessor.getInstance().process(ctx, msg);
    }
}
