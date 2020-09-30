package motepl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class JavaCVPrjt01 {
    static VideoCapture camera;

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

        Mat frame = new Mat();
        Mat outerBox = new Mat();
        Size sz = new Size(1280,720);
        //Size sz = new Size(400,120);


        System.out.println(camera.isOpened());
        camera.open("D:/Downloads/video4.mp4");
        //camera.open("resources\\videoSample.mp4");
        System.out.println(camera.isOpened());

        while (true) {
            if (camera.read(frame)) {
                //camera.read(frame);
                Imgproc.resize(frame,frame, sz );
                outerBox = new Mat(frame.size(), CvType.CV_8UC1);
                Imgproc.cvtColor(frame, outerBox, Imgproc.COLOR_BGR2GRAY);
                //Imgproc.GaussianBlur(outerBox, outerBox, new Size(3, 3), 0);

                ImageIcon image = new ImageIcon(Mat2bufferedImage(outerBox));
                vidpanel.setIcon(image);
                vidpanel.repaint();
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

}