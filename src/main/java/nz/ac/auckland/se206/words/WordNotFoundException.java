package nz.ac.auckland.se206.words;

public class WordNotFoundException extends Exception {

  private static final long serialVersionUID = 1L;
  private String word;
  private String subMessage;

  // constructor for the exception
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
