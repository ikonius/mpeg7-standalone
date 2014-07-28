package org.exist.xquery.modules.mpeg7.x3d.filters;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import org.apache.log4j.Logger;
import org.exist.xquery.modules.mpeg7.x3d.geometries.ShapeIndexExtraction;

/**
 *
 * @author Patti Spala <pd.spala@gmail.com>
 * original code from
 * http://svn.xj3d.org/xj3d_code/trunk/apps/cadfilter/src/java/xj3d/filter/filters/ExtrusionGeometry.java
 */
public class ExtrusionToIFSFilter {

    private static final Logger logger = Logger.getLogger(ExtrusionToIFSFilter.class);
    // Temp holding on to the number of polygons in the system     
    private int polygonCount;

    // Holder of number vertices per face during processing    
    private int[] rawVerticesPerFace;

    // The number of valid coordinate indices in the coordIndex array
    private int numCoordIndex;

    //* The set of coordinate indices from the IFS
    private int[] coordIndices;

    // The coordinate values from the IFS
    private float[] coordinates;

    // Extrusion-specific information
    private boolean beginCap;
    private boolean ccw;
    private boolean convex;
    private float creaseAngle;
    private float[] crossSection;
    private boolean endCap;
    private float[] orientation;
    private float[] scale;
    private Vector3f[] scales;
    private boolean solid;
    private float[] spine;
    private Point3f[] spines;

    // Equal to spines.length
    private int numberOfSpines;

    private AxisAngle4f[] orientations;
    private Matrix3f[] rotations;
    private Matrix4f[] transforms;
    private boolean spineClosed;
    private boolean crossSectionClosed;
    private boolean collinear;
    private int uniqueCrossSectionPoints;
    private int numCrossSection;

    //BoundingBox
    float BBoxSize[];
    float BBoxCenter[];

    public ExtrusionToIFSFilter(String geoParams[]) throws IOException {
        reset();
        importExtrusionData(geoParams[0]);
    }

