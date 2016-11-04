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

import com.google.common.base.Predicate;
import org.terasology.durability.components.DurabilityComponent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.potions.component.EmptyPotionComponent;
import org.terasology.potions.component.PotionComponent;
import org.terasology.protobuf.EntityData;
import org.terasology.workstationCrafting.component.CraftingStationRecipeComponent;
import org.terasology.workstationCrafting.system.recipe.behaviour.ConsumeFluidBehaviour;
import org.terasology.workstationCrafting.system.recipe.behaviour.ConsumeItemCraftBehaviour;
import org.terasology.workstationCrafting.system.recipe.behaviour.InventorySlotResolver;
import org.terasology.workstationCrafting.system.recipe.behaviour.InventorySlotTypeResolver;
import org.terasology.workstationCrafting.system.recipe.render.result.ItemRecipeResultFactory;
import org.terasology.workstationCrafting.system.recipe.workstation.AbstractWorkstationRecipe;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.system.GenomeManager;
import org.terasology.herbalism.Herbalism;
import org.terasology.herbalism.component.HerbComponent;
import org.terasology.herbalism.system.HerbalismClientSystem;
import org.terasology.herbalism.system.HerbalismStationIngredientPredicate;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.layers.ingame.inventory.ItemIcon;
import org.terasology.rendering.nui.widgets.TooltipLine;
import org.terasology.utilities.Assets;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

// A custom crafting station recipe was used
public class HerbalismCraftingStationRecipe extends AbstractWorkstationRecipe {
    public HerbalismCraftingStationRecipe() {
        Predicate<EntityRef> herbComponentPredicate = new Predicate<EntityRef>() {
            @Override
            public boolean apply(EntityRef input) {
                return input.hasComponent(HerbComponent.class);
            }
        };
        addIngredientBehaviour(new ConsumeHerbIngredientBehaviour(herbComponentPredicate, 1, new InventorySlotTypeResolver("INPUT")));
        addFluidBehaviour(new ConsumeFluidBehaviour("Fluid:Water", 0.2f, new InventorySlotTypeResolver("FLUID_INPUT")));
        setRequiredHeat(95f);
        setProcessingDuration(10000);
        setResultFactory(new PotionRecipeResultFactory(Assets.getPrefab("Alchemy:HerbPotion").get(), 1));
    }

    public HerbalismCraftingStationRecipe(String prefabPath, String toolTip) {
        Predicate<EntityRef> herbComponentPredicate = new Predicate<EntityRef>() {
            @Override
            public boolean apply(EntityRef input) {
                return input.hasComponent(HerbComponent.class);
            }
        };
        addIngredientBehaviour(new ConsumeHerbIngredientBehaviour(herbComponentPredicate, 1, new InventorySlotTypeResolver("INPUT")));
        addFluidBehaviour(new ConsumeFluidBehaviour("Fluid:Water", 0.2f, new InventorySlotTypeResolver("FLUID_INPUT")));
        setRequiredHeat(95f);
        setProcessingDuration(10000);
        setResultFactory(new PotionRecipeResultFactory(Assets.getPrefab(prefabPath).get(), toolTip, 1));
    }

    public HerbalismCraftingStationRecipe(String prefabPath, String displayName, float requiredTemperature, long processingDuration) {
        Predicate<EntityRef> herbComponentPredicate = new Predicate<EntityRef>() {
            @Override
            public boolean apply(EntityRef input) {
                return input.hasComponent(HerbComponent.class);
            }
        };
        addIngredientBehaviour(new ConsumeHerbIngredientBehaviour(herbComponentPredicate, 1, new InventorySlotTypeResolver("INPUT")));
        addFluidBehaviour(new ConsumeFluidBehaviour("Fluid:Water", 0.2f, new InventorySlotTypeResolver("FLUID_INPUT")));
        setRequiredHeat(requiredTemperature);
        setProcessingDuration(processingDuration);
        setResultFactory(new PotionRecipeResultFactory(Assets.getPrefab(prefabPath).get(), displayName, 1));
    }

