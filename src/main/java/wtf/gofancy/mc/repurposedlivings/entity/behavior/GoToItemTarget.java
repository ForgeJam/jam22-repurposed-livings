package wtf.gofancy.mc.repurposedlivings.entity.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import wtf.gofancy.mc.repurposedlivings.util.ItemTarget;

import java.util.function.Predicate;

/**
 * Accurate item target position navigation.
 * Doesn't choose a random destination location unlike {@link net.minecraft.world.entity.ai.behavior.GoToTargetLocation GoToTargetLocation}
 */
public class GoToItemTarget<E extends Mob> extends Behavior<E> {
   private final MemoryModuleType<ItemTarget> itemTargetMemory;
   private final float speedModifier;
   private final Predicate<E> condition;

   public GoToItemTarget(MemoryModuleType<ItemTarget> itemTargetMemory, float speedModifier, Predicate<E> condition) {
      super(ImmutableMap.of(itemTargetMemory, MemoryStatus.VALUE_PRESENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT));
      this.itemTargetMemory = itemTargetMemory;
      this.speedModifier = speedModifier;
      this.condition = condition;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel level, E owner) {
      return this.condition.test(owner);
   }

   @Override
   protected void start(ServerLevel level, Mob mob, long gameTime) {
      Brain<?> brain = mob.getBrain();
      ItemTarget itemTarget = brain.getMemory(this.itemTargetMemory).orElseThrow(); 
      BlockPos lookPos = itemTarget.pos();
      BlockPos walkPos = itemTarget.getRelativePos();
      PositionTracker lookTarget = new BlockPosTracker(lookPos);
      WalkTarget walkTarget = new WalkTarget(new BlockPosTracker(walkPos), this.speedModifier, 0);
      
      brain.setMemory(MemoryModuleType.LOOK_TARGET, lookTarget);
      brain.setMemory(MemoryModuleType.WALK_TARGET, walkTarget);
   }
}
