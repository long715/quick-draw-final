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
  private String tempName;
  private int counter = 1;

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  @FXML
  private void onClose() throws IOException {
    // the initial name based on user input
    if (!txtPlayerName.getText().equalsIgnoreCase("")) {
      tempName = txtPlayerName.getText();
    } else {
      tempName = "no name";
    }

    // check if the input is a valid name eg. no duplicates
    StringBuilder sb = new StringBuilder();
    sb.append(tempName);
    while (SceneManager.ifContainsProfile(sb.toString())) {
      sb = new StringBuilder();
      sb.append(tempName);
      sb.append(counter);
      counter++;
    }

    // set the result from string builder to be the final user name
    ChoosePlayerController.setName(sb.toString());

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