    public HerbalismCraftingStationRecipe(String prefabPath, String displayName, List<String> recipeComponents, float requiredTemperature, long processingDuration) {
        Predicate<EntityRef> herbComponentPredicate = new Predicate<EntityRef>() {
            @Override
            public boolean apply(EntityRef input) {
                return input.hasComponent(HerbComponent.class);
            }
        };

        for (String component : recipeComponents) {
            String[] split = component.split("\\*");
            int count = Integer.parseInt(split[0]);
            String type = split[1];
            addIngredientBehaviour(new ConsumeHerbIngredientBehaviour(herbComponentPredicate, count, new InventorySlotTypeResolver("INPUT")));
        }

        addFluidBehaviour(new ConsumeFluidBehaviour("Fluid:Water", 0.2f, new InventorySlotTypeResolver("FLUID_INPUT")));
        setRequiredHeat(requiredTemperature);
        setProcessingDuration(processingDuration);
        setResultFactory(new PotionRecipeResultFactory(Assets.getPrefab(prefabPath).get(), displayName, 1));
    }

    public HerbalismCraftingStationRecipe(CraftingStationRecipeComponent recipe) {
        Predicate<EntityRef> herbComponentPredicate = new Predicate<EntityRef>() {
            @Override
            public boolean apply(EntityRef input) {
                return input.hasComponent(HerbComponent.class);
            }
        };

        // Check to see if the first item in this recipe is a potion container.
        // TODO: Make it so that it doesn't matter where in the recipe the potion bottle is.
        String oldPotionContainerName = recipe.recipeComponents.get(0).split("\\*")[1];
        Prefab potionTest = Assets.getPrefab(oldPotionContainerName).get();
        if (!potionTest.hasComponent(EmptyPotionComponent.class)) {
            oldPotionContainerName = "";
        }

        // Add the fluid behavior and required heat and duration.
        addFluidBehaviour(new ConsumeFluidBehaviour("Fluid:Water", 0.2f, new InventorySlotTypeResolver("FLUID_INPUT")));
        setRequiredHeat(recipe.requiredTemperature);
        setProcessingDuration(recipe.processingDuration);

        // Create and set the custom result factory.
        PotionRecipeResultFactory potionRecipeResultFactory = new PotionRecipeResultFactory(Assets.getPrefab(recipe.recipeId).get(), recipe.itemResult.split("\\*")[1], 1, oldPotionContainerName);
        setResultFactory(potionRecipeResultFactory);

        for (String component : recipe.recipeComponents) {
            String[] split = component.split("\\*");
            int count = Integer.parseInt(split[0]);
            String type = split[1];

            //addIngredientBehaviour(new ConsumeHerbIngredientBehaviour(herbComponentPredicate, count, new InventorySlotTypeResolver("INPUT")));

            // If the first item in this recipe is an empty potion container, add it as a ConsumePotionContainerBehaviour.
            if (!oldPotionContainerName.equals("")) {
                addIngredientBehaviour(new ConsumePotionContainerBehaviour(new HerbalismStationIngredientPredicate(type), count, new InventorySlotTypeResolver("INPUT"), potionRecipeResultFactory));
            }
            else {
                addIngredientBehaviour(new ConsumeHerbIngredientBehaviour(new HerbalismStationIngredientPredicate(type), count, new InventorySlotTypeResolver("INPUT")));
            }

        }
    }

    private final class PotionRecipeResultFactory extends ItemRecipeResultFactory {
        private String toolTip;
        private String oldPotionContainerName;
        private EntityRef potionBottleRef;

        private DurabilityComponent lastDurability;
        private String lastPotionBottleName;

        public void setPotionBottleRef(EntityRef ref) {
            potionBottleRef = ref;
        }

        private PotionRecipeResultFactory(Prefab prefab, int count) {
            super(prefab, count);
            toolTip = "Herb Potion";
        }

        private PotionRecipeResultFactory(Prefab prefab, String toolTip, int count) {
            super(prefab, count);
            this.toolTip = toolTip;
        }

        private PotionRecipeResultFactory(Prefab prefab, String toolTip, int count, String oldContainerName) {
            super(prefab, count);
            this.toolTip = toolTip;
            oldPotionContainerName = oldContainerName;
        }

