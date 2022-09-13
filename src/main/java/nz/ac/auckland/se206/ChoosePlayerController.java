package nz.ac.auckland.se206;

import java.io.File;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class ChoosePlayerController {

  @FXML private Button btnCreatePlayer;
  @FXML private Button btnOK;
  @FXML private Button btnCancel;
  @FXML private Button btnDelete; 
  @FXML private ListView<String> lstvPlayers;

  // used to access the name to be added to the list in other controllers
  private static String name = ""; // default an empty string

  @FXML
  private void initialize() throws NumberFormatException, IOException {

    // load the list of players by firstly getting the list of files
    File folder = new File("src/main/resources/data/users");
    File[] files = folder.listFiles();

    // for each file name, add it into the list
    for (File file : files) {
      if (!file.isHidden()) { // ignores any hidden files
        String userName = file.getName().replace("_", " ").replace(".txt", "");
        lstvPlayers.getItems().add(userName);

        // load the file data via UserProfile constructor
        UserProfile user = new UserProfile(userName);
        user.readDataFromFile(file);
        // add this to the profile map
        SceneManager.storeProfile(userName, user);
      }
    }

    // during initialise where the main user is null, cancel must always be disabled
    btnCancel.setDisable(true);
    // if list is empty, disable ok and delete button
    if (lstvPlayers.getItems().isEmpty()) {
      btnOK.setDisable(true);
      btnDelete.setDisable(true);
    }
  }

  @FXML
  private void onCreate() throws IOException {
    // creating the create player pop up
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(getClass().getResource("/fxml/" + "createplayer" + ".fxml"));
    Parent root = loader.load();

    Stage stage = SceneManager.setModal(btnCreatePlayer, root);

    CreatePlayerController controller = loader.getController();
    controller.setStage(stage);
    stage.showAndWait(); // ensures the pop up is interacted first

    // add the user name to the list
    if (!name.isEmpty()) {
      lstvPlayers.getItems().add(name);
    }
    name = ""; // reset name variable

    // enable the buttons since there is at least one user
    btnOK.setDisable(false);
    btnDelete.setDisable(false);
    // check if this is the first entry to the instance
    if (!SceneManager.getMainUser().equalsIgnoreCase("")) { // if not, enable cancel button
      btnCancel.setDisable(false);
    }
  }

  @FXML
  private void onDelete() {
    // delete the user instance from hash map
    SceneManager.deleteProfile(lstvPlayers.getSelectionModel().getSelectedItem());
    // delete the file
    File file =
        new File(
            "src/main/resources/data/users",
            lstvPlayers.getSelectionModel().getSelectedItem().replace(" ", "_") + ".txt");
    file.delete();
    // remove the name from list view
    lstvPlayers.getItems().remove(lstvPlayers.getSelectionModel().getSelectedIndex());

    // check if there are users
    if (lstvPlayers.getItems().isEmpty()) {
      btnOK.setDisable(true);
      btnDelete.setDisable(true);
    }

    // check if the main user is not in the list, if deleted, disable cancel btn
    if (!lstvPlayers.getItems().contains(SceneManager.getMainUser())) {
      btnCancel.setDisable(true);
    }
  }

  /**
   * This method passes the chosen user profile to the SceneManager and create the UserProfile
   * instance.
   *
   * @throws IOException
   */
  @FXML
  private void onChoose() throws IOException {
    // get the name of the selected user
    String userName = lstvPlayers.getSelectionModel().getSelectedItem();
    if (userName != null) { // prevents the case when user did not select anything from the list

      // check if previous userName is the same as the selected one
      if (!SceneManager.getMainUser().equalsIgnoreCase(userName)) {
        // if they are not the same user, remove the CANVAS key from the sceneMap
        // and reset the menu
        SceneManager.deleteCanvas();
        SceneManager.setMainUser(userName); // set user befpre initialising menu
        SceneManager.replaceUi(SceneManager.AppUi.MENU, App.loadFxml("menu"));
      }

      btnCancel.setDisable(false);
      // switch to the main menu aka onCancel
      onCancel();
    }
  }

  @FXML
  private void onCancel() {
    btnCancel.getScene().setRoot(SceneManager.getUi(SceneManager.AppUi.MENU));
  }

  public static void setName(String playerName) {
    name = playerName;
  }

  public static String getName() {
    return name;
  }
}
