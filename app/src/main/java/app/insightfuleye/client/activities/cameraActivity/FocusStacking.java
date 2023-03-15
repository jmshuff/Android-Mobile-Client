package app.insightfuleye.client.activities.cameraActivity;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.checkerframework.checker.units.qual.A;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FastFeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.ORB;
import org.opencv.features2d.SIFT;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.AlignMTB;

import static org.opencv.core.Core.NORM_HAMMING;
import static org.opencv.core.Core.addWeighted;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.Mat.eye;
import static org.opencv.core.TermCriteria.EPS;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgproc.Imgproc.INTER_LINEAR;
import static org.opencv.imgproc.Imgproc.WARP_INVERSE_MAP;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.resize;
import static org.opencv.imgproc.Imgproc.warpAffine;
import static org.opencv.imgproc.Imgproc.warpPerspective;
import static org.opencv.photo.Photo.createAlignMTB;
import static org.opencv.video.Video.MOTION_AFFINE;
import static org.opencv.video.Video.MOTION_EUCLIDEAN;
import static org.opencv.video.Video.MOTION_HOMOGRAPHY;
import static org.opencv.video.Video.findTransformECC;

public class FocusStacking {
    /**
     * A simple focus stacking algorithm using java and openCV
     *
     * Steps :
     * https://stackoverflow.com/questions/15911783/what-are-some-common-focus-stacking-algorithms
     *
     * Thanks to Charles McGuinness (python exemple)
     *
     * @author Lucas Lelaidier
     */

        /**
         * List of images to merge together
         */
        private ArrayList<Mat> inputs = new ArrayList<Mat>();

        /**
         * Path to the folder which contains the images
         */
        private String path;
        private String time;

        public FocusStacking(String path, String time)
        {
            this.path = path.replace("\\", "/");
            this.time = time.substring(0,10);
        }

        public FocusStacking(ArrayList<Mat> inputs)
        {
            this.inputs = inputs;
        }

        public void setInputs(ArrayList<Mat> inputs)
        {
            this.inputs = inputs;
        }

