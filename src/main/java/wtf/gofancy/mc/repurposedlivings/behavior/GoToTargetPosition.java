package wtf.gofancy.mc.repurposedlivings.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.function.Predicate;

public class GoToTargetPosition<E extends Mob> extends Behavior<E> {
   private final MemoryModuleType<BlockPos> locationMemory;
   private final float speedModifier;
   private final Predicate<E> condition;

   public GoToTargetPosition(MemoryModuleType<BlockPos> locationMemory, float speedModifier, Predicate<E> condition) {
      super(ImmutableMap.of(locationMemory, MemoryStatus.VALUE_PRESENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED));
      this.locationMemory = locationMemory;
      this.speedModifier = speedModifier;
      this.condition = condition;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel level, E owner) {
      return this.condition.test(owner);
   }

   @Override
   protected void start(ServerLevel level, Mob mob, long gameTime) {
      BlockPos blockpos = this.getTargetLocation(mob);
      BehaviorUtils.setWalkAndLookTargetMemories(mob, blockpos, this.speedModifier, 0);
   }

   private BlockPos getTargetLocation(Mob mob) {
      return mob.getBrain().getMemory(this.locationMemory).get();
   }
}
