// To do:
//  -Formulas based on cw, while X3D based on ccw. Formulas should change. At this point,
//  the first and third column of Index are switched to make data cw
// -Rescan the entire project for any instance where the contents of Indexes are used as
// matrix indexes: there is a -1 discrepancy between Matlab and Java which seems to have
// accumulated to -2 and -3 already.
package org.exist.xquery.modules.mpeg7.x3d.geometries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import java.util.ArrayList;
import java.util.HashSet;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

/**
 *
 * @author Markos
 */
public class ShapeIndexExtraction {

    @SuppressWarnings("empty-statement")
    public static String shapeIndexEctraction(String[] args) throws FileNotFoundException, IOException {

        String coordArg = args[0];
        String pointArg = args[1];

        File coordFile = new File(coordArg);
        File pointFile = new File(pointArg);

        //************ Data Input from hard disk *****************//
        BufferedReader CoordFile;
        BufferedReader PointFile;
        FileInputStream inputStream;
        String rawIndexes;
        String rawPoints;
        StringBuilder sb;
        int Indexes[][];
        float Points[][];

        CoordFile = new BufferedReader(new FileReader(args[0]));
        try {
            sb = new StringBuilder();
            String line = CoordFile.readLine();
            while (line != null) {
                sb.append(line);
                sb.append('\n');
                line = CoordFile.readLine();
            }
            rawIndexes = sb.toString();
        } finally {
            CoordFile.close();
        }

        PointFile = new BufferedReader(new FileReader(args[1]));
        try {
            sb = new StringBuilder();
            String line = PointFile.readLine();
            while (line != null) {
                sb.append(line);
                sb.append('\n');
                line = PointFile.readLine();
            }
            rawPoints = sb.toString();
        } finally {
            PointFile.close();
        }

        String delim = "[\n]+";
        String[] tokenizedIndexes = rawIndexes.split(delim);

        delim = "[\n]+";
        String[] tokenizedPoints = rawPoints.split(delim);

        delim = "\\s+";

        String[] tempArray;

        Indexes = new int[tokenizedIndexes.length][3];
        for (int i = 0; i < tokenizedIndexes.length; i++) {
            tempArray = tokenizedIndexes[i].split(delim);
            int jTemp = 0;
            for (int j = 0; j < 3; j++) {
                if (tempArray[j].length() == 0) {
                    jTemp++;
                }
                Indexes[i][j] = Integer.valueOf(tempArray[jTemp]);
                jTemp++;
            }
        }

        Points = new float[tokenizedPoints.length][3];
        for (int i = 0; i < tokenizedPoints.length; i++) {
            tempArray = tokenizedPoints[i].split(delim);
            int jTemp = 0;
            for (int j = 0; j < 3; j++) {
                if (tempArray[j].length() == 0) {
                    jTemp++;
                }
                
                Points[i][j] = Float.parseFloat(tempArray[jTemp]);
                jTemp++;
            }
            
        }
        

        coordFile.delete();
        pointFile.delete();

        String FinalOutput = PerformActualComputation(Points, Indexes);

        return FinalOutput;

        //*******************End of Data Input *******************
    }

