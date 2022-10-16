package nz.ac.auckland.se206;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.media.AudioClip;

public class MenuController {

  @FXML private Button btnNewGame;
  @FXML private Button btnLoadGame;
  @FXML private Button btnChoosePlayer;
  @FXML private Button btnStatistics;
  @FXML private Button btnLeaderboard;
  @FXML private Label lblUser;

  @FXML
  private void initialize() throws IOException {
    btnLoadGame.setDisable(true); // no game to load at initialisation
    lblUser.setText("Hi, " + SceneManager.getMainUser());

    // event handler for load game button
    btnLoadGame.setOnMouseClicked(
        event -> {
          new AudioClip(getClass().getResource("/sounds/OnBackSound.wav").toExternalForm()).play();
        });

    // load the statistics screen and the game settings
    SceneManager.storeUi(SceneManager.AppUi.STATISTICS, App.loadFxml("statistics"));
    SceneManager.storeUi(SceneManager.AppUi.GAMESETTINGS, App.loadFxml("gamesettings"));
    SceneManager.storeUi(SceneManager.AppUi.LEADERBOARD, App.loadFxml("leaderboard"));
  }

  @FXML
  private void onCreateGame() throws IOException {
    // play sound
    new AudioClip(getClass().getResource("/sounds/ButtonClickSound.wav").toExternalForm()).play();

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
  private void onClickLeaderboard() {

    new AudioClip(getClass().getResource("/sounds/ButtonClickSound.wav").toExternalForm()).play();
    Scene sceneBtnIsIn = btnChoosePlayer.getScene();
    sceneBtnIsIn.setRoot(SceneManager.getUi(SceneManager.AppUi.LEADERBOARD));
  }

  @FXML
  private void onSwitchToGame() {

    // load the game instance
    Scene sceneBtnIsIn = btnNewGame.getScene();
    sceneBtnIsIn.setRoot(SceneManager.getUi(SceneManager.AppUi.CANVAS));
    // set the stylesheet specifically for zen mode
    if (SceneManager.getProfile(SceneManager.getMainUser()).isZenMode()) {
      sceneBtnIsIn
          .getRoot()
          .getStylesheets()
          .add(getClass().getResource("/css/zencanvas.css").toExternalForm());
    }
  }

  @FXML
  private void onChoosePlayer() {
    new AudioClip(getClass().getResource("/sounds/ButtonClickSound.wav").toExternalForm()).play();
    // load the choose player root
    Scene sceneBtnIsIn = btnChoosePlayer.getScene();
    sceneBtnIsIn.setRoot(SceneManager.getUi(SceneManager.AppUi.CHOOSEPLAYER));
  }

  @FXML
  private void onClickStatistics() {
    new AudioClip(getClass().getResource("/sounds/ButtonClickSound.wav").toExternalForm()).play();
    Scene sceneBtnIsIn = btnChoosePlayer.getScene();
    sceneBtnIsIn.setRoot(SceneManager.getUi(SceneManager.AppUi.STATISTICS));
  }

  @FXML
  private void onSwitchToGameSettings() {
    new AudioClip(getClass().getResource("/sounds/ButtonClickSound.wav").toExternalForm()).play();
    Scene sceneBtnIsIn = btnChoosePlayer.getScene();
    sceneBtnIsIn.setRoot(SceneManager.getUi(SceneManager.AppUi.GAMESETTINGS));
  }
}
