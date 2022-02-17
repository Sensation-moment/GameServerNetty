package org.tinygame.herostory;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义的消息编码器(Out输出)
 */
public class GameMsgEncoder extends ChannelOutboundHandlerAdapter {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(GameMsgEncoder.class);

    /**
     * 消息编码
     *
     * @param ctx
     * @param msg
     * @param promise
     * @throws Exception
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // 校验合法性
        if (null == ctx || null == msg) {
            return;
        }

        try {
            // 判断消息类型，如果不是Protobuf消息，则直接返回就行
            if (!(msg instanceof GeneratedMessageV3)) {
                super.write(ctx, msg, promise);
                return;
            }

            // 通过消息类型获取对应的消息编码
            int msgCode = GameMsgRecognizer.getMsgCodeByClazz(msg.getClass());

            // 若获取到的消息编码为-1
            if (-1 == msgCode) {
                // 记录错误日志
                LOGGER.error(
                        "无法识别的消息类型, msgClazz = {}",
                        msg.getClass().getSimpleName()
                );
                super.write(ctx, msg, promise);
                return;
            }

            // 转化为字节数组(消息体)
            byte[] msgBody = ((GeneratedMessageV3) msg).toByteArray();

            // 生成消息存储缓冲区
            ByteBuf byteBuf = ctx.alloc().buffer();
            // 消息的长度
            byteBuf.writeShort((short)msgBody.length);
            // 消息的编号
            byteBuf.writeShort((short)msgCode);
            // 消息体
            byteBuf.writeBytes(msgBody);

            // 接收到的消息是这个类型，将消息写回去也是这个类型
            BinaryWebSocketFrame outputFrame = new BinaryWebSocketFrame(byteBuf);

            super.write(ctx, outputFrame, promise);
        }catch (Exception ex) {
            // 记录错误日志
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}
