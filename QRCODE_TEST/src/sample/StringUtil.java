package sample;

import java.util.Arrays;

public class StringUtil {

    // Program to convert int array to String array in Java 8
    public static String ArrayToStringConverter (int[] charset){

        String strArray[] = Arrays.stream(charset)
                .mapToObj(String::valueOf)
                .toArray(String[]::new);

        return Arrays.toString(strArray);
    }

    public static int[] StringToArrayConverter(String string) {

        /*int[] arr = Arrays.stream(string.substring(1, string.length()-1).split(","))
                .map(String::trim).mapToInt(Integer::parseInt).toArray();

        return arr;*/


        String arr = string;
        String[] items = arr.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");

        int[] results = new int[items.length];

        for (int i = 0; i < items.length; i++) {
            try {
                results[i] = Integer.parseInt(items[i]);

            } catch (NumberFormatException nfe) {
                //NOTE: write something here if you need to recover from formatting errors
            }
        }
        return results;
    }


}
