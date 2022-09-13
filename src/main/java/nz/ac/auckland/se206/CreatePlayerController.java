package nz.ac.auckland.se206;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreatePlayerController {
  @FXML private Button btnOk;
  @FXML private TextField txtPlayerName;

  private Stage stage;

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  @FXML
  private void onClose() throws IOException {
    if (!txtPlayerName.getText().equalsIgnoreCase("")) {
      ChoosePlayerController.setName(
          txtPlayerName.getText()); // replaces static every time rather than creating new instances
    } else {
      ChoosePlayerController.setName("no name");
    }

    // create the User Profile instance and create its data file
    UserProfile user = new UserProfile(ChoosePlayerController.getName());
    user.saveData();
    // store the user in the user map
    SceneManager.storeProfile(ChoosePlayerController.getName(), user);

    this.stage.close();
  }

  @FXML
  private void onCancel() {
    this.stage.close();
  }
}
