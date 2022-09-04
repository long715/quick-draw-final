package nz.ac.auckland.se206;


import ai.djl.ModelException;
import ai.djl.modality.Classifications.Classification;
import ai.djl.translate.TranslateException;
import com.opencsv.exceptions.CsvException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;
import nz.ac.auckland.se206.ml.DoodlePrediction;
import nz.ac.auckland.se206.speech.TextToSpeech;
import nz.ac.auckland.se206.words.CategorySelector;
import nz.ac.auckland.se206.words.CategorySelector.Difficulty;

/**
 * This is the controller of the canvas. You are free to modify this class and the corresponding
 * FXML file as you see fit. For example, you might no longer need the "Predict" button because the
 * DL model should be automatically queried in the background every second.
 *
 * <p>!! IMPORTANT !!
 *
 * <p>Although we added the scale of the image, you need to be careful when changing the size of the
 * drawable canvas and the brush size. If you make the brush too big or too small with respect to
 * the canvas size, the ML model will not work correctly. So be careful. If you make some changes in
 * the canvas and brush sizes, make sure that the prediction works fine.
 */
public class CanvasController {

  @FXML private Canvas canvas;
  @FXML private Label lblCategory;
  @FXML private Label lblTime;
  @FXML private Label lblGuesses;
  @FXML private Label lblWinOrLose;
  @FXML private Button btnToMenu;
  @FXML private Button btnReady;
  @FXML private Button clearButton;
  @FXML private Button btnSaveDrawing;
  @FXML private TextField txtDirectory;
  @FXML private TextField txtFileName;

  private GraphicsContext graphic;
  private DoodlePrediction model;
  private String currentWord;
  private TextToSpeech speech;

  // mouse coordinates
  private double currentX;
  private double currentY;
  /**
   * JavaFX calls this method once the GUI elements are loaded. In our case we create a listener for
   * the drawing, and we load the ML model.
   *
   * @throws ModelException If there is an error in reading the input/output of the DL model.
   * @throws IOException If the model cannot be found on the file system.
   * @throws CsvException
   * @throws URISyntaxException
   */
  public void initialize() throws ModelException, IOException, URISyntaxException, CsvException {
    graphic = canvas.getGraphicsContext2D();

    // implement the category selector and display the category on the lbl
    CategorySelector categorySelector = new CategorySelector();
    String randomWord = categorySelector.getRandomCategory(Difficulty.E);
    lblCategory.setText(randomWord);
    currentWord = randomWord;

    onDraw();

    // when a new game page is loaded, we want the following:
    canvas.setDisable(true); // user can't draw unless user presses the ready button
    btnToMenu.setDisable(
        true); // user can't go back and create a new game before finishing the current game
    btnSaveDrawing.setDisable(
        true); // user can't save an empty canvas, drawing can only be saved after game ends

    // since the user can't save, they shouldn't be able to type into the text boxes before and
    // during the game
    txtDirectory.setDisable(true);
    txtFileName.setDisable(true);

    model = new DoodlePrediction();
    speech = new TextToSpeech();

    // create a task and bg thread for the text to speech so that loading doesnt lag
    Task<Void> taskWelcomeSpeech =
        new Task<Void>() {
          protected Void call() {
            // tell the player the word and instructions on how to start the game
            speech.speak(
                "You got 60 seconds to draw "
                    + currentWord
                    + ", press the ready button whenever you are ready!");

            return null;
          }
        };

    Thread bgWelcomeSpeech = new Thread(taskWelcomeSpeech);
    bgWelcomeSpeech.start();
  }

  @FXML
  private void onSwitchToMenu() {
    Scene sceneBtnIsIn = btnToMenu.getScene();
    sceneBtnIsIn.setRoot(SceneManager.getUi(SceneManager.AppUi.MENU));
  }

