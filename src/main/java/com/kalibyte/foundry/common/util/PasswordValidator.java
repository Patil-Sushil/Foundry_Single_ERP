package com.kalibyte.foundry.common.util;

import java.util.regex.Pattern;

public class PasswordValidator {

    private static final String PASSWORD_REGEX =
            "^(?=.*[a-z])" +        // at least one lowercase
                    "(?=.*[A-Z])" +         // at least one uppercase
                    "(?=.*\\d)" +           // at least one digit
                    "(?=.*[@$!%*?&^#])" +   // at least one special char
                    "[A-Za-z\\d@$!%*?&^#]{8,20}$";  // length 8-20

    private static final Pattern pattern = Pattern.compile(PASSWORD_REGEX);

    public static boolean isValid(String password) {
        return pattern.matcher(password).matches();
    }
}