    public static String PerformActualComputation(float Points[][], int Indexes[][]) {

        int NumberOfBins = 100; //Not counting the two more bins for "PlanarSurfaces" and "SingularSurfaces" (the latter to be implemented)
        double CurvatureThresh = 0.01; //This is the eigenvalue magnitude threshold below which we consider a face to be part of a planar surface.
        int BitsPerBin = 12;

        //*****Calculate the total number of vertices and faces
        int nv = Points.length;
        int nf = Indexes.length;

        ////***********Reverse order of columns, since X3D is ccw
        int Temp;

        for (int i = 0; i < nf; i++) {
            Temp = Indexes[i][0];
            Indexes[i][0] = Indexes[i][2];
            Indexes[i][2] = Temp;
        }

        //*****Calculate the area of each face
        double[] FArea;
        FArea = face_area(Indexes, Points);

        //*****Calculate the Normal of each face
        double[][] N_Face;
        N_Face = facenormals(Indexes, Points);

        //*** Calculate Rotation matrices for the normals
        double[][][][] M_Minv;
        M_Minv = VectorRotationMatrix(N_Face);

        //*** Get the list of faces neighbouring each vertex
        ArrayList<Integer>[] VertNeighbours;
        VertNeighbours = vertex_neighbours(Indexes, Points);

        int[][] FaceNeighbours = face_neighbours(Indexes, VertNeighbours);

        //*** Get the centre of mass of each face
        float[][] CMass = centre_mass(Indexes, Points);

        double[] Lambda1 = new double[nf];
        double[] Lambda2 = new double[nf];
        double[][] Dir1 = new double[nf][3];
        double[][] Dir2 = new double[nf][3];

        Integer[] Nce;
        float[][] Ve;
        float[][] We;

        double Dxx;
        double Dxy;
        double Dyy;

        HashSet<Integer> NeSet = new HashSet();

        double[][] FM;
        double[] f;
        double[] abcdef;

        double tmp;

        double v2x;
        double v2y;

        double mag;

        double v1x;
        double v1y;

        double mu1;
        double mu2;

        double[] I1 = new double[2];
        double[] I2 = new double[2];

        double[] dir1_rot = new double[3];
        double[] dir2_rot = new double[3];

        double[] dir1;
        double[] dir2;

        double dir1mag;
        double dir2mag;

        double[] ShapeInd = new double[nf];

        boolean BorderFaceFlag;

        for (int i = 0; i < nf; i++) {
            //*** Calculations made based on the 1-neighbours of each face. While the n-neighborhood can also be used,
            // the increase in complexity has not been found to lead to correspondingly better results (Zaharia, Titus, and
            //Francoise J. Preteux. "3D-shape-based retrieval within the MPEG-7 framework." In Photonics West 2001-Electronic
            //Imaging, pp. 133-145. International Society for Optics and Photonics, 2001.)

            NeSet.clear();

            BorderFaceFlag = false;

            for (int j = 0; j < 3; j++) {
                //If even one N-neighbour is a border face, classify entire face as border
                if (FaceNeighbours[i][j] == -1) {
                    BorderFaceFlag = true;
                    break;
                }
                for (int k = 0; k < 3; k++) {
                    if (FaceNeighbours[FaceNeighbours[i][j]][k] == -1) {
                        BorderFaceFlag = true;
                        break;
                    }
                    for (int l = 0; l < 3; l++) {
                        if (FaceNeighbours[FaceNeighbours[FaceNeighbours[i][j]][k]][l] == -1) {
                            BorderFaceFlag = true;
                            break;
                        }
                        for (int m = 0; m < 3; m++) {
                            if (FaceNeighbours[FaceNeighbours[FaceNeighbours[FaceNeighbours[i][j]][k]][l]][m] == -1) {
                                BorderFaceFlag = true;
                                break;
                            } else {
                                NeSet.add(FaceNeighbours[FaceNeighbours[FaceNeighbours[FaceNeighbours[i][j]][k]][l]][m]);
                            }
                        }
                    }
                }
            }

            Nce = NeSet.toArray(new Integer[0]);

            //There have been cases where a face has three neighbours of the same index-must be my bug. This is a quickfix
            if (BorderFaceFlag == false && Nce.length > 10) {

                Ve = new float[Nce.length][3];

                for (int j = 0; j < Nce.length; j++) {
                    Ve[j] = CMass[Nce[j]];
                }

                //*** Current calculations use as the central face normal, the actual face normal. It is considered preferable
                // to use the mean normal of its 1-neighbourhood instead, though this works generally fine too.
                //----------------------------------
                //** Rotate point coordinates by the central face normal via matrix multiplication
                We = new float[Nce.length][3];

                for (int j = 0; j < Nce.length; j++) {
                    for (int k = 0; k < 3; k++) {
                        for (int l = 0; l < 3; l++) {
                            We[j][k] += Ve[j][l] * M_Minv[i][l][k][1];
                        }
                    }
                }

                //**** Fit a second degree polynomial surface b*x^2+c*y^2+d*xy+e*x+f*y+a on the rotated points
                FM = new double[Nce.length][5];
                f = new double[Nce.length];

                for (int j = 0; j < Nce.length; j++) {
                    FM[j][0] = Math.pow(We[j][1], 2);
                    FM[j][1] = Math.pow(We[j][2], 2);
                    FM[j][2] = We[j][1] * We[j][2];
                    FM[j][3] = We[j][1];
                    FM[j][4] = We[j][2];
                    f[j] = We[j][0];
                }

                OLSMultipleLinearRegression RegressionTest = new OLSMultipleLinearRegression();
                RegressionTest.newSampleData(f, FM);

                abcdef = RegressionTest.estimateRegressionParameters();

                //******* Form the Hessian Matrix  H = [2*b d;d 2*c];
                Dxx = 2 * abcdef[1];
                Dxy = abcdef[3];
                Dyy = 2 * abcdef[2];

                //******* Fast Eigenvalue calculation using only the coefficients of x, y and xy
                // Compute the eigenvectors
                tmp = Math.sqrt(Math.pow(Dxx - Dyy, 2) + 4 * Math.pow(Dxy, 2));
                v2x = 2 * Dxy;
                v2y = Dyy - Dxx + tmp;

                mag = Math.sqrt(Math.pow(v2x, 2) + Math.pow(v2y, 2));

                if (mag != 0) {
                    v2x = v2x / mag;
                    v2y = v2y / mag;
                }

                // The eigenvectors are orthogonal
                v1x = -v2y;
                v1y = v2x;

                // Compute the eigenvalues
                mu1 = (0.5 * (Dxx + Dyy + tmp));
                mu2 = (0.5 * (Dxx + Dyy - tmp));

                // Sort eigen values by absolute value
                if (mu1 < mu2) {
                    Lambda1[i] = mu1;
                    I1[0] = v1x;
                    I1[1] = v1y;
                    Lambda2[i] = mu2;
                    I2[0] = v2x;
                    I2[1] = v2y;

                } else {
                    Lambda1[i] = mu2;
                    I1[0] = v2x;
                    I1[1] = v2y;
                    Lambda2[i] = mu1;
                    I2[0] = v1x;
                    I2[1] = v1y;
                }

                dir1_rot[0] = 0;
                dir1_rot[1] = I1[0];
                dir1_rot[2] = I1[1];

                dir2_rot[0] = 0;
                dir2_rot[1] = I2[0];
                dir2_rot[2] = I2[1];

                //********** Project vectors back to original space;
                dir1 = new double[3];
                dir2 = new double[3];

                for (int j = 0; j < 3; j++) {
                    for (int k = 0; k < 3; k++) {
                        dir1[j] += dir1_rot[k] * M_Minv[i][k][j][0];
                        dir2[j] += dir2_rot[k] * M_Minv[i][k][j][0];
                    }
                }

                for (int j = 0; j < 3; j++) {
                    // System.out.println(String.valueOf(M_Minv[i][j][0][0]) + " " + String.valueOf(M_Minv[i][j][1][0]) + " " + String.valueOf(M_Minv[i][j][2][0]));
                }

                dir1mag = Math.pow(dir1[0], 2) + Math.pow(dir1[1], 2) + Math.pow(dir1[2], 2);
                dir2mag = Math.pow(dir2[0], 2) + Math.pow(dir2[1], 2) + Math.pow(dir2[2], 2);

                for (int j = 0; j < 3; j++) {
                    dir1[j] = dir1[j] / dir1mag;
                    dir2[j] = dir2[j] / dir2mag;
                }

                //*********Calculate the Shape Index for each face
                ShapeInd[i] = (0.5) - (1 / Math.PI) * (Math.atan2(Lambda2[i] + Lambda1[i], Lambda2[i] - Lambda1[i]));
            } else {

                ShapeInd[i] = -1;
            }
        }

        double[] ShapeIndHist = new double[NumberOfBins + 3];
        int BinIndicator;
        double MaxValue = Math.pow(2, BitsPerBin);

        double Sum = 0;

        //********Form the Shape Index Histogram
        for (int i = 0; i < nf; i++) {
            // Normal face
            if (Math.sqrt(Math.pow(Lambda1[i], 2) + Math.pow(Lambda2[i], 2)) > CurvatureThresh) {
                BinIndicator = (int) Math.floor(ShapeInd[i] * NumberOfBins);
                ShapeIndHist[BinIndicator] += FArea[i];
            } else {
                if (ShapeInd[i] > 0) {
                    // Planar surface face
                    ShapeIndHist[NumberOfBins] += FArea[i];
                } else {
                    // Border face
                    ShapeIndHist[NumberOfBins + 1] += FArea[i];
                    //System.out.println(i + " " + Indexes[i][0] + " " + Indexes[i][1] + " " + Indexes[i][2]);
                }
            }
        }

        String FinalOutput = String.valueOf(BitsPerBin);

        for (int i = 0; i < NumberOfBins + 2; i++) {
            FinalOutput = FinalOutput + " " + String.valueOf((int) Math.round(ShapeIndHist[i] * (MaxValue - 1)));
            Sum = Sum + ShapeIndHist[i];
        }

        return FinalOutput;

    }

