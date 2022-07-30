package wtf.gofancy.mc.repurposedlivings.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import wtf.gofancy.mc.repurposedlivings.capabilities.Capabilities;
import wtf.gofancy.mc.repurposedlivings.util.ItemTarget;
import wtf.gofancy.mc.repurposedlivings.util.ModUtil;

import java.util.Optional;

public class AllayMapData {

    public static final Codec<AllayMapData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("mapId").forGetter(AllayMapData::getMapId),
            ItemTarget.CODEC.optionalFieldOf("source").forGetter(AllayMapData::getSource),
            ItemTarget.CODEC.optionalFieldOf("destination").forGetter(AllayMapData::getDestination)
    ).apply(instance, AllayMapData::new));

    public AllayMapData(final int mapId) {
        this.dirty = false;
        this.mapId = mapId;
        this.source = null;
        this.destination = null;
    }

    private AllayMapData(int mapId, Optional<ItemTarget> source, Optional<ItemTarget> destination) {
        this.dirty = true;
        this.mapId = mapId;
        this.source = source.orElse(null);
        this.destination = destination.orElse(null);
    }

    private boolean dirty;

    private final int mapId;

    private ItemTarget source;
    private ItemTarget destination;

    public int getMapId() {
        return this.mapId;
    }

    public Optional<ItemTarget> getSource() {
        return Optional.ofNullable(this.source);
    }

    public void setSource(final ItemTarget source) {
        this.source = source;
        this.dirty = true;
    }

    public Optional<ItemTarget> getDestination() {
        return Optional.ofNullable(this.destination);
    }

    public void setDestination(ItemTarget destination) {
        this.destination = destination;
        this.dirty = true;
    }

    public void tick(final Level level) {
        if (!this.dirty) return;

        final var map = this.getCorrespondingMapData(level);

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

        level.players()
                .stream()
                .map(player -> player.getCapability(Capabilities.ALLAY_MAP_DATA_SYNC_FLAG).resolve().orElseThrow())
                .forEach(syncFlag -> syncFlag.invalidate(this.mapId));

        this.dirty = false;
    }

    public MapItemSavedData getCorrespondingMapData(final Level level) {
        return level.getMapData(MapItem.makeKey(this.mapId));
    }
}
