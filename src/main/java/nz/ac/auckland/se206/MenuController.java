package nz.ac.auckland.se206;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;

public class MenuController {

  @FXML private Button btnNewGame;
  @FXML private Button btnLoadGame;

  @FXML
  private void initialize() {
    btnLoadGame.setDisable(true); // no game to load at initialisation
  }

  @FXML
  private void onCreateGame() throws IOException {

    // check if there's a previous game instance
    if (SceneManager.ifPrevGameExists()) {
      // replace the game instance with a new game instance
      SceneManager.replaceUi(SceneManager.AppUi.CANVAS, App.loadFxml("canvas"));
    } else {
      // initialise and store the game instance
      SceneManager.storeUi(SceneManager.AppUi.CANVAS, App.loadFxml("canvas"));
    }

    // load the new game and enable the load button to allow user to reload the
    // current game from the menu
    onSwitchToGame();
    btnLoadGame.setDisable(false);
  }

  @FXML
  private void onSwitchToGame() {
    // load the game instance
    Scene sceneBtnIsIn = btnNewGame.getScene();
    sceneBtnIsIn.setRoot(SceneManager.getUi(SceneManager.AppUi.CANVAS));
  }
}
