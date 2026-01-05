package com.xlxyvergil.hamstercore.modification;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.modification.event.GetItemModificationSlotsEvent;
import dev.shadowsoffire.placebo.util.CachedObject.CachedObjectSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

/**
 * 槽位辅助类 - 模仿 Apotheosis 的 SocketHelper
 */
public class SocketHelper {

    public static final ResourceLocation MODIFICATIONS_CACHED_OBJECT = new ResourceLocation(HamsterCore.MODID + ":modifications");

    public static final String AFFIX_DATA = "AffixData";
    public static final String MODIFICATIONS = "modifications";
    public static final String SOCKETS = "sockets";
    public static final String SPECIAL_SOCKETS = "specialSockets";

    /**
     * 获取槽位数量
     */
    public static int getSockets(ItemStack stack) {
        CompoundTag afxData = stack.getTagElement(AFFIX_DATA);
        int sockets = afxData != null ? afxData.getInt(SOCKETS) : 0;
        var event = new GetItemModificationSlotsEvent(stack, sockets);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getSockets();
    }

    /**
     * 获取特殊槽位数量
     */
    public static int getSpecialSockets(ItemStack stack) {
        CompoundTag afxData = stack.getTagElement(AFFIX_DATA);
        int specialSockets = afxData != null ? afxData.getInt(SPECIAL_SOCKETS) : 0;
        return specialSockets;
    }

    /**
     * 设置槽位数量
     */
    public static void setSockets(ItemStack stack, int sockets) {
        stack.getOrCreateTagElement(AFFIX_DATA).putInt(SOCKETS, sockets);
    }

    /**
     * 设置特殊槽位数量
     */
    public static void setSpecialSockets(ItemStack stack, int specialSockets) {
        stack.getOrCreateTagElement(AFFIX_DATA).putInt(SPECIAL_SOCKETS, specialSockets);
    }

    /**
     * 获取改装件列表
     */
    public static SocketedModifications getModifications(ItemStack stack) {
        return CachedObjectSource.getOrCreate(stack, MODIFICATIONS_CACHED_OBJECT, SocketHelper::getModificationsImpl, SocketHelper::hashSockets);
    }

    /**
     * 计算改装件缓存的失效哈希值
     */
    private static int hashSockets(ItemStack stack) {
        return Objects.hash(stack.getTagElement(AFFIX_DATA), getSockets(stack));
    }

    /**
     * getModifications 的实现
     */
    private static SocketedModifications getModificationsImpl(ItemStack stack) {
        int size = getSockets(stack);
        if (size <= 0 || stack.isEmpty()) return new SocketedModifications(Collections.emptyList());

        List<ModificationInstance> modifications = new java.util.ArrayList<>(Collections.nCopies(size, ModificationInstance.EMPTY));
        int i = 0;
        CompoundTag afxData = stack.getTagElement(AFFIX_DATA);
        if (afxData != null && afxData.contains(MODIFICATIONS)) {
            ListTag modData = afxData.getList(MODIFICATIONS, Tag.TAG_COMPOUND);
            for (Tag tag : modData) {
                CompoundTag compound = (CompoundTag) tag;
                ModificationInstance inst = ModificationInstance.fromNBT(compound);
                if (inst.isValid()) {
                    modifications.set(i++, inst);
                }
                if (i >= size) break;
            }
        }

        return new SocketedModifications(modifications);
    }

    /**
     * 设置改装件列表
     */
    public static void setModifications(ItemStack stack, List<ModificationInstance> modifications) {
        CompoundTag afxData = stack.getOrCreateTagElement(AFFIX_DATA);
        ListTag list = new ListTag();
        for (ModificationInstance inst : modifications) {
            if (inst.isValid()) {
                list.add(inst.toNBT());
            }
        }
        afxData.put(MODIFICATIONS, list);
    }

    /**
     * 检查是否有空槽位
     */
    public static boolean hasEmptySockets(ItemStack stack) {
        return getModifications(stack).streamValidModifications().count() < getSockets(stack);
    }

    /**
     * 获取第一个空槽位索引
     */
    public static int getFirstEmptySocket(ItemStack stack) {
        SocketedModifications mods = getModifications(stack);
        for (int socket = 0; socket < mods.size(); socket++) {
            if (!mods.get(socket).isValid()) return socket;
        }
        return 0;
    }
}
