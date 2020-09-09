// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism;

import org.terasology.anotherWorld.util.ChanceRandomizer;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.Share;
import org.terasology.potions.HerbEffect;

/**
 * This class is used for storing and retrieving the default herb effects in a registry format.
 */
@RegisterSystem
@Share(HerbEffectRegistry.class)
public class DefaultHerbEffectRegistry extends BaseComponentSystem implements HerbEffectRegistry {
    /**
     * Randomizer for selecting herb effects.
     */
    private final ChanceRandomizer<HerbEffect> herbEffectRandomizer = new ChanceRandomizer<>(1000);

    @Override
    public void postBegin() {
        herbEffectRandomizer.initialize();
    }

    /**
     * Register an herb effect into the registry. It can later be retrieved.
     *
     * @param rarity How often should this effect occur.
     * @param herbEffect What the effect is.
     */
    @Override
    public void registerHerbEffect(float rarity, HerbEffect herbEffect) {
        herbEffectRandomizer.addChance(rarity, herbEffect);
    }

    /**
     * Return an herb effect based on the input value. Though, the effect returned is random.
     *
     * @param value Value of the effect.
     * @return A random herb effect.
     */
    @Override
    public HerbEffect getHerbEffect(float value) {
        return herbEffectRandomizer.getObject(value);
    }
}
