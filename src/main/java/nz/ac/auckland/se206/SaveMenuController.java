package nz.ac.auckland.se206;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class SaveMenuController {

  @FXML private Button btnSave;
  @FXML private Label lblDir;

  private BufferedImage image;
  private Stage stage;
  private String pathName;

  public void setImage(BufferedImage image) {
    this.image = image;
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  @FXML
  private void initialize() {
    btnSave.setDisable(true);
  }

  @FXML
  private void onChooseFile() {
    // get the directory from the chooser and set the label to
    // show the user the chosen directory
    FileChooser fc = new FileChooser();
    lblDir.setText(fc.showSaveDialog(stage).getAbsolutePath());

    // get the name from the text area
    pathName = lblDir.getText();

    // enable the save button
    btnSave.setDisable(false);
  }

  @FXML
  private void onSave() throws IOException {

    // create a file for the canvas drawing
    File createdFile = saveCurrentSnapshotOnFile();

    // this file input stream instance reads the created file into
    // bytes so that the file output stream can write it
    FileInputStream fis = new FileInputStream(createdFile);
    byte[] arr = fis.readAllBytes();
    fis.close();

    // create the file path for the saved canvas drawing
    Path newFilePath = Paths.get(pathName);
    Files.createFile(newFilePath); // create a file in this file path

    // initialise the output stream and write on the created file
    FileOutputStream fos = new FileOutputStream(pathName);
    fos.write(arr);
    fos.close();

    this.stage.close();
  }

  /**
   * Save the current snapshot on a bitmap file.
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
