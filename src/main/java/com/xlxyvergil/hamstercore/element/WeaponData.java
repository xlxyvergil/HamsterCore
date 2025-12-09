package com.xlxyvergil.hamstercore.element;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * 武器数据类，存储武器的默认元素属性配置
 * 类似于TACZ的GunData设计
 * 结构按照：3种特殊元素+物理元素+基础元素+复合元素
 */
public class WeaponData {
    
    // MOD相关信息
    public String modid;
    public String itemId;
    public String gunId;
    public String translationKey;
    
    // 元素数据
    public WeaponElementData elementData = new WeaponElementData();
    
    // 默认特殊属性值
    private double critical_chance = 0.1;  // 默认暴击率
    private double critical_damage = 0.5;  // 默认暴击伤害
    private double trigger_chance = 0.2;   // 默认触发率 10%
    
    // 物理元素默认值
    private double impact_ratio = 0.0;     // 冲击元素默认占比
    private double puncture_ratio = 0.0;   // 穿刺元素默认占比
    private double slash_ratio = 0.0;      // 切割元素默认占比
    
    // 基础元素默认值
    private double cold_ratio = 0.0;       // 冰冻元素默认占比
    private double electricity_ratio = 0.0; // 电击元素默认占比
    private double heat_ratio = 0.0;       // 火焰元素默认占比
    private double toxin_ratio = 0.0;      // 毒素元素默认占比
    
    // 复合元素默认值
    private double blast_ratio = 0.0;      // 爆炸元素默认占比
    private double corrosive_ratio = 0.0;  // 腐蚀元素默认占比
    private double gas_ratio = 0.0;        // 毒气元素默认占比
    private double magnetic_ratio = 0.0;   // 磁力元素默认占比
    private double radiation_ratio = 0.0;  // 辐射元素默认占比
    private double viral_ratio = 0.0;      // 病毒元素默认占比
    
    // 派系元素默认值
    private double corpus_ratio = 0.0;      // Corpus派系增伤默认值
    private double grineer_ratio = 0.0;     // Grineer派系增伤默认值
    private double infested_ratio = 0.0;    // Infested派系增伤默认값
    private double orokin_ratio = 0.0;      // Orokin派系增伤默认값
    private double sentient_ratio = 0.0;    // Sentient派系增伤默认값
    private double murmur_ratio = 0.0;      // Murmur派系增伤默认값
    
    public double getCriticalChance() {
        return critical_chance;
    }
    
    public void setCriticalChance(double criticalChance) {
        this.critical_chance = criticalChance;
    }
    
    public double getCriticalDamage() {
        return critical_damage;
    }
    
    public void setCriticalDamage(double criticalDamage) {
        this.critical_damage = criticalDamage;
    }
    
    public double getTriggerChance() {
        return trigger_chance;
    }
    
    public void setTriggerChance(double triggerChance) {
        this.trigger_chance = triggerChance;
    }
    
    public double getImpactRatio() {
        return impact_ratio;
    }
    
    public void setImpactRatio(double impactRatio) {
        this.impact_ratio = impactRatio;
    }
    
    public double getPunctureRatio() {
        return puncture_ratio;
    }
    
    public void setPunctureRatio(double punctureRatio) {
        this.puncture_ratio = punctureRatio;
    }
    
    public double getSlashRatio() {
        return slash_ratio;
    }
    
    public void setSlashRatio(double slashRatio) {
        this.slash_ratio = slashRatio;
    }
    
    public double getColdRatio() {
        return cold_ratio;
    }
    
    public void setColdRatio(double coldRatio) {
        this.cold_ratio = coldRatio;
    }
    
    public double getElectricityRatio() {
        return electricity_ratio;
    }
    
    public void setElectricityRatio(double electricityRatio) {
        this.electricity_ratio = electricityRatio;
    }
    
    public double getHeatRatio() {
        return heat_ratio;
    }
    
    public void setHeatRatio(double heatRatio) {
        this.heat_ratio = heatRatio;
    }
    
    public double getToxinRatio() {
        return toxin_ratio;
    }
    
    public void setToxinRatio(double toxinRatio) {
        this.toxin_ratio = toxinRatio;
    }
    
    public double getBlastRatio() {
        return blast_ratio;
    }
    
    public void setBlastRatio(double blastRatio) {
        this.blast_ratio = blastRatio;
    }
    
    public double getCorrosiveRatio() {
        return corrosive_ratio;
    }
    
