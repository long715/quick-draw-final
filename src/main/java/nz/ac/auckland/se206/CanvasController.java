package nz.ac.auckland.se206;

import ai.djl.ModelException;
import ai.djl.modality.Classifications.Classification;
import ai.djl.translate.TranslateException;
import com.opencsv.exceptions.CsvException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import nz.ac.auckland.se206.ml.DoodlePrediction;
import nz.ac.auckland.se206.speech.TextToSpeech;
import nz.ac.auckland.se206.words.CategorySelector;
import nz.ac.auckland.se206.words.CategorySelector.Difficulty;
import java.io.File;


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
  @FXML private Label lblWinOrLose;
  @FXML private Label lblGuesses;
  @FXML private Button btnToMenu;
  @FXML private Button btnReady;
  @FXML private Button clearButton;
  @FXML private Button btnSaveDrawing;

  private GraphicsContext graphic;
  private DoodlePrediction model;
  private String currentWord;
  private TextToSpeech speech;

  private IntegerProperty seconds = new SimpleIntegerProperty(60);
  private Timeline timeline = new Timeline();
  private UserProfile currentUser = SceneManager.getProfile(SceneManager.getMainUser());


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
    ArrayList<String> playedWords = currentUser.getWords();


    String randomWord = categorySelector.getRandomCategory(Difficulty.E);
    while (playedWords.contains(randomWord)){
      randomWord = categorySelector.getRandomCategory(Difficulty.E);
    }

    currentUser.addWord(randomWord);
    lblCategory.setText(randomWord);
    currentWord = randomWord;

    // save coordinates when mouse is pressed on the canvas
    canvas.setOnMousePressed(
        e -> {
          currentX = e.getX();
          currentY = e.getY();
        });
    onDraw();

    // when a new game page is loaded, we want the following:
    canvas.setDisable(true); // user can't draw unless user presses the ready button
    btnToMenu.setDisable(
        true); // user can't go back and create a new game before finishing the current game
    btnSaveDrawing.setDisable(
        true); // user can't save an empty canvas, drawing can only be saved after game ends

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

    // Start the timer

    startTimer();

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
                  timeline.pause();
                  return true;
                }
              }
            }

            return false;
          }
        };

    // bind the title property to the guesses label
    lblGuesses.textProperty().bind(taskPredict.titleProperty());

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

          // update the winOrLose label and use the text to speech to tell the user if the they have
          // won or lost
          try {
            if (taskPredict.get()) { // returns true if user has won
              lblWinOrLose.setText("WIN");
              currentUser.addWin();

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
              currentUser.addLoss();

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
            currentUser.writeData(new File("src/main/resources/data/users", SceneManager.getMainUser().replace(" ", "_") + ".txt"));

          } catch (InterruptedException | ExecutionException | IOException e) {
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

    // This is the colour of the brush.
    graphic.setStroke(Color.BLACK);
    setStrokeProperties(6);
  }

  @FXML
  private void onErase() {

    graphic.setStroke(Color.WHITE);
    setStrokeProperties(8);
  }

  /**
   * This is a helper method for onDraw an onErase to set the stroke properties for the canvas.
   *
   * @param brushSize The size of the brush which depends if user is drawing or erasing
   */
  private void setStrokeProperties(double brushSize) {
    canvas.setOnMouseDragged(
        e -> {
          // Brush size (you can change this, it should not be too small or too large).
          final double size = brushSize;

          final double x = e.getX() - size / 2;
          final double y = e.getY() - size / 2;

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

    // create new loader to load save menu modal
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(getClass().getResource("/fxml/" + "savemenu" + ".fxml"));
    Parent root = loader.load();

    // configure the modal
    Stage stage = SceneManager.setModal(btnReady, root);
    stage.setTitle("Save Menu");

    // use instance methods of the controller to pass in the current snapshot
    SaveMenuController controller = loader.getController();
    controller.setImage(getCurrentSnapshot());
    controller.setStage(stage);

    // show the modal
    stage.showAndWait();
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
   * code adapted from
   * https://asgteach.com/2011/10/javafx-animation-and-binding-simple-countdown-timer-2/#:~:text=To%20start%20the%20timer%2C%20you,15%20and%20restarts%20the%20countdown.
   *
   * <p>Starts the timer time-line for the count-down timer
   */
  private void startTimer() {

    // creates new timeline for the countdown timer
    seconds.set(60);
    timeline = new Timeline();
    timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(60), new KeyValue(seconds, 0)));

    // binds the timer label to the timeline
    lblTime.textProperty().bind(seconds.asString());
    // time line plays once only
    timeline.setCycleCount(1);
    timeline.playFromStart();
  }
}
