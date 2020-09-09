// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.component;

import org.terasology.engine.entitySystem.Component;

/**
 * This component is used for storing the base genome of a generated herb.
 */
public final class GeneratedHerbComponent implements Component {
    /**
     * A string description of this herb's base genome.
     */
    public String herbBaseGenome;
}
