package wtf.gofancy.mc.repurposedlivings.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import wtf.gofancy.mc.repurposedlivings.util.ModUtil;

import java.util.List;

public class ItemWithDescription extends Item {
    
    public ItemWithDescription(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        
        tooltipComponents.add(ModUtil.getItemTranslation(this, "description").withStyle(ChatFormatting.DARK_GRAY));
    }
}
