package nz.ac.auckland.se206.words;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import nz.ac.auckland.se206.App;

public class CategorySelector {
  // difficulty is represented by these letters in the csv file
  public enum Difficulty {
    E,
    M,
    H
  }

  // hash map that uses the difficulty as the key to the categories
  // shown in the csv file
  private Map<Difficulty, List<String>> difficulty2categories;

  public CategorySelector() throws URISyntaxException, IOException, CsvException {
    // initialise the hash map and map the enums to its corresponding list
    difficulty2categories = new HashMap<>();
    for (Difficulty difficulty : Difficulty.values()) {
      difficulty2categories.put(difficulty, new ArrayList<>());
    }

    for (String[] line : getLines()) {
      // get the list of the respective difficulty and add in the category
      difficulty2categories.get(Difficulty.valueOf(line[1])).add(line[0]);
    }
  }

  /**
   * This method reads the CSV file and returns a list of string arrays where each array represents
   * the category and difficulty.
   *
   * @return list containing the category and its respective difficulty
   */
  protected List<String[]> getLines() throws URISyntaxException, IOException, CsvException {

    // get the file URL, read and convert the file into a list of string array
    // representing each row the CSV file
    File file = new File(App.class.getResource("/category_difficulty.csv").toURI());
    try (FileReader fr = new FileReader(file, StandardCharsets.UTF_8);
        CSVReader reader = new CSVReader(fr)) {
      return reader.readAll();
    }
  }

  /**
   * This method randomly chooses a category from a specified difficulty.
   *
   * @param difficulty difficulty of the game
   * @return a category from the specified difficulty
   */
  public String getRandomCategory(int wordsSettings) {
    // gets the list of categories and calculates a random int from 0 to the size of
    // the list, to index the list of categories and return the resulting category
    List<String> listOfWords = getDifficultyList(wordsSettings);

    return listOfWords.get(new Random().nextInt(listOfWords.size()));
  }

  /**
   * This method returns the entire list of words of a given difficulty
   *
   * @param difficulty
   * @return
   */
  public List<String> getDifficultyList(int wordsSettings) {
    List<String> listOfWords = new ArrayList<String>();
    if (wordsSettings == 3) {
      // EASY category only
      listOfWords.addAll(difficulty2categories.get(Difficulty.E));
    } else if (wordsSettings == 2) {
      // EASY and MEDIUM words
      listOfWords.addAll(difficulty2categories.get(Difficulty.E));
      listOfWords.addAll(difficulty2categories.get(Difficulty.M));
    } else if (wordsSettings == 1) {
      // EASY, MEDIUM and HARD words
      listOfWords.addAll(difficulty2categories.get(Difficulty.E));
      listOfWords.addAll(difficulty2categories.get(Difficulty.M));
      listOfWords.addAll(difficulty2categories.get(Difficulty.H));
    } else {
      // MASTER mode which is only HARD words
      listOfWords.addAll(difficulty2categories.get(Difficulty.H));
    }

    return listOfWords;
  }
}
