package org.tinygame.herostory.cmdhandler;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.model.User;
import org.tinygame.herostory.model.UserManager;
import org.tinygame.herostory.msg.GameMsgProtocol;

import java.util.Collection;

/**
 * 用户查询其他用户在场处理器
 */
public class WhoElseIsHereCmdHandler implements ICmdHandler<GameMsgProtocol.WhoElseIsHereCmd>{
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(WhoElseIsHereCmdHandler.class);

    /**
     * 处理查询其他用户在场消息
     *
     * @param ctx
     */
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.WhoElseIsHereCmd cmd) {
        // 校验合法性
        if (null == ctx || null == cmd) {
            return;
        }
        GameMsgProtocol.WhoElseIsHereResult.Builder resultBuilder = GameMsgProtocol.WhoElseIsHereResult.newBuilder();
        // 获取用户列表
        Collection<User> userList = UserManager.listUser();
        // 遍历用户字典
        for (User curUser : userList) {
            // 跳过为空的用户
            if (null == curUser) {
                continue;
            }
            GameMsgProtocol.WhoElseIsHereResult.UserInfo.Builder userInfoBuilder = GameMsgProtocol.WhoElseIsHereResult.UserInfo.newBuilder();
            userInfoBuilder.setUserId(curUser.userId);
            userInfoBuilder.setHeroAvatar(curUser.heroAvatar);

            // 构建移动状态
            GameMsgProtocol.WhoElseIsHereResult.UserInfo.MoveState.Builder mvStateBuilder = GameMsgProtocol.WhoElseIsHereResult.UserInfo.MoveState.newBuilder();
            // 填充
            mvStateBuilder.setFromPosX(curUser.moveState.fromPosX);
            mvStateBuilder.setFromPosY(curUser.moveState.fromPosY);
            mvStateBuilder.setToPosX(curUser.moveState.toPosX);
            mvStateBuilder.setToPosY(curUser.moveState.toPosY);
            mvStateBuilder.setStartTime(curUser.moveState.startTime);

            userInfoBuilder.setMoveState(mvStateBuilder);

            resultBuilder.addUserInfo(userInfoBuilder);
        }
        GameMsgProtocol.WhoElseIsHereResult newResult = resultBuilder.build();
        // 注意：这里是谁询问给谁发，而不是群发
        ctx.writeAndFlush(newResult);
    }
}
