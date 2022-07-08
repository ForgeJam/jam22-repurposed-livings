package wtf.gofancy.mc.repurposedlivings.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.level.Level;

public class HijackedAllay extends Allay {

    public HijackedAllay(EntityType<? extends Allay> type, Level level) {
        super(type, level); // TODO Drop mind control device when killed
    }
}
