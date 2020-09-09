// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism;

import org.terasology.potions.HerbEffect;

/**
 * Registry for registering and getting herb effects.
 */
public interface HerbEffectRegistry {
    /**
     * Register a herb effect and its rarity of happening.
     *
     * @param rarity How often should this effect occur.
     * @param herbEffect What the effect is.
     */
    void registerHerbEffect(float rarity, HerbEffect herbEffect);

    /**
     * Get a herb effect based on its value in the herb effect array.
     *
     * @param value Value of the effect.
     * @return The HerbEffect.
     */
    HerbEffect getHerbEffect(float value);
}