    static double[] face_area(int Indexes[][], float Points[][]) {

        //Face area calculated from side lengths using Heron's formula
        double[] Sidelengths = new double[3];
        double SemiPerim;
        double TotalArea = 0;
        double[] FArea = new double[Indexes.length];

        for (int i = 0; i < Indexes.length; i++) {
            Sidelengths[0] = Math.sqrt(Math.pow(Points[Indexes[i][0]][0] - Points[Indexes[i][1]][0], 2) + Math.pow(Points[Indexes[i][0]][1] - Points[Indexes[i][1]][1], 2) + Math.pow(Points[Indexes[i][0]][2] - Points[Indexes[i][1]][2], 2));
            Sidelengths[1] = Math.sqrt(Math.pow(Points[Indexes[i][1]][0] - Points[Indexes[i][2]][0], 2) + Math.pow(Points[Indexes[i][1]][1] - Points[Indexes[i][2]][1], 2) + Math.pow(Points[Indexes[i][1]][2] - Points[Indexes[i][2]][2], 2));
            Sidelengths[2] = Math.sqrt(Math.pow(Points[Indexes[i][2]][0] - Points[Indexes[i][0]][0], 2) + Math.pow(Points[Indexes[i][2]][1] - Points[Indexes[i][0]][1], 2) + Math.pow(Points[Indexes[i][2]][2] - Points[Indexes[i][0]][2], 2));
            SemiPerim = (Sidelengths[0] + Sidelengths[1] + Sidelengths[2]) / 2;
            FArea[i] = Math.sqrt(SemiPerim * (SemiPerim - Sidelengths[0]) * (SemiPerim - Sidelengths[1]) * (SemiPerim - Sidelengths[2]));
            TotalArea += FArea[i];
        }
        for (int i = 0; i < Indexes.length; i++) {
            FArea[i] /= TotalArea;
        }
        return FArea;
    }