    public String filterGeometry() {
        // transform the extrusion to an IndexedTriangleSet
        initializeExtrusion();
        if (!calculateSCP()) {       // check the output -
            //System.out.println("Geometry poorly defined");
            logger.warn("Geometry poorly defined");
        }
        createExtrusionCoordinates();
        createExtrusionIndices();
        numCoordIndex = coordIndices.length;

        // Check the output and adjust accordingly. For max size 3 then just
        // drop the coordinates out now and not do any processing. For anything
        // more than three we need to go through and triangulate whatever we
        // find.
        int max_poly_size = checkMaxPolySize();

        switch (max_poly_size) {
            case 0:
                logger.warn("No valid polygons found: Zero sized polygons");
            //System.out.println("No valid polygons found: Zero sized polygons");

            case 1:
            case 2:
                logger.warn("No valid polygons. Max size " + max_poly_size);
            //System.out.println("No valid polygons. Max size " + max_poly_size);
        }

        StringBuilder coordinatesBuffer = new StringBuilder();
        for (int i = 0; i < coordinates.length; i++) {
            coordinatesBuffer.append(coordinates[i]);
            coordinatesBuffer.append(' ');
        }

        int[] output_indices = null;
        int output_idx = 0;
        int input_idx = 0;

        if (max_poly_size == 3) {
            output_indices = new int[polygonCount * 3];

            if (ccw) {
                for (int i = 0; i < polygonCount; i++) {
                    if (rawVerticesPerFace[i] == 3) {
                        output_indices[output_idx] = coordIndices[input_idx];

                        output_indices[output_idx + 1]
                                = coordIndices[input_idx + 1];
                        output_indices[output_idx + 2]
                                = coordIndices[input_idx + 2];

                        output_idx += 3;
                    }

                    input_idx += rawVerticesPerFace[i] + 1;
                }
            } else {
                for (int i = 0; i < polygonCount; i++) {
                    if (rawVerticesPerFace[i] == 3) {
                        output_indices[output_idx] = coordIndices[input_idx];

                        output_indices[output_idx + 1]
                                = coordIndices[input_idx + 2];
                        output_indices[output_idx + 2]
                                = coordIndices[input_idx + 1];

                        output_idx += 3;
                    }

                    input_idx += rawVerticesPerFace[i] + 1;
                }
            }
        } else {
            // greater than 3, so start doing triangulation.            
            output_indices = new int[numCoordIndex * (max_poly_size + 1) * 3];

            int face_index = 0;
            int i, j = 0;
            for (i = 0; i < numCoordIndex; i++) {

                if (rawVerticesPerFace[face_index] < 3) {
                    i += rawVerticesPerFace[face_index];
                    face_index++;
                    continue;
                }

                if (ccw) {

                    for (j = 0; j < rawVerticesPerFace[face_index] - 2; j++) {
                        if (i + 3 + j > numCoordIndex) {
                            j = rawVerticesPerFace[face_index] - 2;
                            break;
                        }

                        output_indices[output_idx] = coordIndices[i];

                        output_indices[output_idx + 1]
                                = coordIndices[i + 1 + j];
                        output_indices[output_idx + 2]
                                = coordIndices[i + 2 + j];
                        output_idx += 3;
                    }
                } else {
                    for (j = 0; j < rawVerticesPerFace[face_index] - 2; j++) {
                        if (i + 3 + j > numCoordIndex) {
                            j = rawVerticesPerFace[face_index] - 2;
                            break;
                        }

                        output_indices[output_idx] = coordIndices[i];

                        output_indices[output_idx + 1]
                                = coordIndices[i + 2 + j];
                        output_indices[output_idx + 2]
                                = coordIndices[i + 1 + j];
                        output_idx += 3;
                    }
                }

                i += j + 2;
                face_index++;
            }
        }

        StringBuilder indicesBuffer = new StringBuilder();
        for (int i = 0; i < output_idx; i++) {
            indicesBuffer.append(output_indices[i]);
            indicesBuffer.append(' ');
        }
        //un-comment to debug
        //System.out.println("<IndexedLineSet coordIndex=\"" + indicesBuffer.toString() + "\" solid=\"" + String.valueOf(solid) + "\" > \n <Coordinate point=\"" + coordinatesBuffer.toString() + "\"/> \n </IndexedLineSet>");
        //System.out.println("<IndexedTriangleSet index=\"" + indicesBuffer.toString() + "\" solid=\"" + String.valueOf(solid) + "\" > \n <Coordinate point=\"" + coordinatesBuffer.toString() + "\"/> \n </IndexedTriangleSet>");
        //debug
        calculateBoundingBox();
        StringBuilder bboxBuffer = new StringBuilder();
        for (int i = 0; i < BBoxSize.length; i++) {
            bboxBuffer.append(BBoxSize[i]);
            bboxBuffer.append(' ');
        }
        for (int i = 0; i < BBoxCenter.length; i++) {
            bboxBuffer.append(BBoxCenter[i]);
            if (i != (BBoxCenter.length - 1)) {
                bboxBuffer.append(' ');
            } else {
                bboxBuffer.append("&");
            }
        }
        //convert to arrays as used by ShapeIndexExtraction Class for Shape3D
        int pointsLength = coordinates.length / 3;
        int indexesLength = output_idx / 3;
        float points[][] = new float[pointsLength][3];
        int indexes[][] = new int[indexesLength][3];

        for (int i = 0; i < pointsLength; i++) {
            points[i][0] = coordinates[i];
            points[i][1] = coordinates[i + 1];
            points[i][2] = coordinates[i + 2];
        }

        for (int i = 0; i < indexesLength; i++) {
            indexes[i][0] = output_indices[i];
            indexes[i][1] = output_indices[i + 1];
            indexes[i][2] = output_indices[i + 2];
        }
        String shapeIndex = ShapeIndexExtraction.PerformActualComputation(points, indexes);
        bboxBuffer.append(shapeIndex);

        return bboxBuffer.toString();
    }

