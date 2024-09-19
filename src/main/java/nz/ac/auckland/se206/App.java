package nz.ac.auckland.se206;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import nz.ac.auckland.se206.controllers.ChatController;
import nz.ac.auckland.se206.controllers.RoomController;
import nz.ac.auckland.se206.speech.FreeTextToSpeech;

/**
 * This is the entry point of the JavaFX application. This class initializes and runs the JavaFX
 * application.
 */
public class App extends Application {
  private static Timer timer = new Timer(null, 300);
  private static Scene scene;
  private static Stage stageWindow;
  private static Parent chatView = null;
  private static ChatController chatController = null;
  private static RoomController roomController = null;
  private static MediaPlayer mediaPlayer;
  private static String[] charactersToShow =
      new String[] {
        "rectLabTechnician",
        "rectScholar",
        "rectLeadScientist",
        "leadscientist",
        "labtechnician",
        "scholar",
      };

  private static String[] charactersToHide =
      new String[] {
        "rectLabTechnician",
        "rectScholar",
        "rectLeadScientist",
        "leadscientist",
        "labtechnician",
        "scholar",
        "leadscientistturned",
        "labtechnicianturned",
        "scholarturned"
      };
  private static boolean isChatOpen = false;

  /**
   * The main method that launches the JavaFX application.
   *
   * @param args the command line arguments
   */
  public static void main(final String[] args) {
    replaceFileContent(
        "src/main/resources/prompts/emptyfile.txt",
        "src/main/resources/prompts/lab_technician_2.txt");
    replaceFileContent(
        "src/main/resources/prompts/emptyfile.txt",
        "src/main/resources/prompts/lead_scientist_2.txt");
    replaceFileContent(
        "src/main/resources/prompts/emptyfile.txt", "src/main/resources/prompts/scholar_2.txt");
    launch();
  }

  public static void replaceFileContent(String sourceFilePath, String destinationFilePath) {
    Path sourcePath = Paths.get(sourceFilePath);
    Path destinationPath = Paths.get(destinationFilePath);

    try {
      // Read content from the source file
      byte[] content = Files.readAllBytes(sourcePath);

      // Write content to the destination file, overwriting it
      Files.write(destinationPath, content);
    } catch (IOException e) {
      System.err.println("An error occurred while replacing file content: " + e.getMessage());
    }
  }