        /**
         * Compute the gradient map of the image
         * @param image image to transform
         * @return image image transformed
         */

/*        public void imageRegistration(){
            int scale = 1;
            int scaleSmall = 4;
            float scaleDiff = scaleSmall / scale;

            for (int i = 0; i< inputs.size(); i++) {
                Mat resizeimage = new Mat();

                //resize(inputs.get(i), resizeimage, new Size(inputs.get(i).size(0)/scale, inputs.get(i).size(1)/scale));
                cvtColor(inputs.get(i), resizeimage, Imgproc.COLOR_RGB2GRAY);
                resize(resizeimage, resizeimage,  new Size(inputs.get(i).size(0)/scaleSmall, inputs.get(i).size(1)/scaleSmall));
                inputsAligned.add(resizeimage);
            }

            // Set a 2x3 or 3x3 warp matrix depending on the motion model.
            // See https://www.learnopencv.com/image-alignment-ecc-in-opencv-c-python/
            // Define the motion model
            int warp_mode = MOTION_AFFINE;
            Mat warp_init;
            Mat warp_matrix;
            Mat warp_matrix_prev;
            MatOfFloat scaleTX;

            // Initialize the matrix to identity
            if (warp_mode == MOTION_HOMOGRAPHY) {
                warp_init = eye(3, 3, CV_32F);
                warp_matrix = eye(3, 3, CV_32F);
                warp_matrix_prev = eye(3, 3, CV_32F);
                scaleTX =new MatOfFloat( 3, 3, CV_32F);
                int row = 0, col = 0;
                scaleTX.put(row, col, 1, 1, scaleDiff, 1, 1, scaleDiff, 1 / scaleDiff, 1 / scaleDiff, 1);
            }
            else {
                warp_init = eye(2, 3, CV_32F);
                scaleTX = new MatOfFloat(2, 3, CV_32F);
                warp_matrix = eye(2, 3, CV_32F);
                warp_matrix_prev = eye(2, 3, CV_32F);
                int row = 0, col = 0;
                scaleTX.put(row, col, 1, 1, scaleDiff, 1, 1, scaleDiff);
            }

            // Specify the number of iterations.
            int number_of_iterations = 500;

            // Specify the threshold of the increment
            // in the correlation coefficient between two iterations
            double termination_eps = 1e-8;

            // Define termination criteria
            TermCriteria criteria(TermCriteria.COUNT + TermCriteria.EPS, number_of_iterations, termination_eps);

            for (int i = 1; i < inputs.size(); i++) {
                // Check images right size
                if (zg[0].rows < 10 || zg[1].rows < 10)
                    return;

                // Run the ECC algorithm at start to get an initial guess. The results are stored in warp_matrix.
                if (i == 1) {
                    findTransformECC(zgSmall[0], zgSmall[i], warp_init, warp_mode, criteria     );

                    // See https://stackoverflow.com/questions/45997891/cv2-motion-euclidean-for-the-warp-mode-in-ecc-image-alignment-method
                    warp_matrix = warp_init* scaleTX;
                }

                // Warp Matrix from previous iteration is used as initialisation
                findTransformECC(zg[0], zg[i], warp_matrix, warp_mode,  criteria);

                if (warp_mode != MOTION_HOMOGRAPHY) {
                    warpAffine(zg[i], ag[i], warp_matrix, zg[i].size(), INTER_LINEAR + WARP_INVERSE_MAP);
                    warpAffine(z[i], acol[i], warp_matrix, zg[i].size(), INTER_LINEAR + WARP_INVERSE_MAP);
                }
                else {
                    // Use warpPerspective for Homography
                    warpPerspective(z[i], acol[i], warp_matrix, z[i].size(), INTER_LINEAR + WARP_INVERSE_MAP);
                    warpPerspective(zg[i], ag[i], warp_matrix, zg[i].size(), INTER_LINEAR + WARP_INVERSE_MAP);
                }
            }
        }*/

        public void alignImagesHomography(ArrayList<Mat> imageList)
        {
            final int warp_mode = MOTION_HOMOGRAPHY;
            ArrayList<Mat> alignedMat = new ArrayList<Mat>();

            for(int i = 1; i<imageList.size(); i++){
                Log.d("alignImages", "aligning image " + String.valueOf(i));
                Log.d("imageSize", String.valueOf(imageList.get(i).height()) + " " + String.valueOf(imageList.get(i).width()));
                Mat matAgray = new Mat(imageList.get(i-1).height(), imageList.get(i-1).width(), CvType.CV_8U);
                Mat matBgray = new Mat(imageList.get(i).height(), imageList.get(i).width(), CvType.CV_8U);
                Mat matBaligned = new Mat(imageList.get(i).height(), imageList.get(i).width(), CvType.CV_8UC3);
                Mat warpMatrix = Mat.eye(3,3,CV_32F);

                Imgproc.cvtColor(imageList.get(i-1), matAgray, Imgproc.COLOR_BGR2GRAY);
                Imgproc.cvtColor(imageList.get(i), matBgray, Imgproc.COLOR_BGR2GRAY);
                int numIter = 500;
                double terminationEps = 1e-10;
                TermCriteria criteria = new TermCriteria(TermCriteria.COUNT + TermCriteria.EPS, numIter, terminationEps);
                findTransformECC(matAgray, matBgray, warpMatrix, warp_mode, criteria, matBgray);
                System.out.println(warpMatrix.dump());
                Imgproc.warpPerspective(imageList.get(i), matBaligned, warpMatrix, imageList.get(i-1).size(), Imgproc.INTER_LINEAR + Imgproc.WARP_INVERSE_MAP);
                //Imgproc.warpAffine(imageList.get(i), matBaligned, warpMatrix, imageList.get(i-1).size(), Imgproc.INTER_LINEAR + Imgproc.WARP_INVERSE_MAP);
//                Bitmap alignedBMP = Bitmap.createBitmap(imageList.get(i).height(), imageList.get(i).width(), Bitmap.Config.RGB_565);
//                Utils.matToBitmap(matBaligned, alignedBMP);
                //Log.d("matBAligned", matBaligned.dump());
                inputs.set(i, matBaligned);
                String filename= "aligned"+i+".bmp";
                Imgcodecs.imwrite(path + filename, matBaligned);
            }

        }

