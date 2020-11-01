import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;

public class Main{
    private static double percentTotal = 0.0;
    private static String writerComp = "";


    public static boolean nullCheck(BufferedImage img, BufferedImage img2){
        return (img != null && img2 != null);
    }
    public static boolean widthCheck(int width, int width2){
        return (width == width2);
    }
    public static boolean heightCheck(int height, int height2){
        return (height == height2);
    }
    public static void imageCompare(BufferedImage img, BufferedImage img2, int width, int height){
        BufferedImage view = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = ((DataBufferInt) view.getRaster().getDataBuffer()).getData();

        BufferedImage view2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels2 = ((DataBufferInt) view2.getRaster().getDataBuffer()).getData();

        int[] imagePixels = new int[width * height];
        img.getRGB(0, 0, width, height, imagePixels, 0, width);

        int[] imagePixels2 = new int[width * height];
        img2.getRGB(0, 0, width, height, imagePixels2, 0, width);


        all: for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++) {
                pixels[x + y * width] = imagePixels[x + y * width];
                pixels2[x + y * width] = imagePixels2[x + y * width];

                int red = ((pixels[x + y * width] >> 16) & 0xFF);
                int green = ((pixels[x + y * width] >> 8) & 0xFF);
                int blue = (pixels[x + y * width] & 0xFF);

                int red2 = ((pixels2[x + y * width] >> 16) & 0xFF);
                int green2 = ((pixels2[x + y * width] >> 8) & 0xFF);
                int blue2 = (pixels2[x + y * width] & 0xFF);
                RGBCompare(red, red2, green, green2, blue, blue2,x,y);
            }
        }
        BigDecimal pa = new BigDecimal(percentTotal/(width*height));
        pa = pa.round(new MathContext(5));
        writerComp += "Accuracy: " + pa + "%\n";
        percentTotal = 0;
    }
    public static void orientation(BufferedImage img, BufferedImage img2, int width, int width2, int height, int height2){
        if(width != width2){
            writerComp += "Widths do not match: " + width + " vs " + width2 + "\n";
        }
        if(height != height2){
            writerComp += "Heights do not match: " + height + " vs " + height2 +"\n";
        }
        writerComp += "Attempting to resize\n";

        String orientation;
        String orientation2;
        if(width > height)
            orientation = "LANDSCAPE"; //LANDSCAPE
        else if (width < height)
            orientation = "PORTRAIT"; //PORTRAIT
        else
            orientation = "SQUARE"; //SQUARE
        if(width2 > height2)
            orientation2 = "LANDSCAPE";
        else if (width2 < height2)
            orientation2 = "PORTRAIT";
        else
            orientation2 = "SQUARE";

        if(orientation == orientation2) {
            writerComp += "Both are " + orientation + ".  Checking which to resize...\n";
            if(width > width2 || height > height2) {
                BufferedImage newImage = new BufferedImage(width2, height2, BufferedImage.TYPE_INT_RGB);

                Graphics g = newImage.createGraphics();
                g.drawImage(img, 0, 0, width2, height2, null);
                g.dispose();
                writerComp += "Resizing the " + width + " x " + height + " image to " + width2 + " x " + height2 + "\n";
                imageCompare(img2, newImage, width2, height2);
            }
            else if(width < width2 || height < height2){
                BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

                Graphics g = newImage.createGraphics();
                g.drawImage(img2, 0, 0, width, height, null);
                g.dispose();
                writerComp += "Resizing the " + width2 + " x " + height2 + " image to " + width + " x " + height + "\n";
                imageCompare(img, newImage, width, height);
            }
        }
        else{
            writerComp += "Orientation do not match: " + orientation + " vs " + orientation2 + ".  Cannot resize\n";
        }
    }
    public static void RGBCompare(int red, int red2, int green, int green2, int blue, int blue2, int x, int y){
        int colorAccuracy = 0;

        //RED
        int diff = Math.abs(red - red2);
        double avg = 0.0;
        if(red2 == 0)
            avg = 100 - (255-red)/255;
        else
            avg = Math.abs(100 - ((((double)diff/255)))*100);
        BigDecimal bd = new BigDecimal(avg);
        bd = bd.round(new MathContext(5));
        double rounded = bd.doubleValue();
        colorAccuracy += rounded;

        //GREEN
        int diff2 = Math.abs(green - green2);
        double avg2 = 0.0;
        if(green2 == 0)
            avg2 = 100 - (255-green)/255;
        else
            avg2 = Math.abs(100 - ((((double)diff2/255)))*100);
        BigDecimal bd2 = new BigDecimal(avg2);
        bd2 = bd2.round(new MathContext(5));
        double rounded2 = bd2.doubleValue();
        colorAccuracy += rounded2;

        //BLUE
        int diff3 = Math.abs(blue - blue2);
        double avg3 = 0.0;
        if(blue2 == 0)
            avg3 = 100 - (255-blue)/255;
        else
            avg3 = Math.abs(100 - ((((double)diff3/255)))*100);
        BigDecimal bd3 = new BigDecimal(avg3);
        bd3 = bd3.round(new MathContext(5));
        double rounded3 = bd3.doubleValue();
        colorAccuracy += rounded3;

        percentTotal += (colorAccuracy/3);
    }

    public static void main(String []args){
        BufferedImage img = null;
        BufferedImage img2 = null;
        FileWriter writer = null;

        FilenameFilter filter = (f, name) -> (name.endsWith(".jpg") || name.endsWith(".png"));

        File f = new File(args[0]);
        String[] pathnames = f.list(filter);

        File f2 = new File(args[1]);
        String[] pathnames2 = f2.list(filter);

        try {
            writer = new FileWriter("image_Comparisons.txt", true);
        }
        catch(IOException e){e.printStackTrace();}

        for(int i = 0; i < pathnames2.length; i++) {
            for (int j = 0; j < pathnames.length; j++) {
                try {
                    if(j == 0) {
                        img = ImageIO.read(new File(args[1] + pathnames2[i]));
                    }
                    writerComp += "Comparing " + pathnames2[i] + " with " + pathnames[j] + "\n";
                    img2 = ImageIO.read(new File(args[0] + pathnames[j]));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (nullCheck(img, img2)) {
                    int width = img.getWidth();
                    int height = img.getHeight();

                    int width2 = img2.getWidth();
                    int height2 = img2.getHeight();

                    if (widthCheck(width, width2) && heightCheck(height, height2)) {
                        writerComp += "No resizing needed\n";
                        imageCompare(img, img2, width, height);
                    } else {
                        orientation(img, img2, width, width2, height, height2);
                    }
                }
                writerComp += "-------------------------------------------------------------\n";
            }
            System.out.println((i+1) + "/" + (pathnames2.length) + " stacks complete");
        }
        try {
            writer.write(writerComp);
            writer.close();
        }
        catch (IOException e){e.printStackTrace();}

    }
}