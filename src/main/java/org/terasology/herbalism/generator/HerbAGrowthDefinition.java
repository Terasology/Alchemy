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
package org.terasology.herbalism.generator;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.terasology.anotherWorld.LocalParameters;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.gf.grass.ReplaceBlockGrowthDefinition;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.generator.plugin.RegisterPlugin;

import java.util.Arrays;

/**
 * This plugin defines the growth of herbs and how they may replace certain blocks.
 */
@RegisterPlugin
public class HerbAGrowthDefinition extends ReplaceBlockGrowthDefinition {
    public static final String ID = "Herbalism:Herb";

    /**
     * Define the growth definition for the base (generated) herbs.
     */
    public HerbAGrowthDefinition() {
        super(ID, Arrays.asList(
                        new BlockUri("Alchemy:HerbGrowA"), new BlockUri("Alchemy:HerbGrownA"), new BlockUri("CoreAssets:DeadBush")),
                50 * 1000, 200 * 1000,
                new Predicate<LocalParameters>() {
                    @Override
                    public boolean apply(LocalParameters input) {
                        return input.getHumidity() > 0.2f && input.getTemperature() > 15f;
                    }
                },
                new Function<LocalParameters, Float>() {
                    @Override
                    public Float apply(LocalParameters input) {
                        return 0.2f * input.getHumidity();
                    }
                }
        );
    }

    /**
     * Replace this particular herb plant block with the next stage of the herb plant block.
     *
     * @param worldProvider     WorldProvider instance to interface with the game world blocks.
     * @param blockManager      BlockManager instance to get the specific block type.
     * @param plant             Reference to the herb plant.
     * @param position          World position of the plant block in Vector3 coordinates.
     * @param nextStage         Next stage of the herb plant (block) growth.
     * @param isLast            Whether this is the last stage of herb plant growth.
     */
    @Override
    protected void replaceBlock(WorldProvider worldProvider, BlockManager blockManager, EntityRef plant, Vector3i position, BlockUri nextStage, boolean isLast) {
        // If this is not the last stage of herb plant growth, continue as normal. Otherwise, just call the parent method.
        if (!isLast) {
            // We need to copy the genome between growth stages. Otherwise it will be lost upon replacing this block.
            final GenomeComponent genome = plant.getComponent(GenomeComponent.class);

            GenomeComponent genomeCopy = new GenomeComponent();
            genomeCopy.genomeId = genome.genomeId;
            genomeCopy.genes = genome.genes;

            // After copying, call the parent replace block as normal.
            super.replaceBlock(worldProvider, blockManager, plant, position, nextStage, isLast);

            // Get the herb plant block at this location, and add the copied genome onto it.
            final EntityRef blockEntity = CoreRegistry.get(BlockEntityRegistry.class).getEntityAt(position);
            blockEntity.addComponent(genomeCopy);
        } else {
            super.replaceBlock(worldProvider, blockManager, plant, position, nextStage, isLast);
        }
    }
}
