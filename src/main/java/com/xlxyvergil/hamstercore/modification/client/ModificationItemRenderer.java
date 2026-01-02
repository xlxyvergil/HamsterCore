package com.xlxyvergil.hamstercore.modification.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 改装件物品渲染器
 * 基于TACZ的AttachmentItemRenderer实现
 */
public class ModificationItemRenderer extends BlockEntityWithoutLevelRenderer {

    public ModificationItemRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
        super(pBlockEntityRenderDispatcher, pEntityModelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        // 从ItemStack中获取改装件ID
        String modificationId = null;
        if (stack.hasTag() && stack.getTag().contains("ModificationId")) {
            modificationId = stack.getTag().getString("ModificationId");
        }
        
        poseStack.pushPose();
        
        if (modificationId != null) {
            // 构建材质资源位置
            // 材质路径格式：hamstercore:textures/modification/{modification_id}.png
            ResourceLocation textureLocation = new ResourceLocation(
                "hamstercore",
                "textures/modification/" + modificationId.replace(':', '/') + ".png"
            );
            
            // GUI 特殊渲染
            if (transformType == ItemDisplayContext.GUI) {
                poseStack.translate(0.5, 1.5, 0.5);
                poseStack.mulPose(Axis.ZN.rotationDegrees(180));
                // 使用TACZ风格渲染：渲染配件材质
                renderModificationTexture(poseStack, pBuffer, pPackedLight, pPackedOverlay, textureLocation);
            } else {
                poseStack.translate(0.5, 2, 0.5);
                // 反转模型
                poseStack.scale(-1, -1, 1);
                if (transformType == ItemDisplayContext.FIXED) {
                    poseStack.mulPose(Axis.YN.rotationDegrees(90f));
                }
                // 使用TACZ风格渲染：渲染配件材质
                renderModificationTexture(poseStack, pBuffer, pPackedLight, pPackedOverlay, textureLocation);
            }
        } else {
            // 没有改装件ID，渲染黑紫材质以提醒，参考TACZ实现
            poseStack.translate(0.5, 1.5, 0.5);
            poseStack.mulPose(Axis.ZN.rotationDegrees(180));
            VertexConsumer buffer = pBuffer.getBuffer(RenderType.entityTranslucent(MissingTextureAtlasSprite.getLocation()));
            renderSimplePlane(poseStack, buffer, pPackedLight, pPackedOverlay);
        }
        
        poseStack.popPose();
    }
    
    /**
     * 渲染改装件材质
     */
    private void renderModificationTexture(PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay, ResourceLocation textureLocation) {
        // 使用正确的渲染类型：entityCutout，用于实体和物品的不透明材质
        VertexConsumer vertexConsumer = pBuffer.getBuffer(RenderType.entityCutout(textureLocation));
        renderSimplePlane(poseStack, vertexConsumer, pPackedLight, pPackedOverlay);
    }
    
    /**
     * 渲染一个简单的平面，参考TACZ的实现
     */
    private void renderSimplePlane(PoseStack poseStack, VertexConsumer vertexConsumer, int pPackedLight, int pPackedOverlay) {
        // 简化的平面渲染，参考TACZ的SlotModel
        float size = 0.5f;
        
        // 渲染一个正方形平面
        poseStack.pushPose();
        
        // 调整大小
        poseStack.scale(size, size, size);
        
        // 渲染四边形
        // 顶点顺序：右上、左上、左下、右下
        vertexConsumer.vertex(poseStack.last().pose(), 1.0f, 1.0f, 0.0f).color(1.0f, 1.0f, 1.0f, 1.0f).uv(1.0f, 1.0f).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(0.0f, 0.0f, 1.0f).endVertex();
        vertexConsumer.vertex(poseStack.last().pose(), -1.0f, 1.0f, 0.0f).color(1.0f, 1.0f, 1.0f, 1.0f).uv(0.0f, 1.0f).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(0.0f, 0.0f, 1.0f).endVertex();
        vertexConsumer.vertex(poseStack.last().pose(), -1.0f, -1.0f, 0.0f).color(1.0f, 1.0f, 1.0f, 1.0f).uv(0.0f, 0.0f).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(0.0f, 0.0f, 1.0f).endVertex();
        vertexConsumer.vertex(poseStack.last().pose(), 1.0f, -1.0f, 0.0f).color(1.0f, 1.0f, 1.0f, 1.0f).uv(1.0f, 0.0f).overlayCoords(pPackedOverlay).uv2(pPackedLight).normal(0.0f, 0.0f, 1.0f).endVertex();
        
        poseStack.popPose();
    }
}