package com.bungggo.mc;

/**
 * 位置データのエントリ。
 * 各エントリは説明、フェードアウト用の保存時刻、
 * お気に入り状態およびピン留め状態を持ちます。
 * 位置情報は文字列ではなく数値 (x, y, z) として管理されます。
 */
public class LocationEntry {
    public double x;
    public double y;
    public double z;

    public String description;
    public boolean favorite;
    public boolean pinned;
    public long savedTime;

    /**
     * 数値データとして位置情報を指定します。
     *
     * @param x 位置のx座標
     * @param y 位置のy座標
     * @param z 位置のz座標
     */
    public LocationEntry(double x, double y, double z) {
        this(x, y, z, "");
    }
    
    /**
     * 数値データとして位置情報と説明を指定します。
     *
     * @param x 位置のx座標
     * @param y 位置のy座標
     * @param z 位置のz座標
     * @param description 位置情報の説明
     */
    public LocationEntry(double x, double y, double z, String description) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.description = description;
        this.favorite = false;
        this.pinned = false;
        this.savedTime = System.currentTimeMillis();
    }
    
    /**
     * 位置データをフォーマットされた文字列に変換します。  
     * 表示用途などに利用できます。
     *
     * @return フォーマット済みの位置文字列
     */
    public String getLocationText() {
        return String.format("X: %.1f, Y: %.1f, Z: %.1f", x, y, z);
    }

    public boolean isPinned() {
        return pinned;
    }
} 