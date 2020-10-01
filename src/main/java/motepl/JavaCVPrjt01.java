package motepl;

import com.google.common.collect.EvictingQueue;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

public class JavaCVPrjt01 {
    static VideoCapture camera;
    static Mat imag = null;
    static Mat imag2 = null;
    static MatOfPoint correctCrossingArea = null;
    static MatOfPoint2f correctCrossingArea2f = null;
    static MatOfPoint incorrectCrossingArea = null;
    static MatOfPoint2f incorrectCrossingArea2f = null;
    static int counterSinceLastDetection = 30;
    static volatile int counterRegistration = 0;
    final static int counterSinceLastDetectionMax = 30;
    final static int counterForRegistrationMax = 3;
    static int contoursCount = 0;
    static int olderContoursCount = 0;
    static int olderCounterSinceLastDetection = 0;

    static List<BufferedImage> images = new ArrayList<>();
    static List<Double> coordinatesX = new ArrayList<>();
    static List<Double> coordinatesY = new ArrayList<>();
    static Date timestamp = null;

    static EvictingQueue<Crossing> correctCrossing = EvictingQueue.create(30);
    static EvictingQueue<Crossing> incorrectCrossing = EvictingQueue.create(30);

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
        jframe.setSize(1280, 720);
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
        Size sz = new Size(1280, 720);
        //Size sz = new Size(400,120);


