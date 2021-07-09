// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.component;

import org.terasology.gestalt.entitysystem.component.Component;

/**
 * This component is used for storing the base genome of a generated herb.
 */
public final class GeneratedHerbComponent implements Component<GeneratedHerbComponent> {
    /** A string description of this herb's base genome. */
    public String herbBaseGenome;

    @Override
    public void copy(GeneratedHerbComponent other) {
        this.herbBaseGenome = other.herbBaseGenome;
    }
}
