package org.tinygame.herostory;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义的消息解码器(In输入)
 */
public class GameMsgDecoder extends ChannelInboundHandlerAdapter {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(GameMsgDecoder.class);

    /**
     * 解码消息
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 校验合法性
        if (null == ctx || null == msg) {
            return;
        }
        // 如果不是这种消息，那就返回不用运行后面的，因为无法解析
        if (!(msg instanceof BinaryWebSocketFrame)) {
            return;
        }
        try {
            BinaryWebSocketFrame inputFrame = (BinaryWebSocketFrame) msg;
            // 获取消息内容，得到字节的缓冲区
            ByteBuf byteBuf = inputFrame.content();
            // 读取消息的长度(短整型是2个字节)
            byteBuf.readShort();
            // 读取消息编号
            int msgCode = byteBuf.readShort();
            // 定义字节数组读取消息剩下的内容，也即是消息体(用剩下的长度来表示字节数组长度)
            byte[] msgBody = new byte[byteBuf.readableBytes()];
            // 把剩下的内容读取到字节数组中来
            byteBuf.readBytes(msgBody);

            // 获取消息构建器
            Message.Builder msgBuilder = GameMsgRecognizer.getBuilderByMsgCode(msgCode);

            // 校验合法性判空(DIM)
            if (null == msgBuilder) {
                return;
            }

            msgBuilder.clear();
            msgBuilder.mergeFrom(msgBody);

            // 构建消息实体
            Message cmd = msgBuilder.build();

            // 解析出的消息不为空
            if (null != cmd) {
                // 则放回到流水线中
                ctx.fireChannelRead(cmd);
            }
        } catch (Exception ex) {
            // 记录错误日志
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}
