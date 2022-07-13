package wtf.gofancy.mc.repurposedlivings.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import wtf.gofancy.mc.repurposedlivings.ModSetup;
import wtf.gofancy.mc.repurposedlivings.entity.AllayEquipment;
import wtf.gofancy.mc.repurposedlivings.entity.HijackedAllay;

import java.util.List;

public class MindControlDevice extends Item {

    public MindControlDevice(Properties properties) {
        super(properties.stacksTo(1));
    }

    public boolean interactLivingEntityFirst(LivingEntity entity, ItemStack stack) {
        if (!entity.level.isClientSide && entity.getType() == EntityType.ALLAY) {
            ((Allay) entity).dropEquipment();
            HijackedAllay hijackedAllay = new HijackedAllay(ModSetup.HIJACKED_ALLAY_ENTITY.get(), entity.level);
            hijackedAllay.moveTo(entity.position());
            hijackedAllay.setPersistenceRequired();
            hijackedAllay.setEquipmentSlot(AllayEquipment.CONTROLLER, stack.copy());

            entity.remove(Entity.RemovalReason.DISCARDED);
            entity.level.addFreshEntity(hijackedAllay);
            stack.shrink(1);
            
            attachToAllay(hijackedAllay);
            return true;
        }
        return false;
    }

    public void attachToAllay(HijackedAllay allay) {
        allay.getBrain().setActiveActivityToFirstValid(List.of(Activity.PANIC));
    }
}
