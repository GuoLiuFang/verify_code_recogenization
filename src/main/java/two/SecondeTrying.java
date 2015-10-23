package two;

import core.AImageRecognition;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by LiuFangGuo on 10/20/15.
 */
public class SecondeTrying extends AImageRecognition {

    public SecondeTrying(String configFileName) {
        super(configFileName);
    }

    public List<BufferedImage> splitImage(BufferedImage bufferedImage) {
        List<BufferedImage> BufferedImageList = new ArrayList<BufferedImage>();
        List<Integer> xBreakPoints = getSplitPoints(bufferedImage, true);
        for (int i = 0; i < xBreakPoints.size(); i += 2) {
            int xWidth = xBreakPoints.get(i + 1) - xBreakPoints.get(i) + 1;
            int standardWidth = Integer.valueOf(super.getProperties().getProperty("standardWidth"));
            if (xWidth > standardWidth) {
                BufferedImageList.add(getSubSplittedImage(bufferedImage, xBreakPoints.get(i), 0, xWidth / 2, bufferedImage.getHeight()));
                BufferedImageList.add(getSubSplittedImage(bufferedImage, xBreakPoints.get(i) + xWidth / 2, 0, xWidth / 2, bufferedImage.getHeight()));
            } else {
                BufferedImageList.add(getSubSplittedImage(bufferedImage, xBreakPoints.get(i), 0, xBreakPoints.get(i + 1) - xBreakPoints.get(i) + 1, bufferedImage.getHeight()));
            }
        }
        return BufferedImageList;
    }

    private BufferedImage getSubSplittedImage(BufferedImage bufferedImage, int x, int y, int width, int height) {
        BufferedImage xBuffredImage = bufferedImage.getSubimage(x, 0, width, height);
        List<Integer> yBreakPoints = getSplitPoints(xBuffredImage, false);
        BufferedImage xYBuffredImage = xBuffredImage.getSubimage(0, yBreakPoints.get(0), xBuffredImage.getWidth(), yBreakPoints.get(1) - yBreakPoints.get(0) + 1);
        try {
            ImageIO.write(xBuffredImage, "JPG", new File(super.getProperties().getProperty("tmpDirectory") + "SecondeTrying" + x + "--x--tmp" + ".jpg"));
            ImageIO.write(xYBuffredImage, "JPG", new File(super.getProperties().getProperty("tmpDirectory") + "SecondeTrying" + x + "--xY--tmp" + ".jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return xYBuffredImage;
    }

    public void splitImage(String imagesDirctoryPath) {

    }

    /**
     * @param bufferedImage
     * @param xOrY          true表示X轴width
     * @return
     */
    private List<Integer> getSplitPoints(BufferedImage bufferedImage, boolean xOrY) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        List<Integer> Distribution = new ArrayList<Integer>();
        if (xOrY) {
            //先纵向扫描，进行分割
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    //只要保持规则一致，结果就可以一致，每一个都是单独分割的。。而且原数据中要有一个部分进行人工标示，然后把这部分数据做为训练数据。。这部分要求有一定的覆盖率。。
                    if (isWhite(bufferedImage.getRGB(x, y))) {
                        Distribution.add(x);
                        System.out.println("白点在x轴上的分布" + x);
                        break;
                    }
                }
            }
        } else {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (isWhite(bufferedImage.getRGB(x, y))) {//这里始终是x,y
                        Distribution.add(y);
                        System.out.println("白点在y轴上的分布" + y);
                        break;
                    }
                }
            }
        }
        return getBreakPoints(Distribution);
    }

    private List<Integer> getBreakPoints(List<Integer> distribution) {
        if (distribution.size() > 0) {
            List<Integer> breakPoints = new ArrayList<Integer>();
            int pre = distribution.get(0);
            breakPoints.add(pre);
            System.out.println("添加到breakPoints中的数据点是" + pre);
            for (int i = 1; i < distribution.size(); i++) {
                if ((distribution.get(i) - pre) == 1) {
                    pre = distribution.get(i);
                    continue;
                } else {
                    breakPoints.add(pre);
                    System.out.println("添加到breakPoints中的数据点是" + pre);
                    pre = distribution.get(i);
                    breakPoints.add(pre);
                    System.out.println("添加到breakPoints中的数据点是" + pre);
                    continue;
                }
            }
            breakPoints.add(pre);
            System.out.println("添加到breakPoints中的数据点是" + pre);
            return breakPoints;
        }
        return null;
    }

    public static void main(String[] args) {
        AImageRecognition trying = new SecondeTrying("SecondeTryingConfig.properties");
//        String resultMap = trying.recognition(trying.getProperties().getProperty("sourceFile"));
//        System.out.println("识别结果是：" + resultMap);


        Map<String, String> resultMap = trying.recognitionBatch(trying.getProperties().getProperty("sourceDirectory"));
        trying.writeRecognitionResult(trying.getProperties().getProperty("targetDirectory"), resultMap);
    }
}
