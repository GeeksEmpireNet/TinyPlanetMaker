package org.hofapps.tinyplanet;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by fabian on 06.10.2015.
 */
public class PlanetMaker {

    private static final double DEG2RAD = 0.017453292519943;

    private Mat inputImage, planetImage;
    private NativeWrapper nativeWrapper;
    private int outputSize;
    private double size, scale, angle;

    public PlanetMaker(Mat inputImage, NativeWrapper nativeWrapper,int outputSize) {

        this.inputImage = inputImage;
        this.nativeWrapper = nativeWrapper;
        this.outputSize = outputSize;

        initImages();

    }

    public Mat getPlanetImage() {

        return planetImage;

    }

    public void setSize(double size) {

        this.size = size;
        updatePlanet();

    }

    public void setScale(double scale) {

        this.scale = scale;
        updatePlanet();

    }

    public void setAngle(double angle) {

        this.angle = angle;
        updatePlanet();

    }

    private void updatePlanet() {

        nativeWrapper.logPolar(inputImage, planetImage, inputImage.width() * 0.5f, inputImage.height() * 0.5f, size, scale, angle * DEG2RAD);

    }

    private void initImages() {

        Imgproc.resize(inputImage, inputImage, new Size(outputSize, outputSize), 0, 0, Imgproc.INTER_CUBIC);

//        Rotate the image 90 degrees:
        Core.flip(inputImage.t(), inputImage, 1);

//            We need COLOR_BGR2RGBA to flip the color channel AND to get a transparent background:
        Imgproc.cvtColor(inputImage, inputImage, Imgproc.COLOR_BGR2RGBA);
        planetImage = new Mat(inputImage.rows(), inputImage.cols(), inputImage.type());


    }







}