package com.investra.utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PasswordGenerator {

    private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGIT_CHARS = "0123456789";
    private static final String SYMBOL_CHARS = "!@#$%&*";

    private static final String ALL_POSSIBLE_CHARS =
            UPPERCASE_CHARS + LOWERCASE_CHARS + DIGIT_CHARS + SYMBOL_CHARS;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String generatePassword(int length) {
        if (length < 4) {
            throw new IllegalArgumentException("Şifre en az 4 karakter uzunlugunda olmalı");
        }

        List<Character> passwordChars = new ArrayList<>();

        passwordChars.add(getRandomChar(UPPERCASE_CHARS));
        passwordChars.add(getRandomChar(LOWERCASE_CHARS));
        passwordChars.add(getRandomChar(DIGIT_CHARS));
        passwordChars.add(getRandomChar(SYMBOL_CHARS));

        for (int i = 4; i < length; i++) {
            passwordChars.add(getRandomChar(ALL_POSSIBLE_CHARS));
        }

        Collections.shuffle(passwordChars, SECURE_RANDOM);

        StringBuilder passwordBuilder = new StringBuilder();
        for (char c : passwordChars) {
            passwordBuilder.append(c);
        }

        return passwordBuilder.toString();
    }

    private static char getRandomChar(String charSet) {
        return charSet.charAt(SECURE_RANDOM.nextInt(charSet.length()));
    }
}