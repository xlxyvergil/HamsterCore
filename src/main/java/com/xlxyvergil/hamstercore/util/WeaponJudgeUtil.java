package com.xlxyvergil.hamstercore.util;

import com.google.common.collect.Multimap;
import com.xlxyvergil.hamstercore.compat.ModCompat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraftforge.common.Tags;

import java.util.HashSet;
import java.util.Set;

/**
 * 武器判断工具类
 * 专门用于判断物品是否为武器或工具的逻辑
 */
public class WeaponJudgeUtil {
    
    /**
     * 检查物品是否为武器或工具
     * 使用判断方法：
     * 1. 判断物品的itemtag里是否有'#forge:tools'或其子标签
     * @param item 要检查的物品
     * @return 如果是武器或工具返回true
     */
    public static boolean isWeaponOrTool(Item item) {
        // 1. 检查是否在forge:tools标签或其子标签中
        if (isInToolsTagOrSubtags(item)) {
            return true;
        }
        
        return isInToolsTagOrSubtags(item);
    }

    /**
     * 检查物品是否在forge:tools标签或其子标签中
     * @param item 要检查的物品
     * @return 如果在tools标签或其子标签中返回true
     */
    private static boolean isInToolsTagOrSubtags(Item item) {
        // 遍历物品的所有标签
        for (TagKey<Item> tagKey : item.builtInRegistryHolder().tags().toList()) {
            // 检查是否是forge:tools标签或其子标签
            if (tagKey.location().getNamespace().equals("forge") && 
                tagKey.location().getPath().startsWith("tools")) {
                return true;
            }
        }
        return false;
    }
}