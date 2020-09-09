// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.alchemy.system;

import com.google.common.base.Predicate;
import org.terasology.anotherWorldPlants.farm.component.SeedComponent;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.assets.texture.TextureRegionAsset;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.block.Block;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.system.GenomeManager;
import org.terasology.herbalism.Herbalism;
import org.terasology.herbalism.component.HerbComponent;
import org.terasology.herbalism.system.HerbalismClientSystem;
import org.terasology.inventory.rendering.nui.layers.ingame.ItemIcon;
import org.terasology.nui.widgets.TooltipLine;
import org.terasology.workstationCrafting.system.recipe.behaviour.ConsumeItemCraftBehaviour;
import org.terasology.workstationCrafting.system.recipe.behaviour.IngredientCraftBehaviour;
import org.terasology.workstationCrafting.system.recipe.behaviour.ReduceDurabilityCraftBehaviour;
import org.terasology.workstationCrafting.system.recipe.hand.CraftInHandIngredientPredicate;
import org.terasology.workstationCrafting.system.recipe.hand.CraftInHandRecipe;
import org.terasology.workstationCrafting.system.recipe.hand.PlayerInventorySlotResolver;
import org.terasology.workstationCrafting.system.recipe.render.CraftIngredientRenderer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A custom recipe format for seeding herb related recipes. Specifically for crafting herb seeds.
 */
public class SeedingHerbRecipe implements CraftInHandRecipe {
    /**
     * Define craft behavior for knifes. Specifically, reducing the knife's durability by 1 per crat.
     */
    private static final IngredientCraftBehaviour<EntityRef> KNIFE_BEHAVIOUR = new ReduceDurabilityCraftBehaviour(
            new CraftInHandIngredientPredicate("WorkstationCrafting:knife"), 1,
            PlayerInventorySlotResolver.singleton());

    /**
     * Define a herb consumption behavior.
     */
    private static final ConsumeHerbBehaviour HERB_BEHAVIOUR = new ConsumeHerbBehaviour();

    /**
     * Get a list of matching recipes for this character.
     *
     * @param character The character entity that'll be doing the crafting.
     * @return A list containing all matching seed recipe results.
     */
    @Override
    public List<CraftInHandResult> getMatchingRecipeResults(EntityRef character) {
        // If no knife parameter exists for this character, return null.
        String knifeParameter = getKnifeParameter(character);
        if (knifeParameter == null) {
            return null;
        }

        List<CraftInHandResult> results = new LinkedList<>();

        // Construct the list of applicable recipes. These must fulfill both the herb and knife behaviors.
        final List<String> herbParameters = HERB_BEHAVIOUR.getValidToCraft(character, 1);
        for (String herbParameter : herbParameters) {
            results.add(new Result(Arrays.asList(knifeParameter, herbParameter)));
        }

        return results;
    }

    /**
     * Ensure that this character entity actually has a knife.
     *
     * @param character The character entity that'll be doing the crafting.
     * @return The first valid knife parameter.
     */
    private String getKnifeParameter(EntityRef character) {
        List<String> parameters = KNIFE_BEHAVIOUR.getValidToCraft(character, 1);
        if (parameters.size() > 0) {
            return parameters.get(0);
        }
        return null;
    }

    /**
     * Convert a parameter (or ingredients) list into a Result.
     *
     * @param parameters The list of parameters.
     * @return A CraftInHandResult instance which hsa the methods necessary to create the final item.
     */
    @Override
    public CraftInHandResult getResultByParameters(List<String> parameters) {
        return new Result(parameters);
    }

    /**
     * This internal class is used to define the specific crafting result for the seeding herb recipe.
     */
    public static final class Result implements CraftInHandResult {
        /**
         * List of recipe parameters required to make this result.
         */
        private final List<String> parameters;

        /**
         * List of ingredient renderers
         */
        private List<CraftIngredientRenderer> renderers;

        /**
         * Create an instance of this class using a list of recipe parameters.
         *
         * @param parameters List of items in the recipe that are required to craft the final product.
         */
        private Result(List<String> parameters) {
            this.parameters = parameters;
        }

