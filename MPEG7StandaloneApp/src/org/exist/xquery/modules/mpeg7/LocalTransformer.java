package org.exist.xquery.modules.mpeg7;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.io.FileUtils;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import org.exist.xquery.modules.mpeg7.storage.helpers.X3DResourceDetail;
import org.exist.xquery.modules.mpeg7.transformation.MP7Generator;
import org.exist.xquery.modules.mpeg7.x3d.geometries.ExtrusionDetector;
import org.exist.xquery.modules.mpeg7.x3d.geometries.IFSDetector;
import org.exist.xquery.modules.mpeg7.x3d.geometries.ILSDetector;
import org.exist.xquery.modules.mpeg7.x3d.geometries.InlineDetector;
import org.exist.xquery.modules.mpeg7.x3d.textures.TextureDetector;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xmldb.api.base.XMLDBException;

/**
 *
 * @author Patti Spala <pd.spala@gmail.com>
 */
public class LocalTransformer {

    protected static String inputX3DPath, inputX3DFile, outputPath;

    public static void main(String[] args) {
        if (args.length > 2) {
            inputX3DPath = args[0];
            inputX3DFile = args[1];
            outputPath = args[2];
        } else {
            inputX3DPath = "E:\\X3D\\content\\ToUpload\\Savage\\AircraftFixedWing\\AV8B-Harrier-UnitedStates\\";//E:\\X3D\\content\\ToUpload\\";
            inputX3DFile = "AV8bHarrier.x3d";
            outputPath = ""; //IF LEFT BLANK IT WILL BE STORED IN THE PROJECT CLASSPATH!!!!! - Otherwise use something like: //"E:\\X3D\\content\\examples\\Vrml2.0Sourcebook\\Chapter13-PointsLinesFaces\\";
        }
        try {
         
            X3DResourceDetail resource = new X3DResourceDetail(removeExtension(inputX3DFile), inputX3DFile, inputX3DPath);

            String xslSource = "mpeg7_annotation.xsl";
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            //logger.debug("Processing file: " + resource.resourceFileName); //debugging
            File x3dSource = new File(inputX3DPath + inputX3DFile);
            Document doc = builder.parse(new ByteArrayInputStream(FileUtils.readFileToByteArray(x3dSource)));
            InlineDetector inlDetector = new InlineDetector(doc, resource.parentPath);
            doc = inlDetector.retrieveInlineNodes();

            /*debugging
             StringWriter sw = new StringWriter();
             TransformerFactory tf = TransformerFactory.newInstance();
             Transformer transformer = tf.newTransformer();
             transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
             transformer.setOutputProperty(OutputKeys.METHOD, "xml");
             transformer.setOutputProperty(OutputKeys.INDENT, "yes");
             transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
             transformer.transform(new DOMSource(doc), new StreamResult(sw));
             logger.info(sw.toString());
                    
             end debugging*/
            IFSDetector ifsDetector = new IFSDetector(doc);
            //ifsDetector.processShapes();
            ILSDetector ilsDetector = new ILSDetector(doc);
            //ilsDetector.processShapes();
            ExtrusionDetector extrusionDetector = new ExtrusionDetector(doc);
            //extrusionDetector.processShapes(); 
            TextureDetector textureDetector = new TextureDetector(doc, resource.parentPath);
            MP7Generator mp7Generator = new MP7Generator(resource, outputPath, ilsDetector.getParamMap(), ifsDetector.getParamMap(), extrusionDetector.getParamMap(), textureDetector.getHistograms(), textureDetector.getScalableColors(), textureDetector.getSURF(), xslSource);
            mp7Generator.generateDescription();

            //logger.debug("No of MPEG-7 files: " + mpeg7counter); //debugging
        } catch (ParserConfigurationException ex) {
            System.out.println("Exception: " + ex);
        } catch (IOException ex) {
            System.out.println("Exception: " + ex);
        } catch (SAXException ex) {
            System.out.println("Exception: " + ex);
        } catch (XPathExpressionException ex) {
            System.out.println("Exception: " + ex);
        } catch (XMLDBException ex) {
            System.out.println("Exception: " + ex);
        } catch (InstantiationException ex) {
            System.out.println("Exception: " + ex);
        } catch (IllegalAccessException ex) {
            System.out.println("Exception: " + ex);
        } catch (ClassNotFoundException ex) {
            System.out.println("Exception: " + ex);
        }

    }

}
