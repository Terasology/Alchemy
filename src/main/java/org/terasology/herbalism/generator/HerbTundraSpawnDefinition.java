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
import org.terasology.gf.PlantType;
import org.terasology.gf.generator.StaticBlockFloraSpawnDefinition;
import org.terasology.world.block.BlockUri;
import org.terasology.world.generator.plugin.RegisterPlugin;

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
        super(PlantType.GRASS, AnotherWorldBiomes.TUNDRA.getId(), 0.5f, 0.3f, "Herbalism:Herb",
                Arrays.asList(
                        new BlockUri("Alchemy:HerbGeneratedA")),
                new BlockCollectionPredicate(Blocks.getBlock("CoreAssets:Snow")), null);
    }
}
