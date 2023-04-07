package edu.northeastern.markergo.utils;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.regex.Pattern;

public class Util {
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public static boolean isValidEmail(String email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    public static boolean isValidPassword(String password) {
        return (!TextUtils.isEmpty(password) && pattern.matcher(password).matches());
    }

    public static boolean passwordMatches(String password, String confirmPassword) {
        return TextUtils.equals(password, confirmPassword);
    }
}
