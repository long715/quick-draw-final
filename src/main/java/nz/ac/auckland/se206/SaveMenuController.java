package nz.ac.auckland.se206;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class SaveMenuController {

  @FXML private Button btnSave;
  @FXML private Label lblTitle;
  @FXML private Label lblWarning;
  @FXML private TextField txtDir;

  private BufferedImage image;
  private Stage stage;
  private String pathName;

  public void setImage(BufferedImage image) {
    this.image = image;
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  /**
   * Gets the selected directory from the file chooser and updates the text area with the chosen
   * directory with the file extension. This is beacuse the text value is used to create the file
   * path where the canvas snapshot will be saved.
   */
  @FXML
  private void onChooseFile() {
    // get the directory from the chooser and set the label to
    // show the user the chosen directory
    FileChooser fc = new FileChooser();
    File selectedDir = fc.showSaveDialog(stage);
    if (selectedDir != null) {
      txtDir.setText(selectedDir.getAbsolutePath() + ".bmp");
    }
  }

  /**
   * Takes the directory (text value) in the text area, which is used as the path to create the .bmp
   * file of the canvas snapshot. The warning label should show the user why their input is invalid.
   *
   * @throws IOException If and IO error occurs anywhere when saving the snapshot
   */
  @FXML
  private void onSave() throws IOException {
    // if dir doesnt contain the .bmp extension, its considered as
    // invalid
    if (!txtDir.getText().contains(".bmp")) {
      lblWarning.setText("Does not contain .bmp extension");
      return;
    }
    pathName = txtDir.getText();

    // create a file for the canvas drawing
    File createdFile = saveCurrentSnapshotOnFile();

    // this file input stream instance reads the created file into
    // bytes so that the file output stream can write it
    FileInputStream fis = new FileInputStream(createdFile);
    byte[] arr = fis.readAllBytes();
    fis.close();

    // create the file path for the saved canvas drawing
    Path newFilePath = Paths.get(pathName);

    // CHECKING FOR WARNINGS
    File file = new File(pathName);

    // check if file name is invalid
    if (file.getName().equalsIgnoreCase(".bmp")) {
      lblWarning.setText("Invalid file name");
      return;
    }
    // check if there's no parent directory, prevents default dir
    if (file.getParent() == null) {
      lblWarning.setText("No parent directory provided");
      return;
    }
    // check if file exists
    if (file.exists()) {
      lblWarning.setText("File name already exists");
      return;
    }

    try {
      Files.createFile(newFilePath); // create a file in this file path
    } catch (NoSuchFileException e) {
      lblWarning.setText("Invalid path provided");
      return;
    }

    // initialise the output stream and write on the created file
    FileOutputStream fos = new FileOutputStream(pathName);
    fos.write(arr);
    fos.close();

    this.stage.close();
  }

  /**
   * Save the current snapshot on a bitmap file. This is the snapshot saved to the user's local
   * machine.
   *
   * @return The file of the saved image.
   * @throws IOException If the image cannot be saved.
   */
  private File saveCurrentSnapshotOnFile() throws IOException {
    // You can change the location as you see fit.
    final File tmpFolder = new File("tmp");

    if (!tmpFolder.exists()) {
      tmpFolder.mkdir();
    }

    // We save the image to a file in the tmp folder.
    final File imageToClassify =
        new File(tmpFolder.getName() + "/snapshot" + System.currentTimeMillis() + ".bmp");

    // Save the image to a file.
    ImageIO.write(image, "bmp", imageToClassify);

    return imageToClassify;
  }
}