        /**
         * Get the recipe parameters list.
         *
         * @return A list containing the recipe parameters.
         */
        @Override
        public List<String> getParameters() {
            return parameters;
        }

        /**
         * Get the max consumption multiplier possible. This value multiplies the consumption effects in the
         * processIngredient method.
         *
         * @param entity The entity that will be doing the crafting, and thus, has an inventory.
         * @return The max multiplier.
         */
        @Override
        public int getMaxMultiplier(EntityRef entity) {
            int maxMultiplier = KNIFE_BEHAVIOUR.getMaxMultiplier(entity, parameters.get(0));
            maxMultiplier = Math.min(maxMultiplier, HERB_BEHAVIOUR.getMaxMultiplier(entity, parameters.get(1)));
            return maxMultiplier;
        }

        /**
         * Craft the herb seed described in this recipe.
         *
         * @param character The character entity doing the crafting.
         * @param count The process multiplier. This multiplies the consumption effects of processIngredient.
         * @return The crafted herb seed.
         */
        @Override
        public EntityRef craft(EntityRef character, int count) {
            // First, make sure it's actually possible to craft with this multiplier.
            if (!isValidForCrafting(character, count)) {
                return EntityRef.NULL;
            }

            // Process the ingredients.
            KNIFE_BEHAVIOUR.processIngredient(character, character, parameters.get(0), count);
            HERB_BEHAVIOUR.processIngredient(character, character, parameters.get(1), count);

            // Create a new herb seed.
            final EntityRef herbSeed = CoreRegistry.get(EntityManager.class).create("Alchemy:HerbSeedBase");
            final GenomeManager genomeManager = CoreRegistry.get(GenomeManager.class);

            // Attach a new genome to the herb seed.
            GenomeComponent genomeComponent = new GenomeComponent();
            genomeComponent.genomeId = "Herbalism:Herb";
            genomeComponent.genes = HERB_BEHAVIOUR.getSeedGenome(parameters.get(1));
            herbSeed.addComponent(genomeComponent);

            // Add a seed component to the herb seed.
            SeedComponent seedComponent = new SeedComponent();
            seedComponent.blockPlaced = genomeManager.getGenomeProperty(herbSeed, Herbalism.PLANTED_BLOCK_PROPERTY,
                    Block.class);
            herbSeed.addComponent(seedComponent);

            // Replace the icon with a seed bag image.
            ItemComponent itemComponent = herbSeed.getComponent(ItemComponent.class);
            itemComponent.icon =
                    Assets.getTextureRegion("AnotherWorldPlants:SeedBag(" + HERB_BEHAVIOUR.getHerbIconUri(parameters.get(1)) + ")").get();
            herbSeed.saveComponent(itemComponent);

            return herbSeed;
        }

        /**
         * Check to see if it's possible for this entity to craft the given recipe with this consumption multiplier.
         * This multiplier multiplies the consumption effects in the processIngredient method.
         *
         * @param entity The character entity doing the crafting.
         * @param multiplier The consumption effects multiplier.
         * @return True if possible to craft. False otherwise.
         */
        @Override
        public boolean isValidForCrafting(EntityRef entity, int multiplier) {
            // Check against both the knife and herb behaviors. If both pass, return true. Otherwise, return false.
            if (!KNIFE_BEHAVIOUR.isValidToCraft(entity, parameters.get(0), multiplier)) {
                return false;
            }
            return HERB_BEHAVIOUR.isValidToCraft(entity, parameters.get(1), multiplier);
        }

        /**
         * Get the crafting ingredient renderers. These display the item icons in the crafting UI.
         *
         * @param entity The entity that has the crafting UI.
         * @return A list containing all of the CraftIngredientRenderers.
         */
        @Override
        public List<CraftIngredientRenderer> getIngredientRenderers(EntityRef entity) {
            if (renderers == null) {
                renderers = new LinkedList<>();
                renderers.add(HERB_BEHAVIOUR.getRenderer(entity, parameters.get(1)));
                renderers.add(KNIFE_BEHAVIOUR.getRenderer(entity, parameters.get(0)));
            }
            return renderers;
        }

