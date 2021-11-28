package com.dreamteam;

import java.util.Random;

public class Emoji {
    private static final String[] unicodeEmojis = new String[] {
            "M", // man
            "W", // woman
            "C", // child
    };
//    private static final String[] unicodeEmojis = new String[] {
//            "\uD83D\uDC69", // man
//            "\uD83D\uDC68", // woman
//            "\uD83D\uDC66", // boy
//            "\uD83D\uDC67", // girl
//    };
    private static final Random random = new Random();
    public static String getRandom() {
        return unicodeEmojis[random.nextInt(unicodeEmojis.length)];
    }
}
