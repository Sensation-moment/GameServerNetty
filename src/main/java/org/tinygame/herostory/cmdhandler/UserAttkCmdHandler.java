package org.tinygame.herostory.cmdhandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.Broadcaster;
import org.tinygame.herostory.model.User;
import org.tinygame.herostory.model.UserManager;
import org.tinygame.herostory.mq.MqProducer;
import org.tinygame.herostory.mq.VictorMsg;
import org.tinygame.herostory.msg.GameMsgProtocol;

/**
 * 用户攻击命令处理器
 */
public class UserAttkCmdHandler implements ICmdHandler<GameMsgProtocol.UserAttkCmd> {
    /**
     * 日志对象
     */
    static private Logger LOGGER = LoggerFactory.getLogger(UserAttkCmdHandler.class);

    /**
     * 处理用户攻击消息
     *
     * @param ctx
     * @param userAttkCmd
     */
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserAttkCmd userAttkCmd) {
        // 打印攻击日志
        LOGGER.info("UserAttk");

        // 校验合法性(若有空则直接返回)
        if (null == ctx || null == userAttkCmd) {
            return;
        }

        // 获取攻击用户ID
        Integer attkUserId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();

        // 判空
        if (attkUserId == null) {
            return;
        }

        // 获取目标用户ID
        int targetUserId = userAttkCmd.getTargetUserId();
        // 获取目标用户
        User targetUser = UserManager.getByUserId(targetUserId);

        // 若目标用户为空，也即没有目标用户
        if (null == targetUser) {
            // 若没有打到人，也要广播一下消息
            broadcastSubtractHpResult(attkUserId, -1);
            return;
        }

        // 查看一下当前线程
        LOGGER.info("当前线程 = {}", Thread.currentThread().getName());

        // 伤害点数为10
        final int dmgPoint = 10;
        // 受到攻击，目标用户掉血
        targetUser.currHp = targetUser.currHp - dmgPoint;

        // 广播攻击结果
        broadcastAttkResult(attkUserId, targetUserId);
        // 广播减血结果
        broadcastSubtractHpResult(targetUserId, dmgPoint);
        // 判断目标用户血量是否低于0
        if (targetUser.currHp <= 0) {// 如果低于0
            // 广播死亡结果
            broadcastDieResult(targetUserId);

            // 当有人被击杀，排行榜要变动
            // 初始化胜利消息
            VictorMsg newMsg = new VictorMsg();
            // 填充胜利者和战败者ID
            newMsg.winnerId = attkUserId;
            newMsg.loserId = targetUserId;
            MqProducer.sendMsg("herostory_victor", newMsg);
        }
    }

    /**
     * 广播攻击结果
     *
     * @param attkUserId 攻击用户ID
     * @param targetUserId 目标用户ID
     */
    static private void broadcastAttkResult(int attkUserId, int targetUserId) {
        // 判断攻击用户ID是否存在
        if (attkUserId <= 0) {
            return;
        }
        GameMsgProtocol.UserAttkResult.Builder resultBuilder = GameMsgProtocol.UserAttkResult.newBuilder();
        resultBuilder.setAttkUserId(attkUserId);
        resultBuilder.setTargetUserId(targetUserId);

        GameMsgProtocol.UserAttkResult newResult = resultBuilder.build();
        Broadcaster.broadcast(newResult);
    }

    /**
     * 广播减血结果
     *
     * @param targetUserId 目标用户ID
     * @param substractHp 减少的血量
     */
    static private void broadcastSubtractHpResult(int targetUserId, int substractHp) {
        // 判空
        if (targetUserId <= 0 || substractHp <= 0) {
            return;
        }

        GameMsgProtocol.UserSubtractHpResult.Builder resultBuilder = GameMsgProtocol.UserSubtractHpResult.newBuilder();
        resultBuilder.setTargetUserId(targetUserId);
        resultBuilder.setSubtractHp(substractHp);

        GameMsgProtocol.UserSubtractHpResult newResult = resultBuilder.build();
        Broadcaster.broadcast(newResult);
    }

    /**
     * 广播死亡结果
     *
     * @param targetUserId 目标用户ID
     */
    static private void broadcastDieResult(int targetUserId) {
        // 判空
        if (targetUserId <= 0) {
            return;
        }

        GameMsgProtocol.UserDieResult.Builder resultBuilder = GameMsgProtocol.UserDieResult.newBuilder();
        resultBuilder.setTargetUserId(targetUserId);

        GameMsgProtocol.UserDieResult newResult = resultBuilder.build();
        Broadcaster.broadcast(newResult);
    }
}
