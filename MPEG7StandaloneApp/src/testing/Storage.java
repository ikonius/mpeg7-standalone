package testing;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.exist.xquery.modules.mpeg7.storage.database.ExistDB;
import org.exist.xquery.modules.mpeg7.storage.helpers.CollectionDetail;
import org.exist.xquery.modules.mpeg7.storage.helpers.X3DResourceDetail;
import org.exist.xquery.modules.mpeg7.x3d.geometries.ExtrusionDetector;
import org.exist.xquery.modules.mpeg7.x3d.geometries.IFSDetector;
import org.exist.xquery.modules.mpeg7.x3d.geometries.ILSDetector;

/**
 *
 * @author Patti Spala <pd.spala@gmail.com>
 */
public class Storage {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ExistDB db = new ExistDB();

        try {
//            PoolingExample example = new PoolingExample();
//                int[] anArray = {32,44,34};
//                    example.printExample(anArray);
            db.registerInstance();
            List<CollectionDetail> x3dCols = db.listX3DCollections();
            if (!x3dCols.isEmpty()) {
                for (CollectionDetail item : x3dCols) {
//                    System.out.println("Collection--> " + item.name + " LOCATED @: " + item.locationPath);
//                    List<CollectionDetail> innerChildrenCols = db.listX3DCollections(item.name);
//                    if (!innerChildrenCols.isEmpty()) {
//                        for (CollectionDetail child : innerChildrenCols) {
//                            System.out.println("---CHILD--> " + child.name + " LOCATED @: " + child.locationPath);
//
//                        }
//                    }
                    if (item.name.equalsIgnoreCase("x3dforwebauthors")) {
                        System.out.println("Collection--> " + item.name + " LOCATED @: " + item.locationPath);
                        List<X3DResourceDetail> x3dResources = db.getX3DResources(item.locationPath + "/" + item.name);

                        if (!x3dResources.isEmpty()) {
                            for (X3DResourceDetail detail : x3dResources) {
                                if (detail.resourceName.equalsIgnoreCase("IndexedFaceSet")) {
                                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                                    factory.setNamespaceAware(true);
                                    DocumentBuilder builder = factory.newDocumentBuilder();

                                    String docSource = db.retrieveDocument(detail).toString();

                                    Document doc = builder.parse(new ByteArrayInputStream(docSource.getBytes()));
                                    //doc.getDocumentElement().normalize();

                                    IFSDetector ifsDetector = new IFSDetector(doc);
                                    ifsDetector.processShapes();
                                    ILSDetector ilsDetector = new ILSDetector(doc);
                                    ilsDetector.processShapes();
                                    ExtrusionDetector extrusionDetector = new ExtrusionDetector(doc);
                                    extrusionDetector.processShapes();

                                    //MP7Generator mp7Generator = new MP7Generator(detail, extrusionDetector.getParamMap());
                                    //mp7Generator.generateDescription();
                                }

                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(Storage.class.getName()).log(Level.SEVERE, null, e);
        }

    }

}
