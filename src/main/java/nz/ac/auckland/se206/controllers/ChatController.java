package nz.ac.auckland.se206.controllers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;

/**
 * Controller class for the chat view. Handles user interactions and communication with the GPT
 * model via the API proxy.
 */
public class ChatController {

  private static Map<String, Boolean> firstInteraction = new HashMap<>();
  private static Map<String, StringBuilder> chatHistories = new HashMap<>();
  private static boolean talked = false;
  private static RoomController roomController;
  private ArrayList<String> chatTexts = new ArrayList<>();
  private String profession;
  private String filePath;
  private String name = "Speaker";
  private Boolean first;
  private MediaPlayer mediaPlayerChat;
  private int displayedChat = 0;
  @FXML private TextArea txtaChat;
  @FXML private TextField txtInput;
  @FXML private Button btnSend;
  @FXML private Button btnLast;
  @FXML private Button btnNext;
  private ChatCompletionRequest chatCompletionRequest;
  private Task<Void> fetchChatTask;
  private Task<Void> runGptTask;
  private Media greeting;
  private MediaPlayer greetingPlayer;

  // Static Methods
  public static boolean hasTalked() {
    return talked;
  }

  public static void setRoomController(RoomController roomContrl) {
    roomController = roomContrl;
  }

  // Initializer Method
  @FXML
  public void initialize() throws ApiProxyException {
    System.out.println("Chat initialized");
    firstInteraction.put("Lab Technician", true);
    firstInteraction.put("Lead Scientist", true);
    firstInteraction.put("Scholar", true);
  }

  // Chat Functionality Methods

