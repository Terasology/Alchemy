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
package org.terasology.alchemy.system;

import com.google.common.base.Predicate;
import org.terasology.alchemy.Alchemy;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.herbalism.ui.HerbalismCraftingStationRecipe;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.multiBlock.Basic2DSizeFilter;
import org.terasology.multiBlock.BlockUriEntityFilter;
import org.terasology.multiBlock.recipe.LayeredMultiBlockFormItemRecipe;
import org.terasology.processing.system.AnyActivityFilter;
import org.terasology.processing.system.ToolTypeEntityFilter;
import org.terasology.workstationCrafting.component.CraftingStationMaterialComponent;
import org.terasology.workstationCrafting.component.CraftingStationRecipeComponent;
import org.terasology.workstationCrafting.system.CraftInHandRecipeRegistry;
import org.terasology.workstationCrafting.system.CraftingWorkstationProcess;
import org.terasology.workstationCrafting.system.CraftingWorkstationProcessFactory;
import org.terasology.herbalism.component.HerbalismStationRecipeComponent;
import org.terasology.multiBlock.MultiBlockFormRecipeRegistry;
import org.terasology.registry.In;
import org.terasology.workstation.system.WorkstationRegistry;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;

@RegisterSystem
public class RegisterAlchemyRecipes extends BaseComponentSystem {
    @In
    private CraftInHandRecipeRegistry recipeRegistry;
    @In
    private WorkstationRegistry workstationRegistry;
    @In
    private MultiBlockFormRecipeRegistry multiBlockFormRecipeRegistry;
    @In
    private BlockManager blockManager;
    @In
    private PrefabManager prefabManager;

    @Override
    public void initialise() {
        workstationRegistry.registerProcessFactory(Alchemy.HERBALISM_PROCESS_TYPE, new CraftingWorkstationProcessFactory());

        addWorkstationFormingRecipes();

        //addCraftInHandRecipes();

        addHerbalismWorkstationRecipes();

        //addWoodPlankRecipes();

        //addStandardWoodWorkstationBlockShapeRecipes();

        //addBasicStoneWorkstationBlockShapeRecipes();
    }

    private void addWorkstationFormingRecipes() {
        LayeredMultiBlockFormItemRecipe herbalismStationRecipe = new LayeredMultiBlockFormItemRecipe(
                new ToolTypeEntityFilter("mortarAndPestle"), new Basic2DSizeFilter(3, 1), new AnyActivityFilter(),
                "Alchemy:HerbalismStation", null);
        herbalismStationRecipe.addLayer(1, 1, new BlockUriEntityFilter(new BlockUri("Core:Brick")));
        herbalismStationRecipe.addLayer(1, 1, new BlockUriEntityFilter(new BlockUri(new ResourceUrn("Core:CobbleStone"), new ResourceUrn(("Engine:EighthBlock")))));
        multiBlockFormRecipeRegistry.addMultiBlockFormItemRecipe(herbalismStationRecipe);
    }

    // Add all of the recipes to the HerbalismStation.
    private void addHerbalismWorkstationRecipes() {
        // TODO: Temporarily removed for sake of testing.
        /* workstationRegistry.registerProcess(WorkstationCrafting.HERBALISM_PROCESS_TYPE,
                new CraftingWorkstationProcess(WorkstationCrafting.HERBALISM_PROCESS_TYPE, "WorkstationCrafting:HerbPotion", new HerbalismCraftingStationRecipe()));*/

        // Add all the recipes marked with "HerbalismStationRecipeComponent" in their prefabs and add them to the list.
        for (Prefab prefab : prefabManager.listPrefabs(HerbalismStationRecipeComponent.class)) {
            CraftingStationRecipeComponent recipeComponent = prefab.getComponent(CraftingStationRecipeComponent.class);

            workstationRegistry.registerProcess(Alchemy.HERBALISM_PROCESS_TYPE,
                    new CraftingWorkstationProcess(Alchemy.HERBALISM_PROCESS_TYPE, recipeComponent.recipeId,
                            new HerbalismCraftingStationRecipe(recipeComponent)));
        }
    }

    private final class StationTypeFilter implements Predicate<EntityRef> {
        private String stationType;

        private StationTypeFilter(String stationType) {
            this.stationType = stationType;
        }

        @Override
        public boolean apply(EntityRef entity) {
            CraftingStationMaterialComponent stationMaterial = entity.getComponent(CraftingStationMaterialComponent.class);
            return stationMaterial != null && stationMaterial.stationType.equals(stationType);
        }
    }
}
