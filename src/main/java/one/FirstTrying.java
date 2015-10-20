package one;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.List;

/**
 * Created by LiuFangGuo on 10/19/15.
 * 固定大小，固定位置，固定字体
 */
public class FirstTrying {
    private Properties properties;
    Map<BufferedImage, String> imageContentMap;

    public FirstTrying() {
        this.properties = new Properties();
        try {
            this.properties.load(this.getClass().getClassLoader().getResourceAsStream("oneConfig.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isWhite(int colorValue) {
        Color color = new Color(colorValue);
        //255,255,255是白色；0,0,0是黑色
        if ((color.getRed() + color.getGreen() + color.getBlue()) > Integer.valueOf(this.properties.getProperty("whiteThreshold"))) {
            return true;
        }
        return false;
    }

    /**
     * 白的变成更加白，黑的变成更加黑
     *
     * @param imagePath
     * @return
     */
    private BufferedImage removeBackgroudColor(String imagePath) {
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File(imagePath));
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (isWhite(bufferedImage.getRGB(x, y))) {
                        bufferedImage.setRGB(x, y, Color.WHITE.getRGB());
                    } else {
                        bufferedImage.setRGB(x, y, Color.BLACK.getRGB());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bufferedImage;
    }

    /**
     * 这个分割程序其实很关键。。
     *
     * @param bufferedImage
     * @return
     */
    private List<BufferedImage> splitImage(BufferedImage bufferedImage) {
        List<BufferedImage> bufferedImageList = new ArrayList<BufferedImage>();
        int standardWidth = Integer.valueOf(this.properties.getProperty("standardWidth"));
        int standardHeight = Integer.valueOf(this.properties.getProperty("standardHeight"));

        int startX = Integer.valueOf(this.properties.getProperty("upper-left-x"));
        int startY = Integer.valueOf(this.properties.getProperty("upper-left-y"));

        bufferedImageList.add(bufferedImage.getSubimage(startX, startY, standardWidth, standardHeight));//
        bufferedImageList.add(bufferedImage.getSubimage(startX + standardWidth * 1 + 1, startY, standardWidth, standardHeight));
        bufferedImageList.add(bufferedImage.getSubimage(startX + standardWidth * 2 + 2, startY, standardWidth, standardHeight));
        bufferedImageList.add(bufferedImage.getSubimage(startX + standardWidth * 3 + 3, startY, standardWidth, standardHeight));
        return bufferedImageList;
    }

    /**
     * tain => map<image,content>
     */
    private void train() {
        this.imageContentMap = new HashMap<BufferedImage, String>();
        File trainDirctory = new File(this.properties.getProperty("trainDirectory"));
        System.out.println("训练数据的目录是:" + this.properties.getProperty("trainDirectory"));
        File[] trainFiles = trainDirctory.listFiles();
        for (File imageFile : trainFiles) {
            try {
                this.imageContentMap.put(ImageIO.read(imageFile), getImageContentByImageName(imageFile.getName()));
                System.out.println(imageFile.getName() + "文件内容是" + getImageContentByImageName(imageFile.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getImageContentByImageName(String imageFileName) {
        String[] contentAndSuffix = imageFileName.split("[\\.]");
        return contentAndSuffix[0];
    }

    public String predict(String imagePath) {
        BufferedImage removedBgColorImage = removeBackgroudColor(imagePath);
        //split的时候应该保证顺序的一致性。
        List<BufferedImage> imageList = splitImage(removedBgColorImage);
        if (this.imageContentMap == null) {
            train();
        }
        StringBuffer result = new StringBuffer();
        //for的时候也应该保证顺序的一致性。
        for (BufferedImage imge : imageList) {
            String content = getAtomImageContent(imge);
            result.append(content);
        }
        return result.toString();
    }

    private String getAtomImageContent(BufferedImage imge) {
        int maxDegree = Integer.MIN_VALUE;
        BufferedImage matchedImage = null;
        for (BufferedImage standardImage : this.imageContentMap.keySet()) {
            int matchDegree = matchDegree(standardImage, imge);
            if (matchDegree > maxDegree) {
                maxDegree = matchDegree;
                matchedImage = standardImage;
            }
        }
        return ((String) this.imageContentMap.get(matchedImage));
    }

    private int matchDegree(BufferedImage standardImage, BufferedImage imge) {
        int width = imge.getWidth();
        int height = imge.getHeight();
        int matchPointsSum = 0;
//        GoAway:
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (isWhite(imge.getRGB(x, y)) == isWhite(standardImage.getRGB(x, y))) {
                    matchPointsSum++;
//                    break GoAway;
                }
            }
        }
        return matchPointsSum;
    }

    /**
     * Map<ImageAbsolutePath,content>
     *
     * @param imageDirectory
     * @return
     */
    public Map<String, String> predictAll(String imageDirectory) {
        File directory = new File(imageDirectory);
        if (directory.isDirectory()) {
            Map<String, String> result = new HashMap<String, String>();
            train();
            File[] fileArray = directory.listFiles();
            for (File file : fileArray) {
                String content = predict(file.getAbsolutePath());
                System.out.println("目录下的文件是：" + file.getAbsolutePath());
                result.put(file.getAbsolutePath(), content);
            }
            return result;
        }
        return null;
    }

    private void writeResultMap(String targetDirectory, Map<String, String> resultMap) {
        File targetDir = new File(targetDirectory);
        if (targetDir.exists() && targetDir.isDirectory()) {
        } else {
            targetDir.mkdirs();
        }
        FileInputStream fileInputStream;
        FileOutputStream fileOutputStream;
        FileChannel inChannel;
        FileChannel outChannel;
        try {
            for (String fileName : resultMap.keySet()) {
                //1.完成复制；2.对复制后的文件重命名；
                File readyToBeCopyed = new File(fileName);
                fileInputStream = new FileInputStream(readyToBeCopyed);
                fileOutputStream = new FileOutputStream(new File(targetDirectory + File.separator + ((String) resultMap.get(fileName)) + "----" + readyToBeCopyed.getName()));
                inChannel = fileInputStream.getChannel();
                outChannel = fileOutputStream.getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FirstTrying firstTrying = new FirstTrying();
        Map<String, String> resultMap = firstTrying.predictAll(firstTrying.properties.getProperty("sourceDirectory"));
        firstTrying.writeResultMap(firstTrying.properties.getProperty("targetDirectory"), resultMap);
    }
}
