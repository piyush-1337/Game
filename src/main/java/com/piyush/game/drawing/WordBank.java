package com.piyush.game.drawing;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class WordBank {
    private static final String WORDS_FILE = "/com/piyush/game/words.txt";
    private static final List<String> words = new ArrayList<>();
    private static final Random random = new Random();

    static {
        loadWordsFromFile();
    }

    private static void loadWordsFromFile() {
        try (InputStream inputStream = WordBank.class.getResourceAsStream(WORDS_FILE);
             Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {

            while (scanner.hasNextLine()) {
                String word = scanner.nextLine().trim();
                if (!word.isEmpty()) {
                    words.add(word);
                }
            }

        } catch (IOException | NullPointerException e) {
            System.err.println("Error loading words, using fallback list");
            // Fallback words
            words.addAll(List.of("Apple", "Elephant", "Mountain", "Bicycle", "Pizza"));
        }
    }

    public static String getRandomWord() {
        if (words.isEmpty()) return "DefaultWord";
        return words.get(random.nextInt(words.size()));
    }
}