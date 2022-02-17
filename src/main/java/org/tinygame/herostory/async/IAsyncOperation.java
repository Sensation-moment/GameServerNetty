package org.tinygame.herostory.async;

/**
 * 异步操作接口
 */
public interface IAsyncOperation {
    /**
     * 获取绑定ID
     *
     * @return
     */
    default int getBindId() {
        return 0;
    }

    /**
     * 执行异步操作
     */
    void doAsync();

    /**
     * 执行完成逻辑
     * JDK8支持的接口方法默认实现
     */
    default void doFinish() {

    }
}
