package nz.ac.auckland.se206;

import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;

public class BadgesController {
  @FXML private Button btnBack;
  @FXML private ListView<ImageView> lstvBadges;
  @FXML private ImageView imgView;
  @FXML private Label lblHeading;
  private ArrayList<ImageView> badgesList;
  private ArrayList<String> badgeImagePaths;

  @FXML
  private void initialize() {

    // getting the current user's profile
    UserProfile user = SceneManager.getProfile(SceneManager.getMainUser());

    // getting the badges earned by the user
    badgeImagePaths = user.getBadgesEarned();
    badgesList = new ArrayList<ImageView>();

    // image path cannot be empty string
    badgeImagePaths.removeIf(s -> s == null || "".equals(s));

    if (!(user == null)) {
      // if the user has earned some badges, we display them
      if (!(badgeImagePaths.isEmpty())) {
        lblHeading.setText("Badges Earned");

        // converting badge image paths to images and adding them to the image view list
        for (String badgeURL : (badgeImagePaths)) {
          badgesList.add(new ImageView(badgeURL));
        }
        lstvBadges.getItems().addAll(badgesList);

      } else {

        lblHeading.setText("Play more to earn badges!");
      }
    }
  }

  @FXML
  private void onBack() {
    btnBack
        .getScene()
        .setRoot(SceneManager.getUi(SceneManager.AppUi.STATISTICS)); // returns to stats screen
  }
}
