package nz.ac.auckland.se206;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MenuController {

  @FXML private Button btnNewGame;
  @FXML private Button btnLoadGame;
  @FXML private Button btnChoosePlayer;
  @FXML private Button btnStatistics;
  @FXML private Button btnLeaderboard;
  @FXML private Label lblUser;

  /**
   * This is the entry method executed when the instance is firstly loaded. It sets up the
   * statistics and game settings page of the selected user.
   *
   * @throws IOException if the fxml file we are trying to load is not found
   */
  @FXML
  private void initialize() throws IOException {
    btnLoadGame.setDisable(true); // no game to load at initialisation
    lblUser.setText("Hi, " + SceneManager.getMainUser());

    // load the statistics screen and the game settings
    SceneManager.storeUi(SceneManager.AppUi.STATISTICS, App.loadFxml("statistics"));
    SceneManager.storeUi(SceneManager.AppUi.GAMESETTINGS, App.loadFxml("gamesettings"));
    SceneManager.storeUi(SceneManager.AppUi.LEADERBOARD, App.loadFxml("leaderboard"));
  }

  /**
   * This method is executed when the new game method is clicked. Creates a new instance of the
   * canvas page and loads that page.
   *
   * @throws IOException if the fxml file we are trying to load is not found
   */
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

  /**
   * This method is called by the onCreateGame() method or is executed when the "load game" button
   * is clicked. This loads the stored canvas page.
   */
  @FXML
  private void onClickLeaderboard() {
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

  /** This method loads the Choose Player instance when the avatar icon is clicked. */
  @FXML
  private void onChoosePlayer() {
    // load the choose player root
    Scene sceneBtnIsIn = btnChoosePlayer.getScene();
    sceneBtnIsIn.setRoot(SceneManager.getUi(SceneManager.AppUi.CHOOSEPLAYER));
  }

  /**
   * This method loads the stored statistics page of the selected user when the piechart icon is
   * clicked.
   */
  @FXML
  private void onClickStatistics() {
    Scene sceneBtnIsIn = btnChoosePlayer.getScene();
    sceneBtnIsIn.setRoot(SceneManager.getUi(SceneManager.AppUi.STATISTICS));
  }

  /**
   * This method loads the stored GAME statistics page of the user when the "game settings" button
   * is clicked.
   */
  @FXML
  private void onSwitchToGameSettings() {
    Scene sceneBtnIsIn = btnChoosePlayer.getScene();
    sceneBtnIsIn.setRoot(SceneManager.getUi(SceneManager.AppUi.GAMESETTINGS));
  }
}