    public String[] getIFSTranslatedData() {

        StringBuilder ifsPointsBuffer = new StringBuilder();
        for (int i = 0; i < coordinates.length; i++) {
            ifsPointsBuffer.append(coordinates[i]);
            if (i < coordinates.length - 1) {
                ifsPointsBuffer.append(' ');
            }
        }

        StringBuilder ifsCoordsIndexBuffer = new StringBuilder();
        for (int i = 0; i < coordIndices.length; i++) {
            ifsCoordsIndexBuffer.append(coordIndices[i]);
            if (i < coordIndices.length - 1) {
                ifsCoordsIndexBuffer.append(' ');
            }
        }

        String[] joined = new String[]{ifsPointsBuffer.toString(), ifsCoordsIndexBuffer.toString()};
        return joined;
    }
    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------
    /**
     * Clear the currently stored values and return to the defaults for this
     * geometry type.
     */
    final void reset() {
        solid = true;
        ccw = true;
        convex = true;

        coordinates = null;
        coordIndices = null;

        // Extrusion-specific information
        beginCap = true;
        ccw = true;
        convex = true;
        creaseAngle = 0;
        crossSection = new float[]{1, 1, 1, -1, -1, -1, -1, 1, 1, 1};
        endCap = true;
        orientation = new float[]{0, 0, 1, 0};
        scale = new float[]{1, 1};
        solid = true;
        spine = new float[]{0, 0, 0, 0, 1, 0};

        //BoundingBox
        BBoxSize = new float[3];
        BBoxCenter = new float[3];
    }

    final void importExtrusionData(String geoParams) throws FileNotFoundException, IOException {
        BufferedReader ExtrusionFile = new BufferedReader(new FileReader(geoParams));
        String delim = "\\s+";
        String inputCrossSection = null;
        String inputSpine = null;
        String inputScale = null;
        String inputOrientation = null;

        try {
            String line = ExtrusionFile.readLine();

            while (line != null) {
                if (line.equalsIgnoreCase("CrossSection")) {
                    line = ExtrusionFile.readLine();
                    inputCrossSection = line;
                    crossSection = toFloatArray(inputCrossSection.split(delim));
                }
                if (line.equalsIgnoreCase("Spine")) {
                    line = ExtrusionFile.readLine();
                    inputSpine = line;
                    spine = toFloatArray(inputSpine.split(delim));
                }
                if (line.equalsIgnoreCase("Scale")) {
                    line = ExtrusionFile.readLine();
                    inputScale = line;
                    scale = toFloatArray(inputScale.split(delim));
                }
                if (line.equalsIgnoreCase("Orientation")) {
                    line = ExtrusionFile.readLine();
                    inputOrientation = line;
                    orientation = toFloatArray(inputOrientation.split(delim));
                }
                if (line.equalsIgnoreCase("solid")) {
                    line = ExtrusionFile.readLine();
                    solid = Boolean.parseBoolean(line);
                }
                if (line.equalsIgnoreCase("convex")) {
                    line = ExtrusionFile.readLine();
                    convex = Boolean.parseBoolean(line);
                }
                if (line.equalsIgnoreCase("ccw")) {
                    line = ExtrusionFile.readLine();
                    ccw = Boolean.parseBoolean(line);
                }
                if (line.equalsIgnoreCase("beginCap")) {
                    line = ExtrusionFile.readLine();
                    beginCap = Boolean.parseBoolean(line);
                }
                if (line.equalsIgnoreCase("endCap")) {
                    line = ExtrusionFile.readLine();
                    endCap = Boolean.parseBoolean(line);
                }
                if (line.equalsIgnoreCase("creaseAngle")) {
                    line = ExtrusionFile.readLine();
                    creaseAngle = Float.parseFloat(line);
                }
                line = ExtrusionFile.readLine();
            }
        } finally {
            ExtrusionFile.close();
        }
    }

    private float[] toFloatArray(final String[] inputArray) {
        float[] d = new float[inputArray.length];
        for (int i = 0; i < d.length; i++) {
            d[i] = Float.parseFloat(inputArray[i]);
        }
        return d;
    }

