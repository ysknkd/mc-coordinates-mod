package com.bungggo.mc;

/**
 * 位置データのエントリ。
 * 各エントリはテキスト、説明、フェードアウト用の保存時刻、
 * お気に入り状態およびピン留め状態を持ちます。
 */
public class LocationEntry {
    public String text;
    public String description;
    public boolean favorite;
    public boolean pinned;
    public long savedTime;

    /**
     * テキストのみ指定の場合、説明は空文字列となります。
     *
     * @param text 位置情報のテキスト
     */
    public LocationEntry(String text) {
        this(text, "");
    }

    /**
     * テキストと説明を指定します。
     *
     * @param text        位置情報のテキスト
     * @param description 位置情報の説明
     */
    public LocationEntry(String text, String description) {
        this.text = text;
        this.description = description;
        this.favorite = false;
        this.pinned = false;
        this.savedTime = System.currentTimeMillis();
    }
} 