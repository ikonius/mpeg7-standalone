/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.exist.xquery.modules.mpeg7;

import java.util.List;
import java.util.Map;
import org.exist.xquery.AbstractInternalModule;
import org.exist.xquery.FunctionDef;

/**
 *
 * @author Patti Spala <pd.spala@gmail.com>
 */
public class MPEG7Module extends AbstractInternalModule {

    public final static String NAMESPACE_URI = "http://exist-db.org/xquery/mpeg7";
    public final static String PREFIX = "mpeg7";

    private final static FunctionDef[] functions = {
        new FunctionDef(BatchTransform.signature, BatchTransform.class),
        new FunctionDef(SingleTransform.signature, SingleTransform.class)
    };

    public MPEG7Module(Map<String, List<? extends Object>> parameters) {
        super(functions, parameters);
    }

    @Override
    public String getNamespaceURI() {
        return NAMESPACE_URI;
    }

    @Override
    public String getDefaultPrefix() {
        return PREFIX;
    }

    @Override
    public String getDescription() {
        return "MPEG7 Internal Transformation module";
    }

    @Override
    public String getReleaseVersion() {
        return "2.0a";
    }

}
