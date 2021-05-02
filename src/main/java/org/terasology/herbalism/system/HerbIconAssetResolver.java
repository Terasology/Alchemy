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

import com.google.common.collect.ImmutableSet;
import org.terasology.gestalt.assets.AssetDataProducer;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetDataProducer;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureData;
import org.terasology.engine.rendering.assets.texture.TextureRegionAsset;
import org.terasology.engine.rendering.assets.texture.TextureUtil;
import org.terasology.gestalt.naming.Name;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * This class is used to provide asset data for the creation of herb icons.
 */
@RegisterAssetDataProducer
public class HerbIconAssetResolver implements AssetDataProducer<TextureData>  {
    /** Constant used for storing the name of this module. */
    private static final Name HERBALISM_MODULE = new Name("herbalism");

    /** AssetManager which will be used to manage the herb assets. */
    private AssetManager assetManager;

    /**
     * Create a new instance of this class using an instance of AssetManager to later work with assets.
     *
     * @param assetManager  Instance of an asset manager that will be used to interface with multiple assets.
     */
    public HerbIconAssetResolver(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    /**
     * Create a URI for this herb using the given iconURI and the herb hue values.
     *
     * @param iconUri       String containing the herb icon's URI.
     * @param hueValues     Float array containing the herb colors.
     * @return              URI for the herb icon containing both the base URI and all of the hue values.
     */
    public static String getHerbUri(String iconUri, float[] hueValues) {
        StringBuilder sb = new StringBuilder();
        sb.append("Alchemy:Herb(");
        sb.append(iconUri);
        for (float hueValue : hueValues) {
            sb.append(",").append(String.valueOf(hueValue));
        }
        sb.append(")");

        return sb.toString();
    }

    /**
     * Get the available set of resource URNs that this AssetDataProducer can provide data for. See parent interface
     * for the full description.
     *
     * @return An empty set - meaning this producer cannot provide data for any resource URN.
     */
    @Override
    public Set<ResourceUrn> getAvailableAssetUrns() {
        return Collections.emptySet();
    }

    /**
     * The names of modules for which this producer can produce asset data with the given resource name for. In this
     * case, if the resource name starts with herb, the Herbalism module is returned.
     *
     * @param resourceName  The name of a resource.
     * @return              A set containing either nothing, or only the Herbalism module, which contains the resource.
     */
    @Override
    public Set<Name> getModulesProviding(Name resourceName) {
        if (!resourceName.toLowerCase().startsWith("herb(")) {
            return Collections.emptySet();
        }
        return ImmutableSet.of(HERBALISM_MODULE);
    }

    /**
     * See interface for full description.
     * For herb icons, URN redirects will be not be allowed.
     *
     * @param urn   The URN to redirect.
     * @return      The original URN.
     */
    @Override
    public ResourceUrn redirect(ResourceUrn urn) {
        return urn;
    }

    /**
     * Get the asset data for this herb from this URN. Specifically, the herb icon image with the herb hues applied.
     *
     * @param urn           The URN to get AssetData from.
     * @return              An optional with the herb TextureData, if available.
     * @throws IOException  If there is an error producing the AssetData.
     */
    @Override
    public Optional<TextureData> getAssetData(ResourceUrn urn) throws IOException {
        // Get the asset name.
        final String assetName = urn.getResourceName().toString().toLowerCase();

        // Only contain if the URN's module name begins with Herb. Otherwise, we know that it's an unrelated module and
        // therefore, an empty optional should be returned.
        if (!HERBALISM_MODULE.equals(urn.getModuleName())
                || !assetName.startsWith("herb(")) {
            return Optional.empty();
        }

        // Split the asset name to get parameters.
        String[] split = assetName.split("\\(");

        // From this split String, get the parameters and their values.
        String parameters = split[1].substring(0, split[1].length() - 1);
        String[] parameterValues = parameters.split(",");
        String textureResourceUri = parameterValues[0];

        // Get the resource texture (i.e., the herb icon) using the textureResourceUri, and convert the loaded texture
        // to an image file.
        Optional<TextureRegionAsset> resourceImageAsset = assetManager.getAsset(textureResourceUri,
                TextureRegionAsset.class);
        BufferedImage resourceImage = TextureUtil.convertToImage(resourceImageAsset.get());
        int imageSize = resourceImage.getHeight();

        // Calculate the frame count.
        int frameCount = resourceImage.getWidth() / imageSize;

        // If the frame count is not equal to the specified value in the parameter list, return an optional empty.
        if (frameCount != parameterValues.length - 1) {
            return Optional.empty();
        }

        // Create a blank square BufferedImage in the int ARGB format. This will be the finalized image.
        BufferedImage resultImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);

        float[] hsv = new float[3];

        // Apply each herb hue to each pixel of the resourceImage to create the resultImage.
        for (int i = 0; i < frameCount; i++) {
            float hue = Float.parseFloat(parameterValues[i + 1]);
            for (int y = 0; y < imageSize; y++) {
                for (int x = 0; x < imageSize; x++) {
                    int argb = resourceImage.getRGB(x + i * imageSize, y);
                    int a = (argb >> 24) & 0xFF;
                    if (a > 0) {
                        Color.RGBtoHSB((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, hsv);

                        int resultRgb = Color.HSBtoRGB(hue, hsv[1], hsv[2]);
                        int resultArgb = (a << 24) | (resultRgb & 0x00FFFFFF);
                        resultImage.setRGB(x, y, resultArgb);
                    }
                }
            }
        }

        // Convert the result image to a byte buffer.
        final ByteBuffer byteBuffer = TextureUtil.convertToByteBuffer(resultImage);

        // Place the result image data into a texture data format, and return it in an Optional wrapper.
        return Optional.of(new TextureData(resultImage.getWidth(), resultImage.getHeight(),
                new ByteBuffer[]{byteBuffer}, Texture.WrapMode.REPEAT, Texture.FilterMode.NEAREST));
    }
}
