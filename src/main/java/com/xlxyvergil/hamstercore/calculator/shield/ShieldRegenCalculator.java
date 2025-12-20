package com.xlxyvergil.hamstercore.calculator.shield;


/**
 * 护盾恢复计算器
 */
public class ShieldRegenCalculator {
    
    /**
     * 计算护盾恢复速度
     * 每秒护盾回复 = 15 + 0.05 × 护盾容量
     * @param maxShield 最大护盾值
     * @return 每秒恢复的护盾值
     */
    public static float calculateRegenRate(float maxShield) {
        return 15.0f + 0.05f * maxShield;
    }
    
    /**
     * 计算护盾恢复延迟
     * 玩家护盾恢复延迟：2秒
     * 玩家护盾耗尽时恢复延迟：6秒
     * 怪物（mobs）护盾恢复延迟：3秒
     * @param isPlayer 是否为玩家
     * @param isDepleted 护盾是否耗尽
     * @return 恢复延迟（tick）
     */
    public static int calculateRegenDelay(boolean isPlayer, boolean isDepleted) {
        if (isPlayer) {
            if (isDepleted) {
                return 6 * 20; // 6秒
            } else {
                return 2 * 20; // 2秒
            }
        } else {
            return 3 * 20; // 3秒
        }
    }
}