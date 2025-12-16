package com.xlxyvergil.hamstercore.handler.modifier;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 元素修饰器接口
 * 参考TACZ的IAttachmentModifier设计
 *
 * @param <T> Json读取后处理的数据类型
 * @param <K> 元素缓存属性值
 */
public interface IElementModifier<T, K> {
    /**
     * 元素修饰器ID
     *
     * @return 修饰器唯一标识符
     */
    String getId();

    /**
     * 初始化缓存，用于填入武器的默认数据
     *
     * @param weapon     当前武器物品
     * @param attacker   攻击者
     * @param target     目标
     * @param baseDamage 基础伤害值
     * @return 初始化读取的数据
     */
    K initCache(ItemStack weapon, LivingEntity attacker, LivingEntity target, float baseDamage);

    /**
     * 计算，用于将各个元素数据与武器数据求值，最终计算出来
     *
     * @param modifiedValues 各个元素的数据值
     * @param cache          缓存的武器默认数值
     */
    void eval(List<T> modifiedValues, K cache);
}