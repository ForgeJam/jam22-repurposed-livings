package wtf.gofancy.mc.repurposedlivings.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemWithDescription extends Item {
    
    public ItemWithDescription(final Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(final ItemStack stack, @Nullable final Level level, final List<Component> tooltipComponents, final TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        
        tooltipComponents.add(TranslationUtils.tooltip(this, "description").withStyle(ChatFormatting.DARK_GRAY));
    }
}