        public void alignImagesFeatureMatch(ArrayList<Mat> imageList){
            for(int i = 1; i<imageList.size(); i++) {
                Log.d("alignImages", "aligning image " + String.valueOf(i));
                ORB orb_detector = ORB.create(5000);
                Mat img1 = new Mat(imageList.get(i - 1).height(), imageList.get(i - 1).width(), CvType.CV_8U); //read image 0
                Mat img2 = new Mat(imageList.get(i).height(), imageList.get(i).width(), CvType.CV_8U); //read image 1
                Imgproc.cvtColor(imageList.get(i-1), img1, Imgproc.COLOR_BGR2GRAY); //convert image 0 to grayscale
                Imgproc.cvtColor(imageList.get(i), img2, Imgproc.COLOR_BGR2GRAY); //convert image 1 to grayscale

                Mat mask = null;
                MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
                Mat descriptors1 = new Mat();
                MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
                Mat descriptors2 = new Mat();

                //Extract features from both images
                orb_detector.detect(img1, keypoints1);
                orb_detector.detect(img2, keypoints2);
                orb_detector.compute(img1, keypoints1, descriptors1);
                orb_detector.compute(img2, keypoints2, descriptors2);

                DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

                // Match features between the 2 images
                MatOfDMatch matches = new MatOfDMatch();
                matcher.match(descriptors1, descriptors2, matches);

                //Filter out the matches based on their distance. Define a starting min and max distance and then identify the true min and max distance in the images
                List<DMatch> matchesList = matches.toList();
                Double max_dist = 0.0;
                Double min_dist = 100.0;

                for(int j = 0;j < matchesList.size(); j++){
                    Double dist = (double) matchesList.get(j).distance;
                    if (dist < min_dist)
                        min_dist = dist;
                    if ( dist > max_dist)
                        max_dist = dist;
                }

                //Filter the matches based on minimum distance
                LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
                for(int j = 0;j < matchesList.size(); j++){
                    if (matchesList.get(j).distance <= (10 * min_dist)) //Can change 10 to get more or less matches
                        good_matches.addLast(matchesList.get(j));
                }

                // Printing
                MatOfDMatch goodMatches = new MatOfDMatch();
                goodMatches.fromList(good_matches);
                System.out.println(matches.size() + " " + goodMatches.size());

                // get keypoint coordinates of good matches to find homography and remove outliers using ransac
                List<Point> pts1 = new ArrayList<Point>();
                List<Point> pts2 = new ArrayList<Point>();
                for(int j = 0; j<good_matches.size(); j++){
                    pts1.add(keypoints1.toList().get(good_matches.get(j).queryIdx).pt);
                    pts2.add(keypoints2.toList().get(good_matches.get(j).trainIdx).pt);
                }

                // convertion of data types - there is maybe a more beautiful way
                Mat outputMask = new Mat();
                MatOfPoint2f pts1Mat = new MatOfPoint2f();
                pts1Mat.fromList(pts1);
                MatOfPoint2f pts2Mat = new MatOfPoint2f();
                pts2Mat.fromList(pts2);

                // Find homography - here just used to perform match filtering with RANSAC, but could be used to e.g. stitch images
                // the smaller the allowed reprojection error (here 15), the more matches are filtered
                Mat Homog = Calib3d.findHomography(pts1Mat, pts2Mat, Calib3d.RANSAC, 15, outputMask, 2000, 0.995);

                // outputMask contains zeros and ones indicating which matches are filtered
/*                LinkedList<DMatch> better_matches = new LinkedList<DMatch>();
                for (int j = 0; j < good_matches.size(); j++) {
                    if (outputMask.get(j, 0)[0] != 0.0) {
                        better_matches.add(good_matches.get(j));
                    }
                }*/

                Mat transformed_img= new Mat(imageList.get(i).height(), imageList.get(i).width(), CvType.CV_8UC3);
                Imgproc.warpPerspective(imageList.get(i), transformed_img, Homog, imageList.get(i-1).size(), Imgproc.INTER_LINEAR + Imgproc.WARP_INVERSE_MAP);
                inputs.set(i, transformed_img);
                String filename= "aligned"+i+".bmp";
                Imgcodecs.imwrite(path + filename, transformed_img);

                Mat outputImg = new Mat();
                // this will draw all matches, works fine
                MatOfDMatch good_matches_mat = new MatOfDMatch();
                good_matches_mat.fromList(good_matches);
                Features2d.drawMatches(img1, keypoints1, img2, keypoints2, good_matches_mat, outputImg);

                // save image
                Imgcodecs.imwrite(path + "result.jpg", outputImg);


            }

        }


