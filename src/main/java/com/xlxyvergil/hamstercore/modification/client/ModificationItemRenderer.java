package com.xlxyvergil.hamstercore.modification.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 改装件物品渲染器
 * 参考Apotheosis的GemModel实现
 */
public class ModificationItemRenderer extends BlockEntityWithoutLevelRenderer {

    public ModificationItemRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
        super(pBlockEntityRenderDispatcher, pEntityModelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        // 直接使用Minecraft的默认渲染器，让它根据ItemStack的NBT和模型系统自动选择正确的模型
        Minecraft.getInstance().getItemRenderer().render(stack, transformType, false, poseStack, pBuffer, pPackedLight, pPackedOverlay, Minecraft.getInstance().getItemRenderer().getModel(stack, null, null, 0));
    }
}