    private void initializeExtrusion() {

        numberOfSpines = spine.length / 3;
        numCrossSection = crossSection.length / 2;

        collinear = false;

        // if the tail meets the head
        spineClosed = (spine[0] == spine[spine.length - 3]
                && spine[1] == spine[spine.length - 2]
                && spine[2] == spine[spine.length - 1]);

        uniqueCrossSectionPoints = numCrossSection;
        crossSectionClosed = (crossSection[0] == crossSection[crossSection.length - 2]
                && crossSection[1] == crossSection[crossSection.length - 1]);
        if (crossSectionClosed) {
            uniqueCrossSectionPoints--;
        }

        // Convert the spine array into a Point3f array for
        // easier manipulation later
        spines = new Point3f[numberOfSpines];
        for (int i = 0; i < spines.length; i++) {
            spines[i] = new Point3f(spine[i * 3],
                    spine[i * 3 + 1],
                    spine[i * 3 + 2]);
        }

        // Convert the scale points and the orientation points so
        // they match specification:
        // if the number of scale or orientation points is greater than
        // the number of spine points, the excess values are ignored.
        // If they contain one value, it is applied at all spine points.
        // (results are 'undefined' if the number of scale or orientation
        // values is greater than one but less than the number of spine
        // points... in such a case, we repeat the final set of values for
        // the remainder of spine points)
        scales = new Vector3f[numberOfSpines];
        for (int i = 0; i < scales.length; i++) {
            if (i * 2 + 1 < scale.length) {
                scales[i] = new Vector3f(scale[i * 2],
                        1,
                        scale[i * 2 + 1]);
            } else {
                scales[i] = new Vector3f(scales[i - 1]);
            }
        }

        orientations = new AxisAngle4f[numberOfSpines];
        for (int i = 0; i < orientations.length; i++) {
            if (i * 4 + 3 < orientation.length) {
                orientations[i] = new AxisAngle4f(
                        orientation[i * 4],
                        orientation[i * 4 + 1],
                        orientation[i * 4 + 2],
                        orientation[i * 4 + 3]
                );
            } else {
                orientations[i] = new AxisAngle4f(orientations[i - 1]);
            }
        }

        rotations = new Matrix3f[numberOfSpines];

        // if entirely collinear
        Vector3d v2 = new Vector3d();
        Vector3d v1 = new Vector3d();
        Vector3d v0 = new Vector3d();
        double d = 0;
        for (int i = 1; i < spines.length - 1; i++) {
            v2.set(spines[i + 1]);
            v1.set(spines[i]);
            v0.set(spines[i - 1]);
            v2.sub(v1);
            v1.sub(v0);
            v0.cross(v2, v1);
            d += v0.dot(v0);
        }

        collinear = (d == 0);
    }

