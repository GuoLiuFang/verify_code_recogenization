package core;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;

/**
 * Created by LiuFangGuo on 10/21/15.
 */
public abstract class AImageRecognition implements IImageRecognition {
    //这个属性文件是后面的子类共享的
    private Properties properties;
    //后面的方法要共享标准数据
    private Map<BufferedImage, String> imageContentMap;

    public AImageRecognition(String configFileName) {
        setProperties(configFileName);
    }

    public void setProperties(String configFileName) {
        this.properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream(configFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public boolean isWhite(int rgb) {
        Color color = new Color(rgb);
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
    public BufferedImage removeBackgroud(String imagePath) {
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

    public Map<BufferedImage, String> train() {
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
        return this.imageContentMap;
    }

    private String getImageContentByImageName(String imageFileName) {
        String[] contentAndSuffix = imageFileName.split("[\\.]");
        return "" + contentAndSuffix[0].charAt(0);
    }

    public String recognitionAtom(BufferedImage bufferedImage) {
        int maxDegree = Integer.MIN_VALUE;
        BufferedImage matchedImage = null;
        for (BufferedImage standardImage : this.imageContentMap.keySet()) {
            int matchDegree = matchDegree(standardImage, bufferedImage);
            if (matchDegree > maxDegree) {
                maxDegree = matchDegree;
                matchedImage = standardImage;
            }
        }
        return ((String) this.imageContentMap.get(matchedImage));
    }

    private int matchDegree(BufferedImage standardImage, BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth() < standardImage.getWidth() ? bufferedImage.getWidth() : standardImage.getWidth();
        int height = bufferedImage.getHeight() < standardImage.getHeight() ? bufferedImage.getHeight() : standardImage.getHeight();
        int matchPointsSum = 0;
//        GoAway:
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (isWhite(bufferedImage.getRGB(x, y)) == isWhite(standardImage.getRGB(x, y))) {//现在报这个错是因为，还没有标准图库。。其实不是这个原因而是因为作者的图片跟我的尺寸不一样。。
                    matchPointsSum++;
//                    break GoAway;
                }
            }
        }
        return matchPointsSum;
    }

    public String recognition(String imagePath) {
        BufferedImage removedBgColorImage = removeBackgroud(imagePath);
        //split的时候应该保证顺序的一致性。
        java.util.List<BufferedImage> imageList = splitImage(removedBgColorImage);
        if (this.imageContentMap == null) {
            train();
        }
        StringBuffer result = new StringBuffer();
        //for的时候也应该保证顺序的一致性。
        for (BufferedImage imge : imageList) {
            String content = recognitionAtom(imge);
            result.append(content);
        }
        return result.toString();
    }

    public Map<String, String> recognitionBatch(String imagesDirctoryPath) {
        File directory = new File(imagesDirctoryPath);
        if (directory.isDirectory()) {
            Map<String, String> result = new HashMap<String, String>();
            train();
            File[] fileArray = directory.listFiles();
            for (File file : fileArray) {
                String content = recognition(file.getAbsolutePath());
                System.out.println("目录下的文件是：" + file.getAbsolutePath());
                result.put(file.getAbsolutePath(), content);
            }
            return result;
        }
        return null;
    }

    public void writeRecognitionResult(String targetDirectory, Map<String, String> recognitionResultMap) {
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
            for (String fileName : recognitionResultMap.keySet()) {
                //1.完成复制；2.对复制后的文件重命名；
                File readyToBeCopyed = new File(fileName);
                fileInputStream = new FileInputStream(readyToBeCopyed);
                fileOutputStream = new FileOutputStream(new File(targetDirectory + File.separator + ((String) recognitionResultMap.get(fileName)) + "----" + readyToBeCopyed.getName()));
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
}
