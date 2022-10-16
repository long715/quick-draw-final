package nz.ac.auckland.se206.words;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class DefinitionFetcher {
  private static final String API_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/";

  /**
   * Gets the definition of the query string that is passed in from the dictionary api.
   *
   * @param query String whose definition we need to fetch
   * @return the definition of the query string
   * @throws IOException If error occurs when accessing the api url
   * @throws WordNotFoundException If definition for the word could not be found in the database
   */
  public static String getDefinition(String query) throws IOException, WordNotFoundException {

    OkHttpClient client = new OkHttpClient();
    Request request =
        new Request.Builder().url(API_URL + query).build(); // build the request to send to the api
    Response response =
        client.newCall(request).execute(); // send the request and receive the response

    ResponseBody responseBody = response.body();

    String jsonString = responseBody.string();

    // check if the query string found a definition
    try {
      JSONObject jsonObj = (JSONObject) new JSONTokener(jsonString).nextValue();
      String title = jsonObj.getString("title");
      String subMessage = jsonObj.getString("message");
      throw new WordNotFoundException(query, title, subMessage);
    } catch (ClassCastException e) {
      System.out.println(e);
    }

    JSONArray jsonArray = (JSONArray) new JSONTokener(jsonString).nextValue();

    // get the first entry
    JSONObject jsonEntryObj = jsonArray.getJSONObject(0);

    // get the array of meanings
    JSONArray jsonMeanings = jsonEntryObj.getJSONArray("meanings");

    // get the first meaning
    JSONObject jsonMeaningObj = jsonMeanings.getJSONObject(0);

    // get the array of definitions
    JSONArray jsonDefinitions = jsonMeaningObj.getJSONArray("definitions");

    // get the first definition
    JSONObject jsonDefinitionObj = jsonDefinitions.getJSONObject(0);

    String definition = jsonDefinitionObj.getString("definition");
    return definition;
  }
}
