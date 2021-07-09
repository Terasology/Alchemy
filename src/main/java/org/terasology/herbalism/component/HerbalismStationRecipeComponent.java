// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.component;

import com.google.common.collect.Lists;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;

/**
 * Add this Component to any recipe prefab that is supposed to be creatable in a HerbalismStation or similar.
 * Include in prefab along with CraftingStationRecipeComponent to work properly.
 */
public class HerbalismStationRecipeComponent implements Component<HerbalismStationRecipeComponent> {
    // The following variables are unused.
    public String recipeId;
    public List<String> recipeComponents;
    public List<String> recipeTools;
    public List<String> recipeFluids;

    public float requiredTemperature;
    public long processingDuration;

    public String itemResult;
    public String blockResult;

    @Override
    public void copy(HerbalismStationRecipeComponent other) {
        this.recipeId = other.recipeId;
        this.recipeComponents = Lists.newArrayList(other.recipeComponents);
        this.recipeTools = Lists.newArrayList(other.recipeTools);
        this.recipeFluids = Lists.newArrayList(other.recipeFluids);
        this.requiredTemperature = other.requiredTemperature;
        this.processingDuration = other.processingDuration;
        this.itemResult = other.itemResult;
        this.blockResult = other.blockResult;
    }
}
