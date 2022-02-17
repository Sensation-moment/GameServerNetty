package org.tinygame.herostory.model;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户管理器(工具类)
 */
public final class UserManager {
    /**
     * 用户字典，用于记录后来登录的用户无法看到前面已经登录用户的位置
     */
    static private final Map<Integer, User> _userMap = new ConcurrentHashMap<>();

    /**
     * 私有化类默认构造器
     */
    private UserManager() {

    }

    /**
     * 添加用户到字典
     *
     * @param user
     */
    static public void addUser(User user) {
        // 校验合法性
        if (null != user) {
            // 防止并发问题
            _userMap.putIfAbsent(user.userId, user);
        }
    }

    /**
     * 根据用户ID从字典中移除用户
     *
     * @param userId
     */
    static public void removeByUserId(int userId) {
        _userMap.remove(userId);
    }


    /**
     * 列表用户
     *
     * @return
     */
    static public Collection<User> listUser() {
        return _userMap.values();
    }

    /**
     * 根据用户ID获取用户
     *
     * @param userId 用户ID
     * @return
     */
    static public User getByUserId(int userId) {
        return _userMap.get(userId);
    }
}