    /**
     * Create the spine information and verify that this is a valid object.
     *
     * @return true if everything is valid, false otherwise
     */
    private boolean calculateSCP() {
        // find an orthonormal basis and construct rotation matrix
        // for each spine. handle special cases in second pass
        Vector3f u, v;

        int last = numberOfSpines - 1;
        Vector3f[] x, y, z;

        x = new Vector3f[numberOfSpines];
        y = new Vector3f[numberOfSpines];
        z = new Vector3f[numberOfSpines];

        if (collinear) {
            if (spineClosed) {
                StringBuilder buf = new StringBuilder("Spine data:");
                for (Point3f sp : spines) {
                    buf.append(sp);
                    buf.append(' ');
                }
                return false;
            }

            // Direction is the first spine point that does not equal to
            // spines[0]
            Vector3f direction = null;
            for (Point3f sp : spines) {
                if (!spines[0].equals(sp)) {
                    direction = new Vector3f(sp);
                }
            }

            y[0] = new Vector3f();
            y[0].sub(direction, spines[0]);

            if (!norm(y[0])) {
                //System.out.println("Error normalizing Y in Extrusion");
                logger.warn("Error normalizing Y in Extrusion");
            }

            // Create an initial x[0]
            if (y[0].x == 1) {
                x[0] = new Vector3f(0, -1, 0);
            } else if (y[0].x == -1) {
                x[0] = new Vector3f(0, 1, 0);
            } else {
                x[0] = new Vector3f(1, 0, 0);
            }
            // Create z[0]
            z[0] = new Vector3f();
            z[0].cross(x[0], y[0]);

            // Create final x[0]
            x[0].cross(y[0], z[0]);
            for (int i = 1; i < spines.length; i++) {
                // redo, this should take the direction of y
                // redone by Pasi Paasiala < < check this >  >
                x[i] = new Vector3f(x[0]);
                y[i] = new Vector3f(y[0]);
                z[i] = new Vector3f(z[0]);
            }
        } else { // "collinear" is false

            // find y[i] for all but first and last
            // most times the exception cases are bad data and hopefully
            // wont happen. It is free to try catch you later, so hopes
            // 99% cases will be one if faster by not checking the if
            for (int i = 1; i < last; i++) {
                y[i] = new Vector3f();
                y[i].sub(spines[i + 1], spines[i - 1]);
                if (!norm(y[i])) {
                    // spines[i+1] equals spines[i - 1]
                    y[i].sub(spines[i + 1], spines[i]);
                    if (!norm(y[i])) {
                        // spines[i+1] equaled spines[i]
                        y[i].sub(spines[i], spines[i - 1]);
                        if (!norm(y[i])) {

                            // spines[i] equaled spines[i - 1]
                            // real bad case, do something
                            int w = i + 2;
                            while ((w < last + 1) && (spines[i - 1].equals(spines[w]))) {
                                w++;
                            }
                            if (w < last + 1) {
                                y[i].sub(spines[w], spines[i - 1]);
                                norm(y[i]); // should never divide by zero here
                            } else { // worst worst case
                                y[i] = new Vector3f(0, 1, 0);
                            }
                        }
                    }
                }
            }

            // y for ends
            if (spineClosed) {
                // spineClosed and not collinear - >  not all one point
                y[0] = new Vector3f();
                y[0].sub(spines[1], spines[last - 1]);
                if (!norm(y[0])) {
                    // bad case that the spine[n-2] == spine[1]
                    int w = last - 2;
                    while ((w > 1) && (spines[1].equals(spines[w]))) {
                        w--;
                    }
                    if (w > 1) {
                        y[0].sub(spines[1], spines[w]);
                        norm(y[0]); // should never divide by zero here
                    } else // how did this happen?
                    {
                        y[0].set(0, 0, 1);
                    }
                }
                y[last] = new Vector3f();
                y[last].sub(spines[1], spines[last - 1]);
                // y[last] = new Vector3f(y[0]);               
            } else {
                y[0] = new Vector3f();
                y[last] = new Vector3f();
                y[0].sub(spines[1], spines[0]);
                if (!norm(y[0])) {
                    int w = 2;
                    while ((w < last) && (spines[0].equals(spines[w]))) {
                        w++;
                    }
                    if (w < last) {
                        y[0].sub(spines[w], spines[0]);
                        norm(y[0]); // should not divide by zero here
                    } else {
                        y[0].set(0, 0, 1);
                    }
                }
                y[last] = new Vector3f();
                y[last].sub(spines[last], spines[last - 1]);

                if (!norm(y[last])) {
                    int w = last - 2;
                    while ((w > -1) && (spines[last].equals(spines[w]))) {
                        w--;
                    }
                    if (w > -1) {
                        y[last].sub(spines[last], spines[w]);
                        norm(y[last]);
                    } else {
                        y[last].set(0, 0, 1);
                    }
                }
            }

            // now z axis for each spine
            // first all except first and last
            boolean recheck = false;
            for (int i = 1; i < last; i++) {
                u = new Vector3f();
                v = new Vector3f();
                z[i] = new Vector3f();
                u.sub(spines[i - 1], spines[i]);
                v.sub(spines[i + 1], spines[i]);
                // spec seems backwards on u and v
                // shouldn't it be z[i].cross(u,v)???
                //z[i].cross(v,u);
                //-- >  z[i].cross(u,v); is correct < < check this >  >
                // Modified by Pasi Paasiala (Pasi.Paasiala@solibri.com)
                z[i].cross(u, v);
                if (!norm(z[i])) {
                    recheck = true;
                }
            }
            if (spineClosed) {
                z[0] = z[last] = new Vector3f();
                u = new Vector3f();
                v = new Vector3f();
                u.sub(spines[last - 1], spines[0]);
                v.sub(spines[1], spines[0]);
                try {
                    z[0].cross(u, v);
                } catch (ArithmeticException ae) {
                    recheck = true;
                }
            } else { // not spineClosed
                z[0] = new Vector3f(z[1]);
                z[last] = new Vector3f(z[last - 1]);
            }

            if (recheck) { // found adjacent collinear spines
                // first z has no length ?
                if (z[0].dot(z[0]) == 0) {
                    for (int i = 1; i < spines.length; i++) {
                        if (z[i].dot(z[i]) > 0) {
                            z[0] = new Vector3f(z[i]);
                        }
                    }
                    // test again could be most degenerate of cases
                    if (z[0].dot(z[0]) == 0) {
                        z[0] = new Vector3f(0, 0, 1);
                    }
                }

                // check rest of z's
                for (int i = 1; i < last + 1; i++) {
                    if (z[i].dot(z[i]) == 0) {
                        z[i] = new Vector3f(z[i - 1]);
                    }
                }
            }

            // finally, do a neighbor comparison
            // and evaluate the x's
            for (int i = 0; i < spines.length; i++) {
                if ((i > 0) && (z[i].dot(z[i - 1]) < 0)) {
                    z[i].negate();
                }

                // at this point, y and z should be nice
                x[i] = new Vector3f();

                //Original was: x[i].cross(z[i],y[i]); < < check this >  >
                //but it doesn't result in right handed coordinates
                // Modified by Pasi Paasiala
                x[i].cross(y[i], z[i]);
                norm(x[i]);
            }
        }

        // should now have orthonormal vectors for each
        // spine. create the rotation matrix with scale for
        // each spine. spec is unclear whether a twist imparted
        // at one of the spines is inherited by its "children"
        // so assume not.
        // also, the order looks like SxTxRscpxRo , ie ,
        // the spec doc looks suspect, double check
        Matrix3f m = new Matrix3f();
        transforms = new Matrix4f[spines.length];
        for (int i = 0; i < spines.length; i++) {
            rotations[i] = new Matrix3f();
            // Original had setRow. This is correct < < check this >  >
            // Modified by Pasi Paasiala
            rotations[i].setColumn(0, x[i]);
            rotations[i].setColumn(1, y[i]);
            rotations[i].setColumn(2, z[i]);
        }
        //Extrusion Triangulation - resolved issues with endCaps
        //Modified by Patti Spala
        if ((!endCap) && (!beginCap)) {
            rotations[0].setRow(0, x[0]);
            rotations[0].setRow(1, y[0]);
            rotations[0].setRow(2, z[0]);
            rotations[last].setRow(0, x[0]);
            rotations[last].setRow(1, y[0]);
            rotations[last].setRow(2, z[0]);
        }
        Matrix3f[] correctionRotations = createCorrectionRotations(z);
        Vector3f tmp = new Vector3f();
        // Create the transforms
        for (int i = 0; i < spines.length; i++) {
            rotations[i].mul(correctionRotations[i]);
            m.set(orientations[i]);
            rotations[i].mul(m);
            transforms[i] = new Matrix4f();
            transforms[i].setIdentity();
            m.m00 = scales[i].x;
            m.m11 = scales[i].y; // should always be '1'
            m.m22 = scales[i].z;
            m.mul(rotations[i]);
            transforms[i].setRotationScale(m);

            tmp.set(spines[i]);
            transforms[i].setTranslation(tmp);
        }

        return true;
    }