    static double[][] facenormals(int Indexes[][], float Points[][]) {
        double[][] Normals;
        Normals = new double[Indexes.length][3];

        double EdgeVectors[][] = new double[3][3];
        double VectorNorm1, VectorNorm2, VectorNorm3;
        double RawNormal[] = new double[3];
        double NormalNorm;
        for (int i = 0; i < Indexes.length; i++) {
            // Get the edge vectors
            EdgeVectors[0][0] = Points[Indexes[i][0]][0] - Points[Indexes[i][1]][0];
            EdgeVectors[0][1] = Points[Indexes[i][0]][1] - Points[Indexes[i][1]][1];
            EdgeVectors[0][2] = Points[Indexes[i][0]][2] - Points[Indexes[i][1]][2];
            EdgeVectors[1][0] = Points[Indexes[i][1]][0] - Points[Indexes[i][2]][0];
            EdgeVectors[1][1] = Points[Indexes[i][1]][1] - Points[Indexes[i][2]][1];
            EdgeVectors[1][2] = Points[Indexes[i][1]][2] - Points[Indexes[i][2]][2];
            EdgeVectors[2][0] = Points[Indexes[i][2]][0] - Points[Indexes[i][0]][0];
            EdgeVectors[2][1] = Points[Indexes[i][2]][1] - Points[Indexes[i][0]][1];
            EdgeVectors[2][2] = Points[Indexes[i][2]][2] - Points[Indexes[i][0]][2];

            // Normalize vector norms to 1
            VectorNorm1 = Math.sqrt(Math.pow(EdgeVectors[0][0], 2) + Math.pow(EdgeVectors[0][1], 2) + Math.pow(EdgeVectors[0][2], 2));
            VectorNorm2 = Math.sqrt(Math.pow(EdgeVectors[1][0], 2) + Math.pow(EdgeVectors[1][1], 2) + Math.pow(EdgeVectors[1][2], 2));
            VectorNorm3 = Math.sqrt(Math.pow(EdgeVectors[2][0], 2) + Math.pow(EdgeVectors[2][1], 2) + Math.pow(EdgeVectors[2][2], 2));
            EdgeVectors[0][0] = EdgeVectors[0][0] / VectorNorm1;
            EdgeVectors[0][1] = EdgeVectors[0][1] / VectorNorm1;
            EdgeVectors[0][2] = EdgeVectors[0][2] / VectorNorm1;
            EdgeVectors[1][0] = EdgeVectors[1][0] / VectorNorm2;
            EdgeVectors[1][1] = EdgeVectors[1][1] / VectorNorm2;
            EdgeVectors[1][2] = EdgeVectors[1][2] / VectorNorm2;
            EdgeVectors[2][0] = EdgeVectors[2][0] / VectorNorm3;
            EdgeVectors[2][1] = EdgeVectors[2][1] / VectorNorm3;
            EdgeVectors[2][2] = EdgeVectors[2][2] / VectorNorm3;

            // Calculate (un-normalized) Normal via the cross-product between the first and third vector
            RawNormal[0] = EdgeVectors[0][1] * EdgeVectors[2][2] - EdgeVectors[2][1] * EdgeVectors[0][2];
            RawNormal[1] = EdgeVectors[0][2] * EdgeVectors[2][0] - EdgeVectors[2][2] * EdgeVectors[0][0];
            RawNormal[2] = EdgeVectors[0][0] * EdgeVectors[2][1] - EdgeVectors[2][0] * EdgeVectors[0][1];
            //Calculate Normal norm
            NormalNorm = Math.sqrt(Math.pow(RawNormal[0], 2) + Math.pow(RawNormal[1], 2) + Math.pow(RawNormal[2], 2));

            //Normalize the face Normal
            Normals[i][0] = RawNormal[0] / NormalNorm;
            Normals[i][1] = RawNormal[1] / NormalNorm;
            Normals[i][2] = RawNormal[2] / NormalNorm;

        }

        return Normals;
    }

