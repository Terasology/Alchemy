// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.component;

import com.google.common.collect.Lists;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;

/**
 * This component is used for storing the hue ranges of a particular herb.
 */
public class HerbHueComponent implements Component<HerbHueComponent> {
    public List<String> hueRanges;

    @Override
    public void copyFrom(HerbHueComponent other) {
        this.hueRanges = Lists.newArrayList(other.hueRanges);
    }
}
