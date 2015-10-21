package GLF_recgonization;

import core.AImageRecognition;
import one.FirstTrying;

import java.util.Map;

/**
 * Hello world!
 */
public class App {
    private void execute(AImageRecognition trying){
        Map<String, String> resultMap = trying.recognitionBatch(trying.getProperties().getProperty("sourceDirectory"));
        trying.writeRecognitionResult(trying.getProperties().getProperty("targetDirectory"), resultMap);
    }
    public static void main(String[] args) {
        System.out.println("Hello World!");
        App app = new App();
        AImageRecognition trying = new FirstTrying("FirstTryingConfig.properties");
        app.execute(trying);

    }

}