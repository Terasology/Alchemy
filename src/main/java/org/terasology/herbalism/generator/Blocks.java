// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.herbalism.generator;

import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;

/**
 * A utility class used for simplifying getting blocks from the BlockManager.
 */
public final class Blocks {
    private Blocks() {
    }

    /**
     * Gets a block from the BlockManager using the blockID.
     *
     * @param blockId ID of the block.
     * @return The block type with the matching ID.
     */
    public static Block getBlock(String blockId) {
        return CoreRegistry.get(BlockManager.class).getBlock(blockId);
    }
}