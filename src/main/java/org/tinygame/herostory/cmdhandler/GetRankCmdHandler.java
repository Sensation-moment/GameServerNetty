package org.tinygame.herostory.cmdhandler;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.msg.GameMsgProtocol;
import org.tinygame.herostory.rank.RankItem;
import org.tinygame.herostory.rank.RankService;

import java.util.Collections;

/**
 * 获取排行榜指令处理器
 */
public class GetRankCmdHandler implements ICmdHandler<GameMsgProtocol.GetRankCmd>{
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(GetRankCmdHandler.class);

    /**
     * 处理获取排行榜消息
     *
     * @param ctx
     * @param cmd
     */
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.GetRankCmd cmd) {
        // 校验
        if (null == ctx || null == cmd) {
            return;
        }

        RankService.getInstance().getRank((rankItemList) -> {
            // 判空
            if (null == rankItemList) {
                rankItemList = Collections.emptyList();
            }

            // 构建对象
            GameMsgProtocol.GetRankResult.Builder resultBuilder = GameMsgProtocol.GetRankResult.newBuilder();

            // 遍历列表
            for (RankItem rankItem : rankItemList) {
                // 为空则跳过
                if (null == rankItem) {
                    continue;
                }

                GameMsgProtocol.GetRankResult.RankItem.Builder rankItemBuilder = GameMsgProtocol.GetRankResult.RankItem.newBuilder();
                // 填充属性
                rankItemBuilder.setRankId(rankItem.rankId);
                rankItemBuilder.setUserId(rankItem.userId);
                rankItemBuilder.setUserName(rankItem.userName);
                rankItemBuilder.setHeroAvatar(rankItem.heroAvatar);
                rankItemBuilder.setWin(rankItem.win);

                resultBuilder.addRankItem(rankItemBuilder);
            }
            GameMsgProtocol.GetRankResult newResult = resultBuilder.build();
            // 发送消息
            ctx.writeAndFlush(newResult);

            return null;
        });
    }
}
