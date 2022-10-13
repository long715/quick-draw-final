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

    if (!(user == null)) {

      // if the user has earned some badges, we display them
      if ((badgeImagePaths.size() > 1)) {
        System.out.println(badgeImagePaths.toString());

        // converting badge image paths to images and adding them to the image view list
        for (String badgeURL : (badgeImagePaths)) {
          badgesList.add(new ImageView(badgeURL));
        }
        lstvBadges.getItems().addAll(badgesList);

      } else {
        // this text is displayed when the user has not earned any badges
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
