package com.fakeworth;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WorthFormat {
    private static final Pattern AMOUNT = Pattern.compile("(\\d[\\d,]*(?:\\.\\d+)?)\\s*([KkMmBbTt])?");

    private WorthFormat() {}

    /** "7M" -> 7000000, "999K" -> 999000, "1,234" -> 1234. Returns null if no number found. */
    public static Double parse(String s) {
        if (s == null) return null;
        Matcher m = AMOUNT.matcher(s.trim());
        if (!m.find()) return null;
        try {
            double v = Double.parseDouble(m.group(1).replace(",", ""));
            String suf = m.group(2);
            if (suf != null) {
                switch (Character.toUpperCase(suf.charAt(0))) {
                    case 'K' -> v *= 1_000d;
                    case 'M' -> v *= 1_000_000d;
                    case 'B' -> v *= 1_000_000_000d;
                    case 'T' -> v *= 1_000_000_000_000d;
                    default -> { }
                }
            }
            return v;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 7000000 -> "7M", 221000000 -> "221M", 1500 -> "1.5K", 67 -> "67". */
    public static String format(double v) {
        String[] suffix = {"", "K", "M", "B", "T"};
        int i = 0;
        while (v >= 1000 && i < suffix.length - 1) {
            v /= 1000d;
            i++;
        }
        String num;
        if (v == Math.floor(v)) {
            num = String.valueOf((long) v);
        } else {
            num = String.format(Locale.US, "%.2f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return num + suffix[i];
    }
}
