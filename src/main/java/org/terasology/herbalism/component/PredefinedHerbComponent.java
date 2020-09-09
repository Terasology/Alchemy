// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.component;

import org.terasology.engine.entitySystem.Component;

/**
 * Add this component to an item to indicate that it is a predefined herb. That is, a prefab was directly used to
 * generate this.
 */
public final class PredefinedHerbComponent implements Component {
    public String herbBaseGenome;
}
