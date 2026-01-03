package com.xlxyvergil.hamstercore.element.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * 元素效果数据辅助类
 * 使用实体的PersistentData存储额外的效果数据（如伤害值、伤害源等）
 * 参考Apotheosis的实现方式
 */
public class ElementEffectDataHelper {

    // 数据键前缀
    private static final String KEY_PREFIX = "hamstercore.effect.";

    /**
     * 设置效果的伤害数据
     * @param entity 实体
     * @param effect 效果
     * @param finalDamage 最终伤害值
     */
    public static void setEffectDamage(LivingEntity entity, ElementEffect effect, float finalDamage) {
        String key = getDataKey(effect);
        CompoundTag data = entity.getPersistentData();
        data.putFloat(key + ".damage", finalDamage);
    }

    /**
     * 获取效果的伤害数据
     * @param entity 实体
     * @param effect 效果
     * @return 伤害值，如果不存在则返回0
     */
    public static float getEffectDamage(LivingEntity entity, ElementEffect effect) {
        String key = getDataKey(effect);
        CompoundTag data = entity.getPersistentData();
        return data.getFloat(key + ".damage");
    }

    /**
     * 清除效果的伤害数据
     * @param entity 实体
     * @param effect 效果
     */
    public static void clearEffectData(LivingEntity entity, ElementEffect effect) {
        String key = getDataKey(effect);
        CompoundTag data = entity.getPersistentData();
        data.remove(key + ".damage");
        data.remove(key + ".damage_source_type");
        data.remove(key + ".damage_source_entity");
    }

    /**
     * 检查是否有效果数据
     * @param entity 实体
     * @param effect 效果
     * @return 是否有数据
     */
    public static boolean hasEffectData(LivingEntity entity, ElementEffect effect) {
        String key = getDataKey(effect);
        return entity.getPersistentData().contains(key + ".damage");
    }

    /**
     * 获取数据键
     * @param effect 效果
     * @return 数据键
     */
    private static String getDataKey(ElementEffect effect) {
        ResourceLocation effectId = ElementEffectRegistry.getEffectId(effect);
        return KEY_PREFIX + effectId.toString().replace(":", ".");
    }

    /**
     * 当效果移除时自动清理数据
     * @param entity 实体
     * @param effectInstance 效果实例
     */
    public static void onEffectRemoved(LivingEntity entity, MobEffectInstance effectInstance) {
        if (effectInstance.getEffect() instanceof ElementEffect effect) {
            clearEffectData(entity, effect);
        }
    }
}
