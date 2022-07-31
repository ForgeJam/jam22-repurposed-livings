package wtf.gofancy.mc.repurposedlivings.features.allay.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.allay.AllayAi;
import net.minecraft.world.entity.schedule.Activity;
import wtf.gofancy.mc.repurposedlivings.ModSetup;
import wtf.gofancy.mc.repurposedlivings.features.allay.entity.behavior.GoToItemTarget;

public class HijackedAllayAi extends AllayAi {

    public static Brain<?> createBrain(Brain<HijackedAllay> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initItemTransferActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<HijackedAllay> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
            new Swim(0.8F),
            new AnimalPanic(2.5F),
            new LookAtTargetSink(45, 90),
            new MoveToTargetSink()
        ));
    }
    
    private static void initIdleActivity(Brain<HijackedAllay> brain) {
        brain.addActivityWithConditions(
            Activity.IDLE,
            ImmutableList.of(
                Pair.of(0, new RunSometimes<>(new SetEntityLookTarget(entity -> true, 6.0F), UniformInt.of(30, 60)))
            ),
            ImmutableSet.of(
                Pair.of(ModSetup.ALLAY_SOURCE_TARET.get(), MemoryStatus.VALUE_ABSENT),
                Pair.of(ModSetup.ALLAY_DELIVERY_TARET.get(), MemoryStatus.VALUE_ABSENT)
            )
        );
    }
    
    private static void initItemTransferActivity(Brain<HijackedAllay> brain) {
        brain.addActivityWithConditions(
            ModSetup.ALLAY_TRANSFER_ITEMS.get(),
            ImmutableList.of(
                Pair.of(0, new GoToItemTarget<>(ModSetup.ALLAY_SOURCE_TARET.get(), HijackedAllay::getTransportSpeedMultiplier, e -> e.getItemInHand(InteractionHand.MAIN_HAND).isEmpty())),
                Pair.of(1, new GoToItemTarget<>(ModSetup.ALLAY_DELIVERY_TARET.get(), HijackedAllay::getTransportSpeedMultiplier, Allay::hasItemInHand))
            ),
            ImmutableSet.of(
                Pair.of(ModSetup.ALLAY_SOURCE_TARET.get(), MemoryStatus.VALUE_PRESENT),
                Pair.of(ModSetup.ALLAY_DELIVERY_TARET.get(), MemoryStatus.VALUE_PRESENT)
            )
        );
    }
}
