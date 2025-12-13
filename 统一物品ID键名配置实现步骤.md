# 统一物品ID键名配置文件实现步骤

## 概述
本文档详细描述了为HamsterCore实现统一物品ID作为键名、值为武器配置数组的配置文件系统的全部实现步骤。该系统主要针对TACZ和拔刀剑模组的特殊处理需求。

## 核心设计原则

### 1. 统一ID键名策略
- **TACZ**: 使用 `"tacz:modern_kinetic_gun"` 作为统一键名
- **拔刀剑**: 使用 `"slashblade:slashblade"` 作为统一键名
- **普通物品**: 直接使用物品ID作为键名

### 2. 配置文件结构
```json
{
  "tacz:modern_kinetic_gun": [
    {
      "gunId": "tacz:glock_17",
      "elementData": {
        "Basic": [...],
        "InitialModifiers": [...]
      }
    },
    {
      "gunId": "tacz:ak47", 
      "elementData": {
        "Basic": [...],
        "InitialModifiers": [...]
      }
    }
  ],
  "slashblade:slashblade": [
    {
      "translationKey": "slashblade.named.sword",
      "elementData": {
        "Basic": [...],
        "InitialModifiers": [...]
      }
    }
  ]
}
```

## 实现步骤

### 阶段1：配置文件生成优化

#### 步骤1.1：优化TACZ配置生成
- **目标**: 确保TACZ配置文件使用统一键名结构
- **实现要点**:
  - 保持现有的枪械数据生成逻辑
  - 将所有枪械配置放入数组
  - 使用统一键名 `"tacz:modern_kinetic_gun"`
- **文件**: `WeaponConfig.java` 的 `generateTacZWeaponsConfig()` 方法

#### 步骤1.2：优化拔刀剑配置生成
- **目标**: 确保拔刀剑配置文件使用统一键名结构
- **实现要点**:
  - 保持现有的刀剑数据生成逻辑
  - 将所有刀剑配置放入数组
  - 使用统一键名 `"slashblade:slashblade"`
- **文件**: `WeaponConfig.java` 的 `generateSlashBladeWeaponsConfig()` 方法

#### 步骤1.3：优化配置数据结构
- **目标**: 确保JSON结构包含必要的标识符
- **实现要点**:
  - TACZ配置必须包含 `gunId` 字段
  - 拔刀剑配置必须包含 `translationKey` 字段
  - 保持 `elementData` 结构不变
- **文件**: `WeaponConfig.java` 的 `createWeaponConfigJson()` 方法

### 阶段2：配置读取优化

#### 步骤2.1：优化TACZ配置读取
- **目标**: 实现基于统一键名的配置查找
- **实现要点**:
  - 读取统一键名下的配置数组
  - 遍历数组查找匹配的 `gunId`
  - 支持运行时动态查找
- **文件**: `WeaponConfig.java` 的 `getTacZWeaponConfig()` 方法

#### 步骤2.2：优化拔刀剑配置读取
- **目标**: 实现基于统一键名的配置查找
- **实现要点**:
  - 读取统一键名下的配置数组
  - 遍历数组查找匹配的 `translationKey`
  - 提供默认配置fallback机制
- **文件**: `WeaponConfig.java` 的 `getSlashBladeWeaponConfig()` 方法

#### 步骤2.3：统一配置查找入口
- **目标**: 优化 `getWeaponConfig()` 方法
- **实现要点**:
  - 根据物品ID识别TACZ/拔刀剑物品
  - 调用对应的特殊查找方法
  - 保持向后兼容性
- **文件**: `WeaponConfig.java` 的 `getWeaponConfig()` 方法

### 阶段3：缓存和性能优化

#### 步骤3.1：实现配置缓存机制
- **目标**: 提高配置查找性能
- **实现要点**:
  - 为TACZ和拔刀剑建立专用缓存
  - 缓存键：具体gunId/translationKey
  - 缓存值：对应的WeaponData对象
- **新增字段**:
  ```java
  private static Map<String, WeaponData> taczConfigCache = new HashMap<>();
  private static Map<String, WeaponData> slashBladeConfigCache = new HashMap<>();
  ```

#### 步骤3.2：优化内存映射表
- **目标**: 改进内存中的配置存储结构
- **实现要点**:
  - 维持现有的 `weaponConfigs` 映射表
  - 添加专用映射表用于具体ID查找
  - 确保数据一致性
