package one;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by LiuFangGuo on 10/19/15.
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
        return false;
    }

    private BufferedImage removeBackgroudColor(String imagePath) {
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bufferedImage;
    }

    private List<BufferedImage> splitImage(BufferedImage bufferedImage) {
        List<BufferedImage> bufferedImageList = new ArrayList<BufferedImage>();
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
        List<BufferedImage> imageList = splitImage(removedBgColorImage);
        if (imageContentMap == null) {
            train();
        }
        StringBuffer result = new StringBuffer();
        for (BufferedImage imge : imageList) {
            String content = getAtomImageContent(imge);
            result.append(content);
        }
        return null;
    }

    private String getAtomImageContent(BufferedImage imge) {

        for (BufferedImage standardImage : this.imageContentMap.keySet()) {

        }
        return null;
    }

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

    public static void main(String[] args) {
        FirstTrying firstTrying = new FirstTrying();
        firstTrying.predictAll(firstTrying.properties.getProperty("trainDirectory"));
    }
}
