package org.exist.xquery.modules.mpeg7.x3d.textures;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.validator.UrlValidator;
import org.apache.log4j.Logger;
import org.exist.xquery.modules.mpeg7.x3d.colors.ScalableColorImpl;
import org.exist.xquery.modules.mpeg7.x3d.helpers.CommonUtils;
import org.exist.xquery.modules.mpeg7.x3d.textures.SURFManager.SURFDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Patti Spala <pd.spala@gmail.com>
 */
public class TextureDetector {

    private Document doc;
    String basePath;
    private HashMap<String, TextureParameters> textureParamMap;
    private HashMap<String, String> histograms;
    private HashMap<String, String> scalableColors;
    private HashMap<String, String> surfeatures;
    private static final Logger logger = Logger.getLogger(TextureDetector.class);

    public TextureDetector(Document doc, String basePath) throws XPathExpressionException, ParserConfigurationException, MalformedURLException, IOException {
        this.doc = doc;
        this.basePath = basePath;
        retrieveImageList();
        extractFeatures();

    }

    public Document getDoc() {
        return doc;
    }

    public void setDoc(Document doc) {
        this.doc = doc;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public HashMap<String, String> getHistograms() {
        return histograms;
    }

    public HashMap<String, String> getScalableColors() {
        return scalableColors;
    }
    
       public HashMap<String, String> getSURF() {
        return surfeatures;
    }

    private void retrieveImageList() throws XPathExpressionException, ParserConfigurationException, MalformedURLException {
        textureParamMap = new HashMap<String, TextureParameters>();

        ArrayList<String> textureUrls;
        XPath xPath = XPathFactory.newInstance().newXPath();
        this.getDoc().getDocumentElement().normalize();
        String xPathShapes = "//Shape[//ImageTexture]";
        NodeList shapeSet = (NodeList) xPath.compile(xPathShapes).evaluate(this.getDoc().getDocumentElement(), XPathConstants.NODESET);
        for (int s = 0; s < shapeSet.getLength(); s++) {
            Element shapeNode = (Element) shapeSet.item(s);

            //ImageTextures           
            textureUrls = new ArrayList<String>();
            String xPathImageTexture = "//ImageTexture";
            NodeList textureSet = (NodeList) xPath.compile(xPathImageTexture).evaluate(shapeNode, XPathConstants.NODESET);
            for (int p = 0; p < textureSet.getLength(); p++) {
                Element textureNode = (Element) textureSet.item(p);
                boolean hasUse = textureNode.hasAttribute("USE");
                if (hasUse) {
                    String def = textureNode.getAttribute("USE");
                    textureNode = (Element) xPath.compile("//ImageTexture[@DEF='" + def + "']").evaluate(this.getDoc().getDocumentElement(), XPathConstants.NODE);
                }
                boolean hasUrl = textureNode.hasAttribute("url");
                if (hasUrl) {
                    String urlParams[] = textureNode.getAttribute("url").replaceAll("\"", "").split(" ");
                    for (String urlParam : urlParams) {
                        UrlValidator urlValidator = new UrlValidator();
                        if (!urlValidator.isValid(urlParam)) {
                            textureUrls.add(basePath + "/" + urlParam);
                        } else {
                            URL url = new URL(urlParam);
                            textureUrls.add(url.toString());
                        }
                    }

                }
            }
            //TextureCoordinates
            String[] pointParts = null;
            String xPathTextureCoordinate = "//TextureCoordinate";
            NodeList coordinateSet = (NodeList) xPath.compile(xPathTextureCoordinate).evaluate(shapeNode, XPathConstants.NODESET);
            for (int p = 0; p < coordinateSet.getLength(); p++) {
                Element coordinateNode = (Element) coordinateSet.item(p);
                boolean hasUse = coordinateNode.hasAttribute("USE");
                if (hasUse) {
                    String def = coordinateNode.getAttribute("USE");
                    coordinateNode = (Element) xPath.compile("//TextureCoordinate[@DEF='" + def + "']").evaluate(this.getDoc().getDocumentElement(), XPathConstants.NODE);
                }
                boolean hasPoints = coordinateNode.hasAttribute("point");
                if (hasPoints) {
                    pointParts = new String[coordinateNode.getAttribute("point").split(" ").length];
                    pointParts = coordinateNode.getAttribute("point").split(" ");

                }
            }
            String def = shapeNode.hasAttribute("DEF") ? shapeNode.getAttribute("DEF") + "_" + s : String.valueOf(s);
            TextureParameters params = new TextureParameters(def, textureUrls, pointParts);
            textureParamMap.put(def, params);
        }

    }

    private void extractFeatures() {
        histograms = new HashMap<String, String>();
        scalableColors = new HashMap<String, String>();
        surfeatures = new HashMap<String, String>();
        StringBuilder textureHistogramsBuilder = new StringBuilder();
        StringBuilder textureScalableColorsBuilder = new StringBuilder();
        StringBuilder textureSURFBuilder = new StringBuilder();
        for (Map.Entry<String, TextureParameters> entry : textureParamMap.entrySet()) {

            TextureParameters params = entry.getValue();
            String shapeName = params.nodeName();
            ArrayList<String> textureUrls = params.urls();
            String[] points = params.points();
            int urlSize = textureUrls.size();
            for (int i = 0; i < urlSize; i++) {
                String textureUrl = textureUrls.get(i);
                try {
                    BufferedImage img = ImageIO.read(new URL(textureUrl.toString()));
                    //EHD
                    EdgeHistogramImplementation ehdi = new EdgeHistogramImplementation(img);
                    String bins = ehdi.getStringRepresentation().split(";")[1];
                    textureHistogramsBuilder.append(shapeName);
                    textureHistogramsBuilder.append(':');
                    textureHistogramsBuilder.append(bins);
                    if (i < urlSize) {
                        textureHistogramsBuilder.append('#');
                    }
                    //SCD
                    ScalableColorImpl scdi;
                    if (points == null) {
                        scdi = new ScalableColorImpl(img);
                    } else {
                        int[] pixels = getPixelsFromTextureCoordinates(points, img);
                        //logger.info(Arrays.toString(pixels));
                        scdi = new ScalableColorImpl(pixels);
                    }
                    String scdiBits = scdi.getStringRepresentation();
                    String scalableColorParts = scdiBits.substring(scdiBits.indexOf(";") + 1, scdiBits.length());
                    textureScalableColorsBuilder.append(shapeName);
                    textureScalableColorsBuilder.append(":");
                    textureScalableColorsBuilder.append(scalableColorParts);
                    if (i < urlSize) {
                        textureScalableColorsBuilder.append("#");
                    }
                    //SURF
                    SURFDescriptor surf = new SURFDescriptor(img, 64);
                    String histogram = surf.getStringRepresentation();
                    
                    textureSURFBuilder.append(shapeName);
                    textureSURFBuilder.append(':');
                    textureSURFBuilder.append(histogram);
                    if (i < urlSize) {
                        textureSURFBuilder.append('#');
                    }
                    break;
                } catch (IOException ex) {
//                    logger.warn(ex);
                    System.out.println(ex);
                }
            }

        }
        //logger.info(textureSURFBuilder.toString());
        histograms.put("EHDs", textureHistogramsBuilder.toString());
        scalableColors.put("SCDs", textureScalableColorsBuilder.toString());
        surfeatures.put("SURF", textureSURFBuilder.toString());
        //CommonUtils.printMap(histograms);        

    }

    private int[] getPixelsFromTextureCoordinates(String[] textureCoordinates, BufferedImage img) {
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();
        List<STCoords> stValues = new ArrayList<STCoords>();
        List<XYCoords> invXYValues = new ArrayList<XYCoords>();
        List<XYCoords> xyCoords = new ArrayList<XYCoords>();
        List<int[]> pixels = new ArrayList<int[]>();
        for (int i = 0; i < textureCoordinates.length / 2; i++) {
            Float s = Math.abs(Float.valueOf(textureCoordinates[2 * i]));
            Float t = Math.abs(Float.valueOf(textureCoordinates[2 * i + 1]));
            int x = Math.round(s * imgWidth);
            int _y = Math.round(t * imgHeight);
            int y = imgHeight - _y;
            //edge correction for neighbourhood detection
            if (x >= imgWidth) {
                x = imgWidth - 1;
            }
            if (y >= imgHeight) {
                y = imgHeight - 1;
            }
            STCoords coord = new STCoords(s, t);
            stValues.add(coord);
            XYCoords _xycoord = new XYCoords(x, _y);
            invXYValues.add(_xycoord);
            XYCoords xycoord = new XYCoords(x, y);
            xyCoords.add(xycoord);

        }
        for (int i = 0; i < xyCoords.size(); i++) {
            //logger.info(i + "--> x: " + (xyCoords.get(i)).x() + " y: " + (xyCoords.get(i)).y());
            pixels.add(getPixelData(img, (xyCoords.get(i)).x(), (xyCoords.get(i)).y()));
        }
        return CommonUtils.toIntArray(pixels);
    }

    private static int[] getPixelData(BufferedImage img, int x, int y) {
        int argb = img.getRGB(x, y);

        int rgb[] = new int[]{
            (argb >> 16) & 0xff, //red
            (argb >> 8) & 0xff, //green
            (argb) & 0xff //blue
        };
        return rgb;
    }

}

class STCoords {

    private final Float s;
    private final Float t;

    public STCoords(Float xin, Float yin) {
        s = xin;
        t = yin;
    }

    public Float s() {
        return s;
    }

    public Float t() {
        return t;
    }

}

class XYCoords {

    private final Integer x;
    private final Integer y;

    public XYCoords(Integer xin, Integer yin) {
        x = xin;
        y = yin;
    }

    public Integer x() {
        return x;
    }

    public Integer y() {
        return y;
    }

}

class TextureParameters {

    private final String nodeName;
    private final ArrayList<String> urls;
    private final String[] points;

    public TextureParameters(String name, ArrayList<String> textureUrls, String[] pointSet) {
        this.nodeName = name;
        this.urls = textureUrls;
        this.points = pointSet;
    }

    public String nodeName() {
        return this.nodeName;
    }

    public ArrayList<String> urls() {
        return this.urls;
    }

    public String[] points() {
        return this.points;
    }
}
