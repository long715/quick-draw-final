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
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import nz.ac.auckland.se206.ml.DoodlePrediction;
import nz.ac.auckland.se206.speech.TextToSpeech;
import nz.ac.auckland.se206.words.CategorySelector;
import nz.ac.auckland.se206.words.DefinitionFetcher;
import nz.ac.auckland.se206.words.WordNotFoundException;
import org.apache.commons.lang3.StringUtils;

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
  @FXML private TextFlow txtFlowPrediction;
  @FXML private Button btnToMenu;
  @FXML private Button btnReady;
  @FXML private Button clearButton;
  @FXML private Button btnSaveDrawing;
  @FXML private Label lblReward;
  @FXML private ImageView imgBadge;
  @FXML private Button btnHint;

  private GraphicsContext graphic;
  private DoodlePrediction model;
  private String currentWord;
  private TextToSpeech speech;

  private Timeline timeline = new Timeline();
  private UserProfile currentUser = SceneManager.getProfile(SceneManager.getMainUser());
  private int timeSettings = currentUser.getTimeSettings();
  private volatile IntegerProperty seconds = new SimpleIntegerProperty(timeSettings);

  private int timePlayed;
  private boolean isPredictionStarted = false;
  private boolean isZen = currentUser.isZenMode();
  private boolean isHidden = currentUser.isHiddenMode();
  private String labelText;
  private String randomWord;
  private String textToSpeechString;
  private int hintCounter = 0;

  // mouse coordinates
  private double currentX;
  private double currentY;

  /**
   * JavaFX calls this method once the GUI elements are loaded. In our case we create a listener for
   * the drawing, and we load the ML model.
   *
   * @throws ModelException If there is an error in reading the input/output of the DL model.
   * @throws IOException If the model cannot be found on the file system.
   * @throws CsvException If there is an issue with the opencsv loading
   * @throws URISyntaxException If string cannot be parsed as URI reference
   */
  public void initialize() throws ModelException, IOException, URISyntaxException, CsvException {
    graphic = canvas.getGraphicsContext2D();
    chooseWord();

    // save coordinates when mouse is pressed on the canvas
    canvas.setOnMousePressed(
        e -> {
          currentX = e.getX();
          currentY = e.getY();
        });
    onDrawBlue();

    // when a new game page is loaded, we want the following:
    canvas.setDisable(true); // user can't draw unless user presses the ready button

    // before ready, we dont want hint button to function
    btnHint.setVisible(false);
    btnHint.setDisable(true);

    lblWinOrLose.setTextFill(Color.BLUE);

    if (!isZen) {
      // user can't go back and create a new game before finishing the current game
      btnToMenu.setDisable(true);
      // set the initial time for the timer
      lblTime.setText(String.valueOf(timeSettings));
    }
    // user can't save an empty canvas, drawing can only be saved after game ends
    btnSaveDrawing.setDisable(true);

    model = new DoodlePrediction();
    speech = new TextToSpeech();

    // create a task and bg thread for the text to speech so that loading doesnt lag
    Task<Void> taskWelcomeSpeech =
        new Task<Void>() {
          protected Void call() {
            if (isZen) {
              // custom speech for zen mode since it is a non-competitive mode
              speech.speak("Welcome to Zen Mode! Your word is", currentWord);

            } else {
              // tell the player instructions on how to start the game,
              // the time settings and the word/definitions depending on the game mode
              speech.speak(
                  "You got "
                      + timeSettings
                      + " seconds to draw "
                      + textToSpeechString
                      + ", press the ready button whenever you are ready!");
            }

            return null;
          }
        };

    Thread bgWelcomeSpeech = new Thread(taskWelcomeSpeech);
    bgWelcomeSpeech.start();
  }

  /**
   * This method chooses the word for this canvas game instance. This will be based on game
   * settings, if in ZEN mode, the choices is always from ALL categories.
   *
   * @throws CsvException If there is an issue with the opencsv loading
   * @throws IOException If the model cannot be found on the file system.
   * @throws URISyntaxException If string cannot be parsed as URI reference
   */
  private void chooseWord() throws URISyntaxException, IOException, CsvException {
    // implement the category selector and display the category on the lbl
    CategorySelector categorySelector = new CategorySelector();
    int wordsSettings = currentUser.getWordsSettings();

    // get words current user has played and all words from words settings
    ArrayList<String> playedWords = currentUser.getWords();
    List<String> allWords = categorySelector.getDifficultyList(wordsSettings);

    if (isZen) {
      playedWords = currentUser.getZenWords();
      wordsSettings = 1;
      allWords = categorySelector.getDifficultyList(wordsSettings); // ALL words from all categories
    }

    // check if the player has played all the words
    if (playedWords.containsAll(allWords)) {
      if (isZen) {
        currentUser.newZenRound();
      } else {
        currentUser.newRound();
      }
    }

    randomWord = getNewWord(allWords, playedWords, categorySelector);

    if (isZen) {
      currentUser.addZenWords(randomWord);
      lblCategory.setText(randomWord);
    } else if (isHidden) {
      btnHint.setDisable(false);
      while (true) {
        try {
          textToSpeechString = DefinitionFetcher.getDefinition(randomWord);
          labelText = StringUtils.repeat("_", randomWord.length());
          break;
        } catch (WordNotFoundException e) {
          randomWord = getNewWord(allWords, playedWords, categorySelector);
        }
      }
      lblCategory.setText(StringUtils.repeat("_", randomWord.length()));

    } else {
      currentUser.addWord(randomWord);
      lblCategory.setText(randomWord);
      textToSpeechString = randomWord;
    }
    currentWord = randomWord;
  }

  /**
   * This method finds a random word from categories depending on the user game settings, that has
   * not been played by the user before.
   *
   * @param allWords The list of words from a set of difficulty categories
   * @param playedWords The list of words the user has played before
   * @param categorySelector The instance of the class that fetches the words from the csv
   * @return a string of the random word that the user has not played before
   */
  private String getNewWord(
      List<String> allWords, List<String> playedWords, CategorySelector categorySelector) {
    String randomWord = categorySelector.getRandomCategory(currentUser.getWordsSettings());
    // generate word that user has not played yet in current round
    while (playedWords.contains(randomWord)) {
      randomWord = categorySelector.getRandomCategory(currentUser.getWordsSettings());
    }

    return randomWord;
  }

  /**
   * This method is executed when the cross button is clicked in the Canvas page which switches the
   * root to the menu instance of the user.
   */
  @FXML
  private void onSwitchToMenu() {
    Scene sceneBtnIsIn = btnToMenu.getScene();
    sceneBtnIsIn.setRoot(SceneManager.getUi(SceneManager.AppUi.MENU));
  }

  /**
   * This method is executed when the ready button is clicked, this method starts the timer, enables
   * the convas and its components and the prediction threads.
   *
   * @throws InterruptedException If a running thread is interrupted
   * @throws ExecutionException If retrieving result from a task has failed
   * @throws IOException If errors occur when accessing the file
   */
  @FXML
  private void onStartGame() throws InterruptedException, ExecutionException, IOException {
    // enable the canvas and disable to ready btn
    canvas.setDisable(false);
    btnReady.setDisable(true);
    btnReady.setVisible(false);

    if (isZen) {
      // user should be able to save drawing anytime
      btnSaveDrawing.setDisable(false);
      // since zen mode game doesn't end, zen word data must be written on start
      currentUser.writeData(
          new File(
              "src/main/resources/data/users",
              SceneManager.getMainUser().replace(" ", "_") + ".txt"));
    } else if (isHidden) {
      // when ready is clicked, we should see and use hint button
      btnHint.setVisible(true);
      btnHint.setDisable(false);
      startTimer();
    } else {
      startTimer();
    }
  }

  /**
   * This method is called when the trash can icon button is pressed. This clears the whole canvas/
   * removes all existing drawings.
   */
  @FXML
  private void onClear() {
    graphic.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
  }

  /**
   * This method is called when the Dark Blue paint button is pressed. This switches the brush color
   * to dark blue.
   */
  @FXML
  private void onDrawBlue() {

    // This is the colour of the brush.
    graphic.setStroke(Color.DODGERBLUE);
    setStrokeProperties(12);
  }

  /**
   * This method is executed when the cyan paint button is clicked. This switches the brush color to
   * dark blue.
   */
  @FXML
  private void onDrawCyan() {

    // This is the colour of the brush.
    graphic.setStroke(Color.CYAN);
    setStrokeProperties(12);
  }

  /**
   * This methid is executed when the purple paint button is clicked. This switches the brush color
   * to purple.
   */
  @FXML
  private void onDrawPurple() {

    // This is the colour of the brush.
    graphic.setStroke(Color.DARKORCHID);
    setStrokeProperties(12);
  }

  /**
   * This method is executed when the Magenta paint button is clicked. This switches the brush color
   * to magenta.
   */
  @FXML
  private void onDrawMagenta() {
    // This is the colour of the brush.
    graphic.setStroke(Color.DEEPPINK);
    setStrokeProperties(12);
  }

  /**
   * This method is executed when the Erase icon is clicked. This switches the brush color to black
   * and since the canvas color is black, this acts as an eraser for the canvas.
   */
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

  /**
   * This method is executed when the save button is clicked. This opens up a secondary stage/pop up
   * which shows the save menu.
   *
   * @throws IOException If errors occur when loading the fxml file.
   */
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
   * This method checks if the current word is in the top x classifications. Edited: Changes on
   * param because of the updated winOrLose task.
   *
   * @param classifications The list of the DL predictions
   * @return if current word is included in the top x classifications
   */
  private boolean isWin(List<Classification> classifications) {
    for (int i = 0; i < classifications.size(); i++) {
      // format the category name from ML the same way as current word
      if (classifications.get(i).getClassName().replace("_", " ").equals(currentWord)) {
        // extra condition: user must meet confidence requirements for user to win
        return model.isAboveProbability(classifications.get(i), currentUser.getConfidence());
      }
    }

    return false;
  }

  /**
   * Get the current snapshot of the canvas. Used in the ML predictions and in the save menu.
   * Edited: Changes on the BufferedImage settings, instead of Binary, i've made it to RGB so that
   * it registers the different brush colors.
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

  /**
   * This method is executed when onDrag is detected in the canvas, this is to prevent predictions
   * on an empty canvas before the user starts drawing.
   */
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
              long tempTime = System.currentTimeMillis();

              // run loop while timer is active
              while (timeline.getStatus() != Status.STOPPED) {

                // when a second has passed, run the DL predictor
                if (temp - seconds.intValue() >= 1) {
                  // create a future task for checking wins
                  FutureTask<Boolean> winOrLose =
                      new FutureTask<Boolean>(
                          new Callable<Boolean>() {
                            public Boolean call() throws TranslateException {

                              return isWin(
                                  model.getPredictions(
                                      getCurrentSnapshot(), currentUser.getAccuracy()));
                            }
                          });
                  FutureTask<Void> outsidePrediction =
                      new FutureTask<Void>(
                          new Callable<Void>() {
                            public Void call() throws TranslateException {

                              // get the top 40 predictions and turn this into a string
                              List<Classification> classifications =
                                  model.getPredictions(getCurrentSnapshot(), 40);

                              List<String> predictionString =
                                  DoodlePrediction.getPredictionString(classifications, 40);

                              // check if the string/top 40 has the word, if not tell the user
                              // that they are not in the top 40
                              if (predictionString.get(0).contains(randomWord)) {
                                for (int i = 0; i <= 40; i++) {

                                  // find the random word in the top 40, index in the list
                                  // represented by i
                                  if (classifications
                                      .get(i)
                                      .getClassName()
                                      .replace("_", " ")
                                      .equals(randomWord)) {

                                    // categorise which TOP X the random word is in and tell
                                    // the user that they are in TOP X
                                    if (i <= 10) {
                                      lblWinOrLose.setText("TOP 10");
                                    } else if (i <= 20) {
                                      lblWinOrLose.setText("TOP 20");
                                    } else if (i <= 30) {
                                      lblWinOrLose.setText("TOP 30");
                                    } else if (i <= 40) {
                                      lblWinOrLose.setText("TOP 40");
                                    }
                                  }
                                }
                              } else {
                                lblWinOrLose.setText("NOT EVEN CLOSE");
                              }

                              return null;
                            }
                          });

                  // get the top 10 list and check if the current word is within the top 3 (EASY)
                  Platform.runLater(winOrLose);
                  Platform.runLater(outsidePrediction);
                  getTop10Predictions();

                  // set the temp time
                  temp = seconds.intValue();

                  // check if the user won
                  if (winOrLose.get()) {
                    timeline.pause();
                    return true;
                  }
                }
              }

              while (isZen) {
                if ((int) (System.currentTimeMillis() - tempTime) / 1000 >= 1) {
                  getTop10Predictions();
                  tempTime = System.currentTimeMillis();
                }
              }

              return false;
            }
          };
      // create the bg thread for the dl task
      Thread bgPredict = new Thread(taskPredict);
      bgPredict.start();

      taskPredict.setOnSucceeded(
          event -> {
            setCanvas();

            // update the winOrLose label and use the text to speech to tell the user if the
            // they have won or lost
            try {
              if (taskPredict.get()) { // returns true if user has won
                setCanvasWon();
              } else {
                setCanvasLost();
              }
              SceneManager.replaceUi(SceneManager.AppUi.STATISTICS, App.loadFxml("statistics"));
              SceneManager.replaceUi(SceneManager.AppUi.LEADERBOARD, App.loadFxml("leaderboard"));
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

  /**
   * This method is used to reward the player with a badge
   *
   * @param badgeImagePath The string representing the badge image's path.
   */
  private void awardBadge(String badgeImagePath) {
    if (!(currentUser.getBadgesEarned().contains(badgeImagePath))) {
      currentUser.addBadge(badgeImagePath);
      imgBadge.setImage(new Image(badgeImagePath));
      lblReward.setText("New badge earned !");
    }
  }

  /** This method sets up the canvas page after a game is finished. */
  private void setCanvas() {
    // user should not be able to draw on the canvas or reset the drawing
    canvas.setDisable(true);
    clearButton.setDisable(true);

    btnToMenu.setDisable(false);

    // allow user to save the current drawing and write on the text fields for
    // custom directory and file name inputs
    btnSaveDrawing.setDisable(false);

    // player shouldn't be able to get hints after the game is over
    btnHint.setDisable(true);
    model.closeManager();
  }

  /**
   * This method sets up the winning state of Canvas and runs a text-to-speech to inform the player
   * that they have won.
   */
  private void setCanvasWon() {
    lblWinOrLose.setTextFill(Color.GREEN);
    lblWinOrLose.setText("WIN");
    currentUser.addWin();
    timePlayed = timeSettings - Integer.parseInt(lblTime.getText());

    // awarding the badges to players who win under certain time constraints
    if (timePlayed < 10) {
      awardBadge("/images/Under_10s_win.png");
    } else if (timePlayed < 20) {
      awardBadge("/images/Under_20s_win.png");
    } else if (timePlayed < 30) {
      awardBadge("/images/Under_30s_win.png");
    }

    // since the default best time is -1, og condition will not work
    // therefore I added an alternative condition to check if best time
    // is the default value, in which it should be updated
    if (timePlayed < currentUser.getBestTime() || currentUser.getBestTime() == -1) {
      currentUser.setBestWord(currentWord);
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
  }

  /**
   * This method sets up the losing state of Canvas and tells the user that they lost via
   * text-to-speech
   */
  private void setCanvasLost() {
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

  /**
   * This method runs the top 10 prediction future task and sets the prediction string
   *
   * @throws ExecutionException If retrieving result from a task has failed
   * @throws InterruptedException If a running thread was interrupted
   */
  private void getTop10Predictions() throws InterruptedException, ExecutionException {
    // create a future task for getting the prediction string
    // required for accessing the variable from outside platform run later
    FutureTask<List<String>> predict =
        new FutureTask<List<String>>(
            new Callable<List<String>>() {
              public List<String> call() throws TranslateException {

                // get the list of the top 10 classifications and format the list into
                // stringbuilder
                return DoodlePrediction.getPredictionString(
                    model.getPredictions(getCurrentSnapshot(), 10), currentUser.getAccuracy());
              }
            });

    Platform.runLater(predict);
    Platform.runLater(
        () -> {
          try {
            txtFlowPrediction.getChildren().clear();
            Text topX = new Text(predict.get().get(0));
            if (topX.getText().contains(randomWord)) {
              if (isWin(model.getPredictions(getCurrentSnapshot(), currentUser.getAccuracy()))) {
                topX.setFill(Color.GREEN);
              } else {
                topX.setFill(Color.YELLOW);
              }
            } else {
              topX.setFill(Color.RED);
            }
            Text secondString = new Text(predict.get().get(1));
            secondString.setFill(Color.WHITE);
            txtFlowPrediction.getChildren().addAll(topX, secondString);

          } catch (InterruptedException | ExecutionException | TranslateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        });
  }

  /**
   * This method is executed when the Hint button is clicked (available for hidden word mode only)
   * which updates the label showing the incomplete/hidden characters of the random word chosen.
   */
  @FXML
  private void onHint() {
    if (hintCounter < randomWord.length()) {
      // set the string builder to the recent state of the label that shows
      // the characters of the words
      StringBuilder sb = new StringBuilder(labelText);
      // open up the next character that's not been shown to the user in the
      // previous state
      sb.setCharAt(hintCounter, randomWord.charAt(hintCounter));
      hintCounter++;
      // update the label to show the new state
      labelText = sb.toString();
      lblCategory.setText(labelText);
    }
  }
}
