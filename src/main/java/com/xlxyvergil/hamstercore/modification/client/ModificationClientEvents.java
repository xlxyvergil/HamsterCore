package com.xlxyvergil.hamstercore.modification.client;

import com.mojang.datafixers.util.Either;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.modification.ModificationHelper;
import com.xlxyvergil.hamstercore.modification.SocketedModifications;
import com.xlxyvergil.hamstercore.util.ElementNBTUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.lwjgl.glfw.GLFW;


@Mod.EventBusSubscriber(modid = HamsterCore.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModificationClientEvents {

    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(SocketComponent.class, SocketTooltipRenderer::new);
    }

    @Mod.EventBusSubscriber(modid = HamsterCore.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusSub {

        @SubscribeEvent
        public static void gatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
            ItemStack stack = event.getItemStack();
            boolean hasSockets = ModificationHelper.getSockets(stack) > 0;
            boolean hasElements = ElementNBTUtils.hasNonZeroElements(stack);
            
            // 检查是否按住Ctrl+Shift键
            if (isCtrlShiftPressed() && (hasSockets || hasElements)) {
                // 按住Ctrl+Shift键时，替换整个Tooltip内容
                event.getTooltipElements().clear();
                
                // 添加改造槽信息（如果有）
                if (hasSockets) {
                    SocketedModifications modifications = ModificationHelper.getModifications(stack);
                    event.getTooltipElements().add(
                        Either.right(new SocketComponent(stack, modifications.modifications())));
                }
                
                // 添加武器属性信息（如果有）
                if (hasElements) {
                    com.xlxyvergil.hamstercore.client.renderer.item.WeaponAttributeRenderer.addWeaponAttributesToTooltip(
                        event.getTooltipElements(), stack);
                }
            } else if (!isCtrlShiftPressed() && hasSockets) {
                // 没有按住Ctrl+Shift键时，只添加提示信息
                event.getTooltipElements().add(
                    Either.left(Component.translatable("hamstercore.modification.press_ctrl_shift")));
            }
        }
        
        /**
         * 检查是否按住Ctrl+Shift键
         */
        private static boolean isCtrlShiftPressed() {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.getWindow() == null) {
                return false;
            }
            long window = mc.getWindow().getWindow();
            
            // 检查Ctrl键和Shift键是否都被按下
            boolean isCtrlPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS || 
                                   GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
            boolean isShiftPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS || 
                                    GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            
            return isCtrlPressed && isShiftPressed;
        }
    }
}
