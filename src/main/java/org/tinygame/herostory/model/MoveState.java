package org.tinygame.herostory.model;

/**
 * 移动状态
 */
public class MoveState {
    /**
     * 起始位置 X
     * 位置均设置为浮点型
     */
    public float fromPosX;

    /**
     * 起始位置 Y
     */
    public float fromPosY;

    /**
     * 目标位置 X
     */
    public float toPosX;

    /**
     * 目标位置 Y
     */
    public float toPosY;

    /**
     * 起始时间
     * 因为要用到系统时间，所以设置为长整型long
     */
    public long startTime;
}
