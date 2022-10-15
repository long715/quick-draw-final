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

  @FXML
  private void initialize() {
    List<UserProfile> allUsers = SceneManager.getAllProfiles();

    if (allUsers.isEmpty()) {
      lblNoPlayers.setVisible(true);
    } else {
      List<String> allUserNames = new ArrayList<String>();
      List<String> allBestWords = new ArrayList<String>();
      List<String> allBestTimes = new ArrayList<String>();
      List<Integer> ranks = IntStream.rangeClosed(1, allUsers.size()).boxed().toList();

      Collections.sort(
          allUsers,
          new Comparator<UserProfile>() {

            public int compare(UserProfile o1, UserProfile o2) {

              return Integer.valueOf(o1.getBestTime()).compareTo(Integer.valueOf(o2.getBestTime()));
            }
          });

      for (UserProfile pfl : allUsers) {
        allUserNames.add(pfl.getName());
        allBestWords.add(pfl.getBestWord());
        allBestTimes.add(String.valueOf(pfl.getBestTime()) + "s");
      }

      lstvBestTime.getItems().addAll(allBestTimes);
      lstvBestWord.getItems().addAll(allBestWords);
      lstvName.getItems().addAll(allUserNames);
      lstvRank.getItems().addAll(ranks);
    }
  }

  @FXML
  private void onCancel() {
    btnCancel.getScene().setRoot(SceneManager.getUi(SceneManager.AppUi.MENU));
  }
}
