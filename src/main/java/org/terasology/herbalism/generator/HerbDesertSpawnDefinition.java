// Copyright 2021 The Terasology Foundation
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
 * This plugin defines how herbs should spawn in desert biomes.
 */
@RegisterPlugin
public class HerbDesertSpawnDefinition extends StaticBlockFloraSpawnDefinition {
    /**
     * Define the desert biome herb spawn details.
     */
    public HerbDesertSpawnDefinition() {
        super(PlantType.GRASS, AnotherWorldBiomes.DESERT.getId().toLowerCase(), 0.5f, 0.1f, "Herbalism:Herb",
                Arrays.asList(
                        new BlockUri("Alchemy:AntiPoisonHerb"),
                        new BlockUri("Alchemy:PoisonHerb"),
                        new BlockUri("Alchemy:RageHerb"),
                        new BlockUri("Alchemy:HerbGeneratedA")/*,
                        new BlockUri("WorkstationCrafting:Herb2"),
                        new BlockUri("WorkstationCrafting:Herb6"),
                        new BlockUri("WorkstationCrafting:Herb7")*/),
                new BlockCollectionPredicate(Blocks.getBlock("CoreAssets:Sand")), null);
    }
}
