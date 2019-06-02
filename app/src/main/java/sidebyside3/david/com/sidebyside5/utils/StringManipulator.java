package sidebyside3.david.com.sidebyside5.utils;

/**
 * Created by Gongwei (David) Chen on 4/3/2019.
 */

public class StringManipulator {
    public static String expandUsername(String username){
        return username.replace("."," ");
    }

    public static String condenseUsername(String username){
        return username.replace(" ",".");
    }
}
