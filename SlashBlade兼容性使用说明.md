# 模组兼容性使用说明

## 概述

改进后的兼容性系统采用了 ck 目录中其他模组的最佳实践，统一使用 `ModList` 方式和直接API调用，完全避免了反射，解决了在世界加载阶段无法获取完整translationKey的问题。

## 支持的模组

- **SlashBlade Resharped**: 拔刀剑模组
- **TACZ**: Timeless and Classic Guns 枪械模组

## 主要改进

### 1. 正确的时序处理
- 在 `ServerStartedEvent` 阶段初始化兼容性和获取数据，确保所有模组物品注册都已完成
- 彻底解决了在世界加载阶段无法获取translationKey的问题
- 移除了不必要的 `InterModEnqueueEvent` 处理，简化了代码结构

### 2. 统一的获取策略
- **主要方法**: 通过物品注册表获取数据（更稳定，兼容性更好）
- **备用方法**: 在注册表方法失败时尝试模组API（仅SlashBlade）
- **无反射**: 完全避免反射，使用直接API调用

### 3. 缓存机制
- 缓存模组加载状态，避免重复检查
- 缓存物品ID和translationKey
- 支持数据重新加载

### 4. 原版兼容
- 使用 `item.getDescriptionId()` 获取translationKey
- 符合Minecraft原版的使用方式
- 更好的稳定性和兼容性

### 5. 直接API调用
- 参考 ck 目录中TACZ、L2Hostility等模组的做法
- 避免反射的复杂性和性能开销
- 编译时依赖，运行时可选

## 使用方法

### 基本使用

```java
// 检查拔刀剑是否已加载
if (SlashBladeItemsFetcher.isSlashBladeLoaded()) {
    // 获取所有拔刀剑ID
    Set<ResourceLocation> ids = SlashBladeItemsFetcher.getSlashBladeIDs();
    
    // 获取所有translationKey
    Set<String> translationKeys = SlashBladeItemsFetcher.getSlashBladeTranslationKeys();
    
    // 在服务器运行时获取数据
    Set<ResourceLocation> serverIds = SlashBladeItemsFetcher.getSlashBladeIDs(server);
    Set<String> serverTranslationKeys = SlashBladeItemsFetcher.getSlashBladeTranslationKeys(server);
}
```

### 在服务器启动时使用

```java
// 在服务器启动事件中（推荐时机）
@SubscribeEvent
public void onServerStarted(ServerStartedEvent event) {
    MinecraftServer server = event.getServer();
    
    // 自动加载数据
    Set<ResourceLocation> ids = SlashBladeItemsFetcher.getSlashBladeIDs(server);
    Set<String> translationKeys = SlashBladeItemsFetcher.getSlashBladeTranslationKeys(server);
    
    logger.info("加载了 {} 个拔刀剑", ids.size());
}
```

### 手动设置数据

```java
// 手动设置拔刀剑ID（从外部数据源）
Set<ResourceLocation> customIds = new HashSet<>();
customIds.add(new ResourceLocation("slashblade", "my_custom_blade"));
SlashBladeItemsFetcher.setSlashBladeIDs(customIds);
```

### 重新加载数据

```java
// 重新加载拔刀剑数据
SlashBladeItemsFetcher.reloadData(server);

// 清空缓存
SlashBladeItemsFetcher.clearData();
```

## 兼容性检查

代码会自动检查 `ModList.get().isLoaded("slashblade")`，如果拔刀剑模组未加载：

- `isSlashBladeLoaded()` 返回 `false`
- `getSlashBladeIDs()` 返回空集合
- `getSlashBladeTranslationKeys()` 返回空集合
- 所有方法调用都是安全的，不会抛出异常

## 构建配置

在 `build.gradle` 中使用 `compileOnly` 依赖：

```gradle
// SlashBlade Resharped dependency (compile only for compatibility)
compileOnly fg.deobf("curse.maven:slashblade-resharped-1022428:5410946")
```

这样可以：
- 在编译时获得拔刀剑API的访问权限
- 在运行时不强制依赖拔刀剑模组
- 用户可以选择性安装拔刀剑

## 初始化流程

1. 模组加载时，`HamsterCore` 构造函数注册 `ServerStartedEvent`
2. 服务器启动完成时，`ServerStartedEvent` 触发，此时：
   - 调用 `SlashBladeItemsFetcher.init()` 检查模组兼容性
   - 调用数据加载方法获取完整的物品信息（包括translationKey）
3. 由于`ServerStartedEvent`在所有模组物品注册完成后触发，可以安全获取translationKey

## 注意事项

1. **时序很重要**: 不要在世界加载阶段调用数据获取方法，应该在服务器启动后
2. **线程安全**: 所有方法都是线程安全的
3. **异常处理**: 所有异常都会被捕获并记录，不会影响主模组运行
4. **性能优化**: 缓存机制避免了重复的模组检查和数据获取

## 参考

本实现参考了以下模组的兼容性处理方式：
- TACZ-1.20.1-1.1.7 的 CompatRegistry
- L2Hostility 的 CurioCompat
- Apotheosis 的 PatchouliCompat

这些模组都采用了类似的延迟初始化和缓存机制来处理模组间的兼容性问题。