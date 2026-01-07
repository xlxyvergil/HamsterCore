package com.xlxyvergil.hamstercore.modification;

import dev.shadowsoffire.placebo.config.Configuration;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// 完全复制Apotheosis的AdventureConfig结构，只修改我们需要的部分
public class ModificationConfig {
    
    /**
     * These lists contain "loot table matchers" for the drop chances for loot tables.
     * Loot table matchers take the form of domain:pattern and the float chance is 0..1
     * Omitting the domain causes the pattern to be run for all domains.
     * The pattern is only run on the loot table's path.
     */
    public static final List<LootPatternMatcher> LOOT_RULES = new ArrayList<>();
    
    /**
     * 加载配置文件
     */
    public static void load(Configuration c) {
        c.setTitle("HamsterCore Modification Config");
        
        // 使用简单的注释，避免复杂的转义问题
        String[] lootRules = c.getStringList("modification_loot_rules", "modifications", 
            new String[] { "entities.*|0.05" }, 
            "Loot Rules, in the form of Loot Table Matchers, permitting modifications to spawn in loot tables.\n" +
            "The format for these is domain:pattern|chance and domain is optional.\n" +
            "Domain is a modid, pattern is a regex string, and chance is a float 0..1 chance for the modification to spawn.\n" +
            "If you omit the domain, the format is pattern|chance, and the matcher will run for all domains.\n" +
            "The pattern MUST be a valid regex string.\n" +
            "Example: entities.*|0.05 (所有实体掉落表，5%概率)");
        
        LOOT_RULES.clear();
        for (String s : lootRules) {
            try {
                LOOT_RULES.add(LootPatternMatcher.parse(s));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    // 完全复制Apotheosis的LootPatternMatcher
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