    /**
     * Variant on the traditional normalization process. If the length is zero
     * we've had something go wrong so should let the user know, courtesy of the
     * return value.
     *
     * @param n The vector to normalize
     * @return false when the vector is zero length
     */
    private boolean norm(Vector3f n) {

        float norml = n.x * n.x + n.y * n.y + n.z * n.z;

        if (norml == 0) {
            return false;
        }

        norml = 1 / (float) Math.sqrt(norml);

        n.x *= norml;
        n.y *= norml;
        n.z *= norml;

        return true;
    }

    /**
     * Creates a rotation for each spine point to avoid twisting of the profile
     * when the orientation of SCP changes.
     *
     * @author Pasi Paasiala
     * @param z the vector containing the z unit vectors for each spine point
     */
    private Matrix3f[] createCorrectionRotations(Vector3f[] z) {

        Matrix3f[] correctionRotations = new Matrix3f[spines.length];
        correctionRotations[0] = new Matrix3f();
        correctionRotations[0].setIdentity();
        AxisAngle4f checkAngle = new AxisAngle4f();

        // testPoint is used to find the angle that gives the smallest distance
        // between the previous and current rotation. Find a point that is not
        // in the origin.
        Point3f testPoint = new Point3f(crossSection[0], 0, crossSection[1]);

        for (int i = 0; i < numCrossSection; i++) {
            if (crossSection[i * 2] != 0 || crossSection[i * 2 + 1] != 0) {
                testPoint = new Point3f(crossSection[i * 2], 0, crossSection[i * 2 + 1]);
                break;
            }
        }

        // Fix the orientations by using the angle between previous z and current z
        for (int i = 1; i < spines.length; i++) {
            float angle = z[i].angle(z[i - 1]);
            correctionRotations[i] = correctionRotations[i - 1];
            if (angle != 0) {
                correctionRotations[i] = new Matrix3f(correctionRotations[i - 1]);
                Point3f previous = new Point3f();
                //Point3f previous = testPoint;
                // Test with negative angle:
                Matrix3f previousRotation = new Matrix3f(rotations[i - 1]);
                previousRotation.mul(correctionRotations[i - 1]);
                previousRotation.transform(testPoint, previous);
                Matrix3f delta = new Matrix3f();
                delta.setIdentity();
                delta.rotY(-angle);
                correctionRotations[i].mul(delta);

                Matrix3f negativeRotation = new Matrix3f(rotations[i]);
                negativeRotation.mul(correctionRotations[i]);

                Point3f pointNegative = new Point3f();
                negativeRotation.transform(testPoint, pointNegative);

                float distNegative = pointNegative.distance(previous);

                // Test with positive angle
                delta.rotY(angle * 2);
                correctionRotations[i].mul(delta);
                Matrix3f positiveRotation = new Matrix3f(rotations[i]);
                positiveRotation.mul(correctionRotations[i]);
                Point3f pointPositive = new Point3f();
                positiveRotation.transform(pointPositive);
                float distPositive = pointPositive.distance(previous);

                if (distPositive > distNegative) {
                    // Reset correctionRotations to negative angle
                    delta.rotY(-angle * 2);
                    correctionRotations[i].mul(delta);
                }

                // Check that the angle is not more than PI.
                // If it is subtract PI from angle
                checkAngle.set(correctionRotations[i]);
                if (((float) Math.PI - checkAngle.angle) < 0.001) {
                    correctionRotations[i].rotY((float) (checkAngle.angle - Math.PI));
                }
            }
        }

        return correctionRotations;
    }

