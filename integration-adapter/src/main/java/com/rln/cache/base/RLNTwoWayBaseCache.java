/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.cache.base;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.rln.cache.exception.KeyNotFoundException;
import com.rln.cache.exception.ValueNotFoundException;

public class RLNTwoWayBaseCache<K, V> {

    final private BiMap<K, V> cache;

    public RLNTwoWayBaseCache() {
        cache = Maps.synchronizedBiMap(HashBiMap.create());
    }

    public void write(K value1, V value2) {
        cache.put(value1, value2);
    }

    public V readFromKeyToValue(K key) {
        V matchedValue = cache.get(key);
        if (matchedValue == null) {
            throw new KeyNotFoundException(key.toString());
        }
        return matchedValue;
    }

    public K readFromValueToKey(V key) {
        K matchedKey = cache.inverse().get(key);
        if (matchedKey == null) {
            throw new ValueNotFoundException(key.toString());
        }
        return matchedKey;
    }
}
