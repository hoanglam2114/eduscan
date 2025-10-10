package org.project.backend.util;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImagePreprocessor {

    static {
        nu.pattern.OpenCV.loadLocally();
    }

    public static byte[] binarize(byte[] imageBytes) {
        Mat source = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_UNCHANGED);
        Mat gray = new Mat();
        Imgproc.cvtColor(source, gray, Imgproc.COLOR_BGR2GRAY);

        Mat binary = new Mat();
        Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".png", binary, matOfByte);
        return matOfByte.toArray();
    }
}