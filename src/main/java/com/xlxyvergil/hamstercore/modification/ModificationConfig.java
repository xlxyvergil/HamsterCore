package com.xlxyvergil.hamstercore.modification;

import dev.shadowsoffire.placebo.config.Configuration;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ModificationConfig {
    
    /**
     * 改装件掉落规则列表
     * 包含"战利品表匹配器"，用于控制改装件的掉落机会
     */
    public static final List<LootPatternMatcher> LOOT_RULES = new ArrayList<>();
    
    /**
     * 加载配置文件
     */
    public static void load(Configuration c) {
        c.setTitle("HamsterCore Modification Config");
        
        // 加载掉落规则
        String[] lootRules = c.getStringList(
            "Modification Loot Rules", 
            "modifications", 
            new String[] { "entities.*|0.05" },
            "Loot Rules, in the form of Loot Table Matchers, permitting modifications to spawn in loot tables.\n" +
            "The format for these is domain:pattern|chance and domain is optional.\n" +
            "Domain is a modid, pattern is a regex string, and chance is a float 0..1 chance for the modification to spawn.\n" +
            "If you omit the domain, the format is pattern|chance, and the matcher will run for all domains.\n" +
            "The pattern MUST be a valid regex string." +
            "Note: Only MONSTER category mobs will drop modifications."
        );
        
        LOOT_RULES.clear();
        for (String s : lootRules) {
            try {
                LOOT_RULES.add(LootPatternMatcher.parse(s));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 战利品表匹配器
     * 用于匹配特定的战利品表并应用掉落规则
     */
    public static record LootPatternMatcher(
        String domain,  // 命名空间，null表示匹配所有命名空间
        Pattern pathRegex,  // 路径正则表达式
        float chance  // 掉落概率（0-1）
    ) {
        
        /**
         * 检查给定的战利品表ID是否匹配当前规则
         */
        public boolean matches(ResourceLocation id) {
            return (this.domain == null || this.domain.equals(id.getNamespace())) && 
                   this.pathRegex.matcher(id.getPath()).matches();
        }
        
        /**
         * 从字符串解析LootPatternMatcher
         * 格式：domain:pattern|chance 或 pattern|chance
         */
        public static LootPatternMatcher parse(String s) throws Exception {
            int pipe = s.lastIndexOf('|');
            int colon = s.indexOf(':');
            
            float chance = Float.parseFloat(s.substring(pipe + 1));
            
            String domain;
            String pathPattern;
            
            if (colon == -1 || colon > pipe) {
                // 没有命名空间，格式为 pattern|chance
                domain = null;
                pathPattern = s.substring(0, pipe);
            } else {
                // 有命名空间，格式为 domain:pattern|chance
                domain = s.substring(0, colon);
                pathPattern = s.substring(colon + 1, pipe);
            }
            
            return new LootPatternMatcher(domain, Pattern.compile(pathPattern), chance);
        }
    }
}