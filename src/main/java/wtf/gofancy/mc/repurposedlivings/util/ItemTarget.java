package wtf.gofancy.mc.repurposedlivings.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

public record ItemTarget(BlockPos pos, Direction side) {
    public static final Codec<ItemTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.fieldOf("pos").forGetter(ItemTarget::pos),
        Direction.CODEC.fieldOf("side").forGetter(ItemTarget::side)
    ).apply(instance, ItemTarget::new));
    
    public static ItemTarget fromNbt(Tag tag) {
        DataResult<ItemTarget> result = ItemTarget.CODEC.parse(NbtOps.INSTANCE, tag);
        return result.getOrThrow(false, str -> {});
    }

    public Tag serializeNbt() {
        DataResult<Tag> result = ItemTarget.CODEC.encodeStart(NbtOps.INSTANCE, this);
        return result.getOrThrow(false, str -> {});
    }

    public BlockPos getRelativePos() {
        return pos().relative(side());
    }
}
