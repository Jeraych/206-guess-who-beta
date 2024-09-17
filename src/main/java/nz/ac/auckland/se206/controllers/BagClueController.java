package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.se206.App;

public class BagClueController {

  @FXML private ImageView bagzipper;
  @FXML private Rectangle rectZipper;
  @FXML private Rectangle clickInto;

  private double yOffset = 0;

  @FXML
  private void initialize() {

    // clicking into not available until dragged
    clickInto.setVisible(false);

    // pair zipper with small rectangle which handles drag

    rectZipper.setOnMousePressed(
        event -> {
          yOffset = event.getSceneY() - bagzipper.getLayoutY();
        });

    // update position on drag
    rectZipper.setOnMouseDragged(
        event -> {
          double newY = event.getSceneY() - yOffset;

          // manually determined positions
          double upperBoundary = 0;
          double lowerBoundary = 170;

          // constrain motion to the boundaries
          if (newY < upperBoundary) {
            newY = upperBoundary;
          } else if (newY > lowerBoundary) {
            newY = lowerBoundary;
          }

          // set new positions, with rectangle lower than the start of the zipper.
          bagzipper.setLayoutY(newY);
          rectZipper.setLayoutY(newY + 230);

          clickInto.setVisible(true);
        });
  }

  @FXML
  private void onOpenBag() throws IOException {
    // open up pieces of paper of clues
  }

  @FXML
  private void onExitClue() throws IOException {
    App.setRoot("room");
    System.out.println("exit locker clue");
  }
}
