package com.xlxyvergil.hamstercore.modification;

import net.minecraft.resources.ResourceLocation;


/**
 * 用于安全处理改装件的DynamicHolder，参考Apotheosis的DynamicHolder实现
 */
public class DynamicModificationHolder {
    private final ResourceLocation id;
    private Modification modification;
    private boolean bound = false;

    public DynamicModificationHolder(ResourceLocation id) {
        this.id = id;
        this.modification = ModificationRegistry.getInstance().getModification(id).orElse(null);
        this.bound = this.modification != null;
    }

    /**
     * 检查改装件是否绑定（有效）
     */
    public boolean isBound() {
        return bound;
    }

    /**
     * 获取改装件实例，如果未绑定则返回null
     */
    public Modification get() {
        if (!bound) {
            return null;
        }
        return modification;
    }

    /**
     * 获取改装件ID
     */
    public ResourceLocation getId() {
        return id;
    }

    /**
     * 刷新绑定状态（在改装件重新加载后调用）
     */
    public void refresh() {
        this.modification = ModificationRegistry.getInstance().getModification(id).orElse(null);
        this.bound = this.modification != null;
    }

    /**
     * 获取空的holder
     */
    public static DynamicModificationHolder empty() {
        return new DynamicModificationHolder(null);
    }
}