  @FXML
  private void onStartGame() throws InterruptedException, ExecutionException {
    // enable the canvas and disable to ready btn
    canvas.setDisable(false);
    btnReady.setDisable(true);

    // implementation of timer with concurrency

    // create the task for the timer
    Task<Void> taskTimer =
        new Task<Void>() {
          protected Void call() {
            // get the current time
            long time = System.currentTimeMillis();
            // run loop when time difference is less than or equals to 60 seconds = 60 000ms
            while ((System.currentTimeMillis() - time) <= 60000) {
              // update the text of the label for the timer
              updateTitle(
                  String.valueOf(
                          60
                              - (int)
                                  Math.floor((double) (System.currentTimeMillis() - time) / 1000))
                      + " s");
              // check if task has been cancelled, this is usually due the user winning early
              if (this.isCancelled()) {
                break;
              }
            }

            return null;
          }
        };

    // create the task for the DL predictions
    Task<Boolean> taskPredict =
        new Task<Boolean>() {
          protected Boolean call()
              throws TranslateException, InterruptedException, ExecutionException {
            // get the current time and create a temp time
            long time = System.currentTimeMillis();
            long temp = time;

            // run loop when time difference is less than or equals to 60 seconds = 60 000ms
            // should follow timer task
            while ((System.currentTimeMillis() - time) <= 60000) {

              // when a second has passed, run the DL predictor
              if ((int) Math.floor((double) (System.currentTimeMillis() - temp) / 1000) >= 1) {
                // create a future task for getting the prediction string
                // required for accessing the variable from outside platform run later
                FutureTask<StringBuilder> predict =
                    new FutureTask<StringBuilder>(
                        new Callable<StringBuilder>() {
                          public StringBuilder call() throws TranslateException {
                            // get the list of the top 10 classifications and format the list into
                            // stringbuilder
                            return DoodlePrediction.getPredictionString(
                                model.getPredictions(getCurrentSnapshot(), 10));
                          }
                        });
                // create a fitire task for checking wins
                FutureTask<Boolean> winOrLose =
                    new FutureTask<Boolean>(
                        new Callable<Boolean>() {
                          public Boolean call() throws TranslateException {
                            return isWin(model.getPredictions(getCurrentSnapshot(), 10), 3);
                          }
                        });

                // get the top 10 list and check if the current word is within the top 3 (EASY)
                Platform.runLater(predict);
                Platform.runLater(winOrLose);
                updateTitle(predict.get().toString());

                // set the temp time
                temp = System.currentTimeMillis();

                // check if the user won
                if (winOrLose.get()) {
                  taskTimer.cancel();
                  return true;
                }
              }
            }

            return false;
          }
        };

    // bind the title property to the timer label
    lblTime.textProperty().bind(taskTimer.titleProperty());
    // bind the title property to the guesses label
    lblGuesses.textProperty().bind(taskPredict.titleProperty());

    // create the bg thread for the timer task
    Thread bgTimer = new Thread(taskTimer);
    bgTimer.start();
    // create the bg thread for the dl task
    Thread bgPredict = new Thread(taskPredict);
    bgPredict.start();

    taskPredict.setOnSucceeded(
        event -> {
          // once the game has ended (timer runs out or if they won), we want the following UX:
          canvas.setDisable(true); // user should not be able to draw on the canvas
          btnToMenu.setDisable(
              false); // user can go back to the main menu to load the previous game or create a new
          // game
          clearButton.setDisable(true); // user can't alter or reset the drawing in any way

          // allow user to save the current drawing and write on the text fields for
          // custom directory and file name inputs
          btnSaveDrawing.setDisable(false);
          txtDirectory.setDisable(false);
          txtFileName.setDisable(false);

          // update the winOrLose label and use the text to speech to tell the user if the they have
          // won or lost
          try {
            if (taskPredict.get()) { // returns true if user has won
              lblWinOrLose.setText("WIN");

              // create a task for the winning text-to-speech message
              Task<Void> taskWin =
                  new Task<Void>() {
                    protected Void call() {
                      speech.speak("Congratulations! You won!");
                      return null;
                    }
                  };
              // run the bg thread for the task
              Thread bgWinSpeech = new Thread(taskWin);
              bgWinSpeech.start();

            } else {
              lblWinOrLose.setText("LOSE");

              // create a task for the losing text-to-speech message
              Task<Void> taskLose =
                  new Task<Void>() {
                    protected Void call() {
                      speech.speak("Sorry! You lost!");
                      return null;
                    }
                  };
              // run the bg thread for the task
              Thread bgLoseSpeech = new Thread(taskLose);
              bgLoseSpeech.start();
            }
          } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        });
  }