    /**
     * Result: Completed "coordinates" array: An array of all the float
     * information describing each vertex in the extrusion, created by applying
     * the transforms to the crossSectionPts Note this is almost identical to
     * the createExtrusion() method, except it doesn't duplicate the coordinates
     * of the replicated point in the case of crossSectionClosed.
     */
    private void createExtrusionCoordinates() {

        // calculate the number of coordinates needed for the sides
        // of the extrusion: 3 coordinates per vertex, one vertex per
        // crossSectionPoint, and one set of crossSectionPoints per spinePoint
        coordinates = new float[numberOfSpines * uniqueCrossSectionPoints * 3];

        for (int i = 0; i < numberOfSpines; i++) {

            Matrix4f tx = transforms[i];

            for (int j = 0; j < uniqueCrossSectionPoints; j++) {

                int ind = (i * uniqueCrossSectionPoints + j) * 3;

                // basically a transform, in place
                float c_x = crossSection[j * 2];
                float c_z = crossSection[j * 2 + 1];

                float x = c_x * tx.m00 + c_z * tx.m02 + tx.m03;
                float y = c_x * tx.m10 + c_z * tx.m12 + tx.m13;
                float z = c_x * tx.m20 + c_z * tx.m22 + tx.m23;

                coordinates[ind] = x;
                coordinates[ind + 1] = y;
                coordinates[ind + 2] = z;

            }
        }
    }

