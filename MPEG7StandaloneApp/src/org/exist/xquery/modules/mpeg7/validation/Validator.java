package org.exist.xquery.modules.mpeg7.validation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import mpegMpeg7Schema2001.Mpeg7Document;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

/**
 *
 * @author Patti Spala <pd.spala@gmail.com>
 */
public class Validator {

    private static final Logger logger = Logger.getLogger(Validator.class);
    private final File inputFile;
    private final XmlOptions xmlOptions;
    private final ArrayList validationErrors;
    private Mpeg7Document mpeg7Document;
    private final Map<String, String> implicitNamespaces;

    public Validator(File inputMpeg7) {
        this.inputFile = inputMpeg7;
        this.xmlOptions = new XmlOptions();
        this.validationErrors = new ArrayList();
        this.implicitNamespaces = new HashMap<String, String>();
    }

    public File getInputFile() {
        return inputFile;
    }

    private void setupXmlOptions() {
        this.xmlOptions.setErrorListener(this.validationErrors);
        this.xmlOptions.setSaveNamespacesFirst();
        this.xmlOptions.setSavePrettyPrint();
        this.xmlOptions.setSavePrettyPrintIndent(3);
        this.xmlOptions.setSaveAggressiveNamespaces();
        this.xmlOptions.setSaveImplicitNamespaces(this.implicitNamespaces);
        this.xmlOptions.setUseDefaultNamespace();
    }

    private void setupImplicitNamespaces() {
        this.implicitNamespaces.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    }

    public boolean isValid() {

        try {
            this.setupImplicitNamespaces();
            this.setupXmlOptions();
            this.mpeg7Document = Mpeg7Document.Factory.parse(this.inputFile);
            this.mpeg7Document.save(this.inputFile);
            boolean valid = this.mpeg7Document.validate(xmlOptions);
            //logger.info("MPEG7 Document: " + this.inputFile.getName() + " validation result: " + valid);
            System.out.println("MPEG7 Document: " + this.inputFile.getName() + " validation result: " + valid);
            if (!valid) {                
                Iterator iter = this.validationErrors.iterator();
                while (iter.hasNext()) {
                    //logger.error("Validation Error >> " + iter.next());
                    System.out.println("Validation Error >> " + iter.next());
                }
            }
            return valid;
        } catch (XmlException exXML) {
//            logger.error("XmlException: "+ exXML);
            System.out.println("XmlException: "+ exXML);
            return false;

        } catch (IOException exXML) {
//            logger.error("IOException: "+ exXML);
            System.out.println("IOException: "+ exXML);
            return false;
        }

    }
}
