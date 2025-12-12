package com.xlxyvergil.hamstercore.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xlxyvergil.hamstercore.element.WeaponElementData;
import com.xlxyvergil.hamstercore.element.modifier.ElementCombinationModifier;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;

public class GunRenderer {
    
    /**
     * 计算武器元素组合
     * @param data 武器元素数据
     */
    private static void computeElementCombinations(WeaponElementData data) {
        ElementCombinationModifier.apply(data);
    }
}
