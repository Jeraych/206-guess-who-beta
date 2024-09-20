package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class GameOverController {
  @FXML Text thiefResultDisplay;
  @FXML TextArea feedbackDisplay;
  @FXML Text winLoseDisplay;
  @FXML Button playAgainButton; // used for correct guess (Scholar)
  @FXML Button labButton;
  @FXML Button leadButton;
  @FXML Button scholarButton;
  @FXML Button playAgainButton1; // used for incorrect guesses because position is different
  @FXML Button playAgainButton2; // used for no guess because position is different

  @FXML ImageView incorrectLabTech;
  @FXML ImageView incorrectScientist;
  @FXML ImageView correctScholar;
  @FXML ImageView outOfTime;

  private String userAnswer;
  private ChatCompletionRequest chatCompletionRequest;
  private String profession;
  private String feedback;
  private static GameStateContext context = App.getContext();

  @FXML
  public void initialize() throws ApiProxyException {
    userAnswer = App.getUserAnswer();
    leadButton.setVisible(false);
    labButton.setVisible(false);
    scholarButton.setVisible(false);
    incorrectLabTech.setVisible(false);
    incorrectScientist.setVisible(false);
    correctScholar.setVisible(false);
    feedbackDisplay.setVisible(false);
    playAgainButton.setVisible(false);
    playAgainButton1.setVisible(false);
    playAgainButton2.setVisible(false);
    outOfTime.setVisible(false);
    App.pauseGuessTimer();

    switch (App.getUserGuess()) {
      case "Lead Scientist":
        incorrectScientist.setVisible(true);
        playAgainButton1.setVisible(true);
        break;
      case "Lab Technician":
        incorrectLabTech.setVisible(true);
        playAgainButton1.setVisible(true);
        break;
      case "Scholar":
        correctScholar.setVisible(true);
        feedbackDisplay.setVisible(true);
        getFeedback(userAnswer);
        playAgainButton.setVisible(true);
        break;
      default:
        outOfTime.setVisible(true);
        playAgainButton2.setVisible(true);
        break;
    }

    // Any required initialization code can be placed here
  }

  @FXML
  private void playAgain(ActionEvent event) throws IOException {
    App.setRoot("home"); 
  }

  @FXML
  private void changeSuspect(ActionEvent event) {
    Button clickedButton = (Button) event.getSource();
    String suspectName = clickedButton.getText();
    if (App.gameResult(suspectName)) {
      thiefResultDisplay.setText("The " + suspectName + " is the thief!");
      feedbackDisplay.setText("They stole the research notes!");
      winLoseDisplay.setText("You WON!");
    } else {
      thiefResultDisplay.setText("The " + suspectName + " is not the thief!");
      feedbackDisplay.setText("They didn't steal the research notes!");
      winLoseDisplay.setText("You LOST!");
    }
  }

  public void getFeedback(String useranswer) {
    Task<Void> getFeedbackTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            try {
              ApiProxyConfig config = ApiProxyConfig.readConfig();
              chatCompletionRequest =
                  new ChatCompletionRequest(config)
                      .setN(1)
                      .setTemperature(0.2)
                      .setTopP(0.5)
                      .setMaxTokens(100);
              runGpt(new ChatMessage("system", getSystemPrompt()));
              Platform.runLater(
                  () -> {
                    // set text in feedbackDisplay to feedback
                    feedbackDisplay.setText(feedback);
                  });
            } catch (ApiProxyException e) {
              e.printStackTrace();
            }
            return null;
          }
        };

    Thread backgroundResponseThread = new Thread(getFeedbackTask);
    backgroundResponseThread.setDaemon(true);
    backgroundResponseThread.start();
  }

  public String getSystemPrompt() {
    Map<String, String> map = new HashMap<>();
    map.put("userAnswer", userAnswer);
    return PromptEngineering.getPrompt("modelanswer.txt", map);
  }

  private ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    chatCompletionRequest.addMessage(msg);
    try {
      System.out.println(profession);
      ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
      Choice result = chatCompletionResult.getChoices().iterator().next();
      chatCompletionRequest.addMessage(result.getChatMessage());
      feedback = result.getChatMessage().getContent();
      return result.getChatMessage();
    } catch (ApiProxyException e) {
      e.printStackTrace();
      return null;
    }
  }
}
