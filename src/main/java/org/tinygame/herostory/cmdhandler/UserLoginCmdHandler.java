package org.tinygame.herostory.cmdhandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tinygame.herostory.login.LoginService;
import org.tinygame.herostory.model.User;
import org.tinygame.herostory.model.UserManager;
import org.tinygame.herostory.msg.GameMsgProtocol;

/**
 * 用户登录
 */
public class UserLoginCmdHandler implements ICmdHandler<GameMsgProtocol.UserLoginCmd>{
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(UserLoginCmdHandler.class);

    /**
     * 处理用户登录消息
     *
     * @param ctx
     * @param cmd
     */
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserLoginCmd cmd) {
        // 校验
        if (null == ctx || null == cmd) {
            return;
        }

        // 获取用户名和密码
        String userName = cmd.getUserName();
        String password = cmd.getPassword();

        // 判空
        if (null == userName || null == password) {
            return;
        }

        LOGGER.info("当前线程 = {}", Thread.currentThread().getName());

        // 获取用户实体
        LoginService.getInstance().userLogin(userName, password, (userEntity) -> {
            GameMsgProtocol.UserLoginResult.Builder resultBuilder = GameMsgProtocol.UserLoginResult.newBuilder();

            LOGGER.info("当前线程 = {}", Thread.currentThread().getName());

            // 判空
            if (null == userEntity) {// 登录不成功
                // 规定返回-1
                resultBuilder.setUserId(-1);
                resultBuilder.setUserName("");
                resultBuilder.setHeroAvatar("");
            }else {// 登录成功
                User newUser = new User();
                // 设置用户ID
                newUser.userId = userEntity.userId;
                // 用户名称
                newUser.userName = userEntity.userName;
                // 英雄形象
                newUser.heroAvatar = userEntity.heroAvatar;
                // 血量
                newUser.currHp = 100;
                // 将当前用户加入到用户管理器中
                UserManager.addUser(newUser);

                // 通俗一点说，也就是给这个信道加一个key-value，其中一个作用就是保存用户userId(将用户ID保存至Session)
                ctx.channel().attr(AttributeKey.valueOf("userId")).set(newUser.userId);

                resultBuilder.setUserId(userEntity.userId);
                resultBuilder.setUserName(userEntity.userName);
                resultBuilder.setHeroAvatar(userEntity.heroAvatar);
            }

            GameMsgProtocol.UserLoginResult newResult = resultBuilder.build();
            ctx.writeAndFlush(newResult);
            return null;
        });
    }
}
