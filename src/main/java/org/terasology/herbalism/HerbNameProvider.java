// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism;

import org.terasology.engine.utilities.Assets;
import org.terasology.namegenerator.data.NameGeneratorComponent;
import org.terasology.namegenerator.generators.MarkovNameGenerator;

import java.util.List;

/**
 * Randomly provides names for herbs from a predetermined list.
 */
public class HerbNameProvider {
    /**
     * Markov name generator for a generation of herbs.
     */
    private final MarkovNameGenerator generaGen;

    /**
     * Markov name generator for a family of herbs.
     */
    private final MarkovNameGenerator familyGen;

    /**
     * Create an instance of this class using an input seed for the randomization.
     *
     * @param seed An integer value used for randomizing the output names.
     */
    public HerbNameProvider(int seed) {
        final List<String> families =
                Assets.getPrefab("NameGenerator:floweringPlantsFamilies").get().getComponent(NameGeneratorComponent.class).nameList;
        final List<String> generas =
                Assets.getPrefab("NameGenerator:floweringPlantsGenera").get().getComponent(NameGeneratorComponent.class).nameList;

        generaGen = new MarkovNameGenerator(seed, generas);
        familyGen = new MarkovNameGenerator(seed + 937623, families);
    }

    /**
     * Get a herb name (generation + family) using an input seed.
     *
     * @param seed An integer value used for randomly picking the output name.
     * @return A randomized name for an herb.
     */
    public String getName(String seed) {
        int length = seed.length();
        return generaGen.getName(4, 8, seed.substring(0, length / 2).hashCode()) + " " + familyGen.getName(4, 8,
                seed.substring(0, length).hashCode());
    }
}
