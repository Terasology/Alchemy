// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.ui;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.interactions.InteractionScreenComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.inventory.logic.events.BeforeItemPutInInventory;
import org.terasology.inventory.logic.events.BeforeItemRemovedFromInventory;
import org.terasology.inventory.logic.events.InventorySlotStackSizeChangedEvent;
import org.terasology.workstation.component.WorkstationInventoryComponent;
import org.terasology.workstationCrafting.event.CraftingStationUpgraded;

/**
 * This class is designed for handling certain events related to the HerbalismStation UI.
 */
@RegisterSystem
public class HerbalismStationWindowSystem extends BaseComponentSystem {
    @In
    EntityManager entityManager;

    @In
    private NUIManager nuiManager;

    @Override
    public void initialise() {
    }

    /**
     * Just before an item is placed into one of the herbalism station's inventory slots, update the UI screen.
     *
     * @param event Details of this event.
     * @param entity Entity reference to the workstation. May not be a HerbalismStation though.
     * @param inventory Inventory of the workstation.
     */
    @ReceiveEvent
    public void itemPutIntoInventorySlot(BeforeItemPutInInventory event, EntityRef entity,
                                         WorkstationInventoryComponent inventory) {
        if (entity.getComponent(InteractionScreenComponent.class).screen.equalsIgnoreCase("Alchemy:HerbalismStation")) {
            HerbalismStationWindow screen = (HerbalismStationWindow) nuiManager.getScreen("Alchemy:HerbalismStation");

            if (screen != null) {
                screen.updateAvailableRecipes();
            }
        }
    }

    /**
     * When one of the item stack sizes present in the herbalism station's inventory slots changes, update the UI
     * screen.
     *
     * @param event Details of this event.
     * @param entity Entity reference to the workstation. May not be a HerbalismStation though.
     * @param inventory Inventory of the workstation.
     */
    @ReceiveEvent
    public void itemPutIntoInventorySlot(InventorySlotStackSizeChangedEvent event, EntityRef entity,
                                         WorkstationInventoryComponent inventory) {
        if (entity.getComponent(InteractionScreenComponent.class).screen.equalsIgnoreCase("Alchemy:HerbalismStation")) {
            HerbalismStationWindow screen = (HerbalismStationWindow) nuiManager.getScreen("Alchemy:HerbalismStation");

            if (screen != null) {
                screen.updateAvailableRecipes();
            }
        }
    }

    /**
     * Just before an item is removed from one of the herbalism station's inventory slots, update the UI screen.
     *
     * @param event Details of this event.
     * @param entity Entity reference to the workstation. May not be a HerbalismStation though.
     * @param inventory Inventory of the workstation.
     */
    @ReceiveEvent
    public void itemRemovedFromInventorySlot(BeforeItemRemovedFromInventory event, EntityRef entity,
                                             WorkstationInventoryComponent inventory) {
        if (entity.getComponent(InteractionScreenComponent.class).screen.equalsIgnoreCase("Alchemy:HerbalismStation")) {
            HerbalismStationWindow screen = (HerbalismStationWindow) nuiManager.getScreen("Alchemy:HerbalismStation");

            if (screen != null) {
                screen.updateAvailableRecipes();
            }
        }
    }

    /**
     * When the herbalism station has been upgraded, update the UI screen.
     *
     * @param event Details of this event.
     * @param entity Entity reference to the workstation. May not be a HerbalismStation though.
     * @param inventory Inventory of the workstation.
     */
    @ReceiveEvent
    public void onCraftingStationUpgraded(CraftingStationUpgraded event, EntityRef entity,
                                          WorkstationInventoryComponent inventory) {
        if (entity.getComponent(InteractionScreenComponent.class).screen.equalsIgnoreCase("Alchemy:HerbalismStation")) {
            HerbalismStationWindow screen = (HerbalismStationWindow) nuiManager.getScreen("Alchemy:HerbalismStation");

            if (screen != null) {
                screen.updateAvailableRecipes();
            }
        }
    }
}
