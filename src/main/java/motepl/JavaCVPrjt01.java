package motepl;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JavaCVPrjt01 {
    static VideoCapture camera;
    static Mat imag = null;
    static Mat imag2 = null;
    static MatOfPoint correctCrossingArea = null;
    static MatOfPoint2f correctCrossingArea2f = null;
    static MatOfPoint incorrectCrossingArea = null;
    static MatOfPoint2f incorrectCrossingArea2f = null;
    static int counterSinceLastDetection = 15;

    static {
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.load("D:\\Downloads\\opencv\\build\\java\\x64\\opencv_java2411.dll");
        System.load("D:\\Downloads\\opencv\\build\\x64\\vc12\\bin\\opencv_ffmpeg2411_64.dll");
        //-Djava.library.path=""
        //nu.pattern.OpenCV.loadShared();
        camera = new VideoCapture();
        System.out.println("Hey World !");

        correctCrossingArea = new MatOfPoint(
                new Point(470, 580), new Point(470, 560), new Point(590, 550),
                new Point(800, 570), new Point(600, 620),
                new Point(470, 580)
        );
        correctCrossingArea2f = new MatOfPoint2f(correctCrossingArea.toArray());
        incorrectCrossingArea = new MatOfPoint(
                new Point(360, 500), new Point(530, 500), new Point(1100, 530),
                new Point(800, 540), //new Point(700, 620),
                new Point(360, 500)
        );
        incorrectCrossingArea2f = new MatOfPoint2f(incorrectCrossingArea.toArray());

        //Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
        //camera = new VideoCapture("resources/videoSample.mp4");
    }
    public static void main(String[] args) {
        JFrame jframe = new JFrame("HUMAN MOTION DETECTOR FPS");
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel vidpanel = new JLabel();
        jframe.setContentPane(vidpanel);
        jframe.setSize(1280,720) ;
        //jframe.setSize(400,120) ;
        jframe.setVisible(true);

        JFrame jframe2 = new JFrame("HUMAN MOTION DETECTOR FPS");
        jframe2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel vidpanel2 = new JLabel();
        jframe2.setContentPane(vidpanel2);
        jframe2.setSize(1280, 720);
        //jframe.setSize(400,120) ;
        jframe2.setVisible(true);

        Mat frame = new Mat();
        Mat outerBox = new Mat();
        Mat diff_frame = null;
        Mat diff_frame2 = null;
        Mat tempon_frame = null;
        Mat tempon_frame2 = null;
        Size sz = new Size(1280,720);
        //Size sz = new Size(400,120);


        System.out.println(camera.isOpened());
        camera.open("D:/Downloads/video5.mp4");
        //camera.open("resources\\videoSample.mp4");
        System.out.println(camera.isOpened());

        int i = 0;

        int frames = 0;

        while (true) {
            frames++;
            if (frames == 5) {
                if (camera.read(frame)) {
                frames = 0;
                Imgproc.resize(frame, frame, sz);
                imag = frame.clone();
                outerBox = new Mat(frame.size(), CvType.CV_8UC1);
                //diff_frame2 = frame.clone();

                Imgproc.cvtColor(frame, outerBox, Imgproc.COLOR_BGR2GRAY);
                Imgproc.GaussianBlur(outerBox, outerBox, new Size(5, 5), 0);
                ArrayList<Rect> array = new ArrayList<Rect>();
                //Photo.fastNlMeansDenoising(outerBox, outerBox,5,3,3);
                if (i == 0) {
                    jframe.setSize(frame.width(), frame.height());
                    //diff_frame = new Mat(outerBox.size(), CvType.CV_8UC1);                    /
                    diff_frame = outerBox.clone();
                    imag2 = diff_frame;
                }

                if (i == 1) {
                    diff_frame = tempon_frame;
                    Core.subtract(outerBox, tempon_frame, diff_frame);
                    Imgproc.adaptiveThreshold(diff_frame, diff_frame, 1000,//255,
                            Imgproc.ADAPTIVE_THRESH_MEAN_C,
                            Imgproc.THRESH_BINARY_INV, 5, 2);
                    //imag2 = diff_frame;

                    // Specify size on vertical axis
                    int vertical_size = 5;
                    // Create structure element for extracting vertical lines through morphology operations
                    Mat verticalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size( 1,vertical_size));
                    Mat thicken = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size( 3,20));
                    Mat horisontalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size( vertical_size,1));
                    Mat noice = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size( 1,3));
                    Mat car = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(200, 40));
                    Mat man = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size( 20,90));


                    //Mat blackhat = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size( 53,13));
                    //Mat blackhat = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size( 40,20));

                    Mat blackhat = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size( 40,30));

                    // Apply morphology operations
                    //Imgproc.erode(diff_frame, diff_frame, car);
                    //Imgproc.dilate(diff_frame, diff_frame, verticalStructure);
                    //Imgproc.morphologyEx(diff_frame,diff_frame, Imgproc.MORPH_GRADIENT, man);


                    Imgproc.morphologyEx(diff_frame,diff_frame, Imgproc.MORPH_BLACKHAT, blackhat);

                    /// /Imgproc.dilate(diff_frame, diff_frame, man);


                    //Imgproc.dilate(diff_frame, diff_frame, car);
                    //Imgproc.erode(diff_frame, diff_frame, car);

                    //Imgproc.dilate(diff_frame, diff_frame, man);
                    //Imgproc.erode(diff_frame, diff_frame, man);
                    //Imgproc.dilate(diff_frame, diff_frame,thicken);
                    //Imgproc.erode(diff_frame, diff_frame, noice);

                    array = detection_contours(diff_frame);
                    tempon_frame2 = new Mat(frame.size(), CvType.CV_8UC3);
                    Imgproc.cvtColor(diff_frame, tempon_frame2, Imgproc.COLOR_GRAY2BGR);
                    imag2 = tempon_frame2;
                    if (array.size() > 0) {

                        Iterator<Rect> it2 = array.iterator();
                        while (it2.hasNext()) {
                            Rect obj = it2.next();
                            Core.rectangle(imag, obj.br(), obj.tl(),
                                    new Scalar(0, 255, 0), 1);
                            Core.rectangle(imag2, obj.br(), obj.tl(),
                                    new Scalar(0, 255, 0), 1);
                        }

                    }

                    //areas of interest:

                    //Drawing an arrowed line

                    List<MatOfPoint> matOfPointListCorrect = new ArrayList<>();
                    matOfPointListCorrect.add(correctCrossingArea);

                    List<MatOfPoint> matOfPointListIncorrect = new ArrayList<>();
                    matOfPointListIncorrect.add(incorrectCrossingArea);


                    Imgproc.drawContours(imag, matOfPointListCorrect, -1, new Scalar(0, 255, 255));
                    Imgproc.drawContours(imag2, matOfPointListCorrect, -1, new Scalar(0, 255, 255));

                    Imgproc.drawContours(imag, matOfPointListIncorrect, -1, new Scalar(255, 255, 0));
                    Imgproc.drawContours(imag2, matOfPointListIncorrect, -1, new Scalar(255, 255, 0));
                }
                i = 1;

                ImageIcon image = new ImageIcon(Mat2bufferedImage(imag));
                vidpanel.setIcon(image);
                vidpanel.repaint();

                ImageIcon image2 = new ImageIcon(Mat2bufferedImage(imag2));
                vidpanel2.setIcon(image2);
                vidpanel2.repaint();

                tempon_frame = outerBox.clone();
                }
            }
            else {
                camera.read(frame);
            }
        }
    }

    public static BufferedImage Mat2bufferedImage(Mat image) {
        MatOfByte bytemat = new MatOfByte();
        Highgui.imencode(".jpg", image, bytemat);
        byte[] bytes = bytemat.toArray();
        InputStream in = new ByteArrayInputStream(bytes);
        BufferedImage img = null;
        try {
            img = ImageIO.read(in);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return img;
    }

    public static ArrayList<Rect> detection_contours(Mat outmat) {
        Mat v = new Mat();
        Mat vv = outmat.clone();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        //Imgproc.findCont ours(vv, contours, v, Imgproc.RETR_LIST,
        //        Imgproc.CHAIN_APPROX_SIMPLE);
        //Imgproc.Canny(vv,vv, 10, 50, 3, false);
        Imgproc.findContours(vv, contours, v, Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = 1000;
        double minArea = 400;
        int maxAreaIdx = -1;
        Rect r = null;
        ArrayList<Rect> rect_array = new ArrayList<Rect>();

        int movingContourCount = 0;
        int objectsMoving = 0;
        int detectedFactsMin = 3;
        final int detectedFactsMax = 15;


        for (int idx = 0; idx < contours.size(); idx++) {
            Mat contour = contours.get(idx);
            double contourarea = Imgproc.contourArea(contour);

            if (contourarea > minArea && contourarea < maxArea) {
                // && contour.width() < contour.height() toto understand why it allows wide contours
                // maxArea = contourarea;
                maxAreaIdx = idx;
                r = Imgproc.boundingRect(contours.get(maxAreaIdx));
                if (r.width < r.height) {

                    double rX1 = r.tl().x;
                    double rY1 = r.tl().y;
                    double rX2 = r.br().x;
                    double rY2 = r.br().y;

                    if (
                            Imgproc.pointPolygonTest(correctCrossingArea2f, r.tl(), true) >= 0 ||
                                    Imgproc.pointPolygonTest(correctCrossingArea2f, r.br(), true) >= 0 ||
                                    Imgproc.pointPolygonTest(correctCrossingArea2f, new Point(rX1, rY2), true) >= 0 ||
                                    Imgproc.pointPolygonTest(correctCrossingArea2f, new Point(rX2, rY1), true) >= 0
                                    ||
                                    Imgproc.pointPolygonTest(incorrectCrossingArea2f, r.tl(), true) >= 0 ||
                                    Imgproc.pointPolygonTest(incorrectCrossingArea2f, r.br(), true) >= 0 ||
                                    Imgproc.pointPolygonTest(incorrectCrossingArea2f, new Point(rX1, rY2), true) >= 0 ||
                                    Imgproc.pointPolygonTest(incorrectCrossingArea2f, new Point(rX2, rY1), true) >= 0 ||
                                    Imgproc.pointPolygonTest(incorrectCrossingArea2f, massCenterMatOfPoint2f(contour), true) >= 0 ||
                                    Imgproc.pointPolygonTest(correctCrossingArea2f, massCenterMatOfPoint2f(contour), true) >= 0
                            ) {
                        rect_array.add(r);
                        Imgproc.drawContours(imag, contours, maxAreaIdx, new Scalar(0, 0, 255));
                        Imgproc.drawContours(imag2, contours, maxAreaIdx, new Scalar(0, 0, 255));

                        //new movement started
                        movingContourCount = rect_array.size();
                        counterSinceLastDetection = detectedFactsMax;
                    }
                }
            }
        }
        if (counterSinceLastDetection > 0) {
            counterSinceLastDetection--;
        }
        System.out.println("Detected counters in total: " + movingContourCount + "(" + counterSinceLastDetection + ")");
        v.release();

        return rect_array;

    }

    public static Point massCenterMatOfPoint2f(final Mat map) {
        final Moments moments = Imgproc.moments(map);
        final Point centroid = new Point();
        centroid.x = moments.get_m10() / moments.get_m00();
        centroid.y = moments.get_m01() / moments.get_m00();
        return centroid;
    }
}