package com.xlxyvergil.hamstercore.modification;

import com.xlxyvergil.hamstercore.modification.event.GetItemSocketsEvent;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import com.xlxyvergil.hamstercore.element.ElementCalculationCoordinator;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;

import java.util.*;

/**
 * Utility class for the manipulation of Sockets and Modifications on items.
 */
public class ModificationHelper {

    public static final String AFFIX_DATA = "AffixData";
    public static final String MODIFICATIONS = "modifications";
    public static final String SOCKETS = "sockets";
    public static final String SPECIAL_SOCKETS = "specialSockets"; // 特殊槽位的NBT键

    /**
     * Gets the number of sockets on an item.
     * By default, this equals the nbt-encoded socket count, but it may be modified by {@link GetItemSocketsEvent}.
     *
     * @param stack The stack being queried.
     * @return The number of sockets on the stack.
     */
    public static int getSockets(ItemStack stack) {
        CompoundTag afxData = stack.getTagElement(AFFIX_DATA);
        int sockets = afxData != null ? afxData.getInt(SOCKETS) : 0;
        var event = new GetItemSocketsEvent(stack, sockets);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getSockets();
    }
    
    /**
     * Gets the number of special sockets on an item.
     *
     * @param stack The stack being queried.
     * @return The number of special sockets on the stack.
     */
    public static int getSpecialSockets(ItemStack stack) {
        CompoundTag afxData = stack.getTagElement(AFFIX_DATA);
        return afxData != null ? afxData.getInt(SPECIAL_SOCKETS) : 0;
    }

    /**
     * Sets the number of sockets on the item to the specified value.
     * <p>
     * The value set here is not necessarily the value that will be returned by {@link #getSockets(ItemStack)} due to {@link GetItemSocketsEvent}.
     *
     * @param stack   The stack being modified.
     * @param sockets The number of sockets.
     */
    public static void setSockets(ItemStack stack, int sockets) {
        stack.getOrCreateTagElement(AFFIX_DATA).putInt(SOCKETS, sockets);
    }
    
    /**
     * Sets the number of special sockets on the item to the specified value.
     *
     * @param stack   The stack being modified.
     * @param sockets The number of special sockets.
     */
    public static void setSpecialSockets(ItemStack stack, int sockets) {
        stack.getOrCreateTagElement(AFFIX_DATA).putInt(SPECIAL_SOCKETS, sockets);
    }

    /**
     * Gets the list of modifications socketed into the item. Modifications in the list may be invalid or empty.
     *
     * @param stack The stack being queried
     * @return An immutable list of all modifications socketed in this item.
     */
    public static SocketedModifications getModifications(ItemStack stack) {
        return getModificationsImpl(stack);
    }

    /**
     * Implementation for {@link #getModifications(ItemStack)}
     */
    private static SocketedModifications getModificationsImpl(ItemStack stack) {
        int normalSockets = getSockets(stack);
        int specialSockets = getSpecialSockets(stack);
        int totalSize = normalSockets + specialSockets;
        
        if (totalSize <= 0 || stack.isEmpty()) {
            return SocketedModifications.EMPTY;
        }

        List<ModificationInstance> modifications = NonNullList.withSize(totalSize, ModificationInstance.EMPTY);
        int i = 0;
        CompoundTag afxData = stack.getTagElement(AFFIX_DATA);
        if (afxData != null && afxData.contains(MODIFICATIONS)) {
            ListTag modData = afxData.getList(MODIFICATIONS, Tag.TAG_COMPOUND);
            for (Tag tag : modData) {
                if (i >= totalSize) break;
                CompoundTag modTag = (CompoundTag) tag;
                ModificationInstance inst = ModificationInstance.fromNBT(modTag, ModificationRegistry.getInstance());
                modifications.set(i++, inst);
            }
        }

        return new SocketedModifications(modifications);
    }

