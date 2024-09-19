package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import nz.ac.auckland.se206.App;

public abstract class ClueController {

  @FXML private Label timerLabel;

  @FXML
  void initialize() {
    App.getTimer().setLabel(timerLabel);
  }
}
