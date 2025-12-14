import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponElementData;
import com.xlxyvergil.hamstercore.element.InitialModifierEntry;
import com.xlxyvergil.hamstercore.element.BasicEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import java.util.UUID;

public class TestNBTStructure {
    public static void main(String[] args) {
        // 创建一个测试武器数据
        WeaponData weaponData = new WeaponData();
        
        // 添加一些测试数据
        weaponData.addBasicElement("critical_chance", "CONFIG", 0);
        weaponData.addBasicElement("critical_damage", "CONFIG", 1);
        weaponData.addBasicElement("impact", "CONFIG", 1);
        weaponData.addBasicElement("puncture", "CONFIG", 2);
        weaponData.addBasicElement("slash", "CONFIG", 0);
        weaponData.addBasicElement("trigger_chance", "CONFIG", 2);
        
        // 添加初始修饰符
        UUID criticalChanceUUID = UUID.fromString("930798076-1342817119-1445709451-1353380697".replace("-", ""));
        AttributeModifier criticalChanceModifier = new AttributeModifier(
            criticalChanceUUID, 
            "critical_chance", 
            0.05, 
            AttributeModifier.Operation.ADDITION
        );
        weaponData.addInitialModifier(new InitialModifierEntry("critical_chance", criticalChanceModifier));
        
        // 转换为NBT并打印
        CompoundTag nbt = weaponData.toNBT();
        System.out.println("NBT Structure:");
        System.out.println(nbt.toString());
        
        // 验证可以正确读回
        WeaponData restoredData = WeaponData.fromNBT(nbt);
        System.out.println("Restored data basic elements count: " + 
            restoredData.getBasicElements().size());
        System.out.println("Restored data initial modifiers count: " + 
            restoredData.getInitialModifiers().size());
    }
}