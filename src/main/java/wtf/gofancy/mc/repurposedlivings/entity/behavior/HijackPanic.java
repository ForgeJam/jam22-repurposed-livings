package wtf.gofancy.mc.repurposedlivings.entity.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import wtf.gofancy.mc.repurposedlivings.entity.HijackedAllay;

import javax.annotation.Nullable;
import java.util.Optional;

public class HijackPanic extends Behavior<HijackedAllay> {
    public static final int PANIC_DURATION = 100;
    private static final int PANIC_DISTANCE_HORIZONTAL = 2;
    private static final int PANIC_DISTANCE_VERTICAL = 2;
    private final float speedMultiplier;

    public HijackPanic(float speedMultiplier) {
        super(ImmutableMap.of(), PANIC_DURATION, PANIC_DURATION);
        this.speedMultiplier = speedMultiplier;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, HijackedAllay allay, long gameTime) {
        return true;
    }

    @Override
    protected void start(ServerLevel level, HijackedAllay allay, long gameTime) {
        
    }

    @Override
    protected void stop(ServerLevel level, HijackedAllay allay, long gameTime) {
        allay.onPanicStopped();
    }

    @Override
    protected void tick(ServerLevel level, HijackedAllay allay, long gameTime) {
        if (allay.getNavigation().isDone()) {
            Vec3 panicPos = getPanicPos(allay, level);
            if (panicPos != null) {
                allay.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(panicPos, this.speedMultiplier, 0));
            }
        }
    }

    @Nullable
    private Vec3 getPanicPos(HijackedAllay allay, ServerLevel level) {
        if (allay.isOnFire()) {
            Optional<Vec3> optional = lookForWater(level, allay).map(Vec3::atBottomCenterOf);
            if (optional.isPresent()) return optional.get();
        }

        return LandRandomPos.getPos(allay, PANIC_DISTANCE_HORIZONTAL, PANIC_DISTANCE_VERTICAL);
    }

    private Optional<BlockPos> lookForWater(BlockGetter level, Entity entity) {
        BlockPos blockpos = entity.blockPosition();
        return !level.getBlockState(blockpos).getCollisionShape(level, blockpos).isEmpty() ? Optional.empty()
            : BlockPos.findClosestMatch(blockpos, 5, 1, pos -> level.getFluidState(pos).is(FluidTags.WATER));
    }
}
