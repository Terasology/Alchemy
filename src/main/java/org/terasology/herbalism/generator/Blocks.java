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
     * @param blockId   ID of the block.
     * @return          The block type with the matching ID.
     */
    public static Block getBlock(String blockId) {
        return CoreRegistry.get(BlockManager.class).getBlock(blockId);
    }
}
