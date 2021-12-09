// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.ui;

import com.google.common.base.Predicate;
import org.terasology.durability.components.DurabilityComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.utilities.Assets;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.system.GenomeManager;
import org.terasology.herbalism.Herbalism;
import org.terasology.herbalism.system.HerbalismClientSystem;
import org.terasology.herbalism.system.HerbalismStationIngredientPredicate;
import org.terasology.module.inventory.ui.ItemIcon;
import org.terasology.nui.widgets.TooltipLine;
import org.terasology.potions.component.EmptyPotionComponent;
import org.terasology.potions.component.PotionComponent;
import org.terasology.workstationCrafting.component.CraftingStationRecipeComponent;
import org.terasology.workstationCrafting.system.recipe.behaviour.ConsumeFluidBehaviour;
import org.terasology.workstationCrafting.system.recipe.behaviour.ConsumeItemCraftBehaviour;
import org.terasology.workstationCrafting.system.recipe.behaviour.InventorySlotResolver;
import org.terasology.workstationCrafting.system.recipe.behaviour.InventorySlotTypeResolver;
import org.terasology.workstationCrafting.system.recipe.render.result.ItemRecipeResultFactory;
import org.terasology.workstationCrafting.system.recipe.workstation.AbstractWorkstationRecipe;

import java.util.Arrays;
import java.util.List;

/**
 * A custom workstation recipe format for herbalism related recipes. Note that this is only intended to be used with the
 * HerbalismCraftingStations. Potions specifically.
 */
public class HerbalismCraftingStationRecipe extends AbstractWorkstationRecipe {
    /**
     * Create the Herbalism Crafting Station's recipe based on the assigned CraftingStationRecipeComponent (i.e. the recipe parameters).
     *
     * @param recipe The titular recipe applicable to this HerbalismStation.
     */
    public HerbalismCraftingStationRecipe(CraftingStationRecipeComponent recipe) {
        String oldPotionContainerName = "";

        // Search through the recipe components to find the first empty potion bottle. If it's found, break out of the loop.
        // Otherwise, the oldPotionContainerName will be an empty string.
        for (String component : recipe.recipeComponents) {
            oldPotionContainerName = component.split("\\*")[1];
            Prefab potionTest = Assets.getPrefab(oldPotionContainerName).get();
            if (potionTest != null && !potionTest.hasComponent(EmptyPotionComponent.class)) {
                break;
            } else {
                oldPotionContainerName = "";
            }
        }

        // Add the fluid behavior and required heat and duration based on the recipe's parameters.
        addFluidBehaviour(new ConsumeFluidBehaviour("CoreAssets:Water", 0.2f, new InventorySlotTypeResolver("FLUID_INPUT")));
        setRequiredHeat(recipe.requiredTemperature);
        setProcessingDuration(recipe.processingDuration);

        // Create and set the custom result factory.
        PotionRecipeResultFactory potionRecipeResultFactory = new PotionRecipeResultFactory(Assets.getPrefab(recipe.recipeId).get(),
                recipe.itemResult.split("\\*")[1], 1, oldPotionContainerName);
        setResultFactory(potionRecipeResultFactory);

        // Add each of the ingredient (consumption) behaviors by parsing through the recipe components.
        for (String component : recipe.recipeComponents) {
            String[] split = component.split("\\*");
            int count = Integer.parseInt(split[0]);
            String type = split[1];

            // If the first item in this recipe is an empty potion container, add it as a ConsumePotionContainerBehaviour.
            if (!oldPotionContainerName.equals("")) {
                addIngredientBehaviour(new ConsumePotionContainerBehaviour(new HerbalismStationIngredientPredicate(type), count,
                        new InventorySlotTypeResolver("INPUT"), potionRecipeResultFactory));
            } else {
                // Otherwise, add it as a herb.
                addIngredientBehaviour(new ConsumeHerbIngredientBehaviour(new HerbalismStationIngredientPredicate(type), count,
                        new InventorySlotTypeResolver("INPUT")));
            }
        }
    }

    /**
     * This internal class is used for creating and defining the resultant potion.
     */
    private final class PotionRecipeResultFactory extends ItemRecipeResultFactory {
        private String toolTip;
        /**
         * Potion's tooltip. This id displayed by default when mouse hovered over.
         */
        private String oldPotionContainerName = "";
        /**
         * Name of the potion's container to be used.
         */
        private EntityRef potionBottleRef;
        /**
         * Reference to the current potion bottle.
         */

        private DurabilityComponent lastDurability;
        /**
         * Reference to the last potion bottle's durability.
         */
        private String lastPotionBottleName = "";    /** Name of the last potion bottle used. */

        /**
         * Constructor for when the potion doesn't use bottles and the toolTip can be the default one.
         *
         * @param prefab Prefab of the potion to be created.
         * @param count Number of the potions to be created.
         */
        private PotionRecipeResultFactory(Prefab prefab, int count) {
            super(prefab, count);
            toolTip = "Herb Potion";
        }

        /**
         * Constructor for when the potion doesn't use bottles but the toolTip needs to be replaced.
         *
         * @param prefab Prefab of the potion to be created.
         * @param toolTip Potion's tooltip to be displayed.
         * @param count Number of the potions to be created.
         */
        private PotionRecipeResultFactory(Prefab prefab, String toolTip, int count) {
            super(prefab, count);
            this.toolTip = toolTip;
        }

        /**
         * Constructor for when the potion uses bottles.
         *
         * @param prefab Prefab of the potion to be created.
         * @param toolTip Potion's tooltip to be displayed.
         * @param count Number of the potions to be created.
         * @param oldContainerName Name of the empty potion container.
         */
        private PotionRecipeResultFactory(Prefab prefab, String toolTip, int count, String oldContainerName) {
            super(prefab, count);
            this.toolTip = toolTip;
            oldPotionContainerName = oldContainerName;
        }

        /**
         * Set the reference to the current potion bottle.
         * <p>
         * #param ref   Reference to the potion bottle entity.
         */
        public void setPotionBottleRef(EntityRef ref) {
            potionBottleRef = ref;
        }

        /**
         * Setup the display of the resultant item. This includes the icon and description text.
         *
         * @param parameters List of parameters of this particular recipe component.
         * @param itemIcon Graphical icon of this item.
         */
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

        /**
         * Create the resultant potion(s)(item) using the given recipe components.
         *
         * @param parameters All of the recipe components necessary for brewing this potion.
         * @param multiplier The number of potions that are created by this recipe.
         * @return A reference to the resultant potion item.
         */
        @Override
        public EntityRef createResult(List<String> parameters, int multiplier) {
            // Extract the herb parameters.
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

            // Add the genome component to the resultant item.
            GenomeComponent genome = new GenomeComponent();
            genome.genomeId = genomeId;
            genome.genes = genes;
            result.addComponent(genome);

            // If the resultant item is a potion, assign the durability of the empty potion bottle to the new potion.
            PotionComponent potionComponent = result.getComponent(PotionComponent.class);
            if (potionComponent != null) {
                if (!oldPotionContainerName.equals("")) {
                    DurabilityComponent potionBottleDurabilityComponent = null;

                    if (potionBottleRef != null && potionBottleRef.exists()) {
                        // If the current potion bottle ref exists, then copy the durability and name over to the appropriate instance
                        // variables.
                        potionBottleDurabilityComponent = potionBottleRef.getComponent(DurabilityComponent.class);
                        lastDurability = potionBottleDurabilityComponent;
                        lastPotionBottleName = potionBottleRef.getParentPrefab().getUrn().toString();
                        potionComponent.bottlePrefab = lastPotionBottleName;
                    } else {
                        // Otherwise, use the last durability value as the potionBottleDurabilityComponent - i.e., the one to be
                        // used for this potion. This is necessary as when there's only one bottle in the workstation input,
                        // consuming it will cause the potionBottleRef to become non-existent. Hence, why I'm using the last
                        // known values instead.
                        potionBottleDurabilityComponent = lastDurability;
                    }

                    // Set the bottle prefab of the resultant potion to be the last known potion bottle name.
                    // It's either taken from the entity ref, or from the recipe parameters list if the former is empty.
                    if (!lastPotionBottleName.equals("")) {
                        potionComponent.bottlePrefab = lastPotionBottleName;
                    } else {
                        potionComponent.bottlePrefab = oldPotionContainerName;
                    }

                    // Create a new Durability component, and copy over the values from the old bottle's Durability component,
                    // as long as the last durability exists. Lastly, add this new component to the resultant potion.
                    if (potionBottleDurabilityComponent != null) {
                        DurabilityComponent durabilityComponent = new DurabilityComponent();
                        durabilityComponent.durability = potionBottleDurabilityComponent.durability;
                        durabilityComponent.maxDurability = potionBottleDurabilityComponent.maxDurability;
                        result.addComponent(durabilityComponent);
                    }
                }
            }

            return result;
        }
    }