  public static void appendToFile(String content, String filePath) {
    Path path = Paths.get(filePath);

    try {
      // Append content to the file
      Files.write(path, content.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);

    } catch (IOException e) {
      System.err.println("An error occurred while appending to the file: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public static Timer getTimer() {
    return timer;
  }

  public void setLabel(Label timerLabel) {
    this.timer.setLabel(timerLabel);
  }

  /**
   * Sets the root of the scene to the specified FXML file.
   *
   * @param fxml the name of the FXML file (without extension)
   * @throws IOException if the FXML file is not found
   */
  public static void setRoot(String fxml) throws IOException {
    scene.setRoot(loadFxml(fxml));
  }

  /**
   * Loads the FXML file and returns the associated node. The method expects that the file is
   * located in "src/main/resources/fxml".
   *
   * @param fxml the name of the FXML file (without extension)
   * @return the root node of the FXML file
   * @throws IOException if the FXML file is not found
   */
  private static Parent loadFxml(final String fxml) throws IOException {
    return new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml")).load();
  }

  /**
   * Opens the chat view and sets the profession in the chat controller.
   *
   * @param event the mouse event that triggered the method
   * @param profession the profession to set in the chat controller
   * @throws IOException if the FXML file is not found
   */
  public static void showChatbox(MouseEvent event, String profession) throws IOException {

    // cache chat node and controller
    System.out.println("Showing chat box");
    if (chatView == null) {
      FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/chat.fxml"));
      chatView = loader.load();
      chatController = loader.getController();
    }

    chatController.setProfession(profession);

    Parent root = scene.getRoot();
    if (root instanceof AnchorPane) {
      AnchorPane mainPane = (AnchorPane) root;

      // chatPane is the space in each fxml file where the chat view will be displayed
      AnchorPane chatPane = (AnchorPane) mainPane.lookup("#chatPane");
      if (chatPane != null) {
        System.out.println("Chat pane found");
        chatPane.getChildren().clear();
        chatPane.getChildren().add(chatView);
        chatPane.setVisible(true);
      }

      isChatOpen = true;

      // find characters in scene by id and hide them

      for (String characterId : charactersToHide) {
        Node character = mainPane.lookup("#" + characterId);
        hideCharacter(character);
      }
    }
  }

  private static void hideCharacter(Node character) {
    if (character != null) {
      character.setVisible(false);
      System.out.println("Hiding " + character.getId());
    }
  }

  private static void showCharacter(Node character) {
    if (character != null) {
      character.setVisible(true);
      System.out.println("Showing " + character.getId());
    }
  }

  public static void hideChat() throws IOException {
    Parent root = scene.getRoot();
    if (root instanceof AnchorPane) {
      AnchorPane mainPane = (AnchorPane) root;
      AnchorPane chatPane = (AnchorPane) mainPane.lookup("#chatPane");
      if (chatPane != null) {
        chatPane.getChildren().clear();
        chatPane.setVisible(false);
      }

      isChatOpen = false;

      // find characters by id and show them
      for (String characterId : charactersToShow) {
        Node character = mainPane.lookup("#" + characterId);
        showCharacter(character);
      }
    }
  }

  public static void switchRoom(MouseEvent event, String roomButtonId) {

    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

    // checks which room button was clicked and switches to the corresponding scene
    // TO-DO: Do the loading of fxml files in platform run later? Doesn't make a visual difference
    // but at least buttons won't freeze.
    try {
      switch (roomButtonId) {
        case "clueSceneBtn":
          System.out.println("Switching to clue scene");
          setRoot("room");
          break;
        case "leadScientistSceneButton":
          System.out.println("Switching to lead scientist scene");
          setRoot("leadscientist");
          break;
        case "labTechnicianSceneButton":
          System.out.println("Switching to lab technician scene");
          setRoot("labtechnician");
          break;
        case "scholarSceneButton":
          System.out.println("Switching to scholar scene");
          setRoot("scholar");
          break;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    stage.setScene(scene);
    stage.show();
  }

  public static boolean isChatOpen() {
    return isChatOpen;
  }

  public static boolean gameResult(String suspectName) {
    if (suspectName.equals("Scholar")) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * This method is invoked when the application starts. It loads and shows the "room" scene.
   *
   * @param stage the primary stage of the application
   * @throws IOException if the "src/main/resources/fxml/room.fxml" file is not found
   */
  @Override
  public void start(final Stage stage) throws IOException {
    Parent root = loadFxml("home");
    scene = new Scene(root);
    stage.setScene(scene);
    stage.show();
    stage.setOnCloseRequest(event -> handleWindowClose(event));
    root.requestFocus();
  }

  public static void actuallyStart() throws IOException {
    Parent clueRoot = loadFxml("room");

    StackPane stackPane = new StackPane(scene.getRoot(), clueRoot);

    clueRoot.setOpacity(0);

    // Fade homescreen out
    FadeTransition fadeHome = new FadeTransition(Duration.millis(300), scene.getRoot());
    scene.setRoot(stackPane);
    stackPane.setStyle("-fx-background-color: #c5c7e1;");
    fadeHome.setFromValue(1);
    fadeHome.setToValue(0);

    fadeHome.setOnFinished(
        event -> {
          stackPane.getChildren().remove(0);

          // Fade clue room in
          FadeTransition fadeClue = new FadeTransition(Duration.millis(300), clueRoot);
          fadeClue.setFromValue(0);
          fadeClue.setToValue(1);
          fadeClue.play();
        });

    fadeHome.play();
  }

  private void handleWindowClose(WindowEvent event) {
    FreeTextToSpeech.deallocateSynthesizer();
  }
}
