package nz.ac.auckland.se206;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;

public class GameSettingsController {
  @FXML private Button btnMenu;

  private UserProfile currentUser = SceneManager.getProfile(SceneManager.getMainUser());

  @FXML
  private void onSwitchToMenu() {
    Scene sceneBtnIsIn = btnMenu.getScene();
    sceneBtnIsIn.setRoot(SceneManager.getUi(SceneManager.AppUi.MENU));
  }

  @FXML
  private void onSetAccuracyEasy() {
    currentUser.setAccuracy(3);
  }

  @FXML
  private void onSetAccuracyMedium() {
    currentUser.setAccuracy(2);
  }

  @FXML
  private void onSetAccuracyHard() {
    currentUser.setAccuracy(1);
  }
}
