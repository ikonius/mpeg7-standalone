package org.exist.xquery.modules.mpeg7.x3d.geometries;

import ExtractShapeGoogleDescriptor.ExtractShapeGoogleDescriptor;
import com.mathworks.toolbox.javabuilder.MWException;
import org.apache.log4j.Logger;

/**
 *
 * @author Patti Spala <pd.spala@gmail.com>
 */
public class ShapeGoogleExtraction {

    private static final Logger logger = Logger.getLogger(ShapeGoogleExtraction.class);
    protected String points;
    protected String index;
    protected int vocabularySize;
    protected String bof;
    protected String ssBof;

    protected Object[] shParts;

    public ShapeGoogleExtraction(String points, String index, int vocabularySize) {
        this.index = index;
        this.points = points;
        this.vocabularySize = vocabularySize;
        extract();
    }

    private void extract() {
        try {
            ExtractShapeGoogleDescriptor shDescriptor;
            shDescriptor = new ExtractShapeGoogleDescriptor();
            this.shParts = shDescriptor.ExtractShapeGoogleDescriptor(2, this.points, this.index, this.vocabularySize);
            this.bof = this.shParts[0].toString();
            this.ssBof = this.shParts[1].toString();       
        } catch (MWException ex) {
            logger.error(ex);
        }
    }

    public String getStringRepresentation() {
        String value = Integer.toString(this.vocabularySize) + ";" + this.bof + ";" + this.ssBof;
        return value;
    }
}
