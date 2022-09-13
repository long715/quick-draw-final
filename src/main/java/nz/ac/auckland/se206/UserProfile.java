package nz.ac.auckland.se206;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class UserProfile {

  // fields that define the data stored for each user
  private String name;
  private int wins;
  private int loss;
  private ArrayList<String> words;
  private String best_name;
  // by default this should be null, needs to be checked if null to display either NIL or its value
  private int best_time;

  public UserProfile(String name) {
    // add underscore to names with spaces
    this.name = name.replace(" ", "_");
    this.wins = 0;
    this.loss = 0;
    this.words = new ArrayList<String>();
    this.best_name = "NIL";
  }

  public void saveData() throws IOException {
    // create the file in the data folder
    File file = new File("src/main/resources/data", name + ".txt");
    file.createNewFile();

    // write into the file with all its details
    BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
    writer.write(wins + "\n");
    writer.write(loss + "\n");
    writer.write(words.toString() + "\n");
    writer.write(best_name + "\n");
    writer.write(best_time + "\n");

    writer.close();
  }
}
