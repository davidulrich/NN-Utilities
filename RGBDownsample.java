/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imaging;

import java.awt.Image;
import java.awt.image.PixelGrabber;

import training.NetworkError;
import imaging.ImageSize;
/**
 *
 * @author David
 */
public class RGBDownsample implements Downsample{
    
    private int[] pixelMap;
    private double ratioX;
    private double ratioY;
    private int imageHeight;
    private int imageWidth;
    private int downSampleLeft;
    private int downSampleRight;
    private int downSampleTop;
    private int downSampleBottom;
    private int currentRed;
    private int currentGreen;
    private int currentBlue;
    
    public double[] downSample(final Image image, final int height, final int width) {
        
        processImage(image);
        
        final double[] result = new double[height * width * 3];
        
        final PixelGrabber grabber = new PixelGrabber(image, 0, 0, this.imageWidth, this.imageHeight, true);
        
        try {
            grabber.grabPixels();
        } catch (final InterruptedException e) {
            throw new NetworkError(e);
        }
        
        this.pixelMap = (int[]) grabber.getPixels();
        
        //begin downsampling
        
        this.ratioX = (double) (this.downSampleRight - this.downSampleLeft)
                / (double) width;
        this.ratioY = (double) (this.downSampleBottom - this.downSampleTop)
                / (double) height;
        
        int index = 0;
        for(int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                downSampleRegion(x,y);
                result[index++] = this.currentRed;
                result[index++] = this.currentGreen;
                result[index++] = this.currentBlue;
                
                //System.out.println(index + " (" + this.currentRed + "," + this.currentGreen + "," + this.currentBlue + ")");
            }
        }
        
        return result;
    }
    
    public void downSampleRegion(final int x, final int y) {
        final int startX = (int) (this.downSampleLeft + x * this.ratioX);
        final int startY = (int) (this.downSampleTop + y * this.ratioY);
        int endX = (int) (startX + this.ratioX);
        int endY = (int) (startY + this.ratioY);
        
        endX = Math.min(this.imageWidth, endX);
        endY = Math.min(this.imageHeight, endY);
        
        int redTotal = 0;
        int greenTotal = 0;
        int blueTotal = 0;
        
        int total = 0;
        
        for (int yy = startY; yy < endY; yy++) {
            for (int xx = startX; xx < endX; xx++) {
                final int loc = xx + yy * this.imageWidth;
                final int pixel = this.pixelMap[loc];
                final int red = (pixel >> 16 & 0xff);
                final int green = (pixel >> 8 & 0xff);
                final int blue = (pixel & 0xff);
                
                redTotal += red;
                greenTotal += green;
                blueTotal += blue;
                total++;
            }
        }
        
        this.currentRed = redTotal / total;
        this.currentGreen = greenTotal / total;
        this.currentBlue = blueTotal / total;
    }
    
    //remove whitespace from image
    public void findBounds() {
        for (int y = 0; y < this.imageHeight; y++) {
            if(!hLineClear(y)) { this.downSampleTop = y; break; }
        }
        for(int y = this.imageHeight - 1; y >=0; y--) {
            if(!hLineClear(y)) { this.downSampleBottom = y; break; }
        }
        for(int x = 0; x < this.imageWidth; x++) {
            if(!vLineClear(x)) { this.downSampleLeft = x; break; }
        }
        for(int x = this.imageWidth - 1; x >= 0; x--) {
            if(!vLineClear(x)) { this.downSampleRight = x; break; }
        }
    }
    
    public int getCurrentRed() { return this.currentRed; }
    public int getCurrentGreen() { return this.currentGreen; }
    public int getCurrentBlue() { return this.currentBlue; }
    public int getDownSampleBottom() { return this.downSampleBottom; }
    public int getDownSampleTop() { return this.downSampleTop;}
    public int getDownSampleLeft() { return this.downSampleLeft; }
    public int getDownSampleRight() { return this.downSampleRight; }
    public int getImageHeight() { return this.imageHeight; }
    public int getImageWidth() { return this.imageWidth; }
    public double getRatioX() { return this.ratioX; }
    public double getRatioY() { return this.ratioY; }
    public int[] getPixelMap() { return this.pixelMap; }
    
    private boolean hLineClear(final int y) {
        for(int i = 0; i < this.imageWidth; i++) {
            if(this.pixelMap[y * this.imageWidth + i] != -1) { return false; }
        }
        
        return true;
    }
    
    private boolean vLineClear(final int x) {
        for(int i = 0; i < this.imageHeight; i++) {
            if(this.pixelMap[i * this.imageHeight + x] != -1) { return false; }
        }
        
        return true;
    }
    
    public void processImage(final Image image) {
        final ImageSize size = new ImageSize(image);
        this.imageHeight = size.getHeight();
        this.imageWidth = size.getWidth();
        this.downSampleLeft = 0;
        this.downSampleTop = 0;
        this.downSampleRight = this.imageWidth;
        this.downSampleBottom = this.imageHeight;
        
        this.ratioX = (double) (this.downSampleRight - this.downSampleLeft) / (double) getImageWidth();
        this.ratioY = (double) (this.downSampleBottom - this.downSampleTop) / (double) getImageHeight();
    }
    
    public void setCurrentRed(final int currentRed) { this.currentRed = currentRed; }
    public void setCurrentBlue(final int currentBlue) { this.currentBlue = currentBlue; }
    public void setCurrentGreen(final int currentGreen) { this.currentGreen = currentGreen; }
    
    public void setPixelMap(final int[] pixelMap) { this.pixelMap = pixelMap; }
}
