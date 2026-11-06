package com.amdocs.telecom.util;

import java.io.Console;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public final class InputHelper {

    private static final Scanner SCANNER = new Scanner(System.in);

    private InputHelper() {
    }

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine().trim();
    }

    /** Masked input when run from a real terminal; visible inside the IntelliJ Run window. */
    public static String readPassword(String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] chars = console.readPassword(prompt);
            return chars == null ? "" : new String(chars);
        }
        return readLine(prompt);
    }

    public static int readInt(String prompt) {
        while (true) {
            String text = readLine(prompt);
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    /** Expects yyyy-MM-dd. Returns null if the text is not a valid date. */
    public static LocalDate readDate(String prompt) {
        try {
            return LocalDate.parse(readLine(prompt));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}