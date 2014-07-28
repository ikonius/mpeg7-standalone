/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.exist.xquery.modules.mpeg7.storage.database;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.OutputKeys;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import org.apache.log4j.Logger;
import org.exist.storage.serializers.EXistOutputKeys;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;
import org.exist.xquery.modules.mpeg7.storage.helpers.CollectionDetail;
import org.exist.xquery.modules.mpeg7.storage.helpers.X3DResourceDetail;

/**
 *
 * @author Patti Spala <pd.spala@gmail.com>
 */
public class ExistDB {

    private static final Logger logger = Logger.getLogger(ExistDB.class);
    protected String driver;
    protected String URI;
    protected Database database;

    public ExistDB() {
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    /**
     * Create the main connection instance to the eXist XML Database. All
     * configuration variables are loaded from the "connection.properties" file
     * in the same package.
     *
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     * @throws org.xmldb.api.base.XMLDBException
     * @throws java.lang.ClassNotFoundException
     */
    public void registerInstance() throws InstantiationException, IllegalAccessException, XMLDBException, ClassNotFoundException {
        this.URI = "xmldb:exist://54.72.206.163/exist/xmlrpc";
        this.driver = "org.exist.xmldb.DatabaseImpl";
        Class<?> cl = Class.forName(this.driver);
        this.database = (Database) cl.newInstance();
        this.database.setProperty("ssl-enable", "false");
        DatabaseManager.registerDatabase(this.database);
    }

    /**
     * This method returns a list of Collection details where X3D resources are
     * stored, in the BASE directory. Use the same method with parameters to
     * search for specific X3D child Collections
     *
     * @return List<CollectionDetail> with each Collection name and location
     * path in directory
     * @throws org.xmldb.api.base.XMLDBException
     */
    public List<CollectionDetail> listX3DCollections() throws XMLDBException {

        String baseCollection;
        Collection baseCol;
        List<CollectionDetail> childCols = new ArrayList<CollectionDetail>();
        baseCollection = "/db/3dData/x3d/";
        baseCol = DatabaseManager.getCollection(URI + baseCollection);
        baseCol.setProperty(OutputKeys.INDENT, "yes");
        baseCol.setProperty(EXistOutputKeys.EXPAND_XINCLUDES, "no");
        baseCol.setProperty(EXistOutputKeys.PROCESS_XSL_PI, "yes");
        if (baseCol.getChildCollectionCount() > 0) {
            //childCols = baseCol.listChildCollections();
            for (String child : baseCol.listChildCollections()) {
                childCols.add(new CollectionDetail(child, baseCol.getName()));
            }
        }
        if (baseCol.isOpen()) {
            baseCol.close();
        }
        return childCols;
    }

    /**
     * This method returns a list of Collection details where X3D resources are
     * stored, based on child directory path given.
     *
     * @param childPath
     * @return List<CollectionDetail> with each Collection name and location
     * path in directory
     * @throws org.xmldb.api.base.XMLDBException
     */
    public List<CollectionDetail> listX3DCollections(String childPath) throws XMLDBException {

        String baseCollection;
        Collection baseCol;
        List<CollectionDetail> childCols = new ArrayList<CollectionDetail>();
        baseCollection = "/db/3dData/x3d/";
        baseCol = DatabaseManager.getCollection(URI + baseCollection + childPath);
        baseCol.setProperty(OutputKeys.INDENT, "yes");
        baseCol.setProperty(EXistOutputKeys.EXPAND_XINCLUDES, "no");
        baseCol.setProperty(EXistOutputKeys.PROCESS_XSL_PI, "yes");
        if (baseCol.getChildCollectionCount() > 0) {
            for (String child : baseCol.listChildCollections()) {
                childCols.add(new CollectionDetail(child, baseCol.getName()));
            }
        }
        if (baseCol.isOpen()) {
            baseCol.close();
        }
        return childCols;
    }

    /**
     * This method returns a list of X3D Resource details, based on child
     * directory path given.
     *
     * @param collectionPath
     * @return List<CollectionDetail> with each Collection name and location
     * path in directory
     * @throws org.xmldb.api.base.XMLDBException
     */
    public List<X3DResourceDetail> getX3DResources(String collectionPath) throws XMLDBException {
        Collection col = DatabaseManager.getCollection(URI + collectionPath);
        col.setProperty(OutputKeys.INDENT, "yes");
        col.setProperty(EXistOutputKeys.EXPAND_XINCLUDES, "no");
        col.setProperty(EXistOutputKeys.PROCESS_XSL_PI, "yes");
        List<X3DResourceDetail> childRes = new ArrayList<X3DResourceDetail>();

        XPathQueryService service = (XPathQueryService) col.getService("XPathQueryService", "1.0");
        ResourceSet resultSet = service.query("//X3D");

        ResourceIterator results = resultSet.getIterator();
        while (results.hasMoreResources()) {
            XMLResource res = (XMLResource) results.nextResource();
            childRes.add(new X3DResourceDetail(
                    removeExtension((String) res.getDocumentId()),
                    (String) res.getDocumentId(),
                    res.getParentCollection().getName())
            );
        }
        col.close();
        return childRes;
    }
    
    /**
     * This method returns the contents of an X3D Resource, based on the
     * X3DResourceDetail given.
     *
     * @param detail
     * @return Object with X3D Resource contents, which can be loaded as a
     * Document
     * @throws org.xmldb.api.base.XMLDBException
     */
    public Object retrieveDocument(X3DResourceDetail detail) throws XMLDBException {
        Collection col = DatabaseManager.getCollection(URI + detail.parentPath);
        col.setProperty(OutputKeys.INDENT, "yes");
        col.setProperty(EXistOutputKeys.EXPAND_XINCLUDES, "no");
        col.setProperty(EXistOutputKeys.PROCESS_XSL_PI, "yes");
        XMLResource resource = (XMLResource) col.getResource(detail.resourceFileName);
        Object resContent = resource.getContent();
        if (col.isOpen()) {
            col.close();
        }
        return resContent;

    }

    /**
     * This method retrieves a stored module from the app, based on the
     * moduleName given. The module must be stored in
     * /db/apps/annotation/modules/ .
     *
     * @param moduleName
     * @return Object with module contents, which can be loaded as a Document
     * @throws org.xmldb.api.base.XMLDBException
     */
    public Object retrieveModule(String moduleName) throws XMLDBException {
        String modulePath = "/db/apps/annotation/modules/";
        Collection col = DatabaseManager.getCollection(URI + modulePath);
        col.setProperty(OutputKeys.INDENT, "yes");
        col.setProperty(EXistOutputKeys.EXPAND_XINCLUDES, "no");
        col.setProperty(EXistOutputKeys.PROCESS_XSL_PI, "yes");
        XMLResource resource = (XMLResource) col.getResource(moduleName);
        Object resContent = resource.getContent();
        if (col.isOpen()) {
            col.close();
        }
        return resContent;

    }

    /**
     * Stores an X3DResourceDetail as an XMLResource into the Collection.
     *
     * @param x3dResource
     * @param localFile
     * @throws XMLDBException
     */
    public void storeResource(X3DResourceDetail x3dResource, File localFile) throws XMLDBException {

        Collection col = DatabaseManager.getCollection(URI + x3dResource.parentPath, "admin", "@pds2177");
        col.setProperty(OutputKeys.INDENT, "yes");
        col.setProperty(EXistOutputKeys.EXPAND_XINCLUDES, "no");
        col.setProperty(EXistOutputKeys.PROCESS_XSL_PI, "yes");

        XMLResource resource = (XMLResource) col.createResource(localFile.getName(), "XMLResource");
        resource.setContent(localFile);
        col.storeResource(resource);
        if (col.isOpen()) {
            col.close();
        }
    }
}
