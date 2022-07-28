package wtf.gofancy.mc.repurposedlivings.entity;

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
     * It can be given to the Allay and removed by players. 
     */
    MAP,
    SPEED,
    STORAGE
}
