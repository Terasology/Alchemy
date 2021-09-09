// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.system;

import org.terasology.anotherWorldPlants.farm.component.SeedComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.Assets;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.system.GenomeManager;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.herbalism.Herbalism;
import org.terasology.herbalism.component.HerbComponent;
import org.terasology.module.inventory.ui.GetItemTooltip;
import org.terasology.nui.widgets.TooltipLine;
import org.terasology.potions.component.PotionComponent;

/**
 * Client system for Herbalism that handles tooltips.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class HerbalismClientSystem extends BaseComponentSystem {
    @In
    private GenomeManager genomeManager;

    /**
     * Modify the herb item's tooltip to add information about its genome.
     * @param tooltip   The item's current tooltip. This is displayed when mousing over the item.
     * @param item      Reference to the herb item itself.
     * @param herb      Herb component of the item.
     * @param genome    Genome component of the item.
     */
    @ReceiveEvent
    public void getHerbTooltip(GetItemTooltip tooltip, EntityRef item, HerbComponent herb, GenomeComponent genome) {
        appendSpecie(tooltip, item);
    }

    /**
     * Modify the seed item's tooltip to add information about its genome.
     * @param tooltip   The item's current tooltip. This is displayed when mousing over the item.
     * @param item      Reference to the seed item itself.
     * @param seed      Seed component of the item.
     * @param genome    Genome component of the item.
     */
    @ReceiveEvent
    public void getHerbTooltip(GetItemTooltip tooltip, EntityRef item, SeedComponent seed, GenomeComponent genome) {
        appendSpecie(tooltip, item);
    }

    /**
     * Modify the potion item's tooltip to add information about its genome.
     * @param tooltip   The item's current tooltip. This is displayed when mousing over the item.
     * @param item      Reference to the seed item itself.
     * @param potion    Potion component of the item.
     * @param genome    Genome component of the item.
     */
    @ReceiveEvent
    public void getHerbTooltip(GetItemTooltip tooltip, EntityRef item, PotionComponent potion, GenomeComponent genome) {
        appendSpecie(tooltip, item);
    }

    /**
     * Append the species information to the given item's tooltip. This tooltip is displayed when mousing over the item.
     *
     * @param tooltip   The item's current tooltip.
     * @param item      Reference to the item entity.
     */
    private void appendSpecie(GetItemTooltip tooltip, EntityRef item) {
        String herbName = genomeManager.getGenomeProperty(item, Herbalism.NAME_PROPERTY, String.class);
        tooltip.getTooltipLines().add(getHerbTooltipLine(herbName));
    }

    /**
     * Get a custom tooltip line with a unique skin for herbs.
     * @param herbName  Name of the herb.
     * @return          The customized tooltip line.
     */
    public static TooltipLine getHerbTooltipLine(String herbName) {
        return new TooltipLine("Specie: " + herbName, Assets.getSkin("Alchemy:herbTooltip").get());
    }
}
