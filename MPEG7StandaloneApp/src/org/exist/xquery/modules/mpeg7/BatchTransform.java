package org.exist.xquery.modules.mpeg7;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.exist.xquery.modules.mpeg7.transformation.MP7Generator;
import org.apache.log4j.Logger;
import org.exist.dom.QName;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.BooleanValue;
import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.FunctionReturnSequenceType;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;
import org.w3c.dom.Document;
import org.exist.xquery.modules.mpeg7.storage.database.ExistDB;
import org.exist.xquery.modules.mpeg7.storage.helpers.X3DResourceDetail;
import org.exist.xquery.modules.mpeg7.x3d.geometries.ExtrusionDetector;
import org.exist.xquery.modules.mpeg7.x3d.geometries.IFSDetector;
import org.exist.xquery.modules.mpeg7.x3d.geometries.ILSDetector;
import org.exist.xquery.modules.mpeg7.x3d.geometries.InlineDetector;
import org.exist.xquery.modules.mpeg7.x3d.textures.TextureDetector;
import org.xml.sax.SAXException;
import org.xmldb.api.base.XMLDBException;

/**
 *
 * @author Patti Spala <pd.spala@gmail.com>
 */
public class BatchTransform extends BasicFunction {

    private static final Logger logger = Logger.getLogger(BatchTransform.class);

    public final static FunctionSignature signature = new FunctionSignature(
            new QName("batchTransform", MPEG7Module.NAMESPACE_URI, MPEG7Module.PREFIX),
            "Batch MPEG7 Transformer for .zip extracted Collection stored X3D resources.",
            new SequenceType[]{
                new FunctionParameterSequenceType("path", Type.ITEM, Cardinality.EXACTLY_ONE, "The full path URI of the Collection to transform")
            },
            new FunctionReturnSequenceType(Type.BOOLEAN, Cardinality.EXACTLY_ONE, "true if successful, false otherwise"));

    public BatchTransform(XQueryContext context, FunctionSignature signature) {
        super(context, signature);
    }

    @Override
    public Sequence eval(Sequence[] args, Sequence sqnc) throws XPathException {
        // is argument the empty sequence?
        ValueSequence result = new ValueSequence();
        if (args.length == 0) {
            return Sequence.EMPTY_SEQUENCE;
        }

        try {
            String collectionPath = args[0].getStringValue();
            //int mpeg7counter = 0; //debugging

            //START - remove this connection to eXist
            ExistDB db = new ExistDB();
            db.registerInstance();
            String xslSource = db.retrieveModule("mpeg7_annotation.xsl").toString(); //get xsl from local file in project
            List<X3DResourceDetail> x3dResources = db.getX3DResources(collectionPath); //change this to get the x3d files from a local unzipped folder - not from the db
            //END - remove this connection to eXist
            //START - This is the main logic - keep this
            if (!x3dResources.isEmpty()) {
                //logger.debug("No of X3D files: " + x3dResources.size()); //debugging
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                for (X3DResourceDetail detail : x3dResources) {
                    try {
                        //logger.debug("Processing file: " + detail.resourceFileName);
                        String x3dSource = db.retrieveDocument(detail).toString(); //change logic to parse inline files from local folder - not from the db
                        Document doc = builder.parse(new ByteArrayInputStream(x3dSource.getBytes()));
                        InlineDetector inlDetector = new InlineDetector(doc, detail.parentPath);
                        doc = inlDetector.retrieveInlineNodes();
                        IFSDetector ifsDetector = new IFSDetector(doc);
                        //ifsDetector.processShapes();
                        ILSDetector ilsDetector = new ILSDetector(doc);
                        //ilsDetector.processShapes();
                        ExtrusionDetector extrusionDetector = new ExtrusionDetector(doc);
                        //extrusionDetector.processShapes();
                        TextureDetector textureDetector = new TextureDetector(doc, detail.parentPath);
                        MP7Generator mp7Generator = new MP7Generator(detail, ilsDetector.getParamMap(), ifsDetector.getParamMap(), extrusionDetector.getParamMap(), textureDetector.getHistograms(), textureDetector.getScalableColors(), textureDetector.getSURF(), xslSource);
                        mp7Generator.generateDescription();

                    } catch (XMLDBException ex) {
                        logger.error("XMLDBException: ", ex);
                    } catch (SAXException ex) {
                        logger.error("SAXException: ", ex);
                    } catch (IOException ex) {
                        logger.error("IOException: ", ex);
                    } catch (XPathExpressionException ex) {
                        logger.error("XPathExpressionException: ", ex);
                    } catch (Exception ex) {
                        logger.error("Exception: ", ex);
                    }

                }
            }
            //logger.debug("No of MPEG-7 files: " + mpeg7counter); //debugging
            result.add(new BooleanValue(true)); //todo cases

        } catch (InstantiationException ex) {
            logger.error("InstantiationException: ", ex);
            result.add(new BooleanValue(false));
        } catch (ParserConfigurationException ex) {
            logger.error("ParserConfigurationException: ", ex);
            result.add(new BooleanValue(false));
        } catch (XMLDBException ex) {
            logger.error("XMLDBException: ", ex);
            result.add(new BooleanValue(false));
        } catch (IllegalAccessException ex) {
            logger.error("IllegalAccessException: ", ex);
            result.add(new BooleanValue(false));
        } catch (ClassNotFoundException ex) {
            logger.error("ClassNotFoundException: ", ex);
            result.add(new BooleanValue(false));
        }
        return result;
        //END - This is the main logic - keep this
    }

}
