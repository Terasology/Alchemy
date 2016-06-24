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

import org.terasology.alterationEffects.regenerate.RegenerationAlterationEffect;
import org.terasology.alterationEffects.speed.JumpSpeedAlterationEffect;
import org.terasology.alterationEffects.speed.JumpSpeedComponent;
import org.terasology.alterationEffects.speed.SwimSpeedAlterationEffect;
import org.terasology.alterationEffects.speed.WalkSpeedAlterationEffect;
import org.terasology.audio.AudioManager;
import org.terasology.context.Context;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.system.GenomeManager;
import org.terasology.herbalism.HerbEffect;
import org.terasology.herbalism.Herbalism;
import org.terasology.herbalism.PotionCommonEffects;
import org.terasology.herbalism.component.PotionComponent;
import org.terasology.herbalism.component.PotionEffect;
import org.terasology.herbalism.effect.AlterationEffectWrapperHerbEffect;
import org.terasology.herbalism.effect.DoNothingEffect;
import org.terasology.herbalism.effect.HealEffect;
import org.terasology.herbalism.events.BeforeDrinkPotionEvent;
import org.terasology.herbalism.events.DrinkPotionEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;

import java.util.HashMap;
import java.util.Map;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class DrinkPotionAuthoritySystem extends BaseComponentSystem {
    @In
    private AudioManager audioManager;

    @In
    private GenomeManager genomeManager;

    @In
    private Context context;

    @ReceiveEvent
    public void potionConsumed(ActivateEvent event, EntityRef item, PotionComponent potion, GenomeComponent genome) {

        // TODO: Stopgap fix. If this potion is not supposed to have a dynamically-set Genome, return.
        if (!potion.hasGenome) {
            return;
        }

        final HerbEffect effect = genomeManager.getGenomeProperty(item, Herbalism.EFFECT_PROPERTY, HerbEffect.class);
        final float magnitude = genomeManager.getGenomeProperty(item, Herbalism.MAGNITUDE_PROPERTY, Float.class);
        final long duration = genomeManager.getGenomeProperty(item, Herbalism.DURATION_PROPERTY, Long.class);
        effect.applyEffect(item, event.getInstigator(), magnitude, duration);
    }

    private void checkDrink(EntityRef instigator, EntityRef item, PotionComponent p, HerbEffect h, PotionEffect v) {
        BeforeDrinkPotionEvent beforeDrink = instigator.send(new BeforeDrinkPotionEvent(p, h, v, instigator, item));

        if (!beforeDrink.isConsumed()) {
            float modifiedMagnitude = beforeDrink.getMagnitudeResultValue();
            long modifiedDuration = (long) beforeDrink.getDurationResultValue();

            if (modifiedMagnitude > 0 && modifiedDuration > 0) {
                h.applyEffect(item, instigator, modifiedMagnitude, modifiedDuration);
            }
        }


        //audioManager.playSound(Assets.getSound("engine:drink").get(), 1.0f);
    }

    @ReceiveEvent
    public void onPotionWithoutGenomeConsumed(DrinkPotionEvent event, EntityRef ref) {
        PotionComponent p = event.getPotionComponent();
        HerbEffect e = null;

        EntityRef item = event.getItem();

        // If there are no effects, just play the drink sound and return.
        if (p.effects.size() == 0) {
            audioManager.playSound(Assets.getSound("engine:drink").get(), 1.0f);
            return;
        }

        // Iterate through all effects of this potion and apply them.
        for (PotionEffect pEffect : p.effects) {
            e = null;

            // Figure out what specific effect this is and create a HerbEffect based on that.
            switch (pEffect.effect) {
                case PotionCommonEffects.HEAL:
                    e = new HealEffect();
                    break;
                case PotionCommonEffects.REGEN:
                    RegenerationAlterationEffect effect = new RegenerationAlterationEffect(context);
                    e = new AlterationEffectWrapperHerbEffect(effect, 1f, 1f);
                    break;
                case PotionCommonEffects.WALK_SPEED:
                    WalkSpeedAlterationEffect wsEffect = new WalkSpeedAlterationEffect(context);
                    e = new AlterationEffectWrapperHerbEffect(wsEffect, 1f, 1f);
                    break;
                case PotionCommonEffects.SWIM_SPEED:
                    SwimSpeedAlterationEffect ssEffect = new SwimSpeedAlterationEffect(context);
                    e = new AlterationEffectWrapperHerbEffect(ssEffect, 1f, 1f);
                    break;
                case PotionCommonEffects.JUMP_SPEED:
                    JumpSpeedAlterationEffect jsEffect = new JumpSpeedAlterationEffect(context);
                    e = new AlterationEffectWrapperHerbEffect(jsEffect, 1f, 1f);
                    break;
                default:
                    e = new DoNothingEffect();
                    break;
            }

            checkDrink(event.getInstigator(), event.getItem(), p, e, pEffect);
        }

        audioManager.playSound(Assets.getSound("engine:drink").get(), 1.0f);
    }

    // Consume a potion without a Genome attached to it. Usually predefined ones.
    @ReceiveEvent
    public void potionWithoutGenomeConsumed(ActivateEvent event, EntityRef item, PotionComponent potion) {
        PotionComponent p = item.getComponent(PotionComponent.class);
        event.getInstigator().send(new DrinkPotionEvent(p, event.getInstigator(), item));
    }
}
