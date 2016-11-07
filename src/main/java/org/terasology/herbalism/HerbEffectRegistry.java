/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.herbalism;

import org.terasology.potions.HerbEffect;

/**
 * Registry for registering and getting herb effects.
 */
public interface HerbEffectRegistry {
    /**
     * Register a herb effect and its rarity of happening.
     * @param rarity        How often should this effect occur.
     * @param herbEffect    What the effect is.
     */
    void registerHerbEffect(float rarity, HerbEffect herbEffect);

    /**
     * Get a herb effect based on its value in the herb effect array.
     * @param value         Value of the effect.
     * @return              The HerbEffect.
     */
    HerbEffect getHerbEffect(float value);
}
