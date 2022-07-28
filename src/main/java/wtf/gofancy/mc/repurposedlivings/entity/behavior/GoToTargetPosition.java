package wtf.gofancy.mc.repurposedlivings.entity.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Accurate position target navigation.
 * Doesn't choose a random destination location unlike {@link net.minecraft.world.entity.ai.behavior.GoToTargetLocation GoToTargetLocation}
 */
public class GoToTargetPosition<E extends Mob, M> extends Behavior<E> {
   private final MemoryModuleType<M> locationMemory;
   private final Function<M, BlockPos> posExtractor;
   private final float speedModifier;
   private final Predicate<E> condition;

   public GoToTargetPosition(MemoryModuleType<M> locationMemory, Function<M, BlockPos> posExtractor, float speedModifier, Predicate<E> condition) {
      super(ImmutableMap.of(locationMemory, MemoryStatus.VALUE_PRESENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT));
      this.locationMemory = locationMemory;
      this.posExtractor = posExtractor;
      this.speedModifier = speedModifier;
      this.condition = condition;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel level, E owner) {
      return this.condition.test(owner);
   }

   @Override
   protected void start(ServerLevel level, Mob mob, long gameTime) {
      BlockPos pos = getTargetLocation(mob);
      BehaviorUtils.setWalkAndLookTargetMemories(mob, pos, this.speedModifier, 0);
   }

   private BlockPos getTargetLocation(Mob mob) {
      M memory = mob.getBrain().getMemory(this.locationMemory).orElseThrow();
      return this.posExtractor.apply(memory);
   }
}
