package GLF_recgonization;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LiuFangGuo on 10/22/15.
 */
public class ImagePreProcess2 {


    private static Map<BufferedImage, String> trainMap = null;
    private static int index = 0;

    public static int isBlack(int colorInt) {
        Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue() <= 100) {
            return 1;
        }
        return 0;
    }

    public static int isWhite(int colorInt) {
        Color color = new Color(colorInt);
        if (color.getRed() + color.getGreen() + color.getBlue() > 100) {
            return 1;
        }
        return 0;
    }

    public static BufferedImage removeBackgroud(String picFile)
            throws Exception {
        BufferedImage img = ImageIO.read(new File(picFile));
        return img;
    }

    public static BufferedImage removeBlank(BufferedImage img) throws Exception {
        int width = img.getWidth();
        int height = img.getHeight();
        int start = 0;
        int end = 0;
        Label1:
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                if (isWhite(img.getRGB(x, y)) == 1) {
                    start = y;
                    break Label1;
                }
            }
        }
        Label2:
        for (int y = height - 1; y >= 0; --y) {
            for (int x = 0; x < width; ++x) {
                if (isWhite(img.getRGB(x, y)) == 1) {
                    end = y;
                    break Label2;
                }
            }
        }
        return img.getSubimage(0, start, width, end - start + 1);
    }

    public static List<BufferedImage> splitImage(BufferedImage img)
            throws Exception {
        List<BufferedImage> subImgs = new ArrayList<BufferedImage>();
        int width = img.getWidth();
        int height = img.getHeight();
        List<Integer> weightlist = new ArrayList<Integer>();
        for (int x = 0; x < width; ++x) {
            int count = 0;
            for (int y = 0; y < height; ++y) {
                if (isWhite(img.getRGB(x, y)) == 1) {
                    System.out.println("count" + count++);
                }
            }
            System.out.println("添加到队列中的是" + count);
            weightlist.add(count);
        }

        //
        for (int i = 0; i < weightlist.size(); ) {
            int length = 0;
            //这是沿着x轴看连续的情况。。
            while (weightlist.get(i++) > 1) {
                length++;
            }
            if (length > 12) {
                BufferedImage a = img.getSubimage(i - length - 1, 0, length / 2, height);
                ImageIO.write(a, "JPG", new File("/Users/LiuFangGuo/Documents/ImageRecognization/Trying/glf-debug/" + i + "~~" + length + "--1.jpg"));
                subImgs.add(removeBlank(a));
                BufferedImage b = img.getSubimage(i - length / 2 - 1, 0, length / 2, height);
                ImageIO.write(b, "JPG", new File("/Users/LiuFangGuo/Documents/ImageRecognization/Trying/glf-debug/" + i + "~~" + length + "--2.jpg"));
                subImgs.add(removeBlank(b));
            } else if (length > 3) {
                BufferedImage c = img.getSubimage(i - length - 1, 0, length, height);
                ImageIO.write(c, "JPG", new File("/Users/LiuFangGuo/Documents/ImageRecognization/Trying/glf-debug/" + i + "~~" + length + "--3.jpg"));
                subImgs.add(removeBlank(c));
            }
        }
        return subImgs;
    }

    public static Map<BufferedImage, String> loadTrainData() throws Exception {
        if (trainMap == null) {
            Map<BufferedImage, String> map = new HashMap<BufferedImage, String>();
            File dir = new File("train2");
            File[] files = dir.listFiles();
            for (File file : files) {
                map.put(ImageIO.read(file), file.getName().charAt(0) + "");
            }
            trainMap = map;
        }
        return trainMap;
    }

    public static String getSingleCharOcr(BufferedImage img,
                                          Map<BufferedImage, String> map) {
        String result = "";
        int width = img.getWidth();
        int height = img.getHeight();
        int min = width * height;
        for (BufferedImage bi : map.keySet()) {
            int count = 0;
            int widthmin = width < bi.getWidth() ? width : bi.getWidth();
            int heightmin = height < bi.getHeight() ? height : bi.getHeight();
            Label1:
            for (int x = 0; x < widthmin; ++x) {
                for (int y = 0; y < heightmin; ++y) {
                    if (isWhite(img.getRGB(x, y)) != isWhite(bi.getRGB(x, y))) {
                        count++;
                        if (count >= min)
                            break Label1;
                    }
                }
            }
            if (count < min) {
                min = count;
                result = map.get(bi);
            }
        }
        return result;
    }

    //处理这张照片。。
    public static String getAllOcr(String file) throws Exception {

        BufferedImage img = removeBackgroud(file);
        List<BufferedImage> listImg = splitImage(img);
        Map<BufferedImage, String> map = loadTrainData();
        String result = "";
        for (BufferedImage bi : listImg) {
            result += getSingleCharOcr(bi, map);
        }
        ImageIO.write(img, "JPG", new File("result2\\" + result + ".jpg"));
        return result;
    }


    public static void trainData() throws Exception {
        File dir = new File("temp");
        File[] files = dir.listFiles();
        for (File file : files) {
            BufferedImage img = removeBackgroud("temp\\" + file.getName());
            List<BufferedImage> listImg = splitImage(img);
            if (listImg.size() == 4) {
                for (int j = 0; j < listImg.size(); ++j) {
                    ImageIO.write(listImg.get(j), "JPG", new File("train2\\"
                            + file.getName().charAt(j) + "-" + (index++)
                            + ".jpg"));
                }
            }
        }
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // downloadImage();
//        for (int i = 0; i < 30; ++i) {
//            //传递进来了一张照片的路径。。
//            String text = getAllOcr("img2\\" + i + ".jpg");
//            System.out.println(i + ".jpg = " + text);
//        }
        String text = getAllOcr("/Users/LiuFangGuo/Documents/ImageRecognization/Trying/glf-debug/2b3----29.jpg");
        System.out.println("结果是" + text);
    }
}
