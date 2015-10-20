package common;

import java.awt.*;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by LiuFangGuo on 10/20/15.
 */
public class Common {
    private Properties properties;

    public Common() {
        this.properties = new Properties();
        try {
            this.properties.load(this.getClass().getClassLoader().getResourceAsStream("Config.properties"));
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


}
