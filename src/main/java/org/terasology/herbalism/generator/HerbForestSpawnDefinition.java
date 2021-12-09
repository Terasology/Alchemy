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
 * This plugin defines how herbs should spawn in forest biomes.
 */
@RegisterPlugin
public class HerbForestSpawnDefinition extends StaticBlockFloraSpawnDefinition {
    /**
     * Define the forest biome herb spawn details.
     */
    public HerbForestSpawnDefinition() {
        super(PlantType.GRASS, AnotherWorldBiomes.FOREST.getId().toLowerCase(), 0.5f, 0.7f, "Alchemy:Herb",
                Arrays.asList(
                        new BlockUri("Alchemy:AntiPoisonHerb"),
                        new BlockUri("Alchemy:HealingHerb"),
                        new BlockUri("Alchemy:PoisonHerb"),
                        new BlockUri("Alchemy:RegenHerb"),
                        new BlockUri("Alchemy:WalkSpeedHerb"),
                        new BlockUri("Alchemy:JumpSpeedHerb"),
                        new BlockUri("Alchemy:SwimSpeedHerb"),
                        new BlockUri("Alchemy:RageHerb"),
                        new BlockUri("Alchemy:HerbGeneratedA")/*,
                        new BlockUri("WorkstationCrafting:Herb2"),
                        new BlockUri("WorkstationCrafting:Herb3"),
                        new BlockUri("WorkstationCrafting:Herb4"),
                        new BlockUri("WorkstationCrafting:Herb5"),
                        new BlockUri("WorkstationCrafting:Herb6")*/),
                new BlockCollectionPredicate(Blocks.getBlock("CoreAssets:Grass")), null);
    }
}
