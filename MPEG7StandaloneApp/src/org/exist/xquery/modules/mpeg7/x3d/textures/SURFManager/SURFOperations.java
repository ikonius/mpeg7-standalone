package org.exist.xquery.modules.mpeg7.x3d.textures.SURFManager;

import boofcv.abst.feature.detect.extract.ConfigExtract;
import boofcv.abst.feature.detect.extract.NonMaxSuppression;
import boofcv.abst.feature.orientation.OrientationIntegral;
import boofcv.alg.feature.describe.DescribePointSurf;
import boofcv.alg.feature.detect.interest.FastHessianFeatureDetector;
import boofcv.alg.transform.ii.GIntegralImageOps;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.factory.feature.describe.FactoryDescribePointAlgs;
import boofcv.factory.feature.detect.extract.FactoryFeatureExtractor;
import boofcv.factory.feature.orientation.FactoryOrientationAlgs;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.ScalePoint;
import boofcv.struct.feature.SurfFeature;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import org.exist.xquery.modules.mpeg7.x3d.helpers.ImageConversions;

/**
 *
 * @author Markos
 */
public class SURFOperations {

    public <II extends ImageSingleBand> List<SurfFeature> ParametrizedSURF(ImageFloat32 image) {
        // SURF works off of integral images
        Class<II> integralType = GIntegralImageOps.getIntegralType(ImageFloat32.class);

        // define the feature detection algorithm
        NonMaxSuppression extractor
                = FactoryFeatureExtractor.nonmax(new ConfigExtract(2, 0, 5, true));
        FastHessianFeatureDetector<II> detector
                = new FastHessianFeatureDetector<II>(extractor, 200, 2, 9, 4, 4);

        // estimate orientation
        OrientationIntegral<II> orientation
                = FactoryOrientationAlgs.sliding_ii(null, integralType);

        DescribePointSurf<II> descriptor = FactoryDescribePointAlgs.<II>surfStability(null, integralType);

        // compute the integral image of 'image'
        II integral = GeneralizedImageOps.createSingleBand(integralType, image.width, image.height);
        GIntegralImageOps.transform(image, integral);

        // detect fast hessian features
        detector.detect(integral);
        // tell algorithms which image to process
        orientation.setImage(integral);
        descriptor.setImage(integral);

        List<ScalePoint> points = detector.getFoundPoints();

        List<SurfFeature> descriptions = new ArrayList<SurfFeature>();

        for (ScalePoint p : points) {
            // estimate orientation
            orientation.setScale(p.scale);
            double angle = orientation.compute(p.x, p.y);

            // extract the SURF description for this region
            SurfFeature desc = descriptor.createDescription();
            descriptor.describe(p.x, p.y, angle, p.scale, desc);

            // save everything for processing later on
            descriptions.add(desc);
        }
        return (descriptions);
    }

    public Dataset extractFromFile(String FilenameString) {
        ImageFloat32 image = UtilImageIO.loadImage(FilenameString, ImageFloat32.class);
        //System.out.println("Loaded");
        List<SurfFeature> HardList = ParametrizedSURF(image);
        //System.out.print("Extracted: " + String.valueOf(HardList.size()) + " ");
        Dataset data = new DefaultDataset();
        //System.out.println(HardList.size());
        for (int ii = 0; ii < HardList.size(); ii++) {
            data.add(new DenseInstance(HardList.get(ii).getValue()));
        }
        return data;
    }
    public Dataset extractFromBuffer(BufferedImage bimg) {
        ImageFloat32 image = ImageConversions.loadImage(bimg, ImageFloat32.class);
        //System.out.println("Loaded");
        List<SurfFeature> HardList = ParametrizedSURF(image);
        //System.out.print("Extracted: " + String.valueOf(HardList.size()) + " ");
        Dataset data = new DefaultDataset();
        //System.out.println(HardList.size());
        for (int ii = 0; ii < HardList.size(); ii++) {
            data.add(new DenseInstance(HardList.get(ii).getValue()));
        }
        return data;
    }
    
}
