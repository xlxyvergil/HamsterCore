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
    
    // 特殊元素默认值
    private double criticalChance = 0.1;  // 默认暴击率
    private double criticalDamage = 0.5;  // 默认暴击伤害
    private double triggerChance = 0.2;   // 默认触发率 10%
    
    // 物理元素默认值
    private double impactRatio = 0.0;     // 冲击元素默认占比
    private double punctureRatio = 0.0;   // 穿刺元素默认占比
    private double slashRatio = 0.0;      // 切割元素默认占比
    
    // 基础元素默认值
    private double coldRatio = 0.0;       // 冰冻元素默认占比
    private double electricityRatio = 0.0; // 电击元素默认占比
    private double heatRatio = 0.0;       // 火焰元素默认占比
    private double toxinRatio = 0.0;      // 毒素元素默认占比
    
    // 复合元素默认值
    private double blastRatio = 0.0;      // 爆炸元素默认占比
    private double corrosiveRatio = 0.0;  // 腐蚀元素默认占比
    private double gasRatio = 0.0;        // 毒气元素默认占比
    private double magneticRatio = 0.0;   // 磁力元素默认占比
    private double radiationRatio = 0.0;  // 辐射元素默认占比
    private double viralRatio = 0.0;      // 病毒元素默认占比
    
    public double getCriticalChance() {
        return criticalChance;
    }
    
    public void setCriticalChance(double criticalChance) {
        this.criticalChance = criticalChance;
    }
    
    public double getCriticalDamage() {
        return criticalDamage;
    }
    
    public void setCriticalDamage(double criticalDamage) {
        this.criticalDamage = criticalDamage;
    }
    
    public double getTriggerChance() {
        return triggerChance;
    }
    
    public void setTriggerChance(double triggerChance) {
        this.triggerChance = triggerChance;
    }
    
    public double getImpactRatio() {
        return impactRatio;
    }
    
    public void setImpactRatio(double impactRatio) {
        this.impactRatio = impactRatio;
    }
    
    public double getPunctureRatio() {
        return punctureRatio;
    }
    
    public void setPunctureRatio(double punctureRatio) {
        this.punctureRatio = punctureRatio;
    }
    
    public double getSlashRatio() {
        return slashRatio;
    }
    
    public void setSlashRatio(double slashRatio) {
        this.slashRatio = slashRatio;
    }
    
    public double getColdRatio() {
        return coldRatio;
    }
    
    public void setColdRatio(double coldRatio) {
        this.coldRatio = coldRatio;
    }
    
    public double getElectricityRatio() {
        return electricityRatio;
    }
    
    public void setElectricityRatio(double electricityRatio) {
        this.electricityRatio = electricityRatio;
    }
    
    public double getHeatRatio() {
        return heatRatio;
    }
    
    public void setHeatRatio(double heatRatio) {
        this.heatRatio = heatRatio;
    }
    
    public double getToxinRatio() {
        return toxinRatio;
    }
    
    public void setToxinRatio(double toxinRatio) {
        this.toxinRatio = toxinRatio;
    }
    
    public double getBlastRatio() {
        return blastRatio;
    }
    
    public void setBlastRatio(double blastRatio) {
        this.blastRatio = blastRatio;
    }
    
    public double getCorrosiveRatio() {
        return corrosiveRatio;
    }
    
    public void setCorrosiveRatio(double corrosiveRatio) {
        this.corrosiveRatio = corrosiveRatio;
    }
    
    public double getGasRatio() {
        return gasRatio;
    }
    
    public void setGasRatio(double gasRatio) {
        this.gasRatio = gasRatio;
    }
    
    public double getMagneticRatio() {
        return magneticRatio;
    }
    
    public void setMagneticRatio(double magneticRatio) {
        this.magneticRatio = magneticRatio;
    }
    
    public double getRadiationRatio() {
        return radiationRatio;
    }
    
    public void setRadiationRatio(double radiationRatio) {
        this.radiationRatio = radiationRatio;
    }
    
    public double getViralRatio() {
        return viralRatio;
    }
    
    public void setViralRatio(double viralRatio) {
        this.viralRatio = viralRatio;
    }
    
    /**
     * 获取物理元素的默认值
     */
    public double getDefaultPhysicalValue(ElementType elementType) {
        switch (elementType) {
            case IMPACT:
                return impactRatio;
            case PUNCTURE:
                return punctureRatio;
            case SLASH:
                return slashRatio;
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
                return coldRatio;
            case ELECTRICITY:
                return electricityRatio;
            case HEAT:
                return heatRatio;
            case TOXIN:
                return toxinRatio;
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
                return blastRatio;
            case CORROSIVE:
                return corrosiveRatio;
            case GAS:
                return gasRatio;
            case MAGNETIC:
                return magneticRatio;
            case RADIATION:
                return radiationRatio;
            case VIRAL:
                return viralRatio;
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
                return criticalChance;
            case CRITICAL_DAMAGE:
                return criticalDamage;
            case TRIGGER_CHANCE:
                return triggerChance;
            default:
                return 0.0;
        }
    }
    
    /**
     * 序列化方法
     */
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        
        // 特殊元素
        json.addProperty("critical_chance", criticalChance);
        json.addProperty("critical_damage", criticalDamage);
        json.addProperty("trigger_chance", triggerChance);
        
        // 物理元素
        json.addProperty("impact_ratio", impactRatio);
        json.addProperty("puncture_ratio", punctureRatio);
        json.addProperty("slash_ratio", slashRatio);
        
        // 基础元素
        json.addProperty("cold_ratio", coldRatio);
        json.addProperty("electricity_ratio", electricityRatio);
        json.addProperty("heat_ratio", heatRatio);
        json.addProperty("toxin_ratio", toxinRatio);
        
        // 复合元素
        json.addProperty("blast_ratio", blastRatio);
        json.addProperty("corrosive_ratio", corrosiveRatio);
        json.addProperty("gas_ratio", gasRatio);
        json.addProperty("magnetic_ratio", magneticRatio);
        json.addProperty("radiation_ratio", radiationRatio);
        json.addProperty("viral_ratio", viralRatio);
        
        return json;
    }
    
    /**
     * 反序列化方法
     */
    public static WeaponData deserialize(JsonObject json) {
        WeaponData data = new WeaponData();
        
        // 特殊元素
        if (json.has("critical_chance")) {
            data.criticalChance = json.get("critical_chance").getAsDouble();
        }
        
        if (json.has("critical_damage")) {
            data.criticalDamage = json.get("critical_damage").getAsDouble();
        }
        
        if (json.has("trigger_chance")) {
            data.triggerChance = json.get("trigger_chance").getAsDouble();
        }
        
        // 物理元素
        if (json.has("impact_ratio")) {
            data.impactRatio = json.get("impact_ratio").getAsDouble();
        }
        
        if (json.has("puncture_ratio")) {
            data.punctureRatio = json.get("puncture_ratio").getAsDouble();
        }
        
        if (json.has("slash_ratio")) {
            data.slashRatio = json.get("slash_ratio").getAsDouble();
        }
        
        // 基础元素
        if (json.has("cold_ratio")) {
            data.coldRatio = json.get("cold_ratio").getAsDouble();
        }
        
        if (json.has("electricity_ratio")) {
            data.electricityRatio = json.get("electricity_ratio").getAsDouble();
        }
        
        if (json.has("heat_ratio")) {
            data.heatRatio = json.get("heat_ratio").getAsDouble();
        }
        
        if (json.has("toxin_ratio")) {
            data.toxinRatio = json.get("toxin_ratio").getAsDouble();
        }
        
        // 复合元素
        if (json.has("blast_ratio")) {
            data.blastRatio = json.get("blast_ratio").getAsDouble();
        }
        
        if (json.has("corrosive_ratio")) {
            data.corrosiveRatio = json.get("corrosive_ratio").getAsDouble();
        }
        
        if (json.has("gas_ratio")) {
            data.gasRatio = json.get("gas_ratio").getAsDouble();
        }
        
        if (json.has("magnetic_ratio")) {
            data.magneticRatio = json.get("magnetic_ratio").getAsDouble();
        }
        
        if (json.has("radiation_ratio")) {
            data.radiationRatio = json.get("radiation_ratio").getAsDouble();
        }
        
        if (json.has("viral_ratio")) {
            data.viralRatio = json.get("viral_ratio").getAsDouble();
        }
        
        return data;
    }
}