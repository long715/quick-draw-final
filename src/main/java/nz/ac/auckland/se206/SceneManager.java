package nz.ac.auckland.se206;

import java.util.HashMap;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * This class is used manage the two types of roots in this Scene: Menu and Game.
 *
 * <p>Each type should only have one instance each, the menu is initialised when the app starts, and
 * the game root should be initialised every time a new game is made.
 */
public class SceneManager {

  // create an enum representing the two types of roots: Menu and Game
  public enum AppUi {
    MENU,
    CANVAS,
    CHOOSEPLAYER,
    STATISTICS,
    GAMESETTINGS,
    BADGES
  }

  private static String mainUser = "";

  // use hashmap to store the instances of each root
  private static HashMap<AppUi, Parent> sceneMap = new HashMap<AppUi, Parent>();
  // use hashmap to store user profile instances using its name (string) as the
  // key
  private static HashMap<String, UserProfile> profileMap = new HashMap<String, UserProfile>();

  /**
   * This method maps the ui and its associated root instance to the hashmap.
   *
   * @param ui the type of the root
   * @param root the root instance
   */
  public static void storeUi(AppUi ui, Parent root) {
    sceneMap.put(ui, root);
  }

  /**
   * This method maps the user name to the user profile instances.
   *
   * @param name The name of the selected user
   * @param profile The UserProfile instance associated with the name
   */
  public static void storeProfile(String name, UserProfile profile) {
    profileMap.put(name, profile);
  }

  /**
   * This method retrieves and returns the root instance from the root type.
   *
   * @param ui the type of the root
   * @return the root instance
   */
  public static Parent getUi(AppUi ui) {
    return sceneMap.get(ui);
  }

  /**
   * This method retrieves and returns the UserProfile stance given the name of the user
   *
   * @param name The name of the user
   * @return the user profile instance
   */
  public static UserProfile getProfile(String name) {
    return profileMap.get(name);
  }

  /**
   * This method deletes the user in the database.
   *
   * @param name The name of the user to be deleted
   */
  public static void deleteProfile(String name) {
    profileMap.remove(name);
  }

  /** This method deletes the canvas from sceneMap. */
  public static void deleteCanvas() {
    sceneMap.remove(AppUi.CANVAS);
  }

  /**
   * This method checks if the profile exists in hashmap.
   *
   * @param name The user name to check
   * @return boolean of whether the map contains the name
   */
  public static boolean ifContainsProfile(String name) {
    return profileMap.containsKey(name);
  }

  /**
   * This method checks if a previous game has already been mapped to the CANVAS key.
   *
   * @return a boolean that represents if a previous game already exists
   */
  public static boolean ifPrevGameExists() {
    return sceneMap.containsKey(AppUi.CANVAS);
  }

  /**
   * This method replaces the root instance in the hashmap given the ui type.
   *
   * @param ui the type of the root
   * @param root the root instance that will replace the old value of the ui type
   */
  public static void replaceUi(AppUi ui, Parent root) {
    sceneMap.replace(ui, root);
  }

  /**
   * Helper function to configure the modal for the pop up stage.
   *
   * @param btn The button from the scene
   * @param root The root of which the function is called
   * @return the stage of the pop up
   */
  public static Stage setModal(Button btn, Parent root) {
    Stage stage = new Stage();
    stage.initModality(Modality.WINDOW_MODAL);
    stage.initOwner(btn.getScene().getWindow());
    stage.setScene(new Scene(root));
    return stage;
  }

  public static void setMainUser(String name) {
    mainUser = name;
  }

  public static String getMainUser() {
    return mainUser;
  }
}
