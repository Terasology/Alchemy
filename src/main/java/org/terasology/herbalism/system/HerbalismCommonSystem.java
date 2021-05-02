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

import com.google.common.base.Function;
import org.terasology.alterationEffects.breath.WaterBreathingAlterationEffect;
import org.terasology.alterationEffects.damageOverTime.CureAllDamageOverTimeAlterationEffect;
import org.terasology.alterationEffects.damageOverTime.DamageOverTimeAlterationEffect;
import org.terasology.alterationEffects.regenerate.RegenerationAlterationEffect;
import org.terasology.alterationEffects.speed.JumpSpeedAlterationEffect;
import org.terasology.alterationEffects.speed.MultiJumpAlterationEffect;
import org.terasology.alterationEffects.speed.SwimSpeedAlterationEffect;
import org.terasology.alterationEffects.speed.WalkSpeedAlterationEffect;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.engine.context.Context;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.texture.TextureRegionAsset;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.genome.GenomeDefinition;
import org.terasology.genome.GenomeRegistry;
import org.terasology.genome.breed.BreedingAlgorithm;
import org.terasology.genome.breed.MonoploidBreedingAlgorithm;
import org.terasology.genome.breed.mutator.GeneMutator;
import org.terasology.genome.genomeMap.SeedBasedGenomeMap;
import org.terasology.herbalism.HerbEffectRegistry;
import org.terasology.herbalism.HerbGeneMutator;
import org.terasology.herbalism.HerbNameProvider;
import org.terasology.herbalism.Herbalism;
import org.terasology.herbalism.component.HerbHueComponent;
import org.terasology.potions.HerbEffect;
import org.terasology.potions.effect.AlterationToHerbEffectWrapper;
import org.terasology.potions.effect.DoNothingEffect;
import org.terasology.potions.effect.HealEffect;

/**
 * Common system for Herbalism that handles the registration of herb effects and genome properties.
 */
@RegisterSystem
public class HerbalismCommonSystem extends BaseComponentSystem {
    @In
    private AssetManager assetManager;
    @In
    private WorldProvider worldProvider;
    @In
    private HerbEffectRegistry herbEffectRegistry;
    @In
    private PrefabManager prefabManager;
    @In
    private BlockManager blockManager;
    @In
    private GenomeRegistry genomeRegistry;
    @In
    private Context context;