        System.out.println(camera.isOpened());
        camera.open("D:/Downloads/video5.mp4");
        //camera.open(0);
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
                        Mat verticalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, vertical_size));
                        Mat thicken = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 20));
                        Mat horisontalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(vertical_size, 1));
                        Mat noice = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 3));
                        Mat car = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(200, 40));
                        Mat man = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(20, 90));


                        //Mat blackhat = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size( 53,13));
                        //Mat blackhat = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size( 40,20));

                        Mat blackhat = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(40, 30));

                        // Apply morphology operations
                        //Imgproc.erode(diff_frame, diff_frame, car);
                        //Imgproc.dilate(diff_frame, diff_frame, verticalStructure);
                        //Imgproc.morphologyEx(diff_frame,diff_frame, Imgproc.MORPH_GRADIENT, man);


                        Imgproc.morphologyEx(diff_frame, diff_frame, Imgproc.MORPH_BLACKHAT, blackhat);

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

                        Scalar green = null;
                        int frameThikness = 1;

                        if (isMotionContinuationDetected()) {
                            green = new Scalar(0, 255, 0);
                            frameThikness = 1;
                        }
                        if (isMotionStartDetected()) {
                            green = new Scalar(255, 0, 255);
                            frameThikness = 1;
                        }

                        if (array.size() > 0) {

                            Iterator<Rect> it2 = array.iterator();
                            while (it2.hasNext()) {
                                Rect obj = it2.next();
                                Core.rectangle(imag, obj.br(), obj.tl(),
                                        green, frameThikness);
                                Core.rectangle(imag2, obj.br(), obj.tl(),
                                        green, frameThikness);
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

                    long currentTimestamp = new Date().getTime();

                    // Adding Text
                    Core.putText(
                            imag,                          // Matrix obj of the image
                            currentTimestamp + "",          // Text to be added
                            new Point(10, 50),               // point
                            Core.FONT_HERSHEY_SIMPLEX,      // front face
                            1,                               // front scale
                            new Scalar(0, 0, 0),             // Scalar object for color
                            4                                // Thickness
                    );

                    BufferedImage humanVision;// = Mat2bufferedImage(imag);
                    if (array.size() > 0) {
//                        try {
//                            saveIfNeeded(humanVision, (array.get(0).br().x + array.get(0).x) / 2, (array.get(0).br().y + array.get(0).y) / 2
//                            );
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                        Iterator<Rect> it3 = array.iterator();
                        while (it3.hasNext()) {
                            Rect obj = it3.next();
                            Point objectPoint=new Point((obj.br().x + obj.x) / 2, (obj.br().y + obj.y) / 2);
                            if (Imgproc.pointPolygonTest(correctCrossingArea2f, objectPoint, true) >= 0) {
                                Core.putText(
                                        imag,                          // Matrix obj of the image
                                        "CORRECT AREA",          // Text to be added
                                        new Point(900, 50),               // point
                                        Core.FONT_HERSHEY_SIMPLEX,      // front face
                                        1,                               // front scale
                                        new Scalar(0, 0, 0),             // Scalar object for color
                                        4                                // Thickness
                                );
                                Core.putText(
                                        imag,                          // Matrix obj of the image
                                        "X="+objectPoint.x+" Y="+objectPoint.y,          // Text to be added
                                        new Point(500, 50),               // point
                                        Core.FONT_HERSHEY_SIMPLEX,      // front face
                                        1,                               // front scale
                                        new Scalar(0, 0, 0),             // Scalar object for color
                                        4                                // Thickness
                                );

                                correctCrossing.add(new Crossing(humanVision = Mat2bufferedImage(imag), (obj.br().x + obj.x) / 2, (obj.br().y + obj.y) / 2, currentTimestamp));
                            }

                            if (Imgproc.pointPolygonTest(incorrectCrossingArea2f, objectPoint, true) >= 0) {
                                Core.putText(
                                        imag,                          // Matrix obj of the image
                                        "INCORRECT AREA",          // Text to be added
                                        new Point(900, 50),               // point
                                        Core.FONT_HERSHEY_SIMPLEX,      // front face
                                        1,                               // front scale
                                        new Scalar(0, 0, 0),             // Scalar object for color
                                        4                                // Thickness
                                );
                                Core.putText(
                                        imag,                          // Matrix obj of the image
                                        "X="+objectPoint.x+" Y="+objectPoint.y,          // Text to be added
                                        new Point(500, 50),               // point
                                        Core.FONT_HERSHEY_SIMPLEX,      // front face
                                        1,                               // front scale
                                        new Scalar(0, 0, 0),             // Scalar object for color
                                        4                                // Thickness
                                );

                                incorrectCrossing.add(new Crossing(humanVision = Mat2bufferedImage(imag), (obj.br().x + obj.x) / 2, (obj.br().y + obj.y) / 2, currentTimestamp));
                            }

                        }
                    }

                    ImageIcon image = new ImageIcon(Mat2bufferedImage(imag));
                    vidpanel.setIcon(image);
                    vidpanel.repaint();

                    ImageIcon image2 = new ImageIcon(Mat2bufferedImage(imag2));
                    vidpanel2.setIcon(image2);
                    vidpanel2.repaint();

                    tempon_frame = outerBox.clone();

                    saveIfNeeded();
                }
            } else {
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

        int objectsMoving = 0;
        int detectedFactsMin = 3;
        //final int detectedFactsMax = 15;

        olderContoursCount = contoursCount;
        olderCounterSinceLastDetection = counterSinceLastDetection;

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
                        counterSinceLastDetection = counterSinceLastDetectionMax;

                    }
                }
            }
        }
        contoursCount = rect_array.size();

        if (counterSinceLastDetection > 0) {
            counterSinceLastDetection--;
        }
        System.out.println("Detected counters in total: " + contoursCount + "(" + counterSinceLastDetection + ")");
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

    public static boolean isMotionContinuationDetected() {
        boolean toBeReturned = false;
        toBeReturned = contoursCount >= 1
                && counterSinceLastDetection >= olderCounterSinceLastDetection
                && olderCounterSinceLastDetection > 0;
        System.out.println(contoursCount + "/" + olderContoursCount + "/" + counterSinceLastDetection + "/" + olderCounterSinceLastDetection + "/" + counterRegistration);
        if (toBeReturned) {
            System.out.println("CONT");
            counterRegistration++;
        }
        return toBeReturned;
    }

    public static boolean isMotionStartDetected() {
        boolean toBeReturned = false;
        toBeReturned = contoursCount >= olderContoursCount
                && counterSinceLastDetection > olderCounterSinceLastDetection
                && olderCounterSinceLastDetection == 0;
        System.out.println(contoursCount + "/" + olderContoursCount + "/" + counterSinceLastDetection + "/" + olderCounterSinceLastDetection + "/" + counterRegistration);
        if (toBeReturned) {
            System.out.println("START");
            counterRegistration = 0;
            images.clear();
            coordinatesX.clear();
            coordinatesY.clear();
        }
        return toBeReturned;
    }

    public static void saveIfNeeded(BufferedImage image, double xCoordinate, double yCoordinate) throws IOException {
        int heightTotal = 0;
        int maxWidth = 100;

        if (counterRegistration == 1 && images.size() == 0) {
            System.out.println("ADDING 1!");
            timestamp = new Date();
            images.add(image);
            coordinatesX.add(xCoordinate);
            coordinatesY.add(yCoordinate);
        }
        if (counterRegistration == 2 && images.size() == 1) {
            System.out.println("ADDING 2!");
            images.add(image);
            coordinatesX.add(xCoordinate);
            coordinatesY.add(yCoordinate);
        }
        if (counterRegistration >= 3 && images.size() == 2 && counterSinceLastDetection > 0
                && (coordinatesX.get(1) - coordinatesX.get(0)) / (xCoordinate - coordinatesX.get(1)) > 0
                && (coordinatesY.get(1) - coordinatesY.get(0)) / (yCoordinate - coordinatesY.get(1)) > 0
                ) {
            System.out.println("SAVING!");
            System.out.println((coordinatesX.get(1) - coordinatesX.get(0)) / (xCoordinate - coordinatesX.get(1)));
            System.out.println("(" + coordinatesX.get(1) + "-" + coordinatesX.get(0) + ")/(" + xCoordinate + "-" + coordinatesX.get(1) + ")");
            System.out.println((coordinatesY.get(1) - coordinatesY.get(0)) / (yCoordinate - coordinatesY.get(1)));
            System.out.println("(" + coordinatesY.get(1) + "-" + coordinatesY.get(0) + ")/(" + yCoordinate + "-" + coordinatesY.get(1) + ")");
            images.add(image);
            coordinatesX.add(xCoordinate);
            coordinatesY.add(yCoordinate);
            for (BufferedImage bufferedImage : images) {
                heightTotal += bufferedImage.getHeight();
                if (bufferedImage.getWidth() > maxWidth) {
                    maxWidth = bufferedImage.getWidth();
                }
            }


            int heightCurr = 0;
            BufferedImage concatImage = new BufferedImage(maxWidth, heightTotal, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = concatImage.createGraphics();
            for (BufferedImage bufferedImage : images) {
                g2d.drawImage(bufferedImage, 0, heightCurr, null);
                heightCurr += bufferedImage.getHeight();
            }

            File compressedImageFile = new File("D:\\downloads\\crossing" + timestamp.getTime() + ".jpg");
            OutputStream outputStream = new FileOutputStream(compressedImageFile);


            float imageQuality = 0.9f;
            Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName("jpeg");

            if (!imageWriters.hasNext())
                throw new IllegalStateException("Writers Not Found!!");

            ImageWriter imageWriter = imageWriters.next();
            ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
            imageWriter.setOutput(imageOutputStream);

            ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();

            //Set the compress quality metrics
            imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            imageWriteParam.setCompressionQuality(imageQuality);

            //Created image
            imageWriter.write(null, new IIOImage(concatImage, null, null), imageWriteParam);

            // close all streams
            outputStream.close();
            imageOutputStream.close();
            imageWriter.dispose();
            images.clear();
            coordinatesX.clear();
        }
    }

    public static void saveIfNeeded() {
        System.out.println("INCORRECT: " + incorrectCrossing.stream().count());
        Set<Crossing> incorrectCrossingList = new HashSet<>();// = incorrectCrossing.stream().collect(Collectors.toList());
        List<CrossingPair> incorrectCrossingPairsList = new LinkedList<>();
        incorrectCrossing.stream().reduce( (y, z) -> {
                    if ((z.timestamp - y.timestamp) > 150
                            && (z.timestamp - y.timestamp) < 4000
                            && Math.abs(z.x - y.x) > 10
                            //&& (z.x - y.x)/(z.y - y.y) < 0.5
                            ) {
                        incorrectCrossingList.add(y);
                        incorrectCrossingList.add(z);
                        CrossingPair crossingPair = new CrossingPair(y, z);
                        incorrectCrossingPairsList.add(crossingPair);
                    }
                    return z;
                }
        );
        incorrectCrossingPairsList.stream().reduce((y, z) -> {
                    if (    y.one.timestamp != z.two.timestamp &&
                            y.one.timestamp < z.one.timestamp &&
                            (z.one.x - z.two.x) / (y.one.x - y.two.x) > 0 &&
                            (z.one.timestamp - z.two.timestamp) / (y.one.timestamp - y.two.timestamp) > 0 &&
                            (z.one.timestamp < z.two.timestamp) && (y.one.timestamp < y.two.timestamp) && (y.two.timestamp == z.one.timestamp)
                            ) {
                        try {
                            System.out.println("REMOVAL INCORRECT FROM QUEUE");
                            incorrectCrossing.remove(z.one);
                            incorrectCrossing.remove(z.two);
                            incorrectCrossing.remove(y.one);
                            incorrectCrossing.remove(y.two);
                            saveThreeImage(y, z, false);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return z;
                }
        );
        System.out.println("INCORRECT ONE TRACE: " + incorrectCrossingList.size());
        System.out.println("INCORRECT ONE TRACE PAIRS: " + incorrectCrossingPairsList.size());
        System.out.println("CORRECT: " + correctCrossing.stream().count());
        Set<Crossing> correctCrossingList = new HashSet<>();// correctCrossing.stream().collect(Collectors.toList());
        List<CrossingPair> correctCrossingPairsList = new LinkedList<>();
        correctCrossing.stream().reduce( (y, z) -> {
                    if ((z.timestamp - y.timestamp) > 150
                            && (z.timestamp - y.timestamp) < 2000
                            && Math.abs(z.x - y.x) > 5
                            //&& (z.x - y.x)/(z.y - y.y) > 0
                            ) {
                        correctCrossingList.add(y);
                        correctCrossingList.add(z);
                        CrossingPair crossingPair = new CrossingPair(y, z);
                        correctCrossingPairsList.add(crossingPair);
                    }
                    return z;
                }
        );
        correctCrossingPairsList.stream().reduce((y, z) -> {
                    if (    y.one.timestamp != z.two.timestamp &&
                            y.one.timestamp < z.one.timestamp &&
                            (z.one.y - z.two.y)/(y.two.y - y.one.y) > 0 &&  //only here
                            (z.one.x - z.two.x) / (y.one.x - y.two.x) > 0 &&
                            (z.one.timestamp - z.two.timestamp) / (y.one.timestamp - y.two.timestamp) > 0 &&
                            (z.one.timestamp < z.two.timestamp) && (y.one.timestamp < y.two.timestamp) &&
                            (y.two.timestamp == z.one.timestamp)
                            ) {
                        try {
                            System.out.println("REMOVAL CORRECT FROM QUEUE");
                            correctCrossing.remove(z.one);
                            correctCrossing.remove(z.two);
                            correctCrossing.remove(y.one);
                            correctCrossing.remove(y.two);
                            saveThreeImage(y, z, true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return z;
                }

        );
        System.out.println("CORRECT ONE TRACE: " + correctCrossingList.size());
        System.out.println("CORRECT ONE TRACE PAIRS: " + correctCrossingPairsList.size());
//        if (correctCrossingPairsList.size() > 1) {
//            try {
//                saveThreeImage(correctCrossingPairsList.get(0), correctCrossingPairsList.get(1), true);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        if (incorrectCrossingPairsList.size() > 1) {
//            try {
//                saveThreeImage(incorrectCrossingPairsList.get(0), incorrectCrossingPairsList.get(1), false);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    public static void saveThreeImage(CrossingPair one, CrossingPair two, boolean correct) throws IOException {
        BufferedImage oneImage = one.one.bufferedImage;
        BufferedImage twoImage = one.two.bufferedImage;
        BufferedImage twoImage_ = two.one.bufferedImage;
        BufferedImage threeImage = two.two.bufferedImage;
        System.out.println("EQ CHECK: " + twoImage.equals(twoImage_));
        Long timestamp = one.two.timestamp;

        List<BufferedImage> imagesList = new LinkedList<>();
        imagesList.add(oneImage);
        imagesList.add(twoImage);
        imagesList.add(threeImage);

        int heightTotal = 0;
        int maxWidth = 100;

        for (BufferedImage bufferedImage : imagesList) {
            heightTotal += bufferedImage.getHeight();
            if (bufferedImage.getWidth() > maxWidth) {
                maxWidth = bufferedImage.getWidth();
            }
        }


        int heightCurr = 0;
        BufferedImage concatImage = new BufferedImage(maxWidth, heightTotal, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = concatImage.createGraphics();
        for (BufferedImage bufferedImage : imagesList) {
            g2d.drawImage(bufferedImage, 0, heightCurr, null);
            heightCurr += bufferedImage.getHeight();
        }

        File compressedImageFile = new File("D:\\downloads\\crossing_" + (correct ? "CORRECT" : "INCORRECT") + "_" + timestamp + ".jpg");
        OutputStream outputStream = new FileOutputStream(compressedImageFile);


        float imageQuality = 0.9f;
        Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName("jpeg");

        if (!imageWriters.hasNext())
            throw new IllegalStateException("Writers Not Found!!");

        ImageWriter imageWriter = imageWriters.next();
        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
        imageWriter.setOutput(imageOutputStream);

        ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();

        //Set the compress quality metrics
        imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        imageWriteParam.setCompressionQuality(imageQuality);

        //Created image
        imageWriter.write(null, new IIOImage(concatImage, null, null), imageWriteParam);

        // close all streams
        outputStream.close();
        imageOutputStream.close();
        imageWriter.dispose();
    }
}