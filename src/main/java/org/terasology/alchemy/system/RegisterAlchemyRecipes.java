// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.alchemy.system;

import com.google.common.base.Predicate;
import org.terasology.alchemy.Alchemy;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.herbalism.component.HerbalismStationRecipeComponent;
import org.terasology.herbalism.ui.HerbalismCraftingStationRecipe;
import org.terasology.multiBlock.Basic2DSizeFilter;
import org.terasology.multiBlock.BlockUriEntityFilter;
import org.terasology.multiBlock.MultiBlockFormRecipeRegistry;
import org.terasology.multiBlock.recipe.LayeredMultiBlockFormItemRecipe;
import org.terasology.processing.system.AnyActivityFilter;
import org.terasology.processing.system.ToolTypeEntityFilter;
import org.terasology.workstation.system.WorkstationRegistry;
import org.terasology.workstationCrafting.component.CraftingStationMaterialComponent;
import org.terasology.workstationCrafting.component.CraftingStationRecipeComponent;
import org.terasology.workstationCrafting.system.CraftInHandRecipeRegistry;
import org.terasology.workstationCrafting.system.CraftingWorkstationProcess;
import org.terasology.workstationCrafting.system.CraftingWorkstationProcessFactory;

/**
 * This system registers all of the Alchemy recipes in this module.
 */
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
    @In
    private EntityManager entityManager;

    /**
     * Initialization phase where all of the recipes are added.
     */
    @Override
    public void initialise() {
        // Register the process factory for generic Herbalism process recipes that don't use the Herbalism Station.
        workstationRegistry.registerProcessFactory(Alchemy.HERBALISM_PROCESS_TYPE, new CraftingWorkstationProcessFactory());

        addWorkstationFormingRecipes();

        addHerbalismWorkstationRecipes();
    }

    /**
     * Add the recipe for building the Herbalism Station.
     */
    private void addWorkstationFormingRecipes() {
        LayeredMultiBlockFormItemRecipe herbalismStationRecipe = new LayeredMultiBlockFormItemRecipe(
                new ToolTypeEntityFilter("mortarAndPestle"), new Basic2DSizeFilter(3, 1), new AnyActivityFilter(),
                "Alchemy:HerbalismStation", null);
        herbalismStationRecipe.addLayer(1, 1, new BlockUriEntityFilter(new BlockUri("CoreAssets:Brick")));
        herbalismStationRecipe.addLayer(1, 1, new BlockUriEntityFilter(new BlockUri(
                new ResourceUrn("CoreAssets:CobbleStone"), new ResourceUrn(("Engine:EighthBlock")))));
        multiBlockFormRecipeRegistry.addMultiBlockFormItemRecipe(herbalismStationRecipe);
    }

    /**
     * Add all of the potion recipes to the HerbalismStation.
     */
    private void addHerbalismWorkstationRecipes() {
        // TODO: Temporarily removed for sake of testing.
        /* workstationRegistry.registerProcess(WorkstationCrafting.HERBALISM_PROCESS_TYPE,
                new CraftingWorkstationProcess(WorkstationCrafting.HERBALISM_PROCESS_TYPE,
                "WorkstationCrafting:HerbPotion", new HerbalismCraftingStationRecipe()));*/

        // Add all the recipes marked with "HerbalismStationRecipeComponent" in their prefabs and add them to the list.
        for (Prefab prefab : prefabManager.listPrefabs(HerbalismStationRecipeComponent.class)) {
            // Get the Crafting Station recipe component of this recipe prefab.
            CraftingStationRecipeComponent recipeComponent = prefab.getComponent(CraftingStationRecipeComponent.class);

            // We individually register each process instead of using registerProcessFactory (with CraftingWorkstationProcessFactory)
            // as we need to add in some custom actions. The createProcess method in CraftingWorkstationProcessFactory won't do.
            workstationRegistry.registerProcess(Alchemy.HERBALISM_PROCESS_TYPE,
                    new CraftingWorkstationProcess(Alchemy.HERBALISM_PROCESS_TYPE, recipeComponent.recipeId,
                            new HerbalismCraftingStationRecipe(recipeComponent), prefab, entityManager));
        }
    }

    /**
     * This internal predicate class is used to filter out incompatible crafting station types.
     */
    private final class StationTypeFilter implements Predicate<EntityRef> {
        /** Name of the station type. */
        private String stationType;

        /**
         * Define what station this filter should look for.
         *
         * @param stationType   Name of the station being filtered.
         */
        private StationTypeFilter(String stationType) {
            this.stationType = stationType;
        }

        /**
         * Apply an entity to this filter to see if it has the same station type.
         * @param entity    Reference to the entity being checked.
         * @return          True if the entity has the same station type. False if not.
         */
        @Override
        public boolean apply(EntityRef entity) {
            CraftingStationMaterialComponent stationMaterial = entity.getComponent(CraftingStationMaterialComponent.class);
            return stationMaterial != null && stationMaterial.stationType.equals(stationType);
        }
    }
}
