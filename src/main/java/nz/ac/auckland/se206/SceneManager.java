package nz.ac.auckland.se206;

import java.util.HashMap;
import javafx.scene.Parent;

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
    CHOOSEPLAYER
  }

  // use hashmap to store the instances of each root
  private static HashMap<AppUi, Parent> sceneMap = new HashMap<AppUi, Parent>();

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
   * This method retrieves and returns the root instance from the root type.
   *
   * @param ui the type of the root
   * @return the root instance
   */
  public static Parent getUi(AppUi ui) {
    return sceneMap.get(ui);
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
}