  /** This method is called when the "Clear" button is pressed. */
  @FXML
  private void onClear() {
    graphic.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
  }

  @FXML
  private void onDraw() {
    // save coordinates when mouse is pressed on the canvas
    canvas.setOnMousePressed(
        e -> {
          currentX = e.getX();
          currentY = e.getY();
        });

    canvas.setOnMouseDragged(
        e -> {
          // Brush size (you can change this, it should not be too small or too large).
          final double size = 6;

          final double x = e.getX() - size / 2;
          final double y = e.getY() - size / 2;

          // This is the colour of the brush.
          graphic.setStroke(Color.BLACK);
          graphic.setLineWidth(size);

          // Create a line that goes from the point (currentX, currentY) and (x,y)
          graphic.strokeLine(currentX, currentY, x, y);

          // update the coordinates
          currentX = x;
          currentY = y;
        });
  }

  @FXML
  private void onErase() {
    // save coordinates when mouse is pressed on the canvas
    canvas.setOnMousePressed(
        e -> {
          currentX = e.getX();
          currentY = e.getY();
        });

    canvas.setOnMouseDragged(
        e -> {
          // Brush size (you can change this, it should not be too small or too large).
          final double size = 6;

          final double x = e.getX() - size / 2;
          final double y = e.getY() - size / 2;

          // This is the colour of the brush.
          graphic.setStroke(Color.WHITE);
          graphic.setLineWidth(size);

          // Create a line that goes from the point (currentX, currentY) and (x,y)
          graphic.strokeLine(currentX, currentY, x, y);

          // update the coordinates
          currentX = x;
          currentY = y;
        });
  }

  @FXML
  private void onSave() throws IOException {

    // set the default values for the directory and name, use desktop as the default directory
    String directory =
        FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + "/desktop";
    String name = "default";

    // check if any of the text fields are filled up, if filled up, overwrite the default values
    if (!txtDirectory.getText().equalsIgnoreCase("")) {
      directory = txtDirectory.getText();
    }
    if (!txtFileName.getText().equalsIgnoreCase("")) {
      name = txtFileName.getText();
    }

    // create a file for the canvas drawing
    File createdFile = saveCurrentSnapshotOnFile();

    // this file input stream instance reads the created file into
    // bytes so that the file output stream can write it
    FileInputStream fis = new FileInputStream(createdFile);
    byte[] arr = fis.readAllBytes();
    fis.close();

    // create the file path for the saved canvas drawing
    Path newFilePath = Paths.get(directory + "/" + name + ".bmp");
    Files.createFile(newFilePath); // create a file in this file path

    // initialise the output stream and write on the created file
    FileOutputStream fos = new FileOutputStream(directory + "/" + name + ".bmp");
    fos.write(arr);
    fos.close();
  }

  /**
   * This method checks if the current word is in the top x classifications where x is the number
   * specified.
   *
   * @param topPredictions The boundary value that current word is checked against
   * @param classifications The list of the DL predictions
   * @return if current word is included within the boundary which means the player has won
   */
  private boolean isWin(List<Classification> classifications, int topPredictions) {
    for (int i = 0; i < topPredictions; i++) {
      if (classifications.get(i).getClassName().equals(currentWord)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Get the current snapshot of the canvas.
   *
   * @return The BufferedImage corresponding to the current canvas content.
   */
  private BufferedImage getCurrentSnapshot() {
    final Image snapshot = canvas.snapshot(null, null);
    final BufferedImage image = SwingFXUtils.fromFXImage(snapshot, null);

    // Convert into a binary image.
    final BufferedImage imageBinary =
        new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);

    final Graphics2D graphics = imageBinary.createGraphics();

    graphics.drawImage(image, 0, 0, null);

    // To release memory we dispose.
    graphics.dispose();

    return imageBinary;
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
    ImageIO.write(getCurrentSnapshot(), "bmp", imageToClassify);

    return imageToClassify;
  }
}
