package org.exist.xquery.modules.mpeg7.x3d.geometries;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.FileUtils;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import org.apache.commons.validator.UrlValidator;
import org.exist.xquery.modules.mpeg7.storage.helpers.X3DResourceDetail;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmldb.api.base.XMLDBException;

/**
 *
 * @author Patti Spala <pd.spala@gmail.com>
 */
public class InlineDetector {

    private Document doc;
    String basePath;
    //private static final Logger logger = Logger.getLogger(InlineDetector.class);

    public InlineDetector(Document doc, String basePath) {
        this.doc = doc;
        this.basePath = basePath;
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

    public Document retrieveInlineNodes() throws XPathExpressionException, MalformedURLException, XMLDBException, ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        this.getDoc().getDocumentElement().normalize();
        String xPathInline = "//Inline";
        NodeList inlineSet = (NodeList) xPath.compile(xPathInline).evaluate(this.getDoc().getDocumentElement(), XPathConstants.NODESET);
        for (int p = 0; p < inlineSet.getLength(); p++) {
            Element inlineNode = (Element) inlineSet.item(p);
            Element inlineNodeRef;
            boolean hasUse = inlineNode.hasAttribute("USE");
            boolean hasUrl = inlineNode.hasAttribute("url");
            if (hasUrl) {
                String urlParams[] = inlineNode.getAttribute("url").replaceAll("\"", "").split(" ");
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document inlineDoc = null;
                for (String urlParam : urlParams) {
                    UrlValidator urlValidator = new UrlValidator();
                    if (!urlValidator.isValid(urlParam)) {
                        urlParam = X3DResourceDetail.resolveExistUri(basePath, urlParam);
                        X3DResourceDetail resource = new X3DResourceDetail(removeExtension(urlParam), urlParam, basePath);                       
                        try {
                            File x3dSource = new File(basePath + urlParam);
                            inlineDoc = builder.parse(new ByteArrayInputStream(FileUtils.readFileToByteArray(x3dSource)));
                        } catch (NullPointerException e) {
                            //file not found (maybe old path or not uploaded)
                            continue;
                        }
                        break;
                    } else {
                        URL url = new URL(urlParam);
                        URLConnection connection = url.openConnection();
                        inlineDoc = builder.parse(connection.getInputStream());
                        break;
                    }
                }
                String xPathScene = "/X3D/Scene/*";
                NodeList ilsSceneList = (NodeList) xPath.compile(xPathScene).evaluate(inlineDoc.getDocumentElement(), XPathConstants.NODESET);
                Element inlineScene = (Element) ilsSceneList.item(ilsSceneList.getLength() - 1);
                inlineScene.setAttribute("DEF", inlineNode.getAttribute("DEF"));
                Element parentElement = (Element) inlineNode.getParentNode();
                Node importedNode = this.doc.importNode(inlineScene, true);
                parentElement.replaceChild(importedNode, inlineNode);
                parentElement.normalize();
                this.setDoc(doc);
            }
            if (hasUse) {
                String def = inlineNode.getAttribute("USE");
                inlineNodeRef = (Element) xPath.compile("//*[@DEF='" + def + "']").evaluate(this.getDoc().getDocumentElement(), XPathConstants.NODE);
                Element copiedNodeFromRef = (Element) inlineNodeRef.cloneNode(true);
                Element parentElement = (Element) inlineNode.getParentNode();
                parentElement.replaceChild(copiedNodeFromRef, inlineNode);
                parentElement.normalize();
                this.setDoc(doc);
            }
        }
        //if there are still <Inline> nodes in the resulting document, call the same function recursively until they are all extracted.
        inlineSet = (NodeList) xPath.compile(xPathInline).evaluate(this.getDoc().getDocumentElement(), XPathConstants.NODESET);
        if (inlineSet.getLength() > 0) {
            this.retrieveInlineNodes();
        }
        return this.getDoc();
    }

}