    public void setCorrosiveRatio(double corrosiveRatio) {
        this.corrosive_ratio = corrosiveRatio;
    }
    
    public double getGasRatio() {
        return gas_ratio;
    }
    
    public void setGasRatio(double gasRatio) {
        this.gas_ratio = gasRatio;
    }
    
    public double getMagneticRatio() {
        return magnetic_ratio;
    }
    
    public void setMagneticRatio(double magneticRatio) {
        this.magnetic_ratio = magneticRatio;
    }
    
    public double getRadiationRatio() {
        return radiation_ratio;
    }
    
    public void setRadiationRatio(double radiationRatio) {
        this.radiation_ratio = radiationRatio;
    }
    
    public double getViralRatio() {
        return viral_ratio;
    }
    
    public void setViralRatio(double viralRatio) {
        this.viral_ratio = viralRatio;
    }
    
    // 派系元素getter和setter方法
    public double getCorpusRatio() {
        return corpus_ratio;
    }
    
    public void setCorpusRatio(double corpusRatio) {
        this.corpus_ratio = corpusRatio;
    }
    
    public double getGrineerRatio() {
        return grineer_ratio;
    }
    
    public void setGrineerRatio(double grineerRatio) {
        this.grineer_ratio = grineerRatio;
    }
    
    public double getInfestedRatio() {
        return infested_ratio;
    }
    
    public void setInfestedRatio(double infestedRatio) {
        this.infested_ratio = infestedRatio;
    }
    
    public double getOrokinRatio() {
        return orokin_ratio;
    }
    
    public void setOrokinRatio(double orokinRatio) {
        this.orokin_ratio = orokinRatio;
    }
    
    public double getSentientRatio() {
        return sentient_ratio;
    }
    
    public void setSentientRatio(double sentientRatio) {
        this.sentient_ratio = sentientRatio;
    }
    
    public double getMurmurRatio() {
        return murmur_ratio;
    }
    
    public void setMurmurRatio(double murmurRatio) {
        this.murmur_ratio = murmurRatio;
    }
    
    /**
     * 获取物理元素的默认值
     */
    public double getDefaultPhysicalValue(ElementType elementType) {
        switch (elementType) {
            case IMPACT:
                return impact_ratio;
            case PUNCTURE:
                return puncture_ratio;
            case SLASH:
                return slash_ratio;
            default:
                return 0.0;
        }
    }
    
    /**
     * 获取基础元素的默认值
     */
    public double getDefaultBasicValue(ElementType elementType) {
        switch (elementType) {
            case COLD:
                return cold_ratio;
            case ELECTRICITY:
                return electricity_ratio;
            case HEAT:
                return heat_ratio;
            case TOXIN:
                return toxin_ratio;
            default:
                return 0.0;
        }
    }
    
    /**
     * 获取复合元素的默认值
     */
    public double getDefaultComplexValue(ElementType elementType) {
        switch (elementType) {
            case BLAST:
                return blast_ratio;
            case CORROSIVE:
                return corrosive_ratio;
            case GAS:
                return gas_ratio;
            case MAGNETIC:
                return magnetic_ratio;
            case RADIATION:
                return radiation_ratio;
            case VIRAL:
                return viral_ratio;
            default:
                return 0.0;
        }
    }
    
    /**
     * 获取特殊属性的默认值
     */
    public double getDefaultSpecialValue(ElementType elementType) {
        switch (elementType) {
            case CRITICAL_CHANCE:
                return critical_chance;
            case CRITICAL_DAMAGE:
                return critical_damage;
            case TRIGGER_CHANCE:
                return trigger_chance;
            default:
                return 0.0;
        }
    }
    
    /**
     * 获取派系元素的默认值
     */
    public double getDefaultFactionValue(String factionName) {
        switch (factionName.toLowerCase()) {
            case "corpus":
                return corpus_ratio;
            case "grineer":
                return grineer_ratio;
            case "infested":
                return infested_ratio;
            case "orokin":
                return orokin_ratio;
            case "sentient":
                return sentient_ratio;
            case "murmur":
                return murmur_ratio;
            default:
                return 0.0;
        }
    }
    
    /**
     * 序列化方法
     */
    public WeaponElementData getElementData() {
        return elementData;
    }
    
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        
        // 特殊元素
        json.addProperty("critical_chance", critical_chance);
        json.addProperty("critical_damage", critical_damage);
        json.addProperty("trigger_chance", trigger_chance);
        