    static double[][][][] VectorRotationMatrix(double[][] N_Face) {
        double[][][][] M_Minv = new double[N_Face.length][3][3][2];

        double[][] M;
        double[][] Minv = new double[3][3];
        double[] v = new double[3];
        double[] l = new double[3];
        double[] k = new double[3];
        double lNorm;
        double kNorm;
        RealMatrix MR;
        RealMatrix MinvR;

        for (int i = 0; i < N_Face.length; i++) {
            v[0] = N_Face[i][0];
            v[1] = N_Face[i][1];
            v[2] = N_Face[i][2];

            k[0] = 0.5330; //Math.random();
            k[1] = 0.8842; //Math.random();
            k[2] = 0.7721; //Math.random();

            l[0] = k[1] * v[2] - k[2] * v[1];
            l[1] = k[2] * v[0] - k[0] * v[2];
            l[2] = k[0] * v[1] - k[1] * v[0];

            lNorm = Math.sqrt(Math.pow(l[0], 2) + Math.pow(l[1], 2) + Math.pow(l[2], 2));
            l[0] = l[0] / lNorm;
            l[1] = l[1] / lNorm;
            l[2] = l[2] / lNorm;

            k[0] = l[1] * v[2] - l[2] * v[1];
            k[1] = l[2] * v[0] - l[0] * v[2];
            k[2] = l[0] * v[1] - l[1] * v[0];

            kNorm = Math.sqrt(Math.pow(k[0], 2) + Math.pow(k[1], 2) + Math.pow(k[2], 2));
            k[0] = k[0] / kNorm;
            k[1] = k[1] / kNorm;
            k[2] = k[2] / kNorm;

            Minv[0][0] = v[0];
            Minv[1][0] = v[1];
            Minv[2][0] = v[2];
            Minv[0][1] = l[0];
            Minv[1][1] = l[1];
            Minv[2][1] = l[2];
            Minv[0][2] = k[0];
            Minv[1][2] = k[1];
            Minv[2][2] = k[2];

            MinvR = MatrixUtils.createRealMatrix(Minv);
            MR = new LUDecomposition(MinvR).getSolver().getInverse();

            M = MR.getData();

            for (int jj = 0; jj < 3; jj++) {
                for (int kk = 0; kk < 3; kk++) {
                    M_Minv[i][jj][kk][0] = M[jj][kk];
                    M_Minv[i][jj][kk][1] = Minv[jj][kk];
                }
            }

        }

        return M_Minv;
    }