    /**
     * Result: Completed "coordIndex" array: an integer array representing an
     * IndexedFaceSet representation of the extrusion.
     */
    private void createExtrusionIndices() {

        int sizeOfCoordIndex = 5 * (numCrossSection - 1) * (numberOfSpines - 1);
        if (beginCap) {
            sizeOfCoordIndex += uniqueCrossSectionPoints + 1;
        }
        if (endCap) {
            sizeOfCoordIndex += uniqueCrossSectionPoints + 1;
        }

        coordIndices = new int[sizeOfCoordIndex];

        int indx = 0;
        int curIndex = 0;

        // for each separate segment between two spine points
        for (int i = 0; i < numberOfSpines - 1; i++) {

            curIndex = i * uniqueCrossSectionPoints;

            // for every side along that segment
            for (int j = 0; j < numCrossSection - 1; j++) {

                if (ccw) {
                    coordIndices[ indx++] = j + curIndex;
                    coordIndices[ indx++] = j + curIndex + 1;
                    coordIndices[ indx++] = j + curIndex + uniqueCrossSectionPoints + 1;
                    coordIndices[ indx++] = j + curIndex + uniqueCrossSectionPoints;
                } else {
                    coordIndices[ indx++] = j + curIndex + uniqueCrossSectionPoints;
                    coordIndices[ indx++] = j + curIndex + uniqueCrossSectionPoints + 1;
                    coordIndices[ indx++] = j + curIndex + 1;
                    coordIndices[ indx++] = j + curIndex;
                }

                coordIndices[ indx++] = -1;
            }

            if (crossSectionClosed) {
                coordIndices[indx - 4] -= uniqueCrossSectionPoints;
                coordIndices[indx - 3] -= uniqueCrossSectionPoints;
            }
        }

        if (beginCap) {

            for (int i = 0; i < uniqueCrossSectionPoints; i++) {
                if (ccw) {
                    coordIndices[ indx++] = uniqueCrossSectionPoints - i - 1;
                } else {
                    coordIndices[ indx++] = i;
                }
            }
            coordIndices[ indx++] = -1;
        }
        if (endCap) {

            for (int i = 0; i < uniqueCrossSectionPoints; i++) {
                if (ccw) {
                    coordIndices[ indx++] = (numberOfSpines - 1) * uniqueCrossSectionPoints + i;
                } else {
                    coordIndices[ indx++] = numberOfSpines * uniqueCrossSectionPoints - i - 1;
                }
            }
            coordIndices[ indx] = -1;
        }
    }

    /**
     * Go through the coordIndex array and work out what the maximum polygon
     * size will be before we've done any processing. It does not define the
     * current maxPolySize variable.
     *
     * @return The maximum size that this check found
     * @author Justin Couch
     */
    private int checkMaxPolySize() {
        int cur_size = 0;
        int max_size = 0;
        polygonCount = 0;

        for (int i = 0; i < numCoordIndex; i++) {
            if (coordIndices[i] == -1) {
                if (cur_size > max_size) {
                    max_size = cur_size;
                }

                cur_size = 0;
                polygonCount++;
            } else {
                cur_size++;
            }
        }

        // One last check on the last index. The spec allows the user to not
        // need to specify -1 as the last value. If we don't check for this,
        // the max size would never be set.
        if ((numCoordIndex != 0) && (coordIndices[numCoordIndex - 1] != -1)) {
            if (cur_size > max_size) {
                max_size = cur_size;
            }

            polygonCount++;
        }

        rawVerticesPerFace = new int[polygonCount];
        int current_face = 0;

        for (int i = 0; i < numCoordIndex; i++) {
            if (coordIndices[i] != -1) {
                rawVerticesPerFace[current_face]++;
            } else {
                current_face++;
                if (current_face < polygonCount) {
                    rawVerticesPerFace[current_face] = 0;
                }
            }
        }

        return max_size;
    }

    private void calculateBoundingBox() {
        float maxXYZ[] = new float[3];
        float minXYZ[] = new float[3];

        for (int j = 0; j < 3; j++) {
            maxXYZ[j] = -Float.MAX_VALUE;
            minXYZ[j] = Float.MAX_VALUE;
            for (float point : coordinates) {
                if (maxXYZ[j] < point) {
                    maxXYZ[j] = point;
                }
                if (minXYZ[j] > point) {
                    minXYZ[j] = point;
                }
            }
        }

        BBoxSize[0] = maxXYZ[0] - minXYZ[0];
        BBoxSize[1] = maxXYZ[1] - minXYZ[1];
        BBoxSize[2] = maxXYZ[2] - minXYZ[2];

        BBoxCenter[0] = (maxXYZ[0] + minXYZ[0]) / 2;
        BBoxCenter[1] = (maxXYZ[1] + minXYZ[1]) / 2;
        BBoxCenter[2] = (maxXYZ[2] + minXYZ[2]) / 2;
    }
}
