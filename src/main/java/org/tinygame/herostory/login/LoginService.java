package org.tinygame.herostory.login;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.tinygame.herostory.MySqlSessionFactory;
import org.tinygame.herostory.async.AsyncOperationProcessor;
import org.tinygame.herostory.async.IAsyncOperation;
import org.tinygame.herostory.login.db.IUserDao;
import org.tinygame.herostory.login.db.UserEntity;
import org.tinygame.herostory.util.RedisUtil;
import redis.clients.jedis.Jedis;

import java.util.function.Function;

/**
 * 登录服务
 */
@Service
public final class LoginService {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);
    /**
     * 单例对象
     */
    static private final LoginService _instance = new LoginService();

    /**
     * 私有化类默认构造器
     */
    private LoginService() {

    }

    /**
     * 获取单例对象
     *
     * @return
     */
    static public LoginService getInstance() {
        return _instance;
    }

    /**
     * 用户登录
     *
     * @param userName 用户名
     * @param password 密码
     * @return
     */
    public void userLogin(String userName, String password, Function<UserEntity, Void> callback) {
        // 校验合法性
        if (null == userName || null == password) {
            return;
        }

        AsyncGetUserEntity asyncOp = new AsyncGetUserEntity(userName, password) {
            @Override
            public void doFinish() {
                if (null != callback) {
                    callback.apply(this.getUserEntity());
                }
            }
        };
        AsyncOperationProcessor.getInstance().process(asyncOp);
    }

    /**
     * 更新Redis中的用户基本信息，也即在登录的时候将用户基本信息同步到Redis中
     *
     * @param userEntity 用户实体
     */
    private void updateBasicInfoInRedis(UserEntity userEntity) {
        // 判空
        if (null == userEntity) {
            return;
        }

        try (Jedis redis = RedisUtil.getJedis()) {
            JSONObject jsonObj = new JSONObject();
            // 无需存储整个用户实体
            jsonObj.put("userName", userEntity.userName);
            jsonObj.put("heroAvatar", userEntity.heroAvatar);

            redis.hset("User_" + userEntity.userId, "BasicInfo", jsonObj.toJSONString());
        } catch (Exception ex) {
            // 记录错误日志
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * 异步方式获取用户实体
     */
    private class AsyncGetUserEntity implements IAsyncOperation {
        /**
         * 用户名称
         */
        private final String _userName;
        /**
         * 密码
         */
        private final String _password;
        /**
         * 用户实体
         */
        private UserEntity _userEntity;

        /**
         * 类参数构造器
         *
         * @param userName
         * @param password
         */
        AsyncGetUserEntity(String userName, String password) {
            _userName = userName;
            _password = password;
        }

        /**
         * 获取用户实体
         *
         * @return
         */
        public UserEntity getUserEntity() {
            return _userEntity;
        }

        @Override
        public int getBindId() {
            if (null == _userName) {
                return 0;
            }else {
                return _userName.charAt(_userName.length() - 1);
            }
        }

        @Override
        public void doAsync() {
            try(SqlSession mySqlSession = MySqlSessionFactory.openSession()) {
                // 获取DAO
                IUserDao dao = mySqlSession.getMapper(IUserDao.class);
                // 获取用户实体
                UserEntity userEntity = dao.getByUserName(_userName);

                LOGGER.info("当前线程 = {}", Thread.currentThread().getName());

                // 判空
                if (null != userEntity) {
                    if (!_password.equals(userEntity.password)) {
                        throw new RuntimeException("密码错误");
                    }
                }else {
                    // 新建用户存到数据库
                    userEntity = new UserEntity();
                    userEntity.userName = _userName;
                    userEntity.password = _password;
                    userEntity.heroAvatar = "Hero_Shaman";

                    dao.insertInto(userEntity);
                }

                // 更新Redis
                LoginService.getInstance().updateBasicInfoInRedis(userEntity);
                _userEntity = userEntity;
            }catch (Exception ex) {
                // 记录错误日志
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }
}
