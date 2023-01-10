/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.cache.base;

public interface RLNCache<K, V> {
    void write(K key, V value);

    V read(K key);
}
