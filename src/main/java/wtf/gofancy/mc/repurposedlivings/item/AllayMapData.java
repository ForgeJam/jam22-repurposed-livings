package wtf.gofancy.mc.repurposedlivings.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import wtf.gofancy.mc.repurposedlivings.util.ItemTarget;
import wtf.gofancy.mc.repurposedlivings.util.ModUtil;

import java.util.Optional;

public class AllayMapData {

    public static final Codec<AllayMapData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemTarget.CODEC.optionalFieldOf("source").forGetter(AllayMapData::getSource),
            ItemTarget.CODEC.optionalFieldOf("destination").forGetter(AllayMapData::getDestination)
    ).apply(instance, AllayMapData::new));

    public AllayMapData() {
        this.changed = false;
        this.source = null;
        this.destination = null;
    }

    private AllayMapData(Optional<ItemTarget> source, Optional<ItemTarget> destination) {
        this.changed = true;
        this.source = source.orElse(null);
        this.destination = destination.orElse(null);
    }

    private boolean changed;

    private ItemTarget source;
    private ItemTarget destination;

    public Optional<ItemTarget> getSource() {
        return Optional.ofNullable(this.source);
    }

    public void setSource(final ItemTarget source) {
        this.source = source;
        this.setChanged();
    }

    public Optional<ItemTarget> getDestination() {
        return Optional.ofNullable(this.destination);
    }

    public void setDestination(ItemTarget destination) {
        this.destination = destination;
        this.setChanged();
    }

    public void setChanged() {
        this.setChanged(true);
    }

    public void setChanged(final boolean changed) {
        this.changed = changed;
    }

    public boolean hasChanged() {
        return this.changed;
    }

    public void applyToMap(final MapItemSavedData map) {
        if (this.source != null) {
            map.addDecoration(
                    MapDecoration.Type.TARGET_POINT,
                    null,
                    "source",
                    source.pos().getX(),
                    source.pos().getZ(),
                    0.0,
                    ModUtil.getMapTranslation("source")
            );
        }
        if (this.destination != null) {
            map.addDecoration(
                    MapDecoration.Type.TARGET_X,
                    null,
                    "destination",
                    destination.pos().getX(),
                    destination.pos().getZ(),
                    0.0,
                    ModUtil.getMapTranslation("destination")
            );
        }
    }
}
