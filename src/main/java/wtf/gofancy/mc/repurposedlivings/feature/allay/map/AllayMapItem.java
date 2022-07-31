package wtf.gofancy.mc.repurposedlivings.feature.allay.map;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import wtf.gofancy.mc.repurposedlivings.Capabilities;
import wtf.gofancy.mc.repurposedlivings.ModSetup;
import wtf.gofancy.mc.repurposedlivings.Network;
import wtf.gofancy.mc.repurposedlivings.feature.allay.map.capability.AllayMapDataCapability;
import wtf.gofancy.mc.repurposedlivings.feature.allay.map.network.UpdateAllayMapDataPacket;
import wtf.gofancy.mc.repurposedlivings.util.ItemTarget;
import wtf.gofancy.mc.repurposedlivings.util.ModUtil;
import wtf.gofancy.mc.repurposedlivings.util.TranslationUtils;

import java.util.List;
import java.util.Optional;

/**
 * Contains information about the item source and delivery targets.
 * Can be given to Hijacked Allays.
 */
public class AllayMapItem extends MapItem {

    public AllayMapItem() {
        super(new Properties().stacksTo(1));
    }

    /**
     * Creates a new Allay map by creating and storing its associated {@link MapItemSavedData} and {@link AllayMapData}.
     *
     * The map is created without any target points, you need to add them manually afterwards via the level capability
     * {@link AllayMapDataCapability AllayMapDataCapability}.
     *
     * @param level the level in which this map is created
     * @param playerX the X position of the inventory this map will be placed in, used to calculate the map bounds
     * @param playerZ the Z position of the inventory this map will be placed in, used to calculate the map bounds
     * @return a new Allay map which is linked to its new map data
     */
    public static ItemStack create(Level level, int playerX, int playerZ) {
        final int mapId = level.getFreeMapId();

        final var mapData = MapItemSavedData.createFresh(playerX, playerZ, (byte) 0, true, true, level.dimension());

        level.setMapData(MapItem.makeKey(mapId), mapData);

        level.getCapability(Capabilities.ALLAY_MAP_DATA).resolve().orElseThrow().set(mapId, new AllayMapData(mapId));

        final ItemStack stack = new ItemStack(ModSetup.ALLAY_MAP.get());
        stack.getOrCreateTag().putInt("map", mapId);

        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        if (!isAdvanced.isAdvanced()) {
            // automatically added by super if advanced
            tooltipComponents.add(Component.translatable("filled_map.id", MapItem.getMapId(stack))
                    .withStyle(ChatFormatting.GRAY));
        }

        final Optional<AllayMapData> dataOpt = level.getCapability(Capabilities.ALLAY_MAP_DATA)
                .resolve()
                .orElseThrow()
                .get(stack);

        if (dataOpt.isEmpty()) {
            // called before update packet arrived - we just return here, it will probably soonish anyway
            return;
        }

        final var data = dataOpt.get();

        final var source = data.getSource();
        final var destination = data.getDestination();

        if (data.isComplete()) {
            tooltipComponents.add(TranslationUtils.tooltip(this, "complete").withStyle(ChatFormatting.GOLD));
        } else {
            tooltipComponents.add(TranslationUtils.tooltip(this, "incomplete").withStyle(ChatFormatting.AQUA));
        }
        source.ifPresent(target -> tooltipComponents.add(this.composeTargetComponent("source", target)));
        destination.ifPresent(target -> tooltipComponents.add(this.composeTargetComponent("destination", target)));
    }

    private MutableComponent composeTargetComponent(final String name, final ItemTarget target) {
        return TranslationUtils.tooltip(this, "target_" + name)
                .append(TranslationUtils.tooltip(
                        this,
                        "target_pos",
                        target.pos().getX(),
                        target.pos().getY(),
                        target.pos().getZ()
                ))
                .append(", ")
                .append(TranslationUtils.tooltip(this, "target_side"))
                .append(TranslationUtils.tooltip(this, "direction." + target.side().getName()))
                .withStyle(ChatFormatting.DARK_GRAY);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        final var level = context.getLevel();

        if (level.isClientSide) return super.useOn(context);

        final var pos = context.getClickedPos();
        final var side = context.getClickedFace();

        final var data = level.getCapability(Capabilities.ALLAY_MAP_DATA)
                .resolve()
                .orElseThrow()
                .get(context.getItemInHand())
                .orElseThrow();

        if (data.getSource().isEmpty()) {
            if (ModUtil.isContainer(level, pos, side)) {
                data.setSource(new ItemTarget(pos, side));
                return InteractionResult.SUCCESS;
            }
        }
        if (data.getDestination().isEmpty()) {
            if (ModUtil.isContainer(level, pos, side)) {
                data.setDestination(new ItemTarget(pos, side));
                return InteractionResult.SUCCESS;
            }
        }

        return super.useOn(context);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof ServerPlayer player) {
            final var data = level.getCapability(Capabilities.ALLAY_MAP_DATA)
                    .resolve()
                    .orElseThrow()
                    .get(stack)
                    .orElseThrow();

            data.tick(level);

            final var syncFlag = player.getCapability(Capabilities.ALLAY_MAP_DATA_SYNC_FLAG)
                    .resolve()
                    .orElseThrow();

            if (syncFlag.requiresSync(data.getMapId())) {
                Network.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new UpdateAllayMapDataPacket(data)
                );
                syncFlag.setSynced(data.getMapId());
            }
        }

        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        if (level.isClientSide) return;

        final CompoundTag nbt = stack.getOrCreateTag();

        if (nbt.contains("map_scale_direction", Tag.TAG_ANY_NUMERIC)) {
            final int scale = nbt.getInt("map_scale_direction");

            final AllayMapDataCapability dataStorage = level.getCapability(Capabilities.ALLAY_MAP_DATA)
                    .resolve()
                    .orElseThrow();

            final AllayMapData currentData = dataStorage.get(stack).orElseThrow();
            final MapItemSavedData currentMapData = currentData.getCorrespondingMapData(level);

            final int newMapId = level.getFreeMapId();

            level.setMapData(MapItem.makeKey(newMapId), currentMapData.scaled(scale));
            dataStorage.set(newMapId, currentData.newInstanceFor(newMapId));

            nbt.remove("map_scale_direction");
            nbt.putInt("map", newMapId);
        }

        // TODO: map locking support?
    }
}
