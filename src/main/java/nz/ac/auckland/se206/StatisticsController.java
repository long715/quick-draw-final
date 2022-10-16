package nz.ac.auckland.se206;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class StatisticsController {

  @FXML private Label lblBestTime;
  @FXML private Label lblBestWord;
  @FXML private PieChart pieChart;
  @FXML private Button btnBack;
  @FXML private ListView lstvWordHistory;
  @FXML private Button btnViewBadges;

  /**
   * This is the entry method executed when the Statistics page is firstly loaded. Updates the
   * components (labels, list view and piechart) based on the latest statistics of the user.
   */
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

  /** Loads the Menu instance of the user on the scene. */
  @FXML
  private void onBack() {
    btnBack.getScene().setRoot(SceneManager.getUi(SceneManager.AppUi.MENU)); // returns to menu
  }

  @FXML
  private void onClickViewBadges() {
    Scene sceneBtnIsIn = btnViewBadges.getScene();
    sceneBtnIsIn.setRoot(SceneManager.getUi(SceneManager.AppUi.BADGES));
  }
}
