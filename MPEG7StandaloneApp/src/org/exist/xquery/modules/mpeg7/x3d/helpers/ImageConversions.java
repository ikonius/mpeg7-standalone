/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.exist.xquery.modules.mpeg7.x3d.helpers;

import boofcv.core.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.ImageSingleBand;
import java.awt.image.BufferedImage;

/**
 *
 * @author Patti Spala <pd.spala@gmail.com>
 */
public class ImageConversions extends UtilImageIO {

    /**
     * Loads the image and converts into the specified image type.
     *
     * @param img Path to image file.
     * @param imageType Type of image that should be returned.
     * @return The image or null if the image could not be loaded.
     */
    public static <T extends ImageSingleBand> T loadImage(BufferedImage img, Class<T> imageType) {
        if (img == null) {
            return null;
        }

        return ConvertBufferedImage.convertFromSingle(img, (T) null, imageType);
    }

}
