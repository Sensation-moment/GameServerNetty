package org.tinygame.herostory.async;

import org.tinygame.herostory.MainMsgProcessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 异步操作处理器
 */
public final class AsyncOperationProcessor {
    /**
     * 单例对象
     */
    static private final AsyncOperationProcessor _instance = new AsyncOperationProcessor();
    /**
     * 创建单线程数组(多个单线程的线程池)
     */
    private final ExecutorService[] _esArray = new ExecutorService[8];

    /**
     * 私有化类默认构造器
     */
    private AsyncOperationProcessor() {
        for (int i = 0 ; i < _esArray.length ; i++) {
            // 初始化当前线程的名字
            final String threadName = "AsyncOperationProcessor[ " + i + " ]";
            // 生成单线程线程池
            _esArray[i] = Executors.newSingleThreadExecutor((r) -> {
                Thread t = new Thread(r);
                // 起个名
                t.setName(threadName);
                return t;
            });
        }
    }

    /**
     * 获取单例对象
     *
     * @return 单例对象
     */
    static public AsyncOperationProcessor getInstance() {
        return _instance;
    }

    /**
     * 执行异步操作
     *
     * @param op
     */
    public void process(IAsyncOperation op) {
        // 判空
        if (null == op) {
            return;
        }

        // 避免为负数
        int bindId = Math.abs(op.getBindId());
        int esIndex = bindId % _esArray.length;
        _esArray[esIndex].submit(() -> {
            // 执行异步操作
            op.doAsync();
            // 回到主线程执行完成逻辑
            //MainMsgProcessor.getInstance().process(op::doFinish);
            MainMsgProcessor.getInstance().process(() -> op.doFinish());
        });
    }
}
