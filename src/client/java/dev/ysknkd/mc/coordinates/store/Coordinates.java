package dev.ysknkd.mc.coordinates.store;

import java.util.UUID;

/**
 * 位置データのエントリ。
 * 各エントリは説明、フェードアウト用の保存時刻、
 * お気に入り状態およびピン留め状態を持ちます。
 * 位置情報は文字列ではなく数値 (x, y, z) として管理されます。
 * 追加で所属するワールド（オーバーワールド、ネザーなど）も保存します。
 */
public class Coordinates {
    // 各エントリごとに一意の UUID を持ちます
    public UUID uuid;
    public double x;
    public double y;
    public double z;

    public String description;
    public boolean favorite;
    public boolean pinned;
    public long savedTime;
    
    // 所属ワールドを保存するフィールド
    public String world;

    // アイコン識別子。自動設定されたアイコンを保持し、後から手動での上書きも可能です
    public String icon;
    public boolean share;

    /**
     * 新規エントリ作成時に、新しい UUID を自動生成します。
     *
     * @param x 位置のx座標
     * @param y 位置のy座標
     * @param z 位置のz座標
     * @param description 位置情報の説明
     * @param world 所属するワールド（例: "minecraft:overworld", "minecraft:the_nether"）
     */
    public Coordinates(double x, double y, double z, String description, String world, boolean pinned, String icon) {
        this.uuid = UUID.randomUUID();  // 自動生成
        this.x = x;
        this.y = y;
        this.z = z;
        this.description = description;
        this.world = world;
        this.favorite = false;
        this.pinned = pinned;
        this.icon = icon;
        this.savedTime = System.currentTimeMillis();
    }
    
    public Coordinates(UUID uuid, double x, double y, double z, String description, String world, boolean pinned, String icon) {
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.description = description;
        this.world = world;
        this.favorite = false;
        this.pinned = pinned;
        this.icon = icon;
        this.savedTime = System.currentTimeMillis();
    }

    /**
     * 位置データをフォーマットされた文字列に変換します。  
     * 表示用途などに利用できます。
     *
     * @return フォーマット済みの位置文字列
     */
    public String getCoordinatesText() {
        String worldName = world.replace("minecraft:", "");
        return String.format("X: %.1f, Y: %.1f, Z: %.1f [%s]", x, y, z, worldName);
    }

    public boolean isPinned() {
        return pinned;
    }
} 