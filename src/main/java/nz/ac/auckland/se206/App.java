package nz.ac.auckland.se206;

import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * This is the entry point of the JavaFX application, while you can change this class, it should
 * remain as the class that runs the JavaFX application.
 */
public class App extends Application {
  public static void main(final String[] args) {
    launch();
  }

  /**
   * Returns the node associated to the input file. The method expects that the file is located in
   * "src/main/resources/fxml".
   *
   * @param fxml The name of the FXML file (without extension).
   * @return The node of the input file.
   * @throws IOException If the file is not found.
   */
  public static Parent loadFxml(final String fxml) throws IOException {
    return new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml")).load();
  }

  /**
   * This method is invoked when the application starts. It loads and shows the "Canvas" scene.
   *
   * @param stage The primary stage of the application.
   * @throws IOException If "src/main/resources/fxml/canvas.fxml" is not found.
   */
  @Override
  public void start(final Stage stage) throws IOException {
    // we'll always have only one chooseplayer and menu instances
    SceneManager.storeUi(SceneManager.AppUi.CHOOSEPLAYER, loadFxml("chooseplayer"));
    SceneManager.storeUi(SceneManager.AppUi.MENU, loadFxml("menu"));

    // starting scene should be the choose player root -> forces the user to choose a player
    // or create one before any game
    final Scene scene = new Scene(SceneManager.getUi(SceneManager.AppUi.CHOOSEPLAYER), 640, 480);
    stage.setScene(scene);
    stage.show();

    // have an event handler to when the user closes the app
    // should close the threads
    stage.setOnCloseRequest(
        new EventHandler<WindowEvent>() {
          @Override
          public void handle(WindowEvent event) {
            // calls the application stop method
            Platform.exit();
            // terminates any running programs aka threads
            System.exit(0);
          }
        });
  }
}
