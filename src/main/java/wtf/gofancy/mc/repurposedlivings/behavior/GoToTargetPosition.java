package wtf.gofancy.mc.repurposedlivings.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class GoToTargetPosition<E extends Mob> extends Behavior<E> {
   private final MemoryModuleType<BlockPos> locationMemory;
   private final float speedModifier;

   public GoToTargetPosition(MemoryModuleType<BlockPos> locationMemory, float speedModifier) {
      super(ImmutableMap.of(locationMemory, MemoryStatus.VALUE_PRESENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED));
      this.locationMemory = locationMemory;
      this.speedModifier = speedModifier;
   }

   protected void start(ServerLevel level, Mob mob, long gameTime) {
      BlockPos blockpos = this.getTargetLocation(mob);
      BehaviorUtils.setWalkAndLookTargetMemories(mob, blockpos, this.speedModifier, 0);
   }

   private BlockPos getTargetLocation(Mob p_217249_) {
      return p_217249_.getBrain().getMemory(this.locationMemory).get();
   }
}
