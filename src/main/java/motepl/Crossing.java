package motepl;

import java.awt.image.BufferedImage;

/**
 * Created by eljah32 on 10/1/2020.
 */
public class Crossing {
    public BufferedImage bufferedImage;
    public double x;
    public double y;
    public long timestamp;

    public Crossing(BufferedImage bufferedImage, double x, double y, long timestamp) {
        this.bufferedImage = bufferedImage;
        this.x = x;
        this.y = y;
        this.timestamp = timestamp;
    }
}
