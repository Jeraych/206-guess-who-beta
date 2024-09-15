package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import nz.ac.auckland.se206.App;

public class LeadScientistController extends RoomController {
  @FXML private ImageView leadscientist;
  @FXML private ImageView leadscientistturned;

  @FXML
  public void initialize() {
    hideTurned();
  }

  @FXML
  public void showTurned() {
    if (!App.isChatOpen()) {
      leadscientist.setVisible(false);
      leadscientistturned.setVisible(true);
    }
  }

  @FXML
  public void hideTurned() {
    if (!App.isChatOpen()) {
      leadscientist.setVisible(true);
      leadscientistturned.setVisible(false);
    }
  }

  @FXML
  @Override
  protected void handleRectangleClick(MouseEvent event) throws IOException {
    super.handleRectangleClick(event);
  }
}
