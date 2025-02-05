package com.bungggo.mc;

import java.util.ArrayList;
import java.util.List;

/**
 * 位置データを一元管理するクラス。
 */
public class LocationDataManager {
    public static final List<LocationEntry> entries = new ArrayList<>();

    public static void addEntry(LocationEntry entry) {
        if (entry.pinned) {
            throw new IllegalArgumentException("pinnedエントリは追加できません");
        }
        entries.add(entry);
    }

    public static void removeEntry(LocationEntry entry) {
        entries.remove(entry);
    }

    public static List<LocationEntry> getPinnedEntries() {
        List<LocationEntry> pinnedEntries = new ArrayList<>();
        for (LocationEntry entry : entries) {
            if (entry.pinned) {
                pinnedEntries.add(entry);
            }
        }
        return pinnedEntries;
    }

    public static boolean hasPinnedEntries() {
        for (LocationEntry entry : entries) {
            if (entry.pinned) {
                return true;
            }
        }
        return false;
    }
}