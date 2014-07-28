/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.exist.xquery.modules.mpeg7.x3d.helpers;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Patti Spala <pd.spala@gmail.com>
 */
public class CommonUtils {

    private static final Logger logger = Logger.getLogger(CommonUtils.class);

    public static void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            logger.info(pairs.getKey() + " : " + pairs.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    public static int[] toIntArray(List<int[]> list) {

        int[] ret = new int[list.size() * 3];
        for (int i = 0; i < list.size(); i++) {
            ret[3 * i] = list.get(i)[0];
            ret[3 * i + 1] = list.get(i)[1];
            ret[3 * i + 2] = list.get(i)[2];
        }
        return ret;
    }

    public static void downloadFile(String fileName, URL link) throws IOException {
        //Code to download
        InputStream in = new BufferedInputStream(link.openStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n = 0;
        while (-1 != (n = in.read(buf))) {
            out.write(buf, 0, n);
        }
        out.close();
        in.close();
        byte[] response = out.toByteArray();

        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(response);
        fos.close();
        //End download code
    }
}
