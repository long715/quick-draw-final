package nz.ac.auckland.se206.words;

public class WordNotFoundException extends Exception {

  private static final long serialVersionUID = 1L;
  private String word;
  private String subMessage;

  /**
   * The constructor for the Custom exception which initialises the Exception message.
   *
   * @param word The word which matches no definitions
   * @param message The main message for the exception
   * @param subMessage The sub message for the exception
   */
  WordNotFoundException(String word, String message, String subMessage) {
    super(message);
    this.word = word;
    this.subMessage = subMessage;
  }

  // getter functions
  public String getWord() {
    return word;
  }

  public String getSubMessage() {
    return subMessage;
  }
}
