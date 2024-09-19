package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.Timer;

public class HomeController {
  public Timer timer;

  @FXML
  private void onStart() {
    try {

      App.actuallyStart();
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("Starting game");
  }
}