        @Override
        public void setupDisplay(List<String> parameters, ItemIcon itemIcon) {
            super.setupDisplay(parameters, itemIcon);

            final String herbParameter = parameters.get(0);
            String herbName = "";

            // If the herb parameters are greater than or equal to 3, then it's a herb. Otherwise, it's something else.
            if (herbParameter.split("\\|").length >= 3) {
                herbName = herbParameter.split("\\|")[3];
            }
            itemIcon.setTooltipLines(
                    Arrays.asList(new TooltipLine(toolTip), HerbalismClientSystem.getHerbTooltipLine(herbName)));
        }

        // Create the resultant potion (item) here.
        @Override
        public EntityRef createResult(List<String> parameters, int multiplier) {
            final EntityRef result = super.createResult(parameters, multiplier);
            final String herbParameter = parameters.get(0);
            final String[] herbSplit = herbParameter.split("\\|");

            String genomeId = "";
            String genes = "";

            // If the split is greater than or equal to 3, then it's a herb. Otherwise, it's something else.
            if (herbSplit.length >= 3) {
                genomeId = herbSplit[1];
                genes = herbSplit[2];
            }

            // Add the gneom component to the resultant item.
            GenomeComponent genome = new GenomeComponent();
            genome.genomeId = genomeId;
            genome.genes = genes;
            result.addComponent(genome);

            // If the resultant item is a potion, assign the durability of the empty potion bottle to the new potion.
            PotionComponent potionComponent = result.getComponent(PotionComponent.class);
            if (potionComponent != null) {
                if (!oldPotionContainerName.equals("")) {
                    DurabilityComponent potionBottleDurabilityComponent = null;

                    if (potionBottleRef.exists()) {
                        potionBottleDurabilityComponent = potionBottleRef.getComponent(DurabilityComponent.class);
                        lastDurability = potionBottleDurabilityComponent;
                        lastPotionBottleName = potionBottleRef.getParentPrefab().getUrn().toString();
                        potionComponent.bottlePrefab = lastPotionBottleName;
                    }
                    else {
                        potionBottleDurabilityComponent = lastDurability;
                    }

                    potionComponent.bottlePrefab = lastPotionBottleName;

                    DurabilityComponent durabilityComponent = new DurabilityComponent();
                    durabilityComponent.durability = potionBottleDurabilityComponent.durability;
                    durabilityComponent.maxDurability = potionBottleDurabilityComponent.maxDurability;
                    result.addComponent(durabilityComponent);
                }
            }

            return result;
        }
    }

    private final class ConsumePotionContainerBehaviour extends ConsumeItemCraftBehaviour {
        private PotionRecipeResultFactory potionRecipeResultFactory;

        private ConsumePotionContainerBehaviour(Predicate<EntityRef> matcher, int count, InventorySlotResolver resolver, PotionRecipeResultFactory potionRecipeResultFactory) {
            super(matcher, count, resolver);
            this.potionRecipeResultFactory = potionRecipeResultFactory;
        }

        @Override
        protected String getParameter(List<Integer> slots, EntityRef item) {
            if (item.hasComponent(EmptyPotionComponent.class)) {
                potionRecipeResultFactory.setPotionBottleRef(item);
            }
            return super.getParameter(slots, item);
        }

        @Override
        public void processIngredient(EntityRef instigator, EntityRef entity, String parameter, int multiplier) {
            super.processIngredient(instigator, entity, parameter, multiplier);
        }
    }

    private final class ConsumeHerbIngredientBehaviour extends ConsumeItemCraftBehaviour {
        private ConsumeHerbIngredientBehaviour(Predicate<EntityRef> matcher, int count, InventorySlotResolver resolver) {
            super(matcher, count, resolver);
        }

        @Override
        protected String getParameter(List<Integer> slots, EntityRef item) {
            final GenomeComponent genome = item.getComponent(GenomeComponent.class);
            final String herbName = CoreRegistry.get(GenomeManager.class).getGenomeProperty(item, Herbalism.NAME_PROPERTY, String.class);
            return super.getParameter(slots, item) + "|" + genome.genomeId + "|" + genome.genes + "|" + herbName;
        }

        @Override
        protected List<Integer> getSlots(String parameter) {
            return super.getSlots(parameter.substring(0, parameter.indexOf('|')));
        }
    }
}
