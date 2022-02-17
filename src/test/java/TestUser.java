/**
 * 测试用户
 */
public class TestUser {
    /**
     * 当前血量
     */
    public int currHp;

    /**
     * 减血操作
     *
     * @param val
     */
    synchronized public void subtractHp(int val) {
        // 判空
        if (val <= 0) {
            return;
        }
        // 减血
        this.currHp = this.currHp - val;
    }

    /**
     * 攻击操作
     *
     * @param targetUser
     */
    public void attkUser(TestUser targetUser) {
        // 判空
        if (null == targetUser) {
            return;
        }

        synchronized (this) {
            final int dmgPoint = 10;
            targetUser.subtractHp(dmgPoint);
        }
    }
}