    private static ArrayList[] vertex_neighbours(int[][] Indexes, float[][] Points) {
        ArrayList<Integer>[] VNe = (ArrayList<Integer>[]) new ArrayList[Points.length];

        for (int i = 0; i < Points.length; i++) {
            VNe[i] = new ArrayList<Integer>();
        }

        for (int i = 0; i < Indexes.length; i++) {
            VNe[Indexes[i][0]].add(i);
            VNe[Indexes[i][1]].add(i);
            VNe[Indexes[i][2]].add(i);
        }

        return VNe;
    }

    private static int[][] face_neighbours(int[][] Indexes, ArrayList<Integer>[] VertNeighbours) {

        int FNe[][] = new int[Indexes.length][3];
        for (int iii = 0; iii < Indexes.length; iii++) {
            for (int jjj = 0; jjj < 3; jjj++) {
                FNe[iii][jjj] = -1;
            }
        }
        int Vert1, Vert2, Vert3;
        int CurrentNeighbour;

        for (int i = 0; i < FNe.length; i++) {
            Vert1 = Indexes[i][0];
            Vert2 = Indexes[i][1];
            Vert3 = Indexes[i][2];
            if (FNe[i][0] == -1) {
                for (int j = 0; j < VertNeighbours[Vert1].size(); j++) {
                    CurrentNeighbour = VertNeighbours[Vert1].get(j);
                    if (CurrentNeighbour != i) {
                        if (Indexes[CurrentNeighbour][0] == Vert1) {
                            if (Indexes[CurrentNeighbour][1] == Vert2) {
                                FNe[i][0] = CurrentNeighbour;
                                FNe[CurrentNeighbour][0] = i;
                                break;
                            } else if (Indexes[CurrentNeighbour][2] == Vert2) {
                                FNe[i][0] = CurrentNeighbour;
                                FNe[CurrentNeighbour][1] = i;
                                break;
                            }
                        } else if (Indexes[CurrentNeighbour][1] == Vert1) {
                            if (Indexes[CurrentNeighbour][0] == Vert2) {
                                FNe[i][0] = CurrentNeighbour;
                                FNe[CurrentNeighbour][0] = i;
                                break;
                            } else if (Indexes[CurrentNeighbour][2] == Vert2) {
                                FNe[i][0] = CurrentNeighbour;
                                FNe[CurrentNeighbour][2] = i;
                                break;
                            }
                        } else if (Indexes[CurrentNeighbour][2] == Vert1) {
                            if (Indexes[CurrentNeighbour][0] == Vert2) {
                                FNe[i][0] = CurrentNeighbour;
                                FNe[CurrentNeighbour][1] = i;
                                break;
                            } else if (Indexes[CurrentNeighbour][1] == Vert2) {
                                FNe[i][0] = CurrentNeighbour;
                                FNe[CurrentNeighbour][2] = i;
                                break;
                            }
                        }
                    }
                }
            }

            if (FNe[i][0] == -1) {
                FNe[i][0] = -1;
            }

            if (FNe[i][1] == -1) {
                for (int j = 0; j < VertNeighbours[Vert1].size(); j++) {
                    CurrentNeighbour = VertNeighbours[Vert1].get(j);
                    if (CurrentNeighbour != i) {
                        if (Indexes[CurrentNeighbour][0] == Vert1) {
                            if (Indexes[CurrentNeighbour][1] == Vert3) {
                                FNe[i][1] = CurrentNeighbour;
                                FNe[CurrentNeighbour][0] = i;
                                break;
                            } else if (Indexes[CurrentNeighbour][2] == Vert3) {
                                FNe[i][1] = CurrentNeighbour;
                                FNe[CurrentNeighbour][1] = i;
                                break;
                            }
                        } else if (Indexes[CurrentNeighbour][1] == Vert1) {
                            if (Indexes[CurrentNeighbour][0] == Vert3) {
                                FNe[i][1] = CurrentNeighbour;
                                FNe[CurrentNeighbour][0] = i;
                                break;
                            } else if (Indexes[CurrentNeighbour][2] == Vert3) {
                                FNe[i][1] = CurrentNeighbour;
                                FNe[CurrentNeighbour][2] = i;
                                break;
                            }
                        } else if (Indexes[CurrentNeighbour][2] == Vert1) {
                            if (Indexes[CurrentNeighbour][0] == Vert3) {
                                FNe[i][1] = CurrentNeighbour;
                                FNe[CurrentNeighbour][1] = i;
                                break;
                            } else if (Indexes[CurrentNeighbour][1] == Vert3) {
                                FNe[i][1] = CurrentNeighbour;
                                FNe[CurrentNeighbour][2] = i;
                                break;
                            }
                        }
                    }
                }
            }

            if (FNe[i][1] == -1) {
                FNe[i][1] = -1;
            }

            if (FNe[i][2] == -1) {
                for (int j = 0; j < VertNeighbours[Vert2].size(); j++) {
                    CurrentNeighbour = VertNeighbours[Vert2].get(j);
                    if (CurrentNeighbour != i) {
                        if (Indexes[CurrentNeighbour][0] == Vert2) {
                            if (Indexes[CurrentNeighbour][1] == Vert3) {
                                FNe[i][2] = CurrentNeighbour;
                                FNe[CurrentNeighbour][0] = i;
                                break;
                            } else if (Indexes[CurrentNeighbour][2] == Vert3) {
                                FNe[i][2] = CurrentNeighbour;
                                FNe[CurrentNeighbour][1] = i;
                                break;
                            }
                        } else if (Indexes[CurrentNeighbour][1] == Vert2) {
                            if (Indexes[CurrentNeighbour][0] == Vert3) {
                                FNe[i][2] = CurrentNeighbour;
                                FNe[CurrentNeighbour][0] = i;
                                break;
                            } else if (Indexes[CurrentNeighbour][2] == Vert3) {
                                FNe[i][2] = CurrentNeighbour;
                                FNe[CurrentNeighbour][2] = i;
                                break;
                            }
                        } else if (Indexes[CurrentNeighbour][2] == Vert2) {
                            if (Indexes[CurrentNeighbour][0] == Vert3) {
                                FNe[i][2] = CurrentNeighbour;
                                FNe[CurrentNeighbour][1] = i;
                                break;
                            } else if (Indexes[CurrentNeighbour][1] == Vert3) {
                                FNe[i][2] = CurrentNeighbour;
                                FNe[CurrentNeighbour][2] = i;
                                break;
                            }
                        }
                    }
                }
            }
            if (FNe[i][2] == -1) {
                FNe[i][2] = -1;
            }
        }

        return FNe;
    }

    private static float[][] centre_mass(int[][] Indexes, float[][] Points) {
        float[][] CMass = new float[Indexes.length][3];
        for (int i = 0; i < Indexes.length; i++) {
            CMass[i][0] = (Points[Indexes[i][0]][0] + Points[Indexes[i][1]][0] + Points[Indexes[i][2]][0]) / 3;
            CMass[i][1] = (Points[Indexes[i][0]][1] + Points[Indexes[i][1]][1] + Points[Indexes[i][2]][1]) / 3;
            CMass[i][2] = (Points[Indexes[i][0]][2] + Points[Indexes[i][1]][2] + Points[Indexes[i][2]][2]) / 3;
        }

        return CMass;
    }
}
