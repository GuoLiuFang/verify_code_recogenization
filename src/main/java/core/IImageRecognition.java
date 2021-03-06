package core;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

/**
 * Created by LiuFangGuo on 10/21/15.
 */
public interface IImageRecognition {
    int GRAY_METHOD_AVERAGE = 1;
    int GRAY_METHOD_LUMINOSITY = 2;
    int GRAY_METHOD_DESATURATION = 3;
    int GRAY_METHOD_DECOMPOSITON = 4;
    int GRAY_METHOD_SINGLE_COLOR = 5;

    //预处理方法族群，目前包括isWhite,removeBackgroud
    boolean isWhite(int rgb);

    BufferedImage removeBackgroud(String imagePath);

    BufferedImage getGrayScale(String imagePath, int type);

    //图像分割方法族群，目前包括splitImage
    List<BufferedImage> splitImage(BufferedImage bufferedImage);

    void splitImage(String imagesDirctoryPath);

    //训练方法族群，目前包括train
    Map<BufferedImage, String> train();

    //识别方法族群，目前包括recognition,recongnitionBatch,recognitionAtom
    String recognitionAtom(BufferedImage bufferedImage);

    String recognition(String imagePath);

    Map<String, String> recognitionBatch(String imagesDirctoryPath);

    //结果保存方法族群，目前包括writeRecognitionResult
    void writeRecognitionResult(String targetDirectory, Map<String, String> recognitionResultMap);
}
