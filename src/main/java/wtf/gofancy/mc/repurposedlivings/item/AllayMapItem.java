package wtf.gofancy.mc.repurposedlivings.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import wtf.gofancy.mc.repurposedlivings.ModSetup;
import wtf.gofancy.mc.repurposedlivings.capabilities.Capabilities;
import wtf.gofancy.mc.repurposedlivings.container.AllayMapContainer;
import wtf.gofancy.mc.repurposedlivings.network.AllayMapDataUpdateMessage;
import wtf.gofancy.mc.repurposedlivings.network.Network;
import wtf.gofancy.mc.repurposedlivings.util.ItemTarget;
import wtf.gofancy.mc.repurposedlivings.util.ModUtil;

import java.util.List;
import java.util.stream.Stream;

public class AllayMapItem extends MapItem {

    public AllayMapItem() {
        super(new Properties().stacksTo(1));
    }

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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        // TODO: currently broken (ensureTargetsLoaded needs change + it tries to open everytime you want to click a container even if it is unfinished)
//        if (player instanceof ServerPlayer serverPlayer && ensureTargetsLoaded(level, player, player.getItemInHand(usedHand))) {
//            NetworkHooks.openScreen(serverPlayer, new AllayMapMenuProvider(usedHand), buf -> buf.writeEnum(usedHand));
//        }
        return super.use(level, player, usedHand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        final var data = level.getCapability(Capabilities.ALLAY_MAP_DATA)
                .resolve()
                .orElseThrow()
                .get(stack)
                .orElseThrow();

        final var source = data.getSource();
        final var destination = data.getDestination();

        if (source.isEmpty() || destination.isEmpty()) {
            tooltipComponents.add(ModUtil.getItemTranslation(this, "complete_draft").withStyle(ChatFormatting.AQUA));
        }
        source.ifPresent(itemTarget -> tooltipComponents.add(ModUtil.getTargetTranslation(
                "source",
                itemTarget
        )));
        destination.ifPresent(itemTarget -> tooltipComponents.add(ModUtil.getTargetTranslation(
                "destination",
                itemTarget
        )));
    }

    private boolean ensureTargetsLoaded(Level level, Player player, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("from") && tag.contains("to")) {
            ItemTarget from = ItemTarget.fromNbt(tag.getCompound("from"));
            ItemTarget to = ItemTarget.fromNbt(tag.getCompound("to"));

            boolean loaded = Stream.of(from.pos(), from.getRelativePos(), to.pos(), to.getRelativePos()).allMatch(level::isLoaded);
            if (!loaded) player.displayClientMessage(ModUtil.getItemTranslation(this, "out_of_range").withStyle(ChatFormatting.RED), true);
            else return true;
        }
        return false;
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
                        new AllayMapDataUpdateMessage(data)
                );
                syncFlag.setSynced(data.getMapId());
            }
        }

        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
    }

    @Nullable
    @Override
    public Packet<?> getUpdatePacket(ItemStack stack, Level level, Player player) {
        return super.getUpdatePacket(stack, level, player);
        // TODO: sync capability to client in here -> doing it now in inventoryTick
        // we do not want a global sync mechanism as this would update all clients in the dimension if one map changes
        // by syncing via this update packet we ensure to only send the data to that one client
        // we also do not need to sync the complete capability but rather only that one entry for this specific map the player is holding
        // of course, if the player holds multiple maps this results in many update packets
        // TODO: check what vanilla maps do against the above statement (probably nothing)
    }

    private record AllayMapMenuProvider(InteractionHand hand) implements MenuProvider {

        @Override
            public Component getDisplayName() {
                return ModSetup.ALLAY_MAP.get().getDescription();
            }
    
            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                return new AllayMapContainer(containerId, this.hand, player);
            }
        }
}
