// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism;

import com.google.common.base.Predicate;
import org.terasology.genome.breed.mutator.VocabularyGeneMutator;

/**
 * A genome mutator for herbs.
 */
public class HerbGeneMutator extends VocabularyGeneMutator {
    /**
     * Create the gene mutator.
     */
    public HerbGeneMutator() {
        super("ABCD",
                new Predicate<Integer>() {
                    @Override
                    public boolean apply(Integer input) {
                        return input != 0;
                    }
                });
    }
}
