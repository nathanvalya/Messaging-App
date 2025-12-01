import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GuiClient extends Application{

	HashMap<String, Scene> sceneMap = new HashMap<String, Scene>();
	Client clientConnection;
	String currentUserID = "temp";
	ListView<Button> listCurrentUsers = new ListView<Button>();
	HashMap<String, ListView<String>> messageStorage = new HashMap<String, ListView<String>>();

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		//newUserScene start-------------------------------------------------------------------------------------
		Text titlePrompt = new Text(" Welcome New User\nPlease enter new ID");
		titlePrompt.setFont(Font.font("ariel", FontWeight.BOLD, 20));

		TextField newUserPrompt = new TextField();
		newUserPrompt.setMaxWidth(200);
		newUserPrompt.setEditable(true);

		Button saveNewUserName = new Button();
		saveNewUserName.setText("Send");
		saveNewUserName.setOnAction(e->{
			Message sendID = new Message(1, newUserPrompt.getText());
			currentUserID = newUserPrompt.getText();
			clientConnection.send(sendID);
		});

		HBox sendNewUserName = new HBox(newUserPrompt,saveNewUserName);

		VBox v1 = new VBox(titlePrompt, sendNewUserName);
		v1.setSpacing(80);

		BorderPane newUserBorderPane = new BorderPane();
		newUserBorderPane.setPadding(new Insets(20));
		newUserBorderPane.setCenter(v1);
		BorderPane.setMargin(v1, new Insets(70, 0, 0, 180));

		Scene newUserScene = new Scene(newUserBorderPane, 600, 400);
		//newUserScene end---------------------------------------------------------------------------------------

		//Main Menu Scene start----------------------------------------------------------------------------------
		Text welcome = new Text("Welcome " + currentUserID);
		welcome.setFont(Font.font("verdana", FontWeight.BOLD, 20));

		Text usersTitle = new Text("Users Currently Online: ");
		usersTitle.setTextAlignment(TextAlignment.CENTER);

		listCurrentUsers.setPrefWidth(150);
		listCurrentUsers.setPrefHeight(300);
		VBox users = new VBox(usersTitle, listCurrentUsers);

		Text optionTitle = new Text("Selected a connected\nuser to chat with or select exit");
		optionTitle.setTextAlignment(TextAlignment.CENTER);

		Button exit = new Button("Exit App");
		exit.setMaxWidth(100);
		exit.setOnAction(event -> {

			Message exitMessage = new Message(5, currentUserID);
			clientConnection.send(exitMessage);

			Platform.exit();
			System.exit(0);
		});

		VBox options = new VBox(optionTitle, exit);
		options.setSpacing(25);
		options.setAlignment(Pos.TOP_CENTER);

		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(20));
		borderPane.setPrefSize(400,300);
		borderPane.setLeft(users);
		borderPane.setTop(welcome);
		BorderPane.setAlignment(welcome, Pos.CENTER);
		borderPane.setRight(options);

		Scene mainMenu = new Scene(borderPane,600,400);
		//Main Menu Scene end----------------------------------------------------------------------------------

		//liveChat Message scene start-------------------------------------------------------------------------
		ListView<String> liveChat = new ListView<String>();
		liveChat.setMaxWidth(200);
		liveChat.setMaxHeight(300);
		liveChat.setPrefHeight(300);
		messageStorage.put("all", liveChat);

		// start of scene
		Text liveChatTitle = new Text("All Connected Users");

		TextField enterMessageToAll = new TextField();

		Button backButton = new Button("< Back");
		backButton.setOnAction(e->{
			primaryStage.setScene(sceneMap.get("Main Menu"));
			primaryStage.setTitle("Main Menu");
			primaryStage.show();
		});

		Button sendToAll = new Button("Send");
		sendToAll.setOnAction(e->{
			messageStorage.get("all").getItems().add(currentUserID + ": " + enterMessageToAll.getText());
			Message sendMessage = new Message(2, currentUserID, "all", enterMessageToAll.getText());
			clientConnection.send(sendMessage);
			enterMessageToAll.clear();
		});

		HBox sendMessageToAll = new HBox(enterMessageToAll, sendToAll);
		sendMessageToAll.setAlignment(Pos.CENTER);
		VBox liveMessageApp = new VBox(liveChatTitle, liveChat, sendMessageToAll);
		liveMessageApp.setAlignment(Pos.CENTER);

		BorderPane liveChatBorderPane = new BorderPane();
		liveChatBorderPane.setTop(backButton);
		liveChatBorderPane.setCenter(liveMessageApp);

		Scene liveMessageScene = new Scene(liveChatBorderPane, 600, 400);
		// end of scene

		sceneMap.put("all", liveMessageScene);

		Button liveChatButton = new Button("Message All");
		liveChatButton.setOnAction(e->{

			primaryStage.setScene(sceneMap.get("all"));
			primaryStage.setTitle("Messaging");
			primaryStage.show();

		});

		liveChatButton.setMaxWidth(100);
		liveChatButton.setPrefWidth(100);
		listCurrentUsers.getItems().add(liveChatButton);
		//liveChat Message scene end---------------------------------------------------------------------------

		clientConnection = new Client(data->{
			Platform.runLater(()->{
				Message message = ((Message) data);

				if(message.purpose == 1) {
					if (message.newIDAccepted == true) {
						welcome.setText("Welcome " + currentUserID);
						sceneMap.put("Main Menu", mainMenu);

						for(int i = 0; i < message.allChat.size(); i++){
							messageStorage.get("all").getItems().add(message.allChat.get(i));
						}

						primaryStage.setScene(sceneMap.get("Main Menu"));
						primaryStage.setTitle("Main Menu");
						primaryStage.show();
					}
					else {
						titlePrompt.setText("This userID already exist\nEnter a different one");
						titlePrompt.setTextAlignment(TextAlignment.CENTER);
						newUserPrompt.clear();
					}
				}

				if(message.purpose == 2){

					if(message.toID.equals("all")){
						messageStorage.get("all").getItems().add(message.fromID + ": " + message.message);
					}
					else {
						messageStorage.get(message.fromID).getItems().add(message.fromID + ": " + message.message);
					}

				}

				if(message.purpose == 4){

					for(int i = 0; i < message.userNames.size(); i++){
						if(!(message.userNames.get(i).equals(currentUserID) || message.userNames.get(i).equals("temp"))){
							if(sceneMap.get(message.userNames.get(i)) == null){

								ListView<String> chat = new ListView<String>();
								chat.setMaxWidth(200);
								chat.setMaxHeight(300);
								chat.setPrefHeight(300);
								messageStorage.put(message.userNames.get(i), chat);

								// start of scene-------------------------------------------------------------
								Text otherUserName = new Text();
								otherUserName.setText(message.userNames.get(i));

								TextField enterMessage = new TextField();

								Button back = new Button("< Back");
								back.setOnAction(e->{
									primaryStage.setScene(sceneMap.get("Main Menu"));
									primaryStage.setTitle("Main Menu");
									primaryStage.show();
								});

								Button send = new Button("Send");
								send.setOnAction(e->{

									messageStorage.get(otherUserName.getText()).getItems().add(currentUserID + ": " + enterMessage.getText());
									Message sendMessage = new Message(2, currentUserID, otherUserName.getText(), enterMessage.getText());
									clientConnection.send(sendMessage);
									enterMessage.clear();
								});

								HBox sendMessage = new HBox(enterMessage, send);
								sendMessage.setAlignment(Pos.CENTER);
								VBox messageApp = new VBox(otherUserName, chat, sendMessage);
								messageApp.setAlignment(Pos.CENTER);

								BorderPane messageBorderPane = new BorderPane();
								messageBorderPane.setTop(back);
								messageBorderPane.setCenter(messageApp);

								Scene messageScene = new Scene(messageBorderPane, 600, 400);

								// end of scene-----------------------------------------------------------------------

								sceneMap.put(message.userNames.get(i), messageScene);

								Button newButton = new Button(message.userNames.get(i));
								newButton.setOnAction(e->{

									primaryStage.setScene(sceneMap.get(otherUserName.getText()));
									primaryStage.setTitle("Messaging");
									primaryStage.show();

								});
								newButton.setText(message.userNames.get(i));
								newButton.setMaxWidth(100);
								newButton.setPrefWidth(100);
								listCurrentUsers.getItems().add(newButton);

							}
						}
					}
				}

				if(message.purpose == 5){
					for(int i = 0; i < listCurrentUsers.getItems().size(); i++){
						if(listCurrentUsers.getItems().get(i).getText().equals(message.userId)){
							listCurrentUsers.getItems().remove(listCurrentUsers.getItems().get(i));
						}
					}

					messageStorage.get(message.userId).getItems().add(message.userId + " has disconnected. Please leave.");
					sceneMap.remove(message.userId);
					messageStorage.remove(message.userId);
					messageStorage.get("all").getItems().add(message.userId + " has disconnected.");
				}
			});
		});

		clientConnection.start();
		sceneMap.put("client",  newUserScene);

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

		primaryStage.setScene(sceneMap.get("client"));
		primaryStage.setTitle("Client");
		primaryStage.show();

	}
}


