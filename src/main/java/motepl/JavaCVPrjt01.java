package motepl;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

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

    static {
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.load("D:\\Downloads\\opencv\\build\\java\\x64\\opencv_java2411.dll");
        System.load("D:\\Downloads\\opencv\\build\\x64\\vc12\\bin\\opencv_ffmpeg2411_64.dll");
        //-Djava.library.path=""
        //nu.pattern.OpenCV.loadShared();
        camera = new VideoCapture();
        System.out.println("Hey World !");
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
            if (frames == 10) {
                if (camera.read(frame)) {
                frames = 0;
                Imgproc.resize(frame, frame, sz);
                imag = frame.clone();
                outerBox = new Mat(frame.size(), CvType.CV_8UC1);
                //diff_frame2 = frame.clone();

                Imgproc.cvtColor(frame, outerBox, Imgproc.COLOR_BGR2GRAY);
                Imgproc.GaussianBlur(outerBox, outerBox, new Size(9, 9), 0);
                ArrayList<Rect> array = new ArrayList<Rect>();
                //Photo.fastNlMeansDenoising(outerBox, outerBox,5,3,3);
                if (i == 0) {
                    jframe.setSize(frame.width(), frame.height());
                    //diff_frame = new Mat(outerBox.size(), CvType.CV_8UC1);                    /
                    diff_frame = outerBox.clone();
                    imag2 = diff_frame;
                }

                if (i == 1) {
                    Core.subtract(outerBox, tempon_frame, diff_frame);
                    Imgproc.adaptiveThreshold(diff_frame, diff_frame, 1000,//255,
                            Imgproc.ADAPTIVE_THRESH_MEAN_C,
                            Imgproc.THRESH_BINARY_INV, 5, 2);
                    //imag2 = diff_frame;
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

        double maxArea = 100;
        int maxAreaIdx = -1;
        Rect r = null;
        ArrayList<Rect> rect_array = new ArrayList<Rect>();

        for (int idx = 0; idx < contours.size(); idx++) {
            Mat contour = contours.get(idx);
            double contourarea = Imgproc.contourArea(contour);
            if (contourarea > maxArea) {
                // maxArea = contourarea;
                maxAreaIdx = idx;
                r = Imgproc.boundingRect(contours.get(maxAreaIdx));
                rect_array.add(r);
                Imgproc.drawContours(imag, contours, maxAreaIdx, new Scalar(0, 0, 255));
                Imgproc.drawContours(imag2, contours, maxAreaIdx, new Scalar(0, 0, 255));
            }
        }
        v.release();

        return rect_array;

    }
}