    /**
     * This internal class is used to define the custom consumption behavior of potion bottles during crafting.
     */
    private final class ConsumePotionContainerBehaviour extends ConsumeItemCraftBehaviour {
        /**
         * Reference to the potion crafting result factory.
         */
        private PotionRecipeResultFactory potionRecipeResultFactory;

        /**
         * Constructor which creates the baseline for this item consumption behavior.
         *
         * @param matcher Predicate matcher for filtering out items that are not empty potion containers.
         * @param count Number of potion containers to consume while crafting.
         * @param resolver To manage the inventory changes during this behavior.
         * @param potionRecipeResultFactory Reference to the associated potion result factory.
         */
        private ConsumePotionContainerBehaviour(Predicate<EntityRef> matcher, int count, InventorySlotResolver resolver,
                                                PotionRecipeResultFactory potionRecipeResultFactory) {
            super(matcher, count, resolver);
            this.potionRecipeResultFactory = potionRecipeResultFactory;
        }

        /**
         * Get the ingredient parameters and where they are located in the workstation's inventory, and return them as a String. If the
         * current item being checked is an empty potion bottle, give the result factory class that information.
         *
         * @param slots List of workstation inventory slots that the item is present in.
         * @param item Reference to the recipe component item in question.
         * @return The ingredient parameters of this item in a combined String.
         */
        @Override
        protected String getParameter(List<Integer> slots, EntityRef item) {
            if (item.hasComponent(EmptyPotionComponent.class)) {
                potionRecipeResultFactory.setPotionBottleRef(item);
            }
            return super.getParameter(slots, item);
        }
    }

    /**
     * This internal class is used to define the custom consumption behavior of herbs during crafting.
     */
    private final class ConsumeHerbIngredientBehaviour extends ConsumeItemCraftBehaviour {
        /**
         * Constructor which creates the baseline for this item consumption behavior.
         *
         * @param matcher Predicate matcher for filtering out items that are not herbs.
         * @param count Number of herbs of this type to consume while crafting.
         * @param resolver To manage the inventory changes during this behavior.
         */
        private ConsumeHerbIngredientBehaviour(Predicate<EntityRef> matcher, int count, InventorySlotResolver resolver) {
            super(matcher, count, resolver);
        }

        /**
         * Get the ingredient parameters and where they are located in the workstation's inventory, and return them as a String. If
         * possible, add the genome parameters of the herbs to the parameter String.
         *
         * @param slots List of workstation inventory slots that the item is present in.
         * @param item Reference to the herb item in question.
         * @return The ingredient parameters of this item plus the genome information in a combined String.
         */
        @Override
        protected String getParameter(List<Integer> slots, EntityRef item) {
            final GenomeComponent genome = item.getComponent(GenomeComponent.class);
            final String herbName = CoreRegistry.get(GenomeManager.class).getGenomeProperty(item, Herbalism.NAME_PROPERTY, String.class);
            return super.getParameter(slots, item) + "|" + genome.genomeId + "|" + genome.genes + "|" + herbName;
        }

        /**
         * Return an integer list of workstation inventory slots this herb is present in.
         *
         * @param parameter Name of the item being searched for.
         * @return What inventory slots this item (likely a herb) is present in.
         */
        @Override
        protected List<Integer> getSlots(String parameter) {
            return super.getSlots(parameter.substring(0, parameter.indexOf('|')));
        }
    }
}