    /**
     * Sets the modification list on the item to the provided list of modifications.<br>
     * Setting more modifications than there are sockets will cause the extra modifications to be lost.
     *
     * @param stack         The stack being modified.
     * @param modifications The list of socketed modifications.
     */
    public static void setModifications(ItemStack stack, List<ModificationInstance> modifications) {
        CompoundTag afxData = stack.getOrCreateTagElement(AFFIX_DATA);
        ListTag modData = new ListTag();
        
        // 只保存有效且不超过总槽位数量的改装件
        int normalSockets = getSockets(stack);
        int specialSockets = getSpecialSockets(stack);
        int totalSockets = normalSockets + specialSockets;
        
        int savedCount = 0;
        for (ModificationInstance inst : modifications) {
            if (savedCount >= totalSockets) {
                break;
            }
            if (inst.isValid()) {
                modData.add(inst.toNBT());
                savedCount++;
            }
        }
        
        afxData.put(MODIFICATIONS, modData);
        
        // 应用所有改装件的词缀到item stack中
        applyAllModifications(stack);
    }

    /**
     * Sets the modification list on the item to the provided SocketedModifications.<br>
     *
     * @param stack         The stack being modified.
     * @param modifications The SocketedModifications to set.
     */
    public static void setModifications(ItemStack stack, SocketedModifications modifications) {
        setModifications(stack, modifications.modifications());
    }

    /**
     * Checks if any of the sockets on the item are empty.
     *
     * @param stack The stack being queried.
     * @return True, if any sockets are empty, otherwise false.
     */
    public static boolean hasEmptySockets(ItemStack stack) {
        return getModifications(stack).streamValidModifications().count() < getSockets(stack);
    }

    /**
     * Computes the index of the first empty socket, used during socketing.
     *
     * @param stack The stack being queried.
     * @return The index of the first empty socket in the stack's modification list.
     */
    public static int getFirstEmptySocket(ItemStack stack) {
        SocketedModifications mods = getModifications(stack);
        for (int socket = 0; socket < mods.size(); socket++) {
            if (!mods.get(socket).isValid()) {
                return socket;
            }
        }
        return 0;
    }

    /**
     * Adds a modification to the first empty socket.
     *
     * @param stack         The stack being modified.
     * @param modification  The modification to add.
     * @return True if the modification was added successfully, false otherwise.
     */
    public static boolean addModification(ItemStack stack, Modification modification) {
        if (!hasEmptySockets(stack)) {
            return false;
        }
        // 检查改装件是否适用于该物品
        if (!modification.canApplyTo(stack, stack)) {
            return false;
        }
        List<ModificationInstance> modifications = new ArrayList<>(getModifications(stack).modifications());
        int index = getFirstEmptySocket(stack);
        modifications.set(index, new ModificationInstance(modification));
        setModifications(stack, modifications);
        return true;
    }

    /**
     * Removes a modification at the specified index.
     *
     * @param stack  The stack being modified.
     * @param index  The index of the modification to remove.
     * @return True if the modification was removed successfully, false otherwise.
     */
    public static boolean removeModification(ItemStack stack, int index) {
        List<ModificationInstance> modifications = new ArrayList<>(getModifications(stack).modifications());
        if (index < 0 || index >= modifications.size()) {
            return false;
        }
        modifications.set(index, ModificationInstance.EMPTY);
        setModifications(stack, modifications);
        return true;
    }

    /**
     * Clears all modifications from the item.
     *
     * @param stack The stack being modified.
     */
    public static void clearModifications(ItemStack stack) {
        CompoundTag afxData = stack.getTagElement(AFFIX_DATA);
        if (afxData != null) {
            afxData.remove(MODIFICATIONS);
        }
        // 重新计算元素值，确保所有词缀被正确移除
        ElementCalculationCoordinator.INSTANCE.calculateAndStoreElements(stack, WeaponDataManager.getWeaponData(stack));
    }

    /**
     * Applies all valid modifications to the item stack using AffixAPI.
     *
     * @param stack The stack being modified.
     */
    public static void applyAllModifications(ItemStack stack) {
        getModifications(stack).applyAllModifications(stack);
    }
    
    /**
     * Gets the modification ID from an ItemStack, extracting just the path part (without namespace).
     * This is used for model selection.
     *
     * @param stack The stack being queried.
     * @return The modification ID path, or null if not found.
     */
    public static String getModificationId(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("ModificationId")) {
            String fullId = stack.getTag().getString("ModificationId");
            if (fullId == null) {
                return null;
            }
            // 从完整的资源位置中提取路径部分（去除命名空间）
            if (fullId.contains(":")) {
                return fullId.substring(fullId.lastIndexOf(":") + 1);
            }
            return fullId;
        }
        return null;
    }
}
