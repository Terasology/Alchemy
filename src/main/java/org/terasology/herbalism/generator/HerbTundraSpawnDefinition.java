// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.generator;

import org.terasology.anotherWorld.AnotherWorldBiomes;
import org.terasology.anotherWorld.decorator.BlockCollectionPredicate;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.gf.PlantType;
import org.terasology.gf.generator.StaticBlockFloraSpawnDefinition;

import java.util.Arrays;

/**
 * This plugin defines how herbs should spawn in tundra biomes.
 */
@RegisterPlugin
public class HerbTundraSpawnDefinition extends StaticBlockFloraSpawnDefinition {
    /**
     * Define the tundra biome herb spawn details.
     */
    public HerbTundraSpawnDefinition() {
        super(PlantType.GRASS, AnotherWorldBiomes.TUNDRA.getId().toLowerCase(), 0.5f, 0.3f, "Herbalism:Herb",
                Arrays.asList(
                        new BlockUri("Alchemy:HerbGeneratedA")),
                new BlockCollectionPredicate(Blocks.getBlock("CoreAssets:Snow")), null);
    }
}
