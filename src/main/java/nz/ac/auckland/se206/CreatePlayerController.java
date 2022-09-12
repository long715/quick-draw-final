package nz.ac.auckland.se206;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreatePlayerController {
	@FXML private Button btnOk; 
	@FXML private TextField txtPlayerName; 
	
	private Stage stage; 
	
	public void setStage(Stage stage) {
		this.stage = stage; 
	}
	
	@FXML 
	private void onClose() {
		this.stage.close();
	}
}
