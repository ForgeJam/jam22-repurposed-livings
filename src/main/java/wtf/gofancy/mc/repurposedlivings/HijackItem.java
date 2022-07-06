package wtf.gofancy.mc.repurposedlivings;

import com.mojang.logging.LogUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class HijackItem extends Item {

    public static final Logger logger = LogUtils.getLogger();

    public HijackItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
        if (!player.level.isClientSide) {
            logger.info("hijacking entity {}", entity);
        }
        return super.interactLivingEntity(stack, player, entity, hand);
    }
}
