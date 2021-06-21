package General_Purpose_IDE.app;

import java.util.ArrayList;
import java.util.Arrays;

public class Autocomplete {
    public static String autocomplete(String token, ArrayList<String> tokens) {
        if (token.length() == 0) {
            //display all options, don't autocomplete
            System.out.println("");
            for (int i = 0; i < tokens.size(); i++) {
                System.out.print(tokens.get(i));
                if (i + 1 < tokens.size()) {
                    System.out.print(", ");
                }
            }
            System.out.println("");
        } else {
            //find tokens that are possible autocomplete options
            String[] legalOptions = new String[tokens.size()];
            Arrays.fill(legalOptions, null);
            int numberOfOptions = 0;
            for (String option : tokens) {
                if (option.length() >= token.length() && option.substring(0, token.length()).compareTo(token) == 0) {
                    legalOptions[numberOfOptions] = option;
                    numberOfOptions++;
                }
            }
            if (numberOfOptions == 1) {
                return legalOptions[0];
            }
            else if (numberOfOptions > 1) {
                String largestAutocomplete = largestCommonString(legalOptions);
                if (largestAutocomplete.compareTo(token) == 0) {
                    System.out.println("");
                    for (int i = 0; i < numberOfOptions; i++) {
                        System.out.print(legalOptions[i]);
                        if (i + 1 < numberOfOptions) {
                            System.out.print(", ");
                        }
                    }
                    System.out.println("");
                }
                return largestAutocomplete;
            }
            return token;
        }
        return "";
    }

    private static String largestCommonString(String[] legalOptions) {
        String currentLargest = legalOptions[0].substring(0, 0);
        int index = 0;

        while (true) {
            if (legalOptions[0].length() < index) {
                return currentLargest;
            }

            String tmpLargest = legalOptions[0].substring(0, index);
            for (int currStr = 0; currStr < legalOptions.length && legalOptions[currStr] != null; currStr++) {
                String str = legalOptions[currStr];
                if (str.length() < index || str.substring(0, index).compareTo(tmpLargest) != 0) {
                    return currentLargest;
                }
            }
            currentLargest = tmpLargest;
            index++;
        }
    }
}
