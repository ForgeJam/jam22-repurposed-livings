package wtf.gofancy.mc.repurposedlivings.feature.allay.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import wtf.gofancy.mc.repurposedlivings.Capabilities;
import wtf.gofancy.mc.repurposedlivings.util.ItemTarget;
import wtf.gofancy.mc.repurposedlivings.util.TranslationUtils;

import java.util.Optional;

public class AllayMapData {

    public static final Codec<AllayMapData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("mapId").forGetter(AllayMapData::getMapId),
                    ItemTarget.CODEC.optionalFieldOf("source").forGetter(AllayMapData::getSource),
                    ItemTarget.CODEC.optionalFieldOf("destination").forGetter(AllayMapData::getDestination)
            )
            .apply(
                    instance,
                    (mapId, source, destination) -> new AllayMapData(
                            mapId,
                            source.orElse(null),
                            destination.orElse(null)
                    )
            ));

    public AllayMapData(final int mapId) {
        this.dirty = false;
        this.mapId = mapId;
        this.source = null;
        this.destination = null;
    }

    private AllayMapData(int mapId, ItemTarget source, ItemTarget destination) {
        this.dirty = true;
        this.mapId = mapId;
        this.source = source;
        this.destination = destination;
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

    public boolean isComplete() {
        return this.source != null && this.destination != null;
    }

    public void tick(final Level level) {
        if (!this.dirty) return;

        final var map = this.getCorrespondingMapData(level);

        this.getSource().ifPresent(source -> map.addDecoration(
                MapDecoration.Type.TARGET_POINT,
                null,
                "source",
                source.pos().getX(),
                source.pos().getZ(),
                0.0,
                TranslationUtils.generic("source")
        ));
        this.getDestination().ifPresent(destination -> map.addDecoration(
                MapDecoration.Type.TARGET_X,
                null,
                "destination",
                destination.pos().getX(),
                destination.pos().getZ(),
                0.0,
                TranslationUtils.generic("destination")
        ));

        level.players()
                .stream()
                .map(player -> player.getCapability(Capabilities.ALLAY_MAP_DATA_SYNC_FLAG).resolve().orElseThrow())
                .forEach(syncFlag -> syncFlag.invalidate(this.mapId));

        this.dirty = false;
    }

    public MapItemSavedData getCorrespondingMapData(final Level level) {
        return level.getMapData(MapItem.makeKey(this.mapId));
    }

    public AllayMapData newInstanceFor(final int newMapId) {
        final AllayMapData data = new AllayMapData(newMapId);

        this.getSource().ifPresent(data::setSource);
        this.getDestination().ifPresent(data::setDestination);

        return data;
    }
}
