package nz.ac.auckland.se206;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class LeaderboardController {

  @FXML private ListView<Integer> lstvRank;
  @FXML private ListView<String> lstvName;
  @FXML private ListView<String> lstvBestWord;
  @FXML private ListView<String> lstvBestTime;
  @FXML private Label lblNoPlayers;

  @FXML private Button btnCancel;

  /**
   * This is the entry method that executes when the LeaderBoard is initially loaded. Rank the users
   * based on their latest user statistics: best time.
   */
  @FXML
  private void initialize() {
    // get all the profiles from hashmap in scene manager
    List<UserProfile> allUsers = SceneManager.getAllProfiles();

    if (allUsers.isEmpty()) {
      lblNoPlayers.setVisible(true);
    } else {
      List<String> allUserNames = new ArrayList<String>();
      List<String> allBestWords = new ArrayList<String>();
      List<String> allBestTimes = new ArrayList<String>();
      List<Integer> ranks = IntStream.rangeClosed(1, allUsers.size()).boxed().toList();

      // rank the users based on their best time statistics
      Collections.sort(
          allUsers,
          new Comparator<UserProfile>() {

            public int compare(UserProfile o1, UserProfile o2) {

              return Integer.valueOf(o1.getBestTime()).compareTo(Integer.valueOf(o2.getBestTime()));
            }
          });

      // for each user, add its name, best word and best time in its
      // corresponding list that will be displayed based on ranking since
      // the list has already been previously sorted
      for (UserProfile pfl : allUsers) {
        allUserNames.add(pfl.getName());
        allBestWords.add(pfl.getBestWord());
        allBestTimes.add(String.valueOf(pfl.getBestTime()) + "s");
      }

      // display the rank, name, best words and time in the leaderboard
      lstvBestTime.getItems().addAll(allBestTimes);
      lstvBestWord.getItems().addAll(allBestWords);
      lstvName.getItems().addAll(allUserNames);
      lstvRank.getItems().addAll(ranks);
    }
  }

  /** Loads the Menu instance of the user in the scene. */
  @FXML
  private void onCancel() {
    btnCancel.getScene().setRoot(SceneManager.getUi(SceneManager.AppUi.MENU));
  }
}