        /**
         * Get the crafting process duration.
         *
         * @return 0, as this process is intended to be completed instantly.
         */
        @Override
        public long getProcessDuration() {
            return 0;
        }

        /**
         * Get the number of crafted items.
         *
         * @return 1, as this process is intended only craft 1 herb seed at a time.
         */
        @Override
        public int getResultQuantity() {
            return 1;
        }

        /**
         * Setup the resultant herb seed's icon and tooltip lines. The former will be displayed on the crafting UI, and
         * the latter when the seed is moused over.
         *
         * @param itemIcon The herb seed's item icon.
         */
        @Override
        public void setupResultDisplay(ItemIcon itemIcon) {
            itemIcon.setIcon(Assets.getTextureRegion("AnotherWorldPlants:SeedBag(" + HERB_BEHAVIOUR.getHerbIconUri(parameters.get(1)) + ")").get());
            itemIcon.setTooltipLines(Arrays.asList(new TooltipLine("Herb Seed"),
                    HerbalismClientSystem.getHerbTooltipLine(HERB_BEHAVIOUR.getHerbName(parameters.get(1)))));
        }
    }

    /**
     * This internal class is used to define the custom consumption behavior of herbs during crafting.
     */
    private static class ConsumeHerbBehaviour extends ConsumeItemCraftBehaviour {
        /**
         * Constructor which sets up the predicate or filter of this craft behavior.
         */
        public ConsumeHerbBehaviour() {
            super(new Predicate<EntityRef>() {
                /**
                 * Apply an entity to this filter to see if it's an herb and if it has a genome.
                 *
                 * @param input    Reference to the entity being checked.
                 * @return True if the entity fulfills the above conditions. False if not.
                 */
                @Override
                public boolean apply(EntityRef input) {
                    return input.hasComponent(HerbComponent.class) && input.hasComponent(GenomeComponent.class);
                }
            }, 1, PlayerInventorySlotResolver.singleton());
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

        /**
         * Get the ingredient parameters and where they are located in the workstation's inventory, and return them as a
         * String. If possible, add the genome parameters of the herbs to the parameter String.
         *
         * @param slots List of workstation inventory slots that the item is present in.
         * @param item Reference to the herb item in question.
         * @return The ingredient parameters of this item plus the genome information in a combined String.
         */
        @Override
        protected String getParameter(List<Integer> slots, EntityRef item) {
            final GenomeComponent genome = item.getComponent(GenomeComponent.class);

            final GenomeManager genomeManager = CoreRegistry.get(GenomeManager.class);
            String herbName = genomeManager.getGenomeProperty(item, Herbalism.NAME_PROPERTY, String.class);
            String herbIconUri = (genomeManager.getGenomeProperty(item, Herbalism.ICON_PROPERTY,
                    TextureRegionAsset.class)).getUrn().toString();

            return super.getParameter(slots, item) + "|" + genome.genes + "|" + herbName + "|" + herbIconUri;
        }

        /**
         * Extract the seed genome from the seeding herb recipe parameters.
         *
         * @param parameter The recipe parameters in one String.
         * @return A String containing the seed genome.
         */
        public String getSeedGenome(String parameter) {
            return parameter.split("\\|")[1];
        }

        /**
         * Extract the herb name from the seeding herb recipe parameters.
         *
         * @param parameter The recipe parameters in one String.
         * @return A String containing the herb name.
         */
        public String getHerbName(String parameter) {
            return parameter.split("\\|")[2];
        }

        /**
         * Extract the herb icon URI from the seeding herb recipe parameters.
         *
         * @param parameter The recipe parameters in one String.
         * @return A String containing the herb icon URI.
         */
        public String getHerbIconUri(String parameter) {
            return parameter.split("\\|")[3];
        }
    }
}
