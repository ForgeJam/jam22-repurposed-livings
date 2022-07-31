package wtf.gofancy.mc.repurposedlivings.feature.allay.entity;

/**
 * Equipment slot keys for every item and upgrade that can be given to a hijacked allay
 */
public enum AllayEquipment {
    /**
     * Given automatically to every hijacked allay. Cannot be removed.
     */
    CONTROLLER,
    /**
     * Contains information about the item source and delivery targets.
     */
    MAP,
    /**
     * Adds 3 extra available slots to the Allay's inventory.
     */
    STORAGE,
    /**
     * Increases the Allay's speed multiplier
     */
    SPEED
}
