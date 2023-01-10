/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.cache.base;

import com.rln.cache.exception.KeyNotFoundException;

import java.util.concurrent.ConcurrentHashMap;

public class RLNBaseCache<K, V> implements RLNCache<K, V> {

    final private ConcurrentHashMap<K, V> cache;

    public RLNBaseCache() {
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public void write(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public V read(K key) {
        V matchedValue = cache.get(key);
        if (matchedValue == null) {
            throw new KeyNotFoundException(key.toString());
        }
        return matchedValue;
    }
}
