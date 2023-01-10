/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.configuration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

public class ConfigurationProducer {
    @ApplicationScoped
    @Produces
    public AdapterModeInMemoryConfigSourceFactory getAdapterModeInMemoryConfigSourceFactory() {
        return new AdapterModeInMemoryConfigSourceFactory();
    }
}
