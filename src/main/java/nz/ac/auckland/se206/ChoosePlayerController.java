package nz.ac.auckland.se206;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class ChoosePlayerController {
	
	@FXML private Button btnCreatePlayer; 
	@FXML private ListView<String> lstvPlayers; 
	
	private static String name; 
	
	@FXML 
	private void onCreate() throws IOException {
		FXMLLoader loader = new FXMLLoader(); 
		loader.setLocation(getClass().getResource("/fxml/"+"createplayer"+".fxml"));
		Parent root = loader.load(); 
		
		Stage stage = SceneManager.setModal(btnCreatePlayer,root);
	    
	    CreatePlayerController controller = loader.getController();
	    controller.setStage(stage);
	    stage.showAndWait();
	    
	    lstvPlayers.getItems().add(name);
		
	}
	
	@FXML
	private void onDelete() {
		lstvPlayers.getItems().remove(lstvPlayers.getSelectionModel().getSelectedIndex()); 
	}
	
	public static void setName(String playerName) {
		name = playerName; 
	}
	
}
