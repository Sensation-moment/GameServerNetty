public class TestMain {
    public static void main(String[] args) {
        for (int i = 0 ; i < 10000 ; i++) {
            System.out.println("第" + i + "次测试");
            (new TestMain()).test3();
        }
    }

    /**
     * 多线程同时修改同一条数据，会出现并发数据错乱问题
     */
    public void test1() {
        // 新建一个测试用户
        TestUser newUser = new TestUser();
        // 设定初始血量
        newUser.currHp = 100;

        // 开两个线程共同操这个用户
        Thread t0 = new Thread(() -> {
            newUser.currHp = newUser.currHp - 1;
        });
        Thread t1 = new Thread(() -> {
            newUser.currHp = newUser.currHp - 1;
        });

        // 启动两个线程
        t0.start();
        t1.start();

        // 卡住线程，让线程不要退出
        try {
            t0.join();
            t1.join();
        }catch (Exception ex) {
            ex.printStackTrace();
        }

        // 若血量≠98，则抛出一个异常
        if (newUser.currHp != 98) {
            throw new RuntimeException("当前血量错误, currHp = " + newUser.currHp);
        }else {
            System.out.println("当前血量正确");
        }
    }

    /**
     * 利用synchronized关键字同步数据，避免并发数据错乱问题
     */
    public void test2() {
        // 新建一个测试用户
        TestUser newUser = new TestUser();
        // 设定初始血量
        newUser.currHp = 100;

        // 开两个线程共同操作这个用户
        Thread t0 = new Thread(() -> {
            newUser.subtractHp(1);
        });
        Thread t1 = new Thread(() -> {
            newUser.subtractHp(1);
        });

        // 启动两个线程
        t0.start();
        t1.start();

        // 卡住线程，让线程不要退出
        try {
            t0.join();
            t1.join();
        }catch (Exception ex) {
            ex.printStackTrace();
        }

        // 若血量≠98，则抛出一个异常
        if (newUser.currHp != 98) {
            throw new RuntimeException("当前血量错误, currHp = " + newUser.currHp);
        }else {
            System.out.println("当前血量正确");
        }
    }

    /**
     * 加了synchronized关键字会导致死锁问题(syn力度过大)，所以要缩小锁力度，但是缩小了之后
     * 同样会导致死锁问题
     */
    public void test3() {
        TestUser user1 = new TestUser();
        user1.currHp = 100;

        TestUser user2 = new TestUser();
        user2.currHp = 100;

        // 开两个线程共同操作这个用户
        Thread t0 = new Thread(() -> {
            user1.attkUser(user2);
        });
        Thread t1 = new Thread(() -> {
            user2.attkUser(user1);
        });

        // 启动两个线程
        t0.start();
        t1.start();

        // 卡住线程，让线程不要退出
        try {
            t0.join();
            t1.join();
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
