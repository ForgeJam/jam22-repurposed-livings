package wtf.gofancy.mc.repurposedlivings;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EmptyMapItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wtf.gofancy.mc.repurposedlivings.features.allay.map.capability.AllayMapDataStorageProvider;
import wtf.gofancy.mc.repurposedlivings.features.allay.map.capability.AllayMapDataSyncFlagProvider;
import wtf.gofancy.mc.repurposedlivings.features.allay.map.AllayMapItem;
import wtf.gofancy.mc.repurposedlivings.features.mindcontrol.MindControlDevice;
import wtf.gofancy.mc.repurposedlivings.util.ItemTarget;
import wtf.gofancy.mc.repurposedlivings.util.ModUtil;
import wtf.gofancy.mc.repurposedlivings.util.TranslationUtils;

public class EventHandler {

    @SubscribeEvent
    public void onAttachCapabilities(final AttachCapabilitiesEvent<Level> event) {
        event.addCapability(
                RepurposedLivings.rl("allay_map_data"),
                new AllayMapDataStorageProvider()
        );
    }

    @SubscribeEvent
    public void onAttachPlayerCapabilities(final AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof ServerPlayer)) return;

        event.addCapability(
                RepurposedLivings.rl("allay_map_data_sync_flag"),
                new AllayMapDataSyncFlagProvider()
        );
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        final var oldPlayer = event.getOriginal();
        final var newPlayer = event.getEntity();

        oldPlayer.reviveCaps();

        final var oldCap = oldPlayer.getCapability(Capabilities.ALLAY_MAP_DATA_SYNC_FLAG).resolve().orElseThrow();
        final var newCap = newPlayer.getCapability(Capabilities.ALLAY_MAP_DATA_SYNC_FLAG).resolve().orElseThrow();

        newCap.deserializeNBT(oldCap.serializeNBT());

        oldPlayer.invalidateCaps();
    }

    @SubscribeEvent
    public void onPlayerJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(Capabilities.ALLAY_MAP_DATA_SYNC_FLAG).resolve().orElseThrow().invalidateAll();
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Direction side = event.getFace();
        Player player = event.getEntity();

        if (!level.isClientSide && player.isShiftKeyDown() && ModUtil.isContainer(level, pos, side)) {
            ItemStack stack = event.getItemStack();
            Item item = stack.getItem();

            if (item instanceof EmptyMapItem) {
                ItemStack draftStack = AllayMapItem.create(level, pos.getX(), pos.getZ());

                level.getCapability(Capabilities.ALLAY_MAP_DATA)
                        .resolve()
                        .orElseThrow()
                        .get(draftStack)
                        .orElseThrow()
                        .setSource(new ItemTarget(pos, side));

                player.setItemInHand(event.getHand(), draftStack);
                player.displayClientMessage(TranslationUtils.message("allay_map_transformed")
                        .withStyle(ChatFormatting.AQUA), true);

                event.setCancellationResult(InteractionResult.CONSUME);
                event.setCanceled(true);
            }
        }
    }

    /**
     * Lets us respond to entity interactions before {@link Entity#interact(Player, InteractionHand)} runs.
     */
    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        Entity target = event.getTarget();
        Item item = stack.getItem();

        if (item instanceof MindControlDevice controller && target instanceof LivingEntity livingEntity) {
            InteractionResult result = controller.interactLivingEntityFirst(livingEntity, stack);
            if (result != InteractionResult.PASS) {
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
        }
    }
}
