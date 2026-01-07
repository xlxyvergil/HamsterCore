package com.xlxyvergil.hamstercore.modification;

import dev.shadowsoffire.placebo.config.Configuration;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ModificationConfig {
    
    /**
     * These lists contain "loot table matchers" for the drop chances for loot tables.
     * Loot table matchers take the form of domain:pattern and the float chance is 0..1
     * Omitting the domain causes the pattern to be run for all domains.
     * The pattern is only run on the loot table's path.
     */
    public static final List<LootPatternMatcher> LOOT_RULES = new ArrayList<>();
    
    /**
     * 怪物掉落改装件的概率
     */
    public static float modificationDropChance = 0.05F;
    
    /**
     * Boss额外掉落概率
     */
    public static float bossBonus = 0.10F;
    
    /**
     * 加载配置文件
     */
    public static void load(Configuration c) throws Exception {
        c.setTitle("HamsterCore Modification Config");

        // 实体掉落概率配置
        modificationDropChance = c.getFloat("Modification Drop Chance", "modifications", modificationDropChance, 0, 1,
            "The chance that a monster will drop a modification when killed by a player. 0 = 0%, 1 = 100%");

        bossBonus = c.getFloat("Boss Bonus", "modifications", bossBonus, 0, 1,
            "The flat bonus chance that Apotheosis bosses have to drop a modification, added to Modification Drop Chance. 0 = 0%, 1 = 100%");

        // 参考Apotheosis的配置方式 - 只用于箱子掉落
        // 实体掉落通过LivingDropsEvent处理，只对Monster掉落
        String[] lootRules = c.getStringList("modification_loot_rules", "modifications",
            new String[] { "minecraft:chests.*|0.15", ".*chests.*|0.10" },
            "Loot Rules, in the form of Loot Table Matchers, permitting modifications to spawn in loot tables (chests only).\n" +
            "The format for these is domain:pattern|chance and domain is optional.\n" +
            "Domain is a modid, pattern is a regex string, and chance is a float 0..1 chance for the modification to spawn.\n" +
            "If you omit the domain, the format is pattern|chance, and the matcher will run for all domains.\n" +
            "The pattern MUST be a valid regex string.\n" +
            "Note: Entity drops are handled by LivingDropsEvent and only apply to monsters (Monster class).\n" +
            "This configuration only affects chest/loot table drops.\n" +
            "Example: minecraft:chests.*|0.15 (Vanilla chests, 15% chance)");
        
        LOOT_RULES.clear();
        for (String s : lootRules) {
            LOOT_RULES.add(LootPatternMatcher.parse(s));
        }

        // 临时调试输出
        System.out.println("[HamsterCore Debug] Loaded Loot Rules: " + LOOT_RULES.size());
        for (LootPatternMatcher rule : LOOT_RULES) {
            System.out.println("[HamsterCore Debug] Rule: " + rule);
        }

        // 保存配置文件（如果有更改）
        if (c.hasChanged()) {
            c.save();
        }
    }
    
    public static float getModificationDropChance() {
        return modificationDropChance;
    }
    
    public static float getBossBonus() {
        return bossBonus;
    }
    
    public static record LootPatternMatcher(String domain, Pattern pathRegex, float chance) {

        public boolean matches(ResourceLocation id) {
            return (this.domain == null || this.domain.equals(id.getNamespace())) && this.pathRegex.matcher(id.getPath()).matches();
        }

        public static LootPatternMatcher parse(String s) throws Exception {
            int pipe = s.lastIndexOf('|');
            int colon = s.indexOf(':');
            float chance = Float.parseFloat(s.substring(pipe + 1));
            String domain = colon == -1 ? null : s.substring(0, colon);
            Pattern pattern = Pattern.compile(s.substring(colon + 1, pipe));
            return new LootPatternMatcher(domain, pattern, chance);
        }
    }
}