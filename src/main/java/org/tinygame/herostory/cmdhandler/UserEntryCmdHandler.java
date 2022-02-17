package org.tinygame.herostory.cmdhandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.Broadcaster;
import org.tinygame.herostory.model.User;
import org.tinygame.herostory.model.UserManager;
import org.tinygame.herostory.msg.GameMsgProtocol;

/**
 * 用户入场功能处理器
 */
public class UserEntryCmdHandler implements ICmdHandler<GameMsgProtocol.UserEntryCmd>{
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(UserEntryCmdHandler.class);

    /**
     * 处理用户入场消息
     *
     * @param ctx
     * @param cmd
     */
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserEntryCmd cmd) {
        // 校验合法性
        if (null == ctx || null == cmd) {
            return;
        }

        // 获取用户ID
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();

        // 判空
        if (null == userId) {
            return;
        }

        User existUser = UserManager.getByUserId(userId);

        // 想办法构建一个返回的对象，发出去即可
        GameMsgProtocol.UserEntryResult.Builder resultBuilder = GameMsgProtocol.UserEntryResult.newBuilder();
        // 填充对象属性
        resultBuilder.setUserId(userId);
        resultBuilder.setUserName(existUser.userName);
        resultBuilder.setHeroAvatar(existUser.heroAvatar);

        // 构建结果并广播出去(建造者模式)
        GameMsgProtocol.UserEntryResult newResult = resultBuilder.build();
        Broadcaster.broadcast(newResult);
    }
}
