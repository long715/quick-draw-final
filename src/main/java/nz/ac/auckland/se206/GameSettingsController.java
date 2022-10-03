package nz.ac.auckland.se206;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;

public class GameSettingsController {
  @FXML private Button btnMenu;
  @FXML private ToggleButton tbtnAccuracyE;
  @FXML private ToggleButton tbtnAccuracyM;
  @FXML private ToggleButton tbtnAccuracyH;

  private UserProfile currentUser = SceneManager.getProfile(SceneManager.getMainUser());
  ;

  @FXML
  private void initialize() {

    // look thru the previously selected ACCURACY settings and
    // set the settings on the new page
    if (currentUser.getAccuracy() == 3) {
      tbtnAccuracyE.setSelected(true);
    } else if (currentUser.getAccuracy() == 2) {
      tbtnAccuracyM.setSelected(true);
    } else {
      tbtnAccuracyH.setSelected(true);
    }
  }

  @FXML
  private void onSwitchToMenu() {
    Scene sceneBtnIsIn = btnMenu.getScene();
    sceneBtnIsIn.setRoot(SceneManager.getUi(SceneManager.AppUi.MENU));
  }

  @FXML
  private void onSetAccuracyEasy() throws IOException {
    currentUser.setAccuracy(3);
    // update the file
    currentUser.saveData();
  }

  @FXML
  private void onSetAccuracyMedium() throws IOException {
    currentUser.setAccuracy(2);
    currentUser.saveData();
  }

  @FXML
  private void onSetAccuracyHard() throws IOException {
    currentUser.setAccuracy(1);
    currentUser.saveData();
  }
}
