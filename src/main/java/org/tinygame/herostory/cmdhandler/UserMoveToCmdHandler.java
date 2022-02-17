package org.tinygame.herostory.cmdhandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.tinygame.herostory.Broadcaster;
import org.tinygame.herostory.model.User;
import org.tinygame.herostory.model.UserManager;
import org.tinygame.herostory.msg.GameMsgProtocol;

/**
 * 用户查询其他用户移动处理器
 */
public class UserMoveToCmdHandler implements ICmdHandler<GameMsgProtocol.UserMoveToCmd>{
    /**
     * 处理查询其他用户移动消息
     *
     * @param ctx
     * @param cmd
     */
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserMoveToCmd cmd) {
        // 校验合法性
        if (null == ctx || null == cmd) {
            return;
        }
        // 获取Session中保存的用户ID
        Integer userId = (Integer)ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        // 校验用户ID的合法性(如果为空，则直接返回无需执行后续内容，避免空值错误导致服务器雪崩效应)
        if (null == userId) {
            return;
        }

        // 从用户管理器中拿到已经登录的用户
        User existUser = UserManager.getByUserId(userId);
        // 判空(考虑已有用户为空的情况)
        if (null == existUser) {
            return;
        }

        // 记录当前系统时间，用于填充用户信息
        long nowTime = System.currentTimeMillis();

        // 获取到当前用户的所有信息
        existUser.moveState.fromPosX = cmd.getMoveFromPosX();
        existUser.moveState.fromPosY = cmd.getMoveFromPosY();
        existUser.moveState.toPosX = cmd.getMoveToPosX();
        existUser.moveState.toPosY = cmd.getMoveToPosY();
        existUser.moveState.startTime = nowTime;

        // 构建返回对象
        GameMsgProtocol.UserMoveToResult.Builder resultBuilder = GameMsgProtocol.UserMoveToResult.newBuilder();
        // 填充对象属性
        // 用户ID
        resultBuilder.setMoveUserId(userId);
        // 起始定位横坐标
        resultBuilder.setMoveFromPosX(cmd.getMoveFromPosX());
        // 起始定位纵坐标
        resultBuilder.setMoveFromPosY(cmd.getMoveFromPosY());
        // 目标定位横坐标
        resultBuilder.setMoveToPosX(cmd.getMoveToPosX());
        // 目标定位纵坐标
        resultBuilder.setMoveToPosY(cmd.getMoveToPosY());
        // 移动开始时间(可以使用系统时间)
        resultBuilder.setMoveStartTime(nowTime);

        // 将消息对象广播出去
        GameMsgProtocol.UserMoveToResult newResult = resultBuilder.build();
        Broadcaster.broadcast(newResult);
    }
}
