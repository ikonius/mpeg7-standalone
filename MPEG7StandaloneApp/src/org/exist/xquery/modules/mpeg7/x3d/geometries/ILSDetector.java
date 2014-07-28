package org.exist.xquery.modules.mpeg7.x3d.geometries;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Patti Spala <pd.spala@gmail.com>
 */
public class ILSDetector extends GeometryDetector {

    private static final Logger logger = Logger.getLogger(ILSDetector.class);

    public ILSDetector(Document doc) throws IOException, XPathExpressionException {
        super(doc);
        processShapes();
    }

    @Override
    public void processShapes() throws IOException, XPathExpressionException {
        String[] coordIndexArray = null;

        List totalPointParts = null;
        ArrayList<String> resultedIFSExtractionList = new ArrayList<String>();
        ArrayList<String> resultedSGDExtractionList = new ArrayList<String>();
        StringBuilder IFSStringBuilder = new StringBuilder();
        StringBuilder SGDStringBuilder = new StringBuilder();
        XPath xPath = XPathFactory.newInstance().newXPath();
        this.getDoc().getDocumentElement().normalize();
        NodeList ifsSet = (NodeList) xPath.evaluate("//Shape/IndexedLineSet", this.getDoc().getDocumentElement(), XPathConstants.NODESET);
        for (int p = 0; p < ifsSet.getLength(); p++) {

            HashMap<String, String[]> coordDictParams = new HashMap<String, String[]>();
            HashMap<String, String[]> pointDictParams = new HashMap<String, String[]>();

            Element elem = (Element) ifsSet.item(p);
            // case: <IndexedFaceSet USE='SardineIndex'/>
            boolean isUseIFS = elem.hasAttribute("USE");
            if (isUseIFS) {
                String def = elem.getAttribute("USE");
                elem = (Element) xPath.compile("//Shape/IndexedLineSet[@DEF='" + def + "']").evaluate(this.getDoc().getDocumentElement(), XPathConstants.NODE);
            }
            String coordIndex = null;
            if (elem.hasAttribute("coordIndex")) {
                coordIndex = elem.getAttribute("coordIndex");
            }

            Element coordinate = (Element) elem.getElementsByTagName("Coordinate").item(0);
            // case:
            // <IndexedFaceSet coordIndex='3 2 5 4 -1 12 13 14 15 -1 21 3 4 22 -1 13 26 27 14 -1 31 21 22 32 -1 26 36 37 27 -1 41 31 32 42 -1 36 46 47 37 -1'>
            //      <Coordinate USE='WindowCoordinates'/>
            // </IndexedFaceSet>
            boolean isUseCoordinate = coordinate.hasAttribute("USE");
            if (isUseCoordinate) {
                String defCoord = coordinate.getAttribute("USE");
                //case:
                // <IndexedFaceSet>
                //      <Coordinate USE='WallCoordinates'/>
                // </IndexedFaceSet>
                if (coordIndex == null) {
                    elem = (Element) xPath.compile("//Shape/IndexedLineSet[Coordinate[@DEF='" + defCoord + "']]").evaluate(this.getDoc().getDocumentElement(), XPathConstants.NODE);
                    coordIndex = elem.getAttribute("coordIndex");
                } else {
                    coordinate = (Element) xPath.compile("//IndexedLineSet/Coordinate[@DEF='" + defCoord + "']").evaluate(this.getDoc().getDocumentElement(), XPathConstants.NODE);
                }
            }
            String points = coordinate.getAttribute("point").replaceAll("\\r|\\n", " ").trim().replaceAll(" +", " ").replaceAll(",", "");
            coordIndex = coordIndex.replaceAll("\\r|\\n", " ").trim().replaceAll(" +", " ").replaceAll(",", "");
            Scanner sc = new Scanner(coordIndex).useDelimiter(" ");
            int maxCoordIndex = 0;
            while (sc.hasNextInt()) {
                int thisVal = sc.nextInt();
                if (thisVal > maxCoordIndex) {
                    maxCoordIndex = thisVal;
                }
            }

            totalPointParts = new ArrayList();
            totalPointParts = getPointParts(points, maxCoordIndex);
            coordIndex = coordIndex.replaceAll(",", "");
            if (coordIndex.contains("-1")) {
                coordIndex = coordIndex.substring(0, coordIndex.lastIndexOf("-1"));
                coordIndexArray = coordIndex.split(" -1 ");
            } else {
                coordIndexArray = new String[1];
                coordIndexArray[0] = coordIndex;
            }

            coordDictParams.put("coordIndex", coordIndexArray);
            pointDictParams.put("points", (String[]) totalPointParts.toArray(new String[0]));
            String coordTempFile = writeParamsToFile(coordDictParams, new File("coordIndex.txt"), this.getWriter());
            String pointTempFile = writeParamsToFile(pointDictParams, new File("point.txt"), this.getWriter());
            String[] tempFiles = {coordTempFile, pointTempFile};
            String resultedExtraction = ShapeIndexExtraction.shapeIndexEctraction(tempFiles);
            resultedIFSExtractionList.add(resultedExtraction);

            //ShapeGoogleExtraction
            int vocabularySize = 32;
            ShapeGoogleExtraction shExtraction = new ShapeGoogleExtraction(points, coordIndex, vocabularySize);
            resultedSGDExtractionList.add(shExtraction.getStringRepresentation());
        }
        for (int i = 0; i < resultedIFSExtractionList.size(); i++) {
            IFSStringBuilder.append(resultedIFSExtractionList.get(i));
            IFSStringBuilder.append("#");
        }
        if (IFSStringBuilder.length() > 0) {
            this.getParamMap().put("ILSPointsExtraction", IFSStringBuilder.toString());
        }

        for (int i = 0; i < resultedSGDExtractionList.size(); i++) {
            SGDStringBuilder.append(resultedSGDExtractionList.get(i));
            SGDStringBuilder.append("#");
        }
        if (SGDStringBuilder.length() > 0) {
            this.getParamMap().put("SGD", SGDStringBuilder.toString());
        }
    }

    @Override
    public String writeParamsToFile(HashMap<String, String[]> dictMap, File file, BufferedWriter writer) throws IOException {
        // if file doesnt exists, then create it
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        this.setWriter(new BufferedWriter(new FileWriter(file)));

        Set set = dictMap.entrySet();
        Iterator i = set.iterator();
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            for (String data : (String[]) me.getValue()) {
                this.getWriter().write(data);
                this.getWriter().newLine();
            }
        }
        this.getWriter().close();

        return file.getAbsolutePath();
    }

    private List getPointParts(String points, int indexSize) {
        List pointParts = new ArrayList();
        Scanner scannedPoints = new Scanner(points).useDelimiter(" ");

        int totalPointIndex = 3 * (indexSize + 1);
        int index = 0;
        double[] floats = new double[totalPointIndex];

        while ((scannedPoints.hasNextDouble()) && (index < floats.length)) {
            double fl = scannedPoints.nextDouble();
            floats[index] = fl;
            index++;
        }
        for (int i = 0; i < floats.length; i += 3) {
            String nextPart = String.valueOf(floats[i]) + " " + String.valueOf(floats[i + 1]) + " " + String.valueOf(floats[i + 2]);
            pointParts.add(nextPart);
        }
        return pointParts;
    }

}
