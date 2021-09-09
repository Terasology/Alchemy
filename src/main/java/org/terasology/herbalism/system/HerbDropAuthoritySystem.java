// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.system;

import org.joml.RoundingMode;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.health.DoDestroyEvent;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.logic.inventory.events.DropItemEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.physics.events.ImpulseEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.texture.TextureRegionAsset;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.entity.CreateBlockDropsEvent;
import org.terasology.engine.world.block.entity.damage.BlockDamageModifierComponent;
import org.terasology.genome.breed.BiodiversityGenerator;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.system.GenomeManager;
import org.terasology.herbalism.HerbGeneMutator;
import org.terasology.herbalism.Herbalism;
import org.terasology.herbalism.component.GeneratedHerbComponent;
import org.terasology.herbalism.component.HerbComponent;
import org.terasology.herbalism.component.PredefinedHerbComponent;
import org.terasology.module.inventory.systems.InventoryManager;

/**
 * Authority system for managing what happens when a herb drops onto the world.
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class HerbDropAuthoritySystem extends BaseComponentSystem {
    @In
    private WorldProvider worldProvider;
    @In
    private EntityManager entityManager;
    @In
    private GenomeManager genomeManager;
    @In
    private InventoryManager inventoryManager;

    /** Random number generator. */
    private Random random;

    /**
     * Before beginning execution of this component system, create the random number generator.
     */
    @Override
    public void preBegin() {
        random = new FastRandom();
    }

    /**
     * When a destroyed block drops an item that has a GeneratedHerbComponent, intercept and consume the event.
     *
     * @param event         Information about the block drop.
     * @param blockEntity   The block entity in question that was destroyed.
     * @param component     Used as a delimiter to filter out block drops that are not generated herbs.
     */
    @ReceiveEvent
    public void whenBlockDropped(CreateBlockDropsEvent event, EntityRef blockEntity, GeneratedHerbComponent component) {
        event.consume();
    }

    /**
     * When a destroyed block drops an item that has a HerbComponent, intercept and consume the event.
     *
     * @param event         Information about the block drop.
     * @param blockEntity   The block entity in question that was destroyed.
     * @param component     Used as a delimiter to filter out block drops that are not herbs.
     */
    @ReceiveEvent
    public void whenBlockDropped(CreateBlockDropsEvent event, EntityRef blockEntity, HerbComponent component) {
        event.consume();
    }

    /**
     * When a grown herb block (or plant) is destroyed, determine if it should drop an herb item.
     *
     * @param event             Information about the destruction.
     * @param entity            Reference to the herb block entity.
     * @param herbComp          Used as a delimiter to filter out blocks that are not herbs.
     * @param genomeComponent   Genome of the herb block.
     * @param locationComp      Where was the herb block located in the world.
     */
    @ReceiveEvent
    public void onGrownHerbDestroyed(DoDestroyEvent event, EntityRef entity, HerbComponent herbComp, GenomeComponent genomeComponent, LocationComponent locationComp) {
        // Get the block damage modifier and set the block drop chance to 1 (that is, 0%).
        BlockDamageModifierComponent blockDamageModifierComponent = event.getDamageType().getComponent(BlockDamageModifierComponent.class);
        float chanceOfBlockDrop = 1;

        // This check is done to ensure that the herb destroyed is actually a plant (or block). If it's an item, it cannot
        // have this component. Thus, it'll be impossible to drop a herb from it. Otherwise, proceed normally by adjusting
        // the drop rate using the blockAnnihilationChance.
        if (blockDamageModifierComponent != null) {
            chanceOfBlockDrop = 1 - blockDamageModifierComponent.blockAnnihilationChance;
        }

        if (random.nextFloat() < chanceOfBlockDrop) {
            // Generate a herb.
            EntityRef herb = entityManager.create("Alchemy:HerbBase");

            // Copy over the old genome from the block into the item.
            GenomeComponent genome = new GenomeComponent();
            genome.genomeId = genomeComponent.genomeId;
            genome.genes = genomeComponent.genes;
            herb.addComponent(genome);

            // Set the icon of the herb.
            final ItemComponent item = herb.getComponent(ItemComponent.class);
            item.icon = genomeManager.getGenomeProperty(herb, Herbalism.ICON_PROPERTY, TextureRegionAsset.class);

            // Save the recently added components. Otherwise, they will be not be saved to disk.
            herb.saveComponent(item);

            // Determine if the herb should be dropped onto the world, and if so, drop it without moving it.
            if (shouldDropToWorld(event, blockDamageModifierComponent, herb)) {
                createDrop(herb, locationComp.getWorldPosition(new Vector3f()), false);
            }
        }
    }

    /**
     * When a generated herb is destroyed, determine if it should drop an herb item.
     *
     * @param event             Information about the destruction.
     * @param entity            Reference to the herb block entity.
     * @param herbComp          Used as a delimiter to filter out blocks that are not generated herbs.
     * @param locationComp      Where was the herb block located in the world.
     */
    @ReceiveEvent
    public void onGeneratedHerbDestroyed(DoDestroyEvent event, EntityRef entity, GeneratedHerbComponent herbComp, LocationComponent locationComp) {
        // Get the block damage modifier and set the block drop chance to 1 (that is, 0%).
        BlockDamageModifierComponent blockDamageModifierComponent = event.getDamageType().getComponent(BlockDamageModifierComponent.class);
        float chanceOfBlockDrop = 1;

        // This check is done to ensure that the herb destroyed is actually a plant (or block). If it's an item, it cannot
        // have this component. Thus, it'll be impossible to drop a herb from it. Otherwise, proceed normally by adjusting
        // the drop rate using the blockAnnihilationChance.
        if (blockDamageModifierComponent != null) {
            chanceOfBlockDrop = 1 - blockDamageModifierComponent.blockAnnihilationChance;
        }

        if (random.nextFloat() < chanceOfBlockDrop) {
            // Get the base genome and world position of the old herb.
            final String herbBaseGenome = herbComp.herbBaseGenome;
            final Vector3i position = new Vector3i(locationComp.getWorldPosition(new Vector3f()), RoundingMode.HALF_UP);

            // Using a BiodiversityGenerator, create a new set of (mutated) genes.
            BiodiversityGenerator generator = new BiodiversityGenerator(worldProvider.getSeed(), 0, new HerbGeneMutator(), herbBaseGenome,
                    3, 0.0002f);
            final String generatedGenes = generator.generateGenes(new Vector2i(position.x, position.y));

            // Create a herb.
            EntityRef herb = entityManager.create("Alchemy:HerbBase");

            // Add a new set of genes to this herb based on the previously generated genes.
            GenomeComponent genomeComponent = new GenomeComponent();
            genomeComponent.genomeId = "Alchemy:Herb";
            genomeComponent.genes = generatedGenes;
            herb.addComponent(genomeComponent);

            // Set the icon of the herb. Due to a glitch with PredefinedHerbs, the icon setting has been temporarily disabled.
            final ItemComponent item = herb.getComponent(ItemComponent.class);
            //item.icon = genomeManager.getGenomeProperty(herb, Herbalism.ICON_PROPERTY, TextureRegionAsset.class);
            herb.saveComponent(item);

            // Determine if the herb should be dropped onto the world, and if so, drop it without moving it.
            if (shouldDropToWorld(event, blockDamageModifierComponent, herb)) {
                createDrop(herb, locationComp.getWorldPosition(new Vector3f()), false);
            }
        }
    }

    /**
     * When a predefined herb is destroyed, determine if it should drop an herb item.
     *
     * @param event             Information about the destruction.
     * @param entity            Reference to the herb block entity.
     * @param herbComp          Used as a delimiter to filter out blocks that are not predefined herbs.
     * @param locationComp      Where was the herb block located in the world.
     */
    @ReceiveEvent
    public void onPredefinedHerbDestroyed(DoDestroyEvent event, EntityRef entity, PredefinedHerbComponent herbComp, LocationComponent locationComp) {
        // Get the block damage modifier and set the block drop chance to 1 (that is, 0%).
        BlockDamageModifierComponent blockDamageModifierComponent = event.getDamageType().getComponent(BlockDamageModifierComponent.class);
        float chanceOfBlockDrop = 1;

        // This check is done to ensure that the herb destroyed is actually a plant (or block). If it's an item, it cannot
        // have this component. Thus, it'll be impossible to drop a herb from it. Otherwise, proceed normally by adjusting
        // the drop rate using the blockAnnihilationChance.
        if (blockDamageModifierComponent != null) {
            chanceOfBlockDrop = 1 - blockDamageModifierComponent.blockAnnihilationChance;
        }

        if (random.nextFloat() < chanceOfBlockDrop) {
            // Create a new herb using the base genome. This should contain the name of the herb prefab.
            EntityRef herb = entityManager.create(herbComp.herbBaseGenome);

            // Determine if the herb should be dropped onto the world, and if so, drop it without moving it.
            if (shouldDropToWorld(event, blockDamageModifierComponent, herb)) {
                createDrop(herb, locationComp.getWorldPosition(new Vector3f()), false);
            }
        }
    }

    /**
     * Determine whether the given item should be dropped onto the world.
     *
     * @param event                         Information about the entity's destruction.
     * @param blockDamageModifierComponent  And damage modifiers for the block.
     * @param dropItem                      The item to be dropped onto the world.
     * @return                              True if the item should be dropped, false if not.
     */
    private boolean shouldDropToWorld(DoDestroyEvent event, BlockDamageModifierComponent blockDamageModifierComponent, EntityRef dropItem) {
        // Get the instigator of this entity's destruction.
        EntityRef instigator = event.getInstigator();

        // If there are no block damage modifiers, no direct pickup, or giving the item directly to the instigator fails,
        // return true.
        return blockDamageModifierComponent == null || !blockDamageModifierComponent.directPickup
                || !inventoryManager.giveItem(instigator, instigator, dropItem);
    }

    /**
     * Send a DropItemEvent indicating that the given item entity has been dropped onto the world. If applyMovement is
     * true, send an impulse event to push the herb a random distance.
     *
     * @param item
     * @param location
     * @param applyMovement
     */
    private void createDrop(EntityRef item, Vector3f location, boolean applyMovement) {
        item.send(new DropItemEvent(location));
        if (applyMovement) {
            item.send(new ImpulseEvent(random.nextVector3f(30.0f, new Vector3f())));
        }
    }
}