        // 物理元素
        json.addProperty("impact_ratio", impact_ratio);
        json.addProperty("puncture_ratio", puncture_ratio);
        json.addProperty("slash_ratio", slash_ratio);
        
        // 基础元素
        json.addProperty("cold_ratio", cold_ratio);
        json.addProperty("electricity_ratio", electricity_ratio);
        json.addProperty("heat_ratio", heat_ratio);
        json.addProperty("toxin_ratio", toxin_ratio);
        
        // 复合元素
        json.addProperty("blast_ratio", blast_ratio);
        json.addProperty("corrosive_ratio", corrosive_ratio);
        json.addProperty("gas_ratio", gas_ratio);
        json.addProperty("magnetic_ratio", magnetic_ratio);
        json.addProperty("radiation_ratio", radiation_ratio);
        json.addProperty("viral_ratio", viral_ratio);
        
        // 派系元素
        json.addProperty("corpus_ratio", corpus_ratio);
        json.addProperty("grineer_ratio", grineer_ratio);
        json.addProperty("infested_ratio", infested_ratio);
        json.addProperty("orokin_ratio", orokin_ratio);
        json.addProperty("sentient_ratio", sentient_ratio);
        json.addProperty("murmur_ratio", murmur_ratio);
        
        return json;
    }
    
    /**
     * 反序列化方法
     */
    public static WeaponData deserialize(JsonObject json) {
        WeaponData data = new WeaponData();
        
        // 特殊元素
        if (json.has("critical_chance")) {
            data.critical_chance = json.get("critical_chance").getAsDouble();
        }
        
        if (json.has("critical_damage")) {
            data.critical_damage = json.get("critical_damage").getAsDouble();
        }
        
        if (json.has("trigger_chance")) {
            data.trigger_chance = json.get("trigger_chance").getAsDouble();
        }
        
        // 物理元素
        if (json.has("impact_ratio")) {
            data.impact_ratio = json.get("impact_ratio").getAsDouble();
        }
        
        if (json.has("puncture_ratio")) {
            data.puncture_ratio = json.get("puncture_ratio").getAsDouble();
        }
        
        if (json.has("slash_ratio")) {
            data.slash_ratio = json.get("slash_ratio").getAsDouble();
        }
        
        // 基础元素
        if (json.has("cold_ratio")) {
            data.cold_ratio = json.get("cold_ratio").getAsDouble();
        }
        
        if (json.has("electricity_ratio")) {
            data.electricity_ratio = json.get("electricity_ratio").getAsDouble();
        }
        
        if (json.has("heat_ratio")) {
            data.heat_ratio = json.get("heat_ratio").getAsDouble();
        }
        
        if (json.has("toxin_ratio")) {
            data.toxin_ratio = json.get("toxin_ratio").getAsDouble();
        }
        
        // 复合元素
        if (json.has("blast_ratio")) {
            data.blast_ratio = json.get("blast_ratio").getAsDouble();
        }
        
        if (json.has("corrosive_ratio")) {
            data.corrosive_ratio = json.get("corrosive_ratio").getAsDouble();
        }
        
        if (json.has("gas_ratio")) {
            data.gas_ratio = json.get("gas_ratio").getAsDouble();
        }
        
        if (json.has("magnetic_ratio")) {
            data.magnetic_ratio = json.get("magnetic_ratio").getAsDouble();
        }
        
        if (json.has("radiation_ratio")) {
            data.radiation_ratio = json.get("radiation_ratio").getAsDouble();
        }
        
        if (json.has("viral_ratio")) {
            data.viral_ratio = json.get("viral_ratio").getAsDouble();
        }
        
        // 派系元素
        if (json.has("corpus_ratio")) {
            data.corpus_ratio = json.get("corpus_ratio").getAsDouble();
        }
        
        if (json.has("grineer_ratio")) {
            data.grineer_ratio = json.get("grineer_ratio").getAsDouble();
        }
        
        if (json.has("infested_ratio")) {
            data.infested_ratio = json.get("infested_ratio").getAsDouble();
        }
        
        if (json.has("orokin_ratio")) {
            data.orokin_ratio = json.get("orokin_ratio").getAsDouble();
        }
        
        if (json.has("sentient_ratio")) {
            data.sentient_ratio = json.get("sentient_ratio").getAsDouble();
        }
        
        if (json.has("murmur_ratio")) {
            data.murmur_ratio = json.get("murmur_ratio").getAsDouble();
        }
        
        return data;
    }
}