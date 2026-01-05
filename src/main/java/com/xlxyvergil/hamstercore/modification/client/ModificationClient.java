package com.xlxyvergil.hamstercore.modification.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.client.renderer.item.WeaponAttributeRenderer;
import com.xlxyvergil.hamstercore.modification.ModificationInstance;
import com.xlxyvergil.hamstercore.modification.SocketHelper;
import com.xlxyvergil.hamstercore.modification.SocketedModifications;
import com.xlxyvergil.hamstercore.util.ElementNBTUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 改装系统客户端类 - 模仿 Apotheosis 的 AdventureModuleClient
 * 处理改装系统的客户端事件注册和渲染
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = HamsterCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModificationClient {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void modificationTooltips(RenderTooltipEvent.GatherComponents e) {
    }
    
    @SubscribeEvent
    public static void gatherModificationComponents(RenderTooltipEvent.GatherComponents e) {
        int sockets = SocketHelper.getSockets(e.getItemStack());
        int specialSockets = SocketHelper.getSpecialSockets(e.getItemStack());
        if (sockets == 0 && specialSockets == 0) return;
    }

    @SubscribeEvent
    public static void modifyTooltipComponents(RenderTooltipEvent.GatherComponents e) {
        // 不再将改装件组件添加到主tooltip中，而是在左侧新页面中渲染
        // 这会阻止现有改装件数据显示在主tooltip中
        int sockets = SocketHelper.getSockets(e.getItemStack());
        int specialSockets = SocketHelper.getSpecialSockets(e.getItemStack());
        if (sockets == 0 && specialSockets == 0) return;
        
        // 移除任何可能已经添加的改装件组件
        List<Either<FormattedText, TooltipComponent>> list = e.getTooltipElements();
        list.removeIf(element -> element.right().isPresent() && 
            element.right().get() instanceof ModificationTooltipRenderer.ModificationComponent);
    }
    
    @SubscribeEvent
    public static void onTooltipColor(RenderTooltipEvent.Color e) {
        ItemStack stack = e.getItemStack();
        
        if (needsLeftPage(stack)) {
            // 渲染右侧页面
            GuiGraphics guiGraphics = e.getGraphics();
            Font font = e.getFont();
            
            // 计算主tooltip的宽度
            int mainTooltipWidth = 0;
            for (ClientTooltipComponent component : e.getComponents()) {
                mainTooltipWidth = Math.max(mainTooltipWidth, component.getWidth(font));
            }
            
            // 计算右侧页面的位置（主tooltip右侧）
            int x = e.getX() + mainTooltipWidth + 10;
            int y = e.getY();
            
            // 创建临时列表来收集右侧页面需要显示的数据
            List<Either<FormattedText, TooltipComponent>> rightPageElements = new ArrayList<>();
            
            // 添加武器属性
            WeaponAttributeRenderer.addWeaponAttributesToTooltip(rightPageElements, stack);
            
            // 添加改装件信息
            int sockets = SocketHelper.getSockets(stack);
            int specialSockets = SocketHelper.getSpecialSockets(stack);
            if (sockets > 0 || specialSockets > 0) {
                SocketedModifications mods = SocketHelper.getModifications(stack);
                List<ModificationInstance> allMods = new ArrayList<>(mods.modifications());
                allMods.addAll(SocketHelper.getSpecialModifications(stack));
                
                rightPageElements.add(Either.right(new ModificationTooltipRenderer.ModificationComponent(
                    stack, 
                    new SocketedModifications(allMods)
                )));
            }
            
            // 计算右侧页面所需的宽度和高度
            int rightPageWidth = calculateRightPageWidth(stack, font);
            int rightPageHeight = calculateRightPageHeight(rightPageElements, font);
            
            // 使用原版tooltip的背景和边框颜色渲染右侧页面，高度根据实际内容调整
            guiGraphics.fillGradient(x - 3, y - 3, x + rightPageWidth + 3, y + rightPageHeight + 3, e.getBackgroundStart(), e.getBackgroundEnd());
            
            // 渲染边框
            guiGraphics.fill(x - 3, y - 3, x + rightPageWidth + 3, y - 2, e.getBorderStart());
            guiGraphics.fill(x - 3, y + rightPageHeight + 2, x + rightPageWidth + 3, y + rightPageHeight + 3, e.getBorderEnd());
            guiGraphics.fill(x - 3, y - 3, x - 2, y + rightPageHeight + 3, e.getBorderStart());
            guiGraphics.fill(x + rightPageWidth + 2, y - 3, x + rightPageWidth + 3, y + rightPageHeight + 3, e.getBorderEnd());
            
            // 渲染右侧页面内容
            x += 5;
            y += 5;
            
            // 渲染右侧页面元素
            renderRightPageElements(rightPageElements, guiGraphics, font, x, y);
        }
    }
    
    // 计算右侧页面所需高度
    private static int calculateRightPageHeight(List<Either<FormattedText, TooltipComponent>> elements, Font font) {
        int height = 8; // 基础高度
        
        for (Either<FormattedText, TooltipComponent> element : elements) {
            if (element.left().isPresent()) {
                // 文本元素高度
                height += font.lineHeight + 2;
            } else if (element.right().isPresent()) {
                // 组件元素高度
                TooltipComponent component = element.right().get();
                if (component instanceof ModificationTooltipRenderer.ModificationComponent) {
                    ModificationTooltipRenderer renderer = new ModificationTooltipRenderer((ModificationTooltipRenderer.ModificationComponent) component);
                    height += renderer.getHeight() + 2;
                }
            }
        }
        
        return Math.max(height, 16); // 最小高度
    }
    
    // 计算右侧页面所需宽度
    private static int calculateRightPageWidth(ItemStack stack, Font font) {
        int maxWidth = 80; // 最小宽度
        
        // 创建临时列表来收集需要显示的数据
        List<Either<FormattedText, TooltipComponent>> elements = new ArrayList<>();
        WeaponAttributeRenderer.addWeaponAttributesToTooltip(elements, stack);
        
        // 添加改装件信息
        int sockets = SocketHelper.getSockets(stack);
        int specialSockets = SocketHelper.getSpecialSockets(stack);
        if (sockets > 0 || specialSockets > 0) {
            SocketedModifications mods = SocketHelper.getModifications(stack);
            List<ModificationInstance> allMods = new ArrayList<>(mods.modifications());
            allMods.addAll(SocketHelper.getSpecialModifications(stack));
            elements.add(Either.right(new ModificationTooltipRenderer.ModificationComponent(
                stack, 
                new SocketedModifications(allMods)
            )));
        }
        
        // 计算最大宽度
        for (Either<FormattedText, TooltipComponent> element : elements) {
            if (element.left().isPresent()) {
                FormattedText text = element.left().get();
                int textWidth = font.width(text.getString());
                maxWidth = Math.max(maxWidth, textWidth);
            } else if (element.right().isPresent()) {
                TooltipComponent component = element.right().get();
                if (component instanceof ModificationTooltipRenderer.ModificationComponent) {
                    ModificationTooltipRenderer renderer = new ModificationTooltipRenderer((ModificationTooltipRenderer.ModificationComponent) component);
                    int componentWidth = renderer.getWidth(font);
                    maxWidth = Math.max(maxWidth, componentWidth);
                }
            }
        }
        
        return Math.min(maxWidth + 10, 180); // 限制最大宽度为180px
    }
    
    // 渲染右侧页面元素
    private static void renderRightPageElements(List<Either<FormattedText, TooltipComponent>> elements, GuiGraphics guiGraphics, Font font, int x, int y) {
        int currentY = y;
        
        for (Either<FormattedText, TooltipComponent> element : elements) {
            if (element.left().isPresent()) {
                // 渲染文本元素，将FormattedText转换为Component
                FormattedText formattedText = element.left().get();
                Component component = Component.literal(formattedText.getString());
                guiGraphics.drawString(font, component, x, currentY, 0xFFFFFF);
                currentY += font.lineHeight + 2;
            } else if (element.right().isPresent()) {
                // 渲染组件元素
                TooltipComponent component = element.right().get();
                if (component instanceof ModificationTooltipRenderer.ModificationComponent) {
                    ModificationTooltipRenderer renderer = new ModificationTooltipRenderer((ModificationTooltipRenderer.ModificationComponent) component);
                    
                    // 渲染组件的图像部分
                    renderer.renderImage(font, x, currentY, guiGraphics);
                    
                    // 渲染组件的文本部分
                    PoseStack poseStack = guiGraphics.pose();
                    poseStack.pushPose();
                    poseStack.translate(0, 0, 100);
                    MultiBufferSource.BufferSource bufferSource = guiGraphics.bufferSource();
                    renderer.renderText(font, x, currentY, poseStack.last().pose(), bufferSource);
                    poseStack.popPose();
                    
                    currentY += renderer.getHeight() + 2;
                }
            }
        }
    }
    
    // 检查是否需要显示左侧页面
    private static boolean needsLeftPage(ItemStack stack) {
        // 检查是否有武器属性
        if (ElementNBTUtils.hasNonZeroElements(stack)) {
            return true;
        }
        
        // 检查是否有改装件
        int sockets = SocketHelper.getSockets(stack);
        int specialSockets = SocketHelper.getSpecialSockets(stack);
        if (sockets > 0 || specialSockets > 0) {
            return true;
        }
        
        return false;
    }
    


    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = HamsterCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusSub {
        
        @SubscribeEvent
        public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent e) {
            e.register(ModificationTooltipRenderer.ModificationComponent.class, ModificationTooltipRenderer::new);
        }

        @SubscribeEvent
        public static void addModificationModels(ModelEvent.RegisterAdditional e) {
            Set<ResourceLocation> locs = Minecraft.getInstance().getResourceManager().listResources(
            "models", 
            loc -> HamsterCore.MODID.equals(loc.getNamespace()) && 
                   loc.getPath().contains("/modification/") && 
                   loc.getPath().endsWith(".json")
        ).keySet();
            
            for (ResourceLocation s : locs) {
                String path = s.getPath().substring("models/".length(), s.getPath().length() - ".json".length());
                e.register(new ResourceLocation(HamsterCore.MODID, path));
            }
        }

        @SubscribeEvent
        public static void replaceModificationModel(ModelEvent.ModifyBakingResult e) {
            ModelResourceLocation key = new ModelResourceLocation(
                new ResourceLocation(HamsterCore.MODID, "modification"), 
                "inventory"
            );
            BakedModel oldModel = e.getModels().get(key);
            if (oldModel != null) {
                e.getModels().put(key, new ModificationModel(oldModel, e.getModelBakery()));
            }
        }
    }
}
