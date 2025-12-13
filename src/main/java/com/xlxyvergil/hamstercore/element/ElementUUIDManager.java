package com.xlxyvergil.hamstercore.element;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 元素UUID管理器
 * 用于管理配置文件中元素类型与固定UUID之间的映射关系
 * 所有UUID都是硬编码的，专门用于配置文件中的元素类型
 */
public class ElementUUIDManager {
    // 元素类型到UUID的映射
    private static final Map<String, UUID> ELEMENT_UUID_MAP = new HashMap<>();
    
    // 初始化配置文件中元素类型的硬编码UUID映射
    static {
        // 基础元素
        registerElementUUID("fire", "aa110000-0000-0000-0000-000000000000");
        registerElementUUID("ice", "aa110000-0001-0000-0000-000000000001");
        registerElementUUID("electricity", "aa110000-0002-0000-0000-000000000002");
        registerElementUUID("toxin", "aa110000-0003-0000-0000-000000000003");
        registerElementUUID("radiation", "aa110000-0004-0000-0000-000000000004");
        registerElementUUID("corrosion", "aa110000-0005-0000-0000-000000000005");
        registerElementUUID("magnetic", "aa110000-0006-0000-0000-000000000006");
        registerElementUUID("viral", "aa110000-0007-0000-0000-000000000007");
        registerElementUUID("gas", "aa110000-0008-0000-0000-000000000008");
        registerElementUUID("blast", "aa110000-0009-0000-0000-000000000009");
        
        // 物理元素
        registerElementUUID("slash", "aa110000-0010-0000-0000-000000000010");
        registerElementUUID("impact", "aa110000-0011-0000-0000-000000000011");
        registerElementUUID("puncture", "aa110000-0012-0000-0000-000000000012");
        
        // 特殊属性
        registerElementUUID("critical_chance", "aa110000-0020-0000-0000-000000000020");
        registerElementUUID("critical_damage", "aa110000-0021-0000-0000-000000000021");
        registerElementUUID("trigger_chance", "aa110000-0022-0000-0000-000000000022");
    }
    
    /**
     * 注册元素类型与UUID的映射关系
     * @param elementType 元素类型
     * @param uuidStr UUID字符串
     */
    private static void registerElementUUID(String elementType, String uuidStr) {
        ELEMENT_UUID_MAP.put(elementType, UUID.fromString(uuidStr));
    }
    
    /**
     * 获取元素类型的固定UUID
     * 这些UUID是专门为配置文件中的元素类型硬编码的
     * @param elementType 元素类型
     * @return 对应的UUID，如果未找到则抛出异常
     * @throws IllegalArgumentException 如果找不到元素类型对应的UUID
     */
    public static UUID getElementUUID(String elementType) {
        // 如果已经有预定义的UUID，直接返回
        if (ELEMENT_UUID_MAP.containsKey(elementType)) {
            return ELEMENT_UUID_MAP.get(elementType);
        }
        
        // 如果没有找到预定义的UUID，抛出异常
        throw new IllegalArgumentException("未找到元素类型 '" + elementType + "' 对应的预定义UUID");
    }
}