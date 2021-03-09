/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.herbalism.generator;

import org.terasology.anotherWorld.AnotherWorldBiomes;
import org.terasology.anotherWorld.decorator.BlockCollectionPredicate;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.gf.PlantType;
import org.terasology.gf.generator.StaticBlockFloraSpawnDefinition;

import java.util.Arrays;

/**
 * This plugin defines how herbs should spawn in plains biomes.
 */
@RegisterPlugin
public class HerbPlainsSpawnDefinition extends StaticBlockFloraSpawnDefinition {
    /**
     * Define the plains biome herb spawn details.
     */
    public HerbPlainsSpawnDefinition() {
        super(PlantType.GRASS, AnotherWorldBiomes.PLAINS.getId().toLowerCase(), 0.5f, 0.3f, "Herbalism:Herb",
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
                        new BlockUri("WorkstationCrafting:Herb6"),
                        new BlockUri("WorkstationCrafting:Herb7")*/),
                new BlockCollectionPredicate(Blocks.getBlock("CoreAssets:Grass")), null);
    }
}
