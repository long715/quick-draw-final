package nz.ac.auckland.se206;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;

public class GameSettingsController {
  @FXML private Button btnMenu;

  @FXML
  private void onSwitchToMenu() {
    Scene sceneBtnIsIn = btnMenu.getScene();
    sceneBtnIsIn.setRoot(SceneManager.getUi(SceneManager.AppUi.MENU));
  }
}
