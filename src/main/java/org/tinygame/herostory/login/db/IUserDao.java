package org.tinygame.herostory.login.db;

public interface IUserDao {
    /**
     * 根据用户名称获取实体
     *
     * @param username 用户名称
     * @return
     */
    UserEntity getByUserName(String username);

    /**
     * 添加用户实体
     *
     * @param newEntity 用户实体
     */
    void insertInto(UserEntity newEntity);
}
