package nz.ac.auckland.se206;

import ai.djl.ModelException;
import ai.djl.modality.Classifications.Classification;
import ai.djl.translate.TranslateException;
import com.opencsv.exceptions.CsvException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.animation.Animation.Status;
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
import javafx.scene.SnapshotParameters;
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
import nz.ac.auckland.se206.words.DefinitionFetcher;
import nz.ac.auckland.se206.words.WordNotFoundException;

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
  @FXML private Button btnHint;
  @FXML private Button clearButton;
  @FXML private Button btnSaveDrawing;

  private GraphicsContext graphic;
  private DoodlePrediction model;
  private String labelText;
  private String randomWord;
  private TextToSpeech speech;

  private Timeline timeline = new Timeline();
  private UserProfile currentUser = SceneManager.getProfile(SceneManager.getMainUser());
  private int timeSettings = currentUser.getTimeSettings();
  private volatile IntegerProperty seconds = new SimpleIntegerProperty(timeSettings);

  private int timePlayed;
  private boolean isPredictionStarted = false;

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

    // get words current user has played and all words from easy category
    ArrayList<String> playedWords = currentUser.getWords();
    List<String> allWords = categorySelector.getDifficultyList(currentUser.getWordsSettings());

    btnHint.setDisable(true);

    // check if the player has played all the words
    if (playedWords.containsAll(allWords)) {
      currentUser.newRound();
    }

    randomWord = getNewWord(allWords, playedWords, categorySelector);

    if (currentUser.getHiddenMode()) {
      while (true) {
        try {
          labelText = DefinitionFetcher.getDefinition(randomWord);
          break;
        } catch (WordNotFoundException e) {
          randomWord = getNewWord(allWords, playedWords, categorySelector);
        }
      }
      lblCategory.setText(labelText);

    } else {
      lblCategory.setText(randomWord);
      labelText = randomWord;
    }

    System.out.println(randomWord);
    currentUser.addWord(randomWord);

    // set the initial time for the timer
    lblTime.setText(String.valueOf(timeSettings));

    // save coordinates when mouse is pressed on the canvas
    canvas.setOnMousePressed(
        e -> {
          currentX = e.getX();
          currentY = e.getY();
        });
    onDrawBlue();

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
                "You got "
                    + timeSettings
                    + " seconds to draw "
                    + labelText
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
  private void onHint() {
    Task<Void> hintSpeech =
        new Task<Void>() {
          protected Void call() {
            // tell the player the word and instructions on how to start the game
            speech.speak("The word is " + randomWord.length() + " letters long");

            return null;
          }
        };

    Thread bgHintSpeech = new Thread(hintSpeech);
    bgHintSpeech.start();
  }

  @FXML
  private void onStartGame() throws InterruptedException, ExecutionException {
    // enable the canvas and disable to ready btn
    canvas.setDisable(false);
    btnReady.setDisable(true);
    if (currentUser.getHiddenMode()) {
      btnHint.setDisable(false);
    } else {
      btnHint.setDisable(true);
    }

    // Start the timer

    startTimer();
  }

  /** This method is called when the "Clear" button is pressed. */
  @FXML
  private void onClear() {
    graphic.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
  }

  @FXML
  private void onDrawBlue() {

    // This is the colour of the brush.
    graphic.setStroke(Color.DODGERBLUE);
    setStrokeProperties(12);
  }

  @FXML
  private void onDrawCyan() {

    // This is the colour of the brush.
    graphic.setStroke(Color.CYAN);
    setStrokeProperties(12);
  }

  @FXML
  private void onDrawPurple() {

    // This is the colour of the brush.
    graphic.setStroke(Color.DARKORCHID);
    setStrokeProperties(12);
  }

  @FXML
  private void onDrawMagenta() {
    // This is the colour of the brush.
    graphic.setStroke(Color.DEEPPINK);
    setStrokeProperties(12);
  }

  @FXML
  private void onErase() {
    graphic.setStroke(Color.BLACK);
    setStrokeProperties(14);
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
      // format the category name from ML the same way as current word
      if (classifications.get(i).getClassName().replace("_", " ").equals(randomWord)) {
        // extra condition: user must meet confidence requirements for user to win
        return model.isAboveProbability(classifications.get(i), currentUser.getConfidence());
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
    // this changes the snapshot backgorund to black
    SnapshotParameters params = new SnapshotParameters();
    params.setFill(Color.BLACK);

    final Image snapshot = canvas.snapshot(params, null);
    final BufferedImage image = SwingFXUtils.fromFXImage(snapshot, null);

    // Convert into a binary image.
    final BufferedImage imageBinary =
        new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

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
    seconds.set(timeSettings);
    timeline = new Timeline();
    timeline
        .getKeyFrames()
        .add(new KeyFrame(Duration.seconds(timeSettings), new KeyValue(seconds, 0)));

    // binds the timer label to the timeline
    lblTime.textProperty().bind(seconds.asString());
    // time line plays once only
    timeline.setCycleCount(1);
    timeline.playFromStart();
  }

  @FXML
  private void startPrediction() {

    if (!isPredictionStarted) {
      isPredictionStarted = true;
      // create the task for the DL predictions
      Task<Boolean> taskPredict =
          new Task<Boolean>() {
            protected Boolean call()
                throws TranslateException, InterruptedException, ExecutionException {
              // get the current time
              int temp = seconds.intValue();

              // run loop while timer is active
              while (timeline.getStatus() != Status.STOPPED) {

                // when a second has passed, run the DL predictor
                if (temp - seconds.intValue() >= 1) {

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
                  // create a future task for checking wins
                  FutureTask<Boolean> winOrLose =
                      new FutureTask<Boolean>(
                          new Callable<Boolean>() {
                            public Boolean call() throws TranslateException {
                              return isWin(
                                  model.getPredictions(getCurrentSnapshot(), 10),
                                  currentUser.getAccuracy());
                            }
                          });

                  // get the top 10 list and check if the current word is within the top 3 (EASY)
                  Platform.runLater(predict);
                  Platform.runLater(winOrLose);
                  updateTitle(predict.get().toString().replace("_", " ")); // remove the underscores

                  // set the temp time
                  temp = seconds.intValue();

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
      // create the bg thread for the dl task
      Thread bgPredict = new Thread(taskPredict);
      bgPredict.start();

      // bind the title property to the guesses label
      lblGuesses.textProperty().bind(taskPredict.titleProperty());

      taskPredict.setOnSucceeded(
          event -> {
            // once the game has ended (timer runs out or if they won), we want the
            // following UX:
            canvas.setDisable(true); // user should not be able to draw on the canvas
            btnToMenu.setDisable(
                false); // user can go back to the main menu to load the previous game or create a
            // new
            // game
            clearButton.setDisable(true); // user can't alter or reset the drawing in any way

            // allow user to save the current drawing and write on the text fields for
            // custom directory and file name inputs
            btnSaveDrawing.setDisable(false);

            btnHint.setDisable(true);

            // close the ML Manager
            model.closeManager();

            // update the winOrLose label and use the text to speech to tell the user if the
            // they
            // have
            // won or lost
            try {
              if (taskPredict.get()) { // returns true if user has won
                lblWinOrLose.setTextFill(Color.GREEN);
                lblWinOrLose.setText("WIN");
                currentUser.addWin();
                timePlayed = timeSettings - Integer.parseInt(lblTime.getText());

                // since the default best time is -1, og condition will not work
                // therefore I added an alternative condition to check if best time
                // is the default value, in which it should be updated
                if (timePlayed < currentUser.getBestTime() || currentUser.getBestTime() == -1) {
                  currentUser.setBestWord(randomWord);
                  currentUser.setBestTime(timePlayed);
                }

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
                lblWinOrLose.setTextFill(Color.RED);
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
              SceneManager.replaceUi(SceneManager.AppUi.STATISTICS, App.loadFxml("statistics"));
              currentUser.writeData(
                  new File(
                      "src/main/resources/data/users",
                      SceneManager.getMainUser().replace(" ", "_") + ".txt"));

            } catch (InterruptedException | ExecutionException | IOException e) {
              e.printStackTrace();
            }
          });
    }
  }

  private String getNewWord(
      List<String> allWords, List<String> playedWords, CategorySelector categorySelector) {
    String randomWord = categorySelector.getRandomCategory(currentUser.getWordsSettings());
    // generate word that user has not played yet in current round
    while (playedWords.contains(randomWord)) {
      randomWord = categorySelector.getRandomCategory(currentUser.getWordsSettings());
    }

    return randomWord;
  }
}
