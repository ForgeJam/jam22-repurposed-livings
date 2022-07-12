package wtf.gofancy.mc.repurposedlivings.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.allay.AllayAi;
import net.minecraft.world.entity.schedule.Activity;
import wtf.gofancy.mc.repurposedlivings.ModSetup;
import wtf.gofancy.mc.repurposedlivings.entity.behavior.GoToTargetPosition;
import wtf.gofancy.mc.repurposedlivings.util.ItemTarget;

public class HijackedAllayAi extends AllayAi {

    public static Brain<?> makeBrain(Brain<Allay> brain) {
        initCoreActivity(brain);
        initItemTransferActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(ModSetup.ALLAY_TRANSFER_ITEMS.get());
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<Allay> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
            new Swim(0.8F),
            new LookAtTargetSink(45, 90),
            new MoveToTargetSink()
        ));
    }
    
    private static void initItemTransferActivity(Brain<Allay> brain) {
        brain.addActivityWithConditions(
            ModSetup.ALLAY_TRANSFER_ITEMS.get(),
            ImmutableList.of(
                Pair.of(0, new GoToTargetPosition<>(ModSetup.ALLAY_SOURCE_TARET.get(), ItemTarget::getRelativePos, 1.75F, e -> e.getItemInHand(InteractionHand.MAIN_HAND).isEmpty())),
                Pair.of(1, new GoToTargetPosition<>(ModSetup.ALLAY_DELIVERY_TARET.get(), ItemTarget::getRelativePos, 1.75F, Allay::hasItemInHand))
            ),
            ImmutableSet.of(
                Pair.of(ModSetup.ALLAY_SOURCE_TARET.get(), MemoryStatus.VALUE_PRESENT),
                Pair.of(ModSetup.ALLAY_DELIVERY_TARET.get(), MemoryStatus.VALUE_PRESENT)
            )
        );
    }
}
