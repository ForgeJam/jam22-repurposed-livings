package wtf.gofancy.mc.repurposedlivings.compat;

import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.styles.LayoutStyle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import wtf.gofancy.mc.repurposedlivings.RepurposedLivings;
import wtf.gofancy.mc.repurposedlivings.feature.allay.entity.HijackedAllay;
import wtf.gofancy.mc.repurposedlivings.util.TranslationUtils;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class ModProbeProvider implements IProbeInfoEntityProvider, Function<ITheOneProbe, Void> {
    public static final ResourceLocation ID = new ResourceLocation(RepurposedLivings.MODID, "probe");
    
    @Override
    public Void apply(ITheOneProbe probe) {
        probe.registerEntityProvider(this);
        
        return null;
    }

    @Override
    public String getID() {
        return ID.toString();
    }

    @Override
    public void addProbeEntityInfo(ProbeMode probeMode, IProbeInfo info, Player player, Level level, Entity entity, IProbeHitEntityData hitEntityData) {
        if (entity instanceof HijackedAllay allay) {
            showEquipmentSlots(info, allay);
            
            if (allay.hasStorageUpgrade()) {
                showCarriedItems(info, allay);
            }
        }
    }
    
    private void showEquipmentSlots(IProbeInfo info, HijackedAllay allay) {
        // Draw equipment slots
        info.text(TranslationUtils.get("probe", "equipment"), info.defaultTextStyle().topPadding(1));
        IProbeInfo vertical = info.vertical(info.defaultLayoutStyle().bottomPadding(-3).borderColor(-16750951).spacing(0));
        IProbeInfo horizontal = vertical.horizontal(new LayoutStyle().spacing(0));

        allay.getEquipmentSlots().stream()
            .filter(stack -> !stack.isEmpty())
            .forEach(horizontal::item);
    }
    
    private void showCarriedItems(IProbeInfo info, HijackedAllay allay) {
        // Draw mainhand item + extended inventory
        List<ItemStack> inventory = Stream.concat(Stream.of(allay.getMainHandItem()), allay.getExtendedInventoryContent().stream())
            .filter(stack -> !stack.isEmpty())
            .toList();
        
        if (!inventory.isEmpty()) {
            info.text(TranslationUtils.get("probe", "carrying"), info.defaultTextStyle().topPadding(1));
            IProbeInfo vertical = info.vertical(info.defaultLayoutStyle().bottomPadding(-3).borderColor(-16750951).spacing(0));
            IProbeInfo horizontal = vertical.horizontal(new LayoutStyle().spacing(0));

            inventory.forEach(horizontal::item);   
        }
    }
}
