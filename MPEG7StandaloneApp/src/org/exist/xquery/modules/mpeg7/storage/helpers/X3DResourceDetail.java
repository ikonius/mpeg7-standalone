/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.exist.xquery.modules.mpeg7.storage.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Patti Spala <pd.spala@gmail.com>
 */
public class X3DResourceDetail {

    public String resourceName;
    public String resourceFileName;
    public String parentPath;

    public X3DResourceDetail(String name, String filename, String path) {
        this.resourceName = name;
        this.resourceFileName = filename;
        this.parentPath = path;
    }

    public static String resolveExistUri(String basePath, String filePath) {
        int countSteps = StringUtils.countMatches(filePath, "../");
        String basePathParts[] = basePath.split("/");
        List<String> basePathPartsList = new ArrayList<String>(Arrays.asList(basePathParts));
        basePathPartsList.removeAll(Arrays.asList("", null));
        String newBasePath = "";
        int remainingParts = basePathPartsList.size() - 1 - countSteps;
        for (int i = 0; i <= remainingParts; i++) {
            newBasePath = newBasePath.concat("/").concat(basePathPartsList.get(i));
        }
        return newBasePath.concat(filePath.substring(filePath.lastIndexOf("../") + 2));
    }
}