        public Mat laplacien(Mat image)
        {
            int kernel_size = 5;
            double blur_size = 5;

            Mat gray = new Mat();
            cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

            Mat gauss = new Mat();
            Imgproc.GaussianBlur(gray, gauss, new Size(blur_size, blur_size), 0);

            Mat laplace = new Mat();
            Imgproc.Laplacian(gauss, laplace, CvType.CV_64F, kernel_size, 1, 0);

            Mat absolute = new Mat();
            Core.convertScaleAbs(laplace, absolute);

            return absolute;
        }

        /**
         * apply focus stacking on inputs
         */
        public void focus_stack()
        {

            if(inputs.size() == 0)
            {
                System.out.println("please select some inputs");
            }
            else
            {
                System.out.println("Computing the laplacian of the blurred images");
                Mat[] laps = new Mat[inputs.size()];

                for (int i = 0 ; i < inputs.size() ; i++)
                {
                    System.out.println("image "+i);
                    laps[i] = laplacien(inputs.get(i));
                }

                Mat vide = Mat.zeros(laps[0].size(), inputs.get(0).type());

                for(int y = 0 ; y < laps[0].cols() ; y++)
                {
                    for(int x = 0 ; x < laps[0].rows() ; x++)
                    {
                        int index = -1;
                        double indexValue = -1;
                        for (int i = 0 ; i < laps.length ; i++)
                        {
                            if(indexValue == -1 || laps[i].get(x,y)[0] > indexValue)
                            {
                                indexValue = laps[i].get(x,y)[0];
                                index = i;
                            }
                        }
                        vide.put(x, y, inputs.get(index).get(x, y));
                    }
                }
                System.out.println("Success !");
                Imgcodecs.imwrite(path + "merged.bmp", vide);
            }
        }


        /**
         * Fill inputs list using the path
         */
        public void fill()
        {
            // Ouvre un repertoire
            File repertoire = new File(path);
            FilenameFilter filter = new FilenameFilter() {

                public boolean accept(File f, String name)
                {
                    return name.startsWith(time);
                }
            };

            if(!repertoire.exists())
            {
                System.out.println("directory : " + path + " doesn't exist");
            }

            else
            {
                // Liste les fichiers du repertorie
                File[] files = repertoire.listFiles(filter);
                Log.d("openCVPath", path);
                Log.d("time", time);
                Log.d("fileLen", String.valueOf(files.length));

                for(int i = 0 ; i < files.length ; i++)
                {
                    String nom = files[i].getName();
                    inputs.add(imread(path + nom));
                    System.out.println(inputs.get(i).height() + " " + inputs.get(i).width());
                }

                alignImagesFeatureMatch(inputs);
            }
        }


}