- **新增字段**:
  ```java
  private static Map<String, WeaponData> gunIdToConfigMap = new HashMap<>();
  private static Map<String, WeaponData> translationKeyToConfigMap = new HashMap<>();
  ```

### 阶段4：NBT应用类设计

#### 步骤4.1：创建Normal配置应用类
- **目标**: 处理普通物品的NBT应用
- **类名**: `NormalConfigApplier`
- **职责**:
  - 读取normal配置文件
  - 应用Basic/Usage/Def三层NBT数据
  - 处理元素复合计算

#### 步骤4.2：创建TACZ配置应用类
- **目标**: 处理TACZ物品的NBT应用
- **类名**: `TacZConfigApplier`
- **职责**:
  - 读取tacz配置文件
  - 根据gunId查找具体配置
  - 应用三层NBT数据
  - 处理枪械特殊逻辑

#### 步骤4.3：创建拔刀剑配置应用类
- **目标**: 处理拔刀剑物品的NBT应用
- **类名**: `SlashBladeConfigApplier`
- **职责**:
  - 读取slashblade配置文件
  - 根据translationKey查找具体配置
  - 应用三层NBT数据
  - 处理刀剑特殊逻辑

#### 步骤4.4：创建Additional配置应用类
- **目标**: 处理额外物品的NBT应用
- **类名**: `AdditionalConfigApplier`
- **职责**:
  - 读取additional配置文件
  - 处理用户自定义物品
  - 应用三层NBT数据

### 阶段5：Basic层数据排序

#### 步骤5.1：实现Def > Config > User优先级排序
- **目标**: 确保Basic层数据按优先级排序
- **实现要点**:
  - Def层优先级最高
  - Config层次之
  - User层优先级最低
  - 维护元素添加顺序

#### 步骤5.2：集成排序逻辑
- **目标**: 在配置应用时执行排序
- **实现要点**:
  - 在各Applier类中集成排序逻辑
  - 确保排序后数据正确写入NBT
  - 保持与ElementCombinationModifier的兼容性

### 阶段6：测试和验证

#### 步骤6.1：配置文件生成测试
- **验证点**:
  - TACZ配置文件格式正确
  - 拔刀剑配置文件格式正确
  - 普通物品配置文件格式正确
  - JSON结构符合预期

#### 步骤6.2：配置读取测试
- **验证点**:
  - 统一键名查找功能正常
  - 具体ID匹配功能正常
  - 缓存机制工作正常
  - 性能满足要求

#### 步骤6.3：NBT应用测试
- **验证点**:
  - Basic层数据正确写入
  - Usage层数据正确计算
  - Def层数据正确应用
  - 元素复合逻辑正常

#### 步骤6.4：集成测试
- **验证点**:
  - 实体生成时默认属性正确
  - 运行时属性修改正常
  - 数据持久化正常
  - 跨mod兼容性正常

## 关键技术细节

### 1. 配置文件路径结构
```
config/hamstercore/Weapon/
├── normal_weapons.json          # 普通物品配置
├── tacz_weapons.json            # TACZ配置
├── slashblade_weapons.json      # 拔刀剑配置
└── additional_normal_weapons.json # 额外物品配置
```

### 2. 数据流转换过程
1. **配置生成**: WeaponConfig → JSON文件
2. **配置读取**: JSON文件 → Applier类 → NBT数据
3. **运行时使用**: NBT数据 → ElementCombinationModifier → 游戏效果

### 3. 关键映射关系
- **物品ID** → **统一键名** → **配置数组** → **具体配置**
- **tacz:modern_kinetic_gun** → **tacz:glock_17** → **WeaponData**
- **slashblade:slashblade** → **slashblade.named.sword** → **WeaponData**

## 预期收益

### 1. 统一性
- 所有配置文件使用一致的键名策略
- 简化配置文件管理和维护

### 2. 性能
- 减少运行时查找开销
- 缓存机制提高响应速度

### 3. 可扩展性
- 便于添加新的特殊物品类型
- 配置结构清晰，易于理解和修改

### 4. 兼容性
- 保持与现有系统的兼容
- 支持渐进式迁移

## 风险和注意事项

### 1. 数据一致性
- 确保内存映射表与实际配置文件同步
- 处理配置文件热重载情况

### 2. 向后兼容
- 保持现有配置文件的兼容性
- 提供配置迁移机制

### 3. 内存使用
- 合理控制缓存大小
- 及时清理无用缓存数据

### 4. 错误处理
- 妥善处理配置文件格式错误
- 提供有意义的错误信息