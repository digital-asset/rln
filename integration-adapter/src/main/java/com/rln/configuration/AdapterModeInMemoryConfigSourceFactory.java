/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.configuration;

import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AdapterModeInMemoryConfigSourceFactory implements ConfigSourceFactory {
    @Override
    public Iterable<ConfigSource> getConfigSources(ConfigSourceContext context) {
        String adapterModeName = context.getValue("adapter.mode").getValue();
        AdapterMode adapterMode = AdapterMode.valueOf(adapterModeName);
        return Collections.singletonList(new AdapterModeInMemoryConfigSource(adapterMode));
    }

    public static class AdapterModeInMemoryConfigSource implements ConfigSource {
        private static final Map<String, String> configuration = new HashMap<>();

        public AdapterModeInMemoryConfigSource(AdapterMode adapterMode) {
            switch (adapterMode) {
                case BANK:
                    switchChannel("initiation-message-in", true);
                    switchChannel("enriched-message-in", false);
                    switchChannel("approve-reject-message-in", true);
                    switchChannel("finalize-reject-settlement-message-in", false);
                    break;
                case SCHEDULER:
                    switchChannel("initiation-message-in", false);
                    switchChannel("enriched-message-in", true);
                    switchChannel("approve-reject-message-in", false);
                    switchChannel("finalize-reject-settlement-message-in", false);
                    break;
                case ASSEMBLER:
                    switchChannel("initiation-message-in", false);
                    switchChannel("enriched-message-in", false);
                    switchChannel("approve-reject-message-in", false);
                    switchChannel("finalize-reject-settlement-message-in", true);
                    break;
            }
        }

        @Override
        public Set<String> getPropertyNames() {
            return configuration.keySet();
        }

        @Override
        public String getValue(final String propertyName) {
            return configuration.get(propertyName);
        }

        @Override
        public String getName() {
            return AdapterModeInMemoryConfigSource.class.getSimpleName();
        }

        private void switchChannel(String name, boolean on) {
            String channelEnabledProperty = "mp.messaging.incoming." + name + ".enabled";
            if (on) {
                configuration.put(channelEnabledProperty, "true");
            } else {
                configuration.put(channelEnabledProperty, "false");
            }
        }
    }
}
