package wtf.gofancy.mc.repurposedlivings;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EmptyMapItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wtf.gofancy.mc.repurposedlivings.item.AllayMapDraftItem;
import wtf.gofancy.mc.repurposedlivings.item.MindControlDevice;
import wtf.gofancy.mc.repurposedlivings.util.ModUtil;

public class EventHandler {

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getWorld();
        BlockPos pos = event.getPos();
        Direction side = event.getFace();
        Player player = event.getPlayer();

        if (!level.isClientSide && player.isShiftKeyDown() && ModUtil.isContainer(level, pos, side)) {
            ItemStack stack = event.getItemStack();
            Item item = stack.getItem();

            if (item instanceof EmptyMapItem) {
                ItemStack draftStack = AllayMapDraftItem.create(pos, side);
                player.setItemInHand(event.getHand(), draftStack);
                player.displayClientMessage(ModUtil.getItemTranslation(draftStack.getItem(), "complete_draft").withStyle(ChatFormatting.AQUA), true);

                event.setCancellationResult(InteractionResult.CONSUME);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        Entity target = event.getTarget();
        Item item = stack.getItem();
        
        if (item instanceof MindControlDevice controller && target instanceof LivingEntity livingEntity) {
            if (controller.interactLivingEntityFirst(livingEntity, stack)) {
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }
}
