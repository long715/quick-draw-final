package nz.ac.auckland.se206;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.media.AudioClip;

public class StatisticsController {

  @FXML private Label lblBestTime;
  @FXML private Label lblBestWord;
  @FXML private PieChart pieChart;
  @FXML private Button btnBack;
  @FXML private ListView lstvWordHistory;
  @FXML private Button btnViewBadges;

  @FXML
  private void initialize() throws IOException {
    SceneManager.storeUi(SceneManager.AppUi.BADGES, App.loadFxml("badges"));
    // set the labels to show the relevant information for the current user
    UserProfile user = SceneManager.getProfile(SceneManager.getMainUser());
    if (!(user == null)) {
      if (user.getBestTime() == -1) {
        // if no best time, show no time
        lblBestTime.setText("-");
      } else {
        lblBestTime.setText(String.valueOf(user.getBestTime()) + "s");
      }
      lblBestWord.setText(user.getBestWord());

      // display word history
      lstvWordHistory.getItems().addAll(user.getWords());

      // add and display wins and losses on the piechart
      PieChart.Data slice1 = new PieChart.Data("Wins: " + user.getWins(), user.getWins());
      PieChart.Data slice2 = new PieChart.Data("Losses: " + user.getLosses(), user.getLosses());

      pieChart.getData().add(slice1);
      pieChart.getData().add(slice2);
    }
  }

  @FXML
  private void onBack() {
    new AudioClip(getClass().getResource("/sounds/OnBackSound.wav").toExternalForm()).play();
    btnBack.getScene().setRoot(SceneManager.getUi(SceneManager.AppUi.MENU)); // returns to menu
  }

  @FXML
  private void onClickViewBadges() {
    new AudioClip(getClass().getResource("/sounds/ButtonClickSound.wav").toExternalForm()).play();
    Scene sceneBtnIsIn = btnViewBadges.getScene();
    sceneBtnIsIn.setRoot(SceneManager.getUi(SceneManager.AppUi.BADGES));
  }
}
