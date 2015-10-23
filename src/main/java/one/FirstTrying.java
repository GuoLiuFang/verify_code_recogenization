package one;

import core.AImageRecognition;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by LiuFangGuo on 10/19/15.
 * 固定大小，固定位置，固定字体
 */
public class FirstTrying extends AImageRecognition {
    /**
     * 不同的实例化类传入不同的配置文件
     */
    public FirstTrying(String configFileName) {
        super(configFileName);
    }

    /**
     * 这个分割程序其实很关键。。
     *
     * @param bufferedImage
     * @return
     */
    public List<BufferedImage> splitImage(BufferedImage bufferedImage) {
        List<BufferedImage> bufferedImageList = new ArrayList<BufferedImage>();
        int standardWidth = Integer.valueOf(super.getProperties().getProperty("standardWidth"));
        int standardHeight = Integer.valueOf(super.getProperties().getProperty("standardHeight"));

        int startX = Integer.valueOf(super.getProperties().getProperty("upper-left-x"));
        int startY = Integer.valueOf(super.getProperties().getProperty("upper-left-y"));

        bufferedImageList.add(bufferedImage.getSubimage(startX, startY, standardWidth, standardHeight));//
        bufferedImageList.add(bufferedImage.getSubimage(startX + standardWidth * 1 + 1, startY, standardWidth, standardHeight));
        bufferedImageList.add(bufferedImage.getSubimage(startX + standardWidth * 2 + 2, startY, standardWidth, standardHeight));
        bufferedImageList.add(bufferedImage.getSubimage(startX + standardWidth * 3 + 3, startY, standardWidth, standardHeight));
        return bufferedImageList;
    }

    public void splitImage(String imagesDirctoryPath) {

    }

    public static void main(String[] args) {
        FirstTrying firstTrying = new FirstTrying("FirstTryingConfig.properties");
        Map<String, String> resultMap = firstTrying.recognitionBatch(firstTrying.getProperties().getProperty("sourceDirectory"));
        firstTrying.writeRecognitionResult(firstTrying.getProperties().getProperty("targetDirectory"), resultMap);
    }
}
