package nz.ac.auckland.se206;

import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.media.AudioClip;

public class BadgesController {
  @FXML private Button btnBack;
  @FXML private ListView<ImageView> lstvBadges;
  @FXML private ImageView imgView;
  @FXML private Label lblHeading;
  @FXML private ListView<String> lstvBadgesInfo;
  @FXML private Label lblBadgeDetail;
  @FXML private Label lblSubheading;
  private ArrayList<ImageView> badgesList;
  private ArrayList<String> badgesInfo;
  private ArrayList<String> badgeImagePaths;

  /** Entry method that displays the latest badge statistics of the user in the components. */
  @FXML
  private void initialize() {

    // getting the current user's profile
    UserProfile user = SceneManager.getProfile(SceneManager.getMainUser());

    // getting the badges earned by the user
    badgeImagePaths = user.getBadgesEarned();

    // image path cannot be empty string
    badgeImagePaths.removeIf(s -> s == null || "".equals(s));

    if (!(user == null)) {
      // if the user has earned some badges, we display them
      if (!(badgeImagePaths.isEmpty())) {
        lblHeading.setText("Badges Earned");
        lblSubheading.setText("You have earned " + badgeImagePaths.size() + " out of 3 badges!");
        lblBadgeDetail.setText("Click on the badge for more info");
        badgesList = new ArrayList<ImageView>();
        badgesInfo = new ArrayList<>();

        // converting badge image paths to images and adding them to the image view list
        for (String badgeURL : (badgeImagePaths)) {
          badgesList.add(new ImageView(badgeURL));
        }
        lstvBadges.getItems().addAll(badgesList);

        // extracting the badge detail from the image's path
        for (int i = 0; i < badgeImagePaths.size(); i++) {
          String info =
              badgeImagePaths.get(i).replace("/images/", "").replace("_", " ").replace(".png", "");
          badgesInfo.add(info);
        }

        // adding a listener to track changes in selection and show detail of the
        // selected badge
        lstvBadges
            .getSelectionModel()
            .selectedItemProperty()
            .addListener(
                event -> {
                  new AudioClip(
                          getClass().getResource("/sounds/ListSelectionSound.wav").toExternalForm())
                      .play();
                  int i = lstvBadges.getSelectionModel().getSelectedIndex();
                  lblBadgeDetail.setText("Badge awarded for : " + badgesInfo.get(i));
                });

      } else {

        lblHeading.setText("Play to earn badges!");
      }
    }
  }

  /** This loads the statistics root instance of the user to the scene. */
  @FXML
  private void onBack() {
    new AudioClip(getClass().getResource("/sounds/OnBackSound.wav").toExternalForm()).play();
    btnBack
        .getScene()
        .setRoot(SceneManager.getUi(SceneManager.AppUi.STATISTICS)); // returns to stats screen
  }
}
