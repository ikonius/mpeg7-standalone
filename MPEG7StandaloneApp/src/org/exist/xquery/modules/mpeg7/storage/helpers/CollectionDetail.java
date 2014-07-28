/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.exist.xquery.modules.mpeg7.storage.helpers;

/**
 *
 * @author Patti Spala <pd.spala@gmail.com>
 */
public class CollectionDetail {

    public String name;
    public String locationPath;

    public CollectionDetail(String name, String path) {
        this.name = name;
        this.locationPath = path;
    }
}
