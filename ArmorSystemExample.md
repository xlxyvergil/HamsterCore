# 怪物护甲系统动态修改示例

## 系统概述

修改后的护甲系统将怪物护甲分为两个部分：
- `baseEntityArmor`：来自配置文件的基础护甲值
- `entityArmor`：根据基础护甲值和怪物等级计算出的实际护甲值

当 `baseEntityArmor` 改变时，系统会自动重新计算 `entityArmor`，确保伤害计算使用最新的护甲值。

## 方法调用示例

### 1. 获取和修改基础护甲值

当玩家通过某种方式（如技能、物品效果等）改变怪物的基础护甲值时，只需调用 `setBaseArmor()` 方法即可，系统会自动重新计算实际护甲值。

### 2. 具体代码示例

```java
import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import com.xlxyvergil.hamstercore.network.EntityArmorSyncToClient;
import net.minecraft.world.entity.LivingEntity;

// 假设这是一个技能效果或物品使用的处理方法
public void applyArmorModifier(LivingEntity targetEntity, double modifier) {
    // 获取目标实体的护甲Capability
    targetEntity.getCapability(EntityArmorCapabilityProvider.CAPABILITY).ifPresent(cap -> {
        // 1. 获取当前基础护甲值
        double currentBaseArmor = cap.getBaseArmor();
        
        // 2. 应用修改（例如增加20%基础护甲）
        double newBaseArmor = currentBaseArmor * (1 + modifier);
        
        // 3. 设置新的基础护甲值 - 这会自动更新实际护甲值
        cap.setBaseArmor(newBaseArmor);
        
        // 4. 同步到客户端，确保客户端显示和计算正确
        EntityArmorSyncToClient.sync(targetEntity);
    });
}
```

### 3. 手动重新计算实际护甲值（可选）

如果需要手动触发实际护甲值的重新计算（例如等级改变时），可以使用以下方法：

```java
targetEntity.getCapability(EntityArmorCapabilityProvider.CAPABILITY).ifPresent(cap -> {
    // 重新计算并更新实际护甲值
    cap.initializeEntityCapabilities(baseLevel, newLevel);
    
    // 同步到客户端
    EntityArmorSyncToClient.sync(targetEntity);
});
```

## 系统工作流程

1. **获取配置护甲值**：`getBaseArmor()` 从配置文件加载基础护甲值
2. **设置新的基础护甲**：`setBaseArmor(newValue)` 更新基础护甲值
3. **自动更新实际护甲**：`setBaseArmor()` 内部调用 `updateArmor()` 重新计算实际护甲值
4. **同步到客户端**：`EntityArmorSyncToClient.sync()` 将新的护甲值发送到所有跟踪该实体的客户端

## 注意事项

1. **服务端操作**：所有护甲修改都应该在服务端进行，然后同步到客户端
2. **同步机制**：必须调用 `EntityArmorSyncToClient.sync()` 方法，确保客户端的护甲值与服务端一致
3. **伤害计算**：伤害计算代码不需要修改，仍然使用 `getArmor()` 获取实际护甲值
4. **配置文件**：修改配置文件中的护甲值只会影响新生成的实体，已存在的实体需要通过代码修改

## 常用方法汇总

| 方法名 | 作用 | 是否自动同步 |
|-------|------|------------|
| `getBaseArmor()` | 获取基础护甲值 | 否 |
| `setBaseArmor(double)` | 设置基础护甲值（自动更新实际护甲值） | 否 |
| `getArmor()` | 获取实际护甲值 | 否 |
| `setArmor(double)` | 直接设置实际护甲值 | 否 |
| `initializeEntityCapabilities(int, int)` | 根据等级重新计算实际护甲值 | 否 |
| `EntityArmorSyncToClient.sync(LivingEntity)` | 同步护甲值到客户端 | 是 |

## 示例场景

### 场景1：技能增加怪物基础护甲

```java
// 技能效果：增加目标怪物25%基础护甲，持续30秒
public void castArmorBoost(LivingEntity target) {
    target.getCapability(EntityArmorCapabilityProvider.CAPABILITY).ifPresent(cap -> {
        double originalBaseArmor = cap.getBaseArmor();
        double boostedBaseArmor = originalBaseArmor * 1.25;
        
        cap.setBaseArmor(boostedBaseArmor);
        EntityArmorSyncToClient.sync(target);
        
        // 30秒后恢复原始护甲值
        scheduleTask(() -> {
            cap.setBaseArmor(originalBaseArmor);
            EntityArmorSyncToClient.sync(target);
        }, 600); // 600游戏刻 = 30秒
    });
}
```

### 场景2：物品效果临时改变护甲

```java
// 物品效果：使用后将目标怪物的基础护甲设置为固定值50
public void useArmorPotion(LivingEntity target) {
    target.getCapability(EntityArmorCapabilityProvider.CAPABILITY).ifPresent(cap -> {
        double originalBaseArmor = cap.getBaseArmor();
        
        cap.setBaseArmor(50.0);
        EntityArmorSyncToClient.sync(target);
        
        // 10秒后恢复
        scheduleTask(() -> {
            cap.setBaseArmor(originalBaseArmor);
            EntityArmorSyncToClient.sync(target);
        }, 200); // 200游戏刻 = 10秒
    });
}
```

通过以上方法，您可以灵活地动态修改怪物的护甲值，系统会自动处理实际护甲值的重新计算和客户端同步，确保游戏体验的一致性。