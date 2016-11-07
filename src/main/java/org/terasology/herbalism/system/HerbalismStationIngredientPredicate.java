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
package org.terasology.herbalism.system;

import com.google.common.base.Predicate;
import org.terasology.potions.component.EmptyPotionComponent;
import org.terasology.workstationCrafting.component.CraftingStationIngredientComponent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.herbalism.component.HerbComponent;

/**
 * This predicate class is used to filter out items that are not compatible with HerbalismStations.
 */
public class HerbalismStationIngredientPredicate implements Predicate<EntityRef> {
    /** Item type to check for. */
    private String itemType;

    /**
     * Define what item type this filter should look for.
     *
     * @param itemType   Item type name being filtered.
     */
    public HerbalismStationIngredientPredicate(String itemType) {
        this.itemType = itemType;
    }

    /**
     * Apply an entity to this filter to see if it's compatible with HerbalismStations and it has the same item type.
     *
     * @param input    Reference to the entity being checked.
     * @return         True if the entity fulfills the above conditions. False if not.
     */
    @Override
    public boolean apply(EntityRef input) {
        HerbComponent hComponent = input.getComponent(HerbComponent.class);
        EmptyPotionComponent epComponent = input.getComponent(EmptyPotionComponent.class);
        CraftingStationIngredientComponent component = input.getComponent(CraftingStationIngredientComponent.class);

        // If this contains a valid instance of (HerbComponent or EmptyPotionComponent, CraftingStationIngredientComponent,
        // and the input's type matches the itemType of this predicate, return true.
        return (hComponent != null || epComponent != null) && component != null && component.type.equalsIgnoreCase(itemType);
    }
}
