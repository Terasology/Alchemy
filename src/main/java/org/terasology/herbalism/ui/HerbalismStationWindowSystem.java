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
package org.terasology.herbalism.ui;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.interactions.InteractionScreenComponent;
import org.terasology.logic.inventory.events.BeforeItemPutInInventory;
import org.terasology.logic.inventory.events.BeforeItemRemovedFromInventory;
import org.terasology.logic.inventory.events.InventorySlotStackSizeChangedEvent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.workstation.component.WorkstationInventoryComponent;
import org.terasology.workstationCrafting.event.CraftingStationUpgraded;

// This class is designed for handling certain events related to the HerbalismStation ui.
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
     * @param event       Details of this event.
     * @param entity      Entity reference to the workstation. May not be a HerbalismStation though.
     * @param inventory   Inventory of the workstation.
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
     * When one of the item stack sizes present in the herbalism station's inventory slots changes, update the UI screen.
     *
     * @param event       Details of this event.
     * @param entity      Entity reference to the workstation. May not be a HerbalismStation though.
     * @param inventory   Inventory of the workstation.
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
     * @param event       Details of this event.
     * @param entity      Entity reference to the workstation. May not be a HerbalismStation though.
     * @param inventory   Inventory of the workstation.
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
     * @param event       Details of this event.
     * @param entity      Entity reference to the workstation. May not be a HerbalismStation though.
     * @param inventory   Inventory of the workstation.
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
