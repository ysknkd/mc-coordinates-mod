package com.bungggo.mc;

/**
 * 位置データのエントリ。
 * 各エントリはテキスト、フェードアウト用の保存時刻、
 * お気に入り状態およびピン留め状態を持ちます。
 */
public class LocationEntry {
    public String text;
    public boolean favorite;
    public boolean pinned;
    public long savedTime;

    public LocationEntry(String text) {
        this.text = text;
        this.favorite = false;
        this.pinned = false;
        this.savedTime = System.currentTimeMillis();
    }
} 