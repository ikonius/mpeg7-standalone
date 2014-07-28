package org.exist.xquery.modules.mpeg7.x3d.geometries;

import org.exist.xquery.modules.mpeg7.x3d.filters.ExtrusionToIFSFilter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
public class ExtrusionDetector extends GeometryDetector {

    private static final Logger logger = Logger.getLogger(ExtrusionDetector.class);

    public ExtrusionDetector(Document doc) throws XPathExpressionException, IOException {
        super(doc);
        processShapes();
    }

    @Override
    public void processShapes() throws XPathExpressionException, IOException {
        String crossSection, spine, scale, orientation;
        String solid, ccw, convex, beginCap, endCap, creaseAngle;
        ArrayList<String> resultedExtrExtractionList = new ArrayList<String>();
        ArrayList<String> resultedExtrBBoxList = new ArrayList<String>();
        ArrayList<String> resultedSGDExtractionList = new ArrayList<String>();
        StringBuilder ExtrShapeStringBuilder = new StringBuilder();
        StringBuilder ExtrBBoxStringBuilder = new StringBuilder();
        StringBuilder SGDStringBuilder = new StringBuilder();

        this.getDoc().getDocumentElement().normalize();
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList extrSet = (NodeList) xPath.evaluate("//Shape/Extrusion", this.getDoc().getDocumentElement(), XPathConstants.NODESET);

        for (int p = 0; p < extrSet.getLength(); p++) {
            Element elem = (Element) extrSet.item(p);
            boolean isUse = elem.hasAttribute("USE");
            if (isUse) {
                String def = elem.getAttribute("USE");
                elem = (Element) xPath.compile("//Shape/Extrusion[@DEF='" + def + "']").evaluate(this.getDoc().getDocumentElement(), XPathConstants.NODE);
            }
            HashMap<String, String[]> dictParams = new HashMap<String, String[]>();
            if (elem.hasAttribute("spine")) {
                spine = elem.getAttribute("spine");
            } else {
                //default Extrusion spine='0 0 0 0 1 0'
                spine = "0 0 0 0 1 0";

            }
            if (elem.hasAttribute("crossSection")) {
                crossSection = elem.getAttribute("crossSection");
            } else {
                //default Extrusion crossSection='1 1 1 -1 -1 -1 -1 1 1 1' is a square
                crossSection = "1 1 1 -1 -1 -1 -1 1 1 1";

            }
            if (elem.hasAttribute("scale")) {
                scale = elem.getAttribute("scale");
            } else {
                //default Extrusion scale='1 1'
                scale = "1 1";
            }
            if (elem.hasAttribute("orientation")) {
                orientation = elem.getAttribute("orientation");
            } else {
                //default Extrusion orientation='0 0 1 0'
                orientation = "0 0 1 0";
            }

            if (elem.hasAttribute("solid")) {
                solid = elem.getAttribute("solid");
            } else {
                //default Extrusion solid='true'
                solid = "true";
            }

            if (elem.hasAttribute("convex")) {
                convex = elem.getAttribute("convex");
            } else {
                //default Extrusion convex='true'
                convex = "true";
            }

            if (elem.hasAttribute("ccw")) {
                ccw = elem.getAttribute("ccw");
            } else {
                //default Extrusion ccw='true'
                ccw = "true";
            }

            if (elem.hasAttribute("beginCap")) {
                beginCap = elem.getAttribute("beginCap");
            } else {
                //default Extrusion beginCap='true'
                beginCap = "true";
            }

            if (elem.hasAttribute("endCap")) {
                endCap = elem.getAttribute("endCap");
            } else {
                //default Extrusion endCap='true'
                endCap = "true";
            }

            if (elem.hasAttribute("creaseAngle")) {
                creaseAngle = elem.getAttribute("creaseAngle");
            } else {
                //default Extrusion creaseAngle='0'
                creaseAngle = "0";
            }
            dictParams.put("scale", new String[]{scale});
            dictParams.put("crossSection", new String[]{crossSection});
            dictParams.put("spine", new String[]{spine});
            dictParams.put("orientation", new String[]{orientation});
            dictParams.put("solid", new String[]{solid});
            dictParams.put("convex", new String[]{convex});
            dictParams.put("ccw", new String[]{ccw});
            dictParams.put("beginCap", new String[]{beginCap});
            dictParams.put("endCap", new String[]{endCap});
            dictParams.put("creaseAngle", new String[]{creaseAngle});

            this.setFile(new File("Extrusion.txt"));

            String coordTempFile = writeParamsToFile(dictParams, this.getFile(), this.getWriter());

            String[] ExtrTempFile = {coordTempFile};
            ExtrusionToIFSFilter ifsFilter = new ExtrusionToIFSFilter(ExtrTempFile);
            String resultedExtraction = ifsFilter.filterGeometry();

            String ExtrBBox = resultedExtraction.substring(0, resultedExtraction.indexOf("&") - 1);
            String ExtrShape = resultedExtraction.substring(resultedExtraction.indexOf("&") + 1);

            resultedExtrBBoxList.add(ExtrBBox);
            resultedExtrExtractionList.add(ExtrShape);

            int vocabularySize = 32;
            ShapeGoogleExtraction shExtraction = new ShapeGoogleExtraction(ifsFilter.getIFSTranslatedData()[0], ifsFilter.getIFSTranslatedData()[1], vocabularySize);
            resultedSGDExtractionList.add(shExtraction.getStringRepresentation());

        }
        for (int i = 0; i < resultedExtrExtractionList.size(); i++) {
            ExtrShapeStringBuilder.append(resultedExtrExtractionList.get(i));
            ExtrShapeStringBuilder.append("#");

            ExtrBBoxStringBuilder.append(resultedExtrBBoxList.get(i));
            ExtrBBoxStringBuilder.append("#");

        }
        if (ExtrShapeStringBuilder.length() > 0) {
            this.getParamMap().put("extrusionPointsExtraction", ExtrShapeStringBuilder.toString());
            this.getParamMap().put("extrusionBBoxParams", ExtrBBoxStringBuilder.toString());
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
    public String writeParamsToFile(HashMap<String, String[]> dictMap, File file, BufferedWriter get) throws IOException {

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
            this.getWriter().write(me.getKey().toString());
            this.getWriter().newLine();
            String value = dictMap.get(me.getKey())[0];
            this.getWriter().write(value);
            this.getWriter().newLine();
        }
        this.getWriter().close();

        return file.getAbsolutePath();
    }

}