    /**
     * Before beginning execution of this component system, register the herb effects and the genome definition for the herbs.
     */
    @Override
    public void preBegin() {
        // Registering all of the default herb effects into the registry.
        herbEffectRegistry.registerHerbEffect(1f, new DoNothingEffect());
        herbEffectRegistry.registerHerbEffect(1f, new HealEffect());
        herbEffectRegistry.registerHerbEffect(1f, new AlterationToHerbEffectWrapper(new WalkSpeedAlterationEffect(context), 1f, 1f));
        herbEffectRegistry.registerHerbEffect(1f, new AlterationToHerbEffectWrapper(new SwimSpeedAlterationEffect(context), 1f, 1f));
        herbEffectRegistry.registerHerbEffect(1f, new AlterationToHerbEffectWrapper(new RegenerationAlterationEffect(context), 1f, 100f));
        herbEffectRegistry.registerHerbEffect(1f, new AlterationToHerbEffectWrapper(new WaterBreathingAlterationEffect(context), 1f, 1f));
        herbEffectRegistry.registerHerbEffect(1f, new AlterationToHerbEffectWrapper(new JumpSpeedAlterationEffect(context), 1f, 1f));
        herbEffectRegistry.registerHerbEffect(1f, new AlterationToHerbEffectWrapper(new MultiJumpAlterationEffect(context), 1f, 1f));
        herbEffectRegistry.registerHerbEffect(1f, new AlterationToHerbEffectWrapper(new DamageOverTimeAlterationEffect(context), 1f, 1f));
        herbEffectRegistry.registerHerbEffect(1f, new AlterationToHerbEffectWrapper(new CureAllDamageOverTimeAlterationEffect(context), 1f, 1f));

        // Defining a herb name provider.
        final HerbNameProvider herbNameProvider = new HerbNameProvider(worldProvider.getSeed().hashCode());

        int genomeLength = 10;

        // Creating the gene mutator and the breeding algorithm.
        GeneMutator herbGeneMutator = new HerbGeneMutator();
        BreedingAlgorithm herbBreedingAlgorithm = new MonoploidBreedingAlgorithm(9, 0.005f, herbGeneMutator);

        // Creating the seed based genome map for the herbs, and adding all of the seed-based properties based on the
        // constants in the Herbalism class. Most are self-explanatory of what they do.
        SeedBasedGenomeMap herbGenomeMap = new SeedBasedGenomeMap(worldProvider.getSeed().hashCode());
        herbGenomeMap.addSeedBasedProperty(Herbalism.EFFECT_PROPERTY, 1, genomeLength, 3, HerbEffect.class,
                new Function<String, HerbEffect>() {
                    @Override
                    public HerbEffect apply(String input) {
                        final int i = input.hashCode();
                        float value;
                        if (i < 0) {
                            value = 0.5f + 0.5f * i / Integer.MIN_VALUE;
                        } else {
                            value = 0.5f * i / Integer.MAX_VALUE;
                        }
                        return herbEffectRegistry.getHerbEffect(value);
                    }
                });
        herbGenomeMap.addSeedBasedProperty(Herbalism.DURATION_PROPERTY, 1, genomeLength, 2, Long.class,
                new Function<String, Long>() {
                    @Override
                    public Long apply(String input) {
                        int multiplier = input.charAt(0) - 'A' + 1;
                        int duration = 1000 * (input.charAt(1) - 'A' + 1);
                        return (long) (duration * multiplier);
                    }
                });
        herbGenomeMap.addSeedBasedProperty(Herbalism.MAGNITUDE_PROPERTY, 1, genomeLength, 2, Float.class,
                new Function<String, Float>() {
                    @Override
                    public Float apply(String input) {
                        int multiplier = input.charAt(0) - 'A' + 1;
                        float duration = 0.25f * 0.25f * (input.charAt(1) - 'A' + 1);
                        return duration * multiplier;
                    }
                });
        herbGenomeMap.addSeedBasedProperty(Herbalism.NAME_PROPERTY, genomeLength, genomeLength, String.class,
                new Function<String, String>() {
                    @Override
                    public String apply(String input) {
                        return herbNameProvider.getName(input);
                    }
                });
        // This is for defining the icon of the herb, and how it can vary based on the herb hues.
        herbGenomeMap.addProperty(Herbalism.ICON_PROPERTY, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, TextureRegionAsset.class,
                new Function<String, TextureRegionAsset>() {
                    @Override
                    public TextureRegionAsset apply(String input) {
                        String type = input.substring(0, 1);
                        String genes = input.substring(1, 10);
                        HerbHueComponent herbHue = prefabManager.getPrefab("Alchemy:HerbHue" + type).getComponent(HerbHueComponent.class);

                        FastRandom rnd = new FastRandom(genes.hashCode() + 3497987);
                        float[] hueValues = new float[herbHue.hueRanges.size()];
                        for (int i = 0; i < hueValues.length; i++) {
                            String[] hueRangeSplit = herbHue.hueRanges.get(i).split("-");
                            float min = Float.parseFloat(hueRangeSplit[0]);
                            float max = Float.parseFloat(hueRangeSplit[1]);
                            hueValues[i] = rnd.nextFloat(min, max);
                        }

                        return Assets.getTextureRegion(HerbIconAssetResolver.getHerbUri("Alchemy:Herb" + type, hueValues)).get();
                    }
                });
        herbGenomeMap.addProperty(Herbalism.PLANTED_BLOCK_PROPERTY, new int[]{0}, Block.class,
                new Function<String, Block>() {
                    @Override
                    public Block apply(String input) {
                        return blockManager.getBlock("Alchemy:HerbGrow" + input);
                    }
                });

        // Based on the above generated breeding algorithm and genome map, use the two to define the herb genome.
        GenomeDefinition herbGenomeDefinition = new GenomeDefinition(herbBreedingAlgorithm, herbGenomeMap);

        // Register the genome definition for any generated herb. This is not applicable to predefined herbs.
        genomeRegistry.registerType("Alchemy:Herb", herbGenomeDefinition);
    }
}
