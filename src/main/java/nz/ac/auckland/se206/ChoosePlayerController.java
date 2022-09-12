package nz.ac.auckland.se206;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class ChoosePlayerController {
	
	@FXML private Button btnCreatePlayer; 
	
	@FXML 
	private void onCreate() throws IOException {
		FXMLLoader loader = new FXMLLoader(); 
		loader.setLocation(getClass().getResource("/fxml/"+"createplayer"+".fxml"));
		Parent root = loader.load(); 
		
		Stage stage = SceneManager.setModal(btnCreatePlayer,root);
	    
	    CreatePlayerController controller = loader.getController();
	    controller.setStage(stage);
	    stage.showAndWait();
		
	}
	
}
