package dev.ysknkd.mc.coordinates.store;

/**
 * Interface for listeners that are notified when coordinate data is updated.
 */
public interface CoordinatesDataListener {
    /**
     * Called when a new coordinate entry is added.
     *
     * @param entry The coordinate entry that was added.
     */
    void onEntryAdded(Coordinates entry);
} 