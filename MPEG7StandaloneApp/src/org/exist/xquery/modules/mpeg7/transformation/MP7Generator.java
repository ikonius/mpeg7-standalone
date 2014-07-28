package org.exist.xquery.modules.mpeg7.transformation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.FileUtils;
import org.exist.xquery.modules.mpeg7.validation.Validator;
import org.apache.log4j.Logger;
import org.exist.xquery.modules.mpeg7.storage.helpers.X3DResourceDetail;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Patti Spala <pd.spala@gmail.com>
 */
public class MP7Generator {

    private static final Logger logger = Logger.getLogger(MP7Generator.class);
    protected X3DResourceDetail x3dResource;
    protected HashMap<String, String> extrusionParamMap, ifsParamMap, ilsParamMap, histograms, scalableColors, surfeatures;

    protected Transformer transformer;
    protected TransformerFactory factory;
    protected Source xslStream;

    public MP7Generator(X3DResourceDetail x3dResource, HashMap<String, String> ilsParamMap, HashMap<String, String> ifsParamMap, HashMap<String, String> extrusionParamMap, HashMap<String, String> histograms, HashMap<String, String> scalableColors, HashMap<String, String> surfeatures, String xslSource) throws IOException {
        this.x3dResource = x3dResource;
        this.extrusionParamMap = extrusionParamMap;
        this.ifsParamMap = ifsParamMap;
        this.ilsParamMap = ilsParamMap;
        this.histograms = histograms;
        this.scalableColors = scalableColors;
        this.surfeatures = surfeatures;
        this.factory = TransformerFactory.newInstance();
        this.xslStream = new StreamSource(MP7Generator.class.getResourceAsStream("org/exist/xquery/modules/mpeg7/transformation/xsl/" + xslSource));
        //this.xslStream = new StreamSource(new ByteArrayInputStream(FileUtils.readFileToByteArray(new File(xslSource))));
    }

    public X3DResourceDetail getX3dResource() {
        return x3dResource;
    }

    public void setX3dResource(X3DResourceDetail x3dResource) {
        this.x3dResource = x3dResource;
    }

    public TransformerFactory getFactory() {
        return factory;
    }

    public Transformer getTransformer() {
        return transformer;
    }

    public void setFactory(TransformerFactory factory) {
        this.factory = factory;
    }

    public void setTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    public Source getXslStream() {
        return xslStream;
    }

    public void setXslStream(Source xslStream) {
        this.xslStream = xslStream;
    }

    public HashMap<String, String> getHistograms() {
        return histograms;
    }

    public void setHistograms(HashMap<String, String> histograms) {
        this.histograms = histograms;
    }

    public void setScalableColors(HashMap<String, String> scalableColors) {
        this.scalableColors = scalableColors;
    }

    public void generateDescription() {
        try {
            //Get X3D source
         
            File x3dSource = new File(x3dResource.parentPath + x3dResource.resourceFileName);
            StreamSource x3dInput = new StreamSource(new ByteArrayInputStream(FileUtils.readFileToByteArray(x3dSource)));

            //Where to write MPEG-7 file
            File mp7File = new File(x3dResource.parentPath + x3dResource.resourceName + ".mp7");
            String mp7Output = mp7File.toURI().toString();

            //Setup transformer options
            this.transformer = this.factory.newTransformer(this.xslStream);
            this.transformer.setErrorListener(new TransformationErrorListener());
            setTranformerParameters();
            StreamResult sr = new StreamResult(new File(mp7Output));
            this.transformer.transform(x3dInput, sr);

            Validator mpeg7Validator = new Validator(mp7File);
            Boolean isValid = mpeg7Validator.isValid();
            // if (isValid) {
            System.out.println("File: " + mp7Output + "is valid: "+ isValid);
            //}            
        } catch (TransformerException ex) {
            System.out.println("TransformerException: " + ex);
        } catch (IllegalArgumentException ex) {
            System.out.println("IllegalArgumentException: " + ex);
        } catch (IOException ex) {
            System.out.println("IOException: " + ex);
        }
    }

    private void setTranformerParameters() {

        this.transformer.setParameter("filename", this.x3dResource.parentPath + "/" + this.x3dResource.resourceFileName);
        if (!this.extrusionParamMap.isEmpty()) {
            for (Map.Entry me : this.extrusionParamMap.entrySet()) {
                this.transformer.setParameter(me.getKey().toString(), me.getValue().toString());
            }
        }        
        if (!this.ifsParamMap.isEmpty()) {
            for (Map.Entry me : this.ifsParamMap.entrySet()) {
                this.transformer.setParameter(me.getKey().toString(), me.getValue().toString());
            }
        }
        if (!this.ilsParamMap.isEmpty()) {
            for (Map.Entry me : this.ilsParamMap.entrySet()) {
                this.transformer.setParameter(me.getKey().toString(), me.getValue().toString());
            }
        }
        if (!this.histograms.isEmpty()) {
            for (Map.Entry entry : this.histograms.entrySet()) {
                this.transformer.setParameter(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        if (!this.scalableColors.isEmpty()) {
            for (Map.Entry entry : this.scalableColors.entrySet()) {
                this.transformer.setParameter(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        if (!this.surfeatures.isEmpty()) {
            for (Map.Entry entry : this.surfeatures.entrySet()) {
                this.transformer.setParameter(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        
    }
}

class TransformationErrorListener implements ErrorListener {

    private static final Logger logger = Logger.getLogger(TransformationErrorListener.class);

    @Override
    public void warning(TransformerException e) throws TransformerException {
        logger.warn("TransformerException: ", e);
        report(e);
    }

    @Override
    public void error(TransformerException e) throws TransformerException {
        logger.error("TransformerException: ", e);
        report(e);
    }

    @Override
    public void fatalError(TransformerException e) throws TransformerException {
        logger.fatal("TransformerException: ", e);
        report(e);
    }

    private void report(TransformerException e) {
        try {

            SourceLocator loc = e.getLocator();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            String docSource = "/usr/local/exist2.2/tools/wrapper/logs/annotation/transformErrors.xml";
            File f = new File(docSource);
            Document doc;
            Element root;
            if (f.exists()) {
                doc = builder.parse(f);
                root = doc.getDocumentElement();
            } else {
                doc = builder.newDocument();
                // root element
                root = doc.createElement("errors");
                doc.appendChild(root);
                root.appendChild(root);
            }
            Element error = doc.createElement("error");

            error.setAttribute("line", (loc != null) ? String.valueOf(loc.getLineNumber()) : "N/A");
            error.setAttribute("column", (loc != null) ? String.valueOf(loc.getColumnNumber()) : "N/A");
            error.setAttribute("publicId", (loc != null) ? loc.getPublicId() : "N/A");
            error.setAttribute("systemId", (loc != null) ? loc.getSystemId() : "N/A");

            Element message = doc.createElement("message");
            message.appendChild(doc.createTextNode(e.getMessageAndLocation()));
            error.appendChild(message);

            Element location = doc.createElement("location");
            location.appendChild(doc.createTextNode(e.getLocationAsString()));
            error.appendChild(location);

            Element exception = doc.createElement("exception");
            exception.appendChild(doc.createTextNode(e.getException().getMessage()));
            error.appendChild(exception);

            Element cause = doc.createElement("cause");
            cause.appendChild(doc.createTextNode(e.getCause().getMessage()));
            error.appendChild(cause);

            root.appendChild(error);

            // create the xml file
            //transform the DOM Object to an XML File
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(f);
            transformer.transform(domSource, streamResult);

        } catch (ParserConfigurationException ex) {
            logger.error("ParserConfigurationException: ", ex);
        } catch (SAXException ex) {
            logger.error("SAXException: ", ex);
        } catch (IOException ex) {
            logger.error("IOException: ", ex);
        } catch (TransformerConfigurationException ex) {
            logger.error("TransformerConfigurationException: ", ex);
        } catch (TransformerException ex) {
            logger.error("TransformerException: ", ex);
        }
    }

}
