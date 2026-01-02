package com.xlxyvergil.hamstercore.modification.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
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
        
        // GUI 特殊渲染
        if (transformType == ItemDisplayContext.GUI) {
            poseStack.translate(0.5, 1.5, 0.5);
            poseStack.mulPose(Axis.ZN.rotationDegrees(180));
        } else {
            poseStack.translate(0.5, 2, 0.5);
            // 反转模型
            poseStack.scale(-1, -1, 1);
            if (transformType == ItemDisplayContext.FIXED) {
                poseStack.mulPose(Axis.YN.rotationDegrees(90f));
            }
        }
        
        // 根据改装件ID渲染材质
        if (modificationId != null) {
            // 构建材质资源位置
            // 材质路径格式：hamstercore:textures/modification/{modification_id}.png
            ResourceLocation textureLocation = new ResourceLocation(
                "hamstercore",
                "textures/modification/" + modificationId.replace(':', '/') + ".png"
            );
            
            // 创建渲染类型
            RenderType renderType = RenderType.entityTranslucent(textureLocation);
            
            // 获取VertexConsumer
            // 这里可以添加自定义模型渲染逻辑，目前暂时使用简单的方块渲染
            // 后续可以扩展为更复杂的模型渲染
            poseStack.scale(0.5f, 0.5f, 0.5f);
            
            // 使用默认物品渲染器渲染
            // 自定义材质将通过模型文件中的predicate和custom_model_data来应用
            // 这里保持默认渲染，确保物品能够正常显示
            Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                transformType,
                pPackedLight,
                pPackedOverlay,
                poseStack,
                pBuffer,
                null,
                0
            );
        }
        
        poseStack.popPose();
    }
}