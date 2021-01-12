// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.system;

import org.joml.Vector3i;
import org.terasology.anotherWorldPlants.farm.component.FarmSoilComponent;
import org.terasology.anotherWorldPlants.farm.event.SeedPlanted;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.system.GenomeManager;
import org.terasology.gf.PlantedSaplingComponent;
import org.terasology.herbalism.Herbalism;
import org.terasology.herbalism.component.PollinatingHerbComponent;
import org.terasology.randomUpdate.RandomUpdateEvent;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;

/**
 * Authority system for Herbalism. Specifically for the planting and pollination of herb species.
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class HerbalismAuthoritySystem extends BaseComponentSystem {
    @In
    private GenomeManager genomeManager;
    @In
    private WorldProvider worldProvider;
    @In
    private BlockEntityRegistry blockEntityRegistry;

    /**
     * When an herb is planted, add a genome component to it.
     *
     * @param event             Details of the herb seed being planted.
     * @param seedItem          The herb seed item itself.
     * @param genomeComponent   The genome component to be added onto the planted seed entity.
     */
    @ReceiveEvent
    public void herbPlanted(SeedPlanted event, EntityRef seedItem, GenomeComponent genomeComponent) {
        Vector3i location = event.getLocation();
        EntityRef plantedEntity = blockEntityRegistry.getEntityAt(location);

        GenomeComponent genome = new GenomeComponent();
        genome.genomeId = genomeComponent.genomeId;
        genome.genes = genomeComponent.genes;

        plantedEntity.addComponent(genome);
    }

    /**
     * For every RandomUpdateEvent, manages how a herb will pollinate. This will work for herbs that are slated
     *
     * @param event                     Details of the random update.
     * @param herb                      The herb item in question being updated.
     * @param genome                    Genome of the herb.
     * @param pollinatingHerbComponent  Details of the herb's pollination.
     * @param block                     The herb plant's block component. This is physically where the herb is located on the world.
     */
    @ReceiveEvent
    public void herbPollination(RandomUpdateEvent event, EntityRef herb, GenomeComponent genome, PollinatingHerbComponent pollinatingHerbComponent, BlockComponent block) {
        Vector3i blockPosition = block.getPosition(new Vector3i());

        FastRandom random = new FastRandom();
        // We get 5 tries to pollinate
        for (int i = 0; i < 5; i++) {
            int x = blockPosition.x + random.nextInt(-3, 3);
            int z = blockPosition.z + random.nextInt(-3, 3);
            for (int dY = 1; dY >= -1; dY--) {
                int y = blockPosition.y + dY;
                EntityRef secondHerb = blockEntityRegistry.getExistingEntityAt(new Vector3i(x, y, z));
                if (secondHerb != null && secondHerb.hasComponent(PollinatingHerbComponent.class)
                        && genomeManager.canBreed(herb, secondHerb)) {
                    for (int j = 0; j < 5; j++) {
                        int resultX = blockPosition.x + random.nextInt(-3, 3);
                        int resultZ = blockPosition.z + random.nextInt(-3, 3);
                        for (int resultDY = 1; resultDY >= -1; resultDY--) {
                            int resultY = blockPosition.y + resultDY;
                            Vector3i plantLocation = new Vector3i(resultX, resultY, resultZ);
                            if (worldProvider.getBlock(plantLocation).isPenetrable()
                                    && blockEntityRegistry.getEntityAt(new Vector3i(resultX, resultY - 1, resultZ)).hasComponent(FarmSoilComponent.class)) {
                                Block plantedBlock = genomeManager.getGenomeProperty(herb, Herbalism.PLANTED_BLOCK_PROPERTY, Block.class);
                                worldProvider.setBlock(plantLocation, plantedBlock);
                                EntityRef plantedHerbEntity = blockEntityRegistry.getEntityAt(plantLocation);
                                plantedHerbEntity.addComponent(new PlantedSaplingComponent());
                                genomeManager.applyBreeding(herb, secondHerb, plantedHerbEntity);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
