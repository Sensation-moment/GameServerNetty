package org.tinygame.herostory.rank;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.tinygame.herostory.async.AsyncOperationProcessor;
import org.tinygame.herostory.async.IAsyncOperation;
import org.tinygame.herostory.util.RedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * 排行榜服务
 */
@Service
public class RankService {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(RankService.class);
    /**
     * 单例对象
     */
    static private final RankService _instance = new RankService();

    /**
     * 私有化类默认构造器
     */
    private RankService() {

    }

    /**
     * 获取单例对象
     *
     * @return 单例对象
     */
    static public RankService getInstance() {
        return _instance;
    }

    /**
     * 获取排行榜
     *
     * @param callback 回调函数
     */
    public void getRank(Function<List<RankItem>, Void> callback) {
        // 校验
        if (null == callback) {
            return;
        }

        AsyncOperationProcessor.getInstance().process(new AsyncGetRank() {
            @Override
            public void doFinish() {
                callback.apply(this.getRankItemList());
            }
        });
    }

    /**
     * 异步方式获取排行榜
     */
    static private class AsyncGetRank implements IAsyncOperation {
        /**
         * 排名条目列表
         */
        private List<RankItem> _rankItemList;

        /**
         * 获取排名条目列表
         *
         * @return 排名条目列表
         */
        List<RankItem> getRankItemList() {
            return _rankItemList;
        }

        @Override
        public void doAsync() {
            try (Jedis redis = RedisUtil.getJedis()){
                // 获取集合字符串，定位获取排行榜的范围，拿10个，所以是0-9
                Set<Tuple> valSet = redis.zrevrangeWithScores("Rank", 0, 9);

                List<RankItem> rankItemList = new ArrayList<>();
                int i = 0;

                for (Tuple t : valSet) {

                    // 判空
                    if (null == t)
                        // 跳过
                        continue;

                    // 获取用户ID
                    int userId = Integer.parseInt(t.getElement());
                    // 获取用户信息
                    String jsonStr = redis.hget("User_" + userId, "BasicInfo");

                    // 判空
                    if (null == jsonStr)
                        // 跳过
                        continue;

                    RankItem newItem = new RankItem();
                    newItem.rankId = ++i;
                    newItem.userId = userId;
                    // 获取分数
                    newItem.win = (int) t.getScore();

                    JSONObject jsonObj = JSONObject.parseObject(jsonStr);
                    newItem.userName = jsonObj.getString("userName");
                    newItem.heroAvatar = jsonObj.getString("heroAvatar");

                    // 放到列表中
                    rankItemList.add(newItem);
                }
                // 赋值回去
                _rankItemList = rankItemList;
            }catch (Exception ex) {
                // 记录错误日志
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }

    /**
     * 刷新排行榜
     *
     * @param winnerId 胜利者ID
     * @param loserId  战败者ID
     */
    public void refreshRank(int winnerId, int loserId) {
        // 校验
        if (winnerId <= 0 || loserId <= 0)
            return;

        try (Jedis redis = RedisUtil.getJedis()) {
            // 执行 hincrby "用户_Id" "Win" 1
            redis.hincrBy("User_" + winnerId, "Win", 1);
            // 执行 hincrby "用户_Id" "Lose" 1
            redis.hincrBy("User_" + loserId, "Lose", 1);

            // 执行 hget "用户_Id" "Win"
            // 看看玩家赢了多少次?
            String winStr = redis.hget("User_" + winnerId, "Win");
            int winNum = Integer.parseInt(winStr);

            // 修改排名
            redis.zadd("Rank", winNum, String.valueOf(winnerId));
        } catch (Exception ex) {
            // 记录错误日志
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}