  /**
   * Sets the profession for the chat context and initializes the ChatCompletionRequest.
   *
   * @param profession the profession to set
   */
  public void setProfession(String profession) {

    System.out.println("Setting profession");
    updateChatTexts();
    this.profession = profession;
    
    switch (profession) {
      case "Lab Technician":
        greeting = new Media(App.class.getResource("/sounds/labtechnician greeting.mp3").toExternalForm());
        greetingPlayer = new MediaPlayer(greeting);
        break;
      case "Lead Scientist":
        greeting = new Media(App.class.getResource("/sounds/leadscientist greeting.mp3").toExternalForm());
        greetingPlayer = new MediaPlayer(greeting);
        break;
      case "Scholar":
        greeting = new Media(App.class.getResource("/sounds/scholar greeting.mp3").toExternalForm());
        greetingPlayer = new MediaPlayer(greeting);
        break;
    }
    
    greetingPlayer.play();
    initializeFilePath();
    fetchChatTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            try {
              first = firstInteraction.get(profession);
              ApiProxyConfig config = ApiProxyConfig.readConfig();
              chatCompletionRequest =
                  new ChatCompletionRequest(config)
                      .setN(1)
                      .setTemperature(0.2)
                      .setTopP(0.5)
                      .setMaxTokens(100);
              runGpt(new ChatMessage("system", getSystemPrompt()));
            } catch (ApiProxyException e) {
              e.printStackTrace();
            }
            return null;
          }
        };

    Thread backgroundChatThread = new Thread(fetchChatTask);
    backgroundChatThread.setDaemon(true);
    backgroundChatThread.start();
  }

  private void initializeFilePath() {
    switch (profession) {
      case "Lab Technician":
        filePath = "src/main/resources/prompts/lab_technician_2.txt";
        break;
      case "Lead Scientist":
        filePath = "src/main/resources/prompts/lead_scientist_2.txt";
        break;
      case "Scholar":
        filePath = "src/main/resources/prompts/scholar_2.txt";
        break;
      default:
        filePath = "src/main/resources/prompts/chat_2.txt";
        break;
    }
  }

  /**
   * Generates the system prompt based on the profession.
   *
   * @return the system prompt string
   */
  private String getSystemPrompt() {
    Map<String, String> map = new HashMap<>();
    map.put("profession", profession);
    String firstFile, secondFile;

    switch (profession) {
      case "Lab Technician":
        firstFile = "lab_technician.txt";
        secondFile = "lab_technician_2.txt";
        break;
      case "Lead Scientist":
        firstFile = "lead_scientist.txt";
        secondFile = "lead_scientist_2.txt";
        break;
      case "Scholar":
        firstFile = "scholar.txt";
        secondFile = "scholar_2.txt";
        break;
      default:
        firstFile = "chat.txt";
        secondFile = null;
        break;
    }

    return readTextFile("src/main/resources/prompts/" + firstFile)
        + "\n\n"
        + readTextFile("src/main/resources/prompts/" + secondFile);
  }

  public static String readTextFile(String filePath) {
    StringBuilder content = new StringBuilder();
    BufferedReader reader = null;

    try {
      // Initialize reader to read the file
      reader = new BufferedReader(new FileReader(filePath));

      String line;
      // Read each line and append it to the content StringBuilder
      while ((line = reader.readLine()) != null) {
        content.append(line).append(System.lineSeparator());
      }

    } catch (IOException e) {
      e.printStackTrace(); // Handle any file read errors
    } finally {
      try {
        if (reader != null) {
          reader.close(); // Close the reader
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return content.toString(); // Return the file content as a string
  }

  // Sound Effects Methods
  public void playHmm() {
    Media hmmSound = new Media(App.class.getResource("/sounds/hmmm.mp3").toExternalForm());
    mediaPlayerChat = new MediaPlayer(hmmSound);
    mediaPlayerChat.setVolume(0.8);
    Platform.runLater(() -> mediaPlayerChat.play());
  }

  // Chat Handling Methods

  /**
   * Appends a chat message to the chat text area.
   *
   * @param msg the chat message to append
   */
  private void appendChatMessage(ChatMessage msg) {
    name = msg.getRole().equals("assistant") ? profession : "You";
    String messageText = name + ": " + msg.getContent() + "\n\n";
    saveChatToFile(msg.getContent() + "\n");
    txtaChat.appendText(messageText);
    StringBuilder history = chatHistories.getOrDefault(profession, new StringBuilder());
    history.append(messageText);
    chatHistories.put(profession, history);
  }

  /**
   * Runs the GPT model with a given chat message.
   *
   * @param msg the chat message to process
   * @return the response chat message
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  private ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    chatCompletionRequest.addMessage(msg);
    try {
      ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
      Choice result = chatCompletionResult.getChoices().iterator().next();
      chatCompletionRequest.addMessage(result.getChatMessage());
      appendChatMessage(result.getChatMessage());
      chatTexts.add(profession + ": " + result.getChatMessage().getContent());
      return result.getChatMessage();
    } catch (ApiProxyException e) {
      e.printStackTrace();
      return null;
    }
  }

  // Save chat history to file
  private void saveChatToFile(String chatContent) {
    try {
      Files.writeString(
          Paths.get(filePath),
          chatContent + "\n",
          StandardOpenOption.CREATE,
          StandardOpenOption.APPEND);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // Event Handlers

  /**
   * Sends a message to the GPT model.
   *
   * @param event the action event triggered by the send button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onSendMessage(ActionEvent event) throws ApiProxyException, IOException {

    updateChatTexts();
    String message = txtInput.getText().trim();

    if (message.isEmpty()) {
      return;
    }
    chatTexts.add("You: " + message);
    txtInput.clear();
    ChatMessage msg = new ChatMessage("user", message);

    appendChatMessage(msg);
    talked = true;
    runGptTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            runGpt(msg);
            return null;
          }
        };

    Thread backgroundGptThread = new Thread(runGptTask);
    backgroundGptThread.setDaemon(true);
    backgroundGptThread.start();
  }

  @FXML
  private void onNextClicked(ActionEvent event) throws ApiProxyException, IOException {
    if (displayedChat == 0) {
      txtInput.setVisible(true);
      updateChatTexts();
    } else {
      this.displayedChat += 1;
      txtaChat.setText(chatTexts.get(chatTexts.size() + displayedChat - 1));
    }
  }

  private void updateChatTexts() {

    txtaChat.clear();
  }

  @FXML
  private void onLastClicked(ActionEvent event) throws ApiProxyException, IOException {
    if (!chatTexts.isEmpty() && -1 * displayedChat < chatTexts.size()) {
      this.displayedChat -= 1;
      txtaChat.setText(chatTexts.get(chatTexts.size() + displayedChat));
      txtInput.setVisible(false);
    }
    if (-1 * displayedChat == chatTexts.size()) {
      displayedChat += 1;
    }
  }

  /**
   * Navigates back to the previous view.
   *
   * @param event the action event triggered by the go back button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onGoBack(ActionEvent event) throws ApiProxyException, IOException {
    App.hideChat();
  }

  private void disbaleChatButton() {
    // txtInput.setDisable(true);
    btnSend.setDisable(true);
  }

  private void enableChatButton() {
    // txtInput.setDisable(false);
    btnSend.setDisable(false);
  }

  private void disableInteraction() {
    RoomController.getRoomController().disableRoom();
    RoomController.getRoomController().disableSuspects();
    disbaleChatButton();
  }

  private void enableInteraction() {
    RoomController.getRoomController().enableRoom();
    RoomController.getRoomController().enableSuspects();
    enableChatButton();
  }
}
