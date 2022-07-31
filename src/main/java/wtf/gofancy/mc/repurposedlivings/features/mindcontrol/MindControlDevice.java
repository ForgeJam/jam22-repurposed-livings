package wtf.gofancy.mc.repurposedlivings.features.mindcontrol;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import wtf.gofancy.mc.repurposedlivings.ModSetup;
import wtf.gofancy.mc.repurposedlivings.features.allay.entity.AllayEquipment;
import wtf.gofancy.mc.repurposedlivings.features.allay.entity.HijackedAllay;

/**
 * Hijacks Allays to enable item transfer functionality.
 * Can only be applied to dancing Allays.
 */
public class MindControlDevice extends Item {

    public MindControlDevice(Properties properties) {
        super(properties.stacksTo(1));
    }

    public InteractionResult interactLivingEntityFirst(LivingEntity entity, ItemStack stack) {
        // If applied on an Allay at a valid time, replace it with our modified variant
        if (entity.getType() == EntityType.ALLAY && canAttachToAllay((Allay) entity)) {
            ((Allay) entity).dropEquipment();
            
            if (entity.level instanceof ServerLevel serverLevel) {
                HijackedAllay hijackedAllay = new HijackedAllay(ModSetup.HIJACKED_ALLAY_ENTITY.get(), entity.level);
                // Copy position and body rotation
                hijackedAllay.moveTo(entity.position());
                hijackedAllay.setXRot(entity.getXRot());
                hijackedAllay.setYRot(entity.getYRot());
                hijackedAllay.setOldPosAndRot();
                hijackedAllay.setYBodyRot(entity.getYRot());
                hijackedAllay.setYHeadRot(entity.getYRot());
                hijackedAllay.setPersistenceRequired();
                hijackedAllay.setEquipmentSlot(AllayEquipment.CONTROLLER, stack.copy());

                entity.remove(Entity.RemovalReason.DISCARDED);
                entity.level.addFreshEntity(hijackedAllay);
                stack.shrink(1);
                
                // Audiovisual feedback
                serverLevel.sendParticles(ParticleTypes.WITCH, hijackedAllay.getX(), hijackedAllay.getY() + 0.2, hijackedAllay.getZ(), 30, 0.35, 0.35, 0.35, 0);
                serverLevel.playSound(null, hijackedAllay, ModSetup.MIND_CONTROL_DEVICE_ATTACH_SOUND.get(), SoundSource.MASTER, 1, 1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    public boolean canAttachToAllay(Allay allay) {
        // Can only hijack dancing allays
        return allay.isDancing();
    }
}
