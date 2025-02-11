package com.bungggo.mc.store;

/**
 * 位置情報の更新時に通知を受け取るリスナー用インターフェースです。
 */
public interface LocationDataListener {
    /**
     * 新たなエントリが追加された際に呼ばれます。
     *
     * @param entry 追加された位置情報エントリ
     */
    void onEntryAdded(LocationEntry entry);
} 