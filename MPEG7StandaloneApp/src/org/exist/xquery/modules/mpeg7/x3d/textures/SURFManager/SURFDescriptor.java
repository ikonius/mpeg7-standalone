package org.exist.xquery.modules.mpeg7.x3d.textures.SURFManager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.distance.EuclideanDistance;
import net.sf.javaml.tools.data.FileHandler;
import org.exist.xquery.modules.mpeg7.x3d.helpers.CommonUtils;

/**
 *
 * @author Markos | Patti Spala <pd.spala@gmail.com>
 */
public class SURFDescriptor {

    private final int numberOfClusters;
    private int[] ImageHist;
    BufferedImage image;

    public SURFDescriptor(BufferedImage image, int numberOfClusters) throws IOException {
        this.image = image;
        this.numberOfClusters = numberOfClusters;
        extract();
    }

    protected void extract() throws IOException {
        DistanceMeasure dm = new EuclideanDistance();
        Dataset Vocab;
        SURFOperations Extractor = new SURFOperations();
        Dataset data = Extractor.extractFromBuffer(image);

        String fileName = "Vocab" + String.valueOf(numberOfClusters) + ".iVocab";
        URL link = new URL("http://54.72.206.163/exist/3dData/Vocabularies/SURF/" + fileName);
        CommonUtils.downloadFile(fileName, link);
        File vocabFile = new File(fileName);

        Vocab = FileHandler.loadDataset(vocabFile);

        this.ImageHist = new int[numberOfClusters];
        for (int i = 0; i < data.size(); i++) {

            int tmpCluster = 0;
            double minDistance = dm.measure(Vocab.get(0), data.instance(i));
            for (int j = 1; j < Vocab.size(); j++) {
                double dist = dm.measure(Vocab.get(j), data.instance(i));
                if (dm.compare(dist, minDistance)) {
                    minDistance = dist;
                    tmpCluster = j;
                }
            }
            this.ImageHist[tmpCluster] += 1;
        }

//        for (int ii = 0; ii < ImageHist.length; ii++) {
//            System.out.print(String.valueOf(ImageHist[ii]) + " ");
//        }
//        System.out.println();
//        System.out.println("Done!");
        //return ImageHist;
    }

    public int clusterSize() {
        return this.numberOfClusters;
    }

    public String getStringRepresentation() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.numberOfClusters);
        stringBuilder.append(";");
        stringBuilder.append(this.ImageHist[0]);
        for (int i = 1; i < this.ImageHist.length; i++) {
            stringBuilder.append(' ');
            stringBuilder.append(this.ImageHist[i]);
        }
        return stringBuilder.toString();
    }

}
