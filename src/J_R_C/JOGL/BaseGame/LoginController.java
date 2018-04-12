
package J_R_C.JOGL.BaseGame;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author KJW finish at 2016/ 08/ 11
 * @version 2.0.0v
 * @description this class Manage The Login window
 * @copyRight of KJW all Rights Reserved and follow the MIT license
 */
public class LoginController implements Initializable {
	/**
	 * Main panel for LoginController
	 */
	@FXML
	private AnchorPane mainPane;

	/**
	 * button for the cancel (exit the windows)
	 */
	@FXML
	private Button btnCancel;

	/**
	 * button for the server login
	 */
	@FXML
	private Button btnLogin;

	/**
	 * button for registering new client to server
	 */
	@FXML
	private Button btnNewClient;

	/**
	 * User ID TextField
	 */
	@FXML
	TextField txtID;

	@FXML
	Label versionID;

	/**
	 * User Password TextField
	 */
	@FXML
	PasswordField txtPassWord;

	/**
	 * client information for socket programming
	 */
	private static ServerClient client;

	/**
	 * primary stage for changing the scene
	 */
	private Stage primaryStage;

	/**
	 * flag variable for checking it is initialize (success about login)
	 */
	private boolean flag;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javafx.fxml.Initializable#initialize(java.net.URL,
	 * java.util.ResourceBundle) init the all state
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {

		Settings.isClientVersionCheck = false;
		flag = false;

		// only one client create (using design pattern <-- singleton
		if (client == null) {
			client = new ServerClient();

			client.startClient();
		}
		// register click event
		btnCancel.setOnAction(e -> handleBtnCancel(e));
		btnLogin.setOnAction(e -> handleBtnLogin(e));
		btnNewClient.setOnAction(e -> handleNewClient(e));
		mainPane.setOnKeyPressed(e -> handleBtnKeyEvent(e));

		// run scheduler for checking
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

		scheduler.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {

				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						if (txtID.getText().length() > 11) {
							Platform.runLater(() -> handlePopup("Limit 11 alphabets."));
							txtID.setText(txtID.getText(0, 11));
						}

						if (txtPassWord.getText().length() > 11) {
							Platform.runLater(() -> handlePopup("Limit 11 alphabets."));
							txtPassWord.setText(txtID.getText(0, 11));
						}

						if (txtFieldUsePassWordCheck != null && txtFieldUsePassWordCheck.getText().length() > 11) {
							Platform.runLater(() -> handlePopup("Limit 11 alphabets."));
							txtFieldUsePassWordCheck.setText(txtFieldUsePassWordCheck.getText(0, 11));
						}

						if (txtFieldUsePassWord != null && txtFieldUsePassWord.getText().length() > 11) {
							Platform.runLater(() -> handlePopup("Limit 11 alphabets."));
							txtFieldUsePassWord.setText(txtFieldUsePassWord.getText(0, 11));
						}

						if (txtFieldUserId != null && txtFieldUserId.getText().length() > 11) {
							Platform.runLater(() -> handlePopup("Limit 11 alphabets."));
							txtFieldUserId.setText(txtFieldUserId.getText(0, 11));
						}

						// if flag turn on then client login game
						if (flag) {
							try {
								flag = false;
								FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/room.fxml"));

								Parent roomList = (Parent) loader.load();
								Scene scene = new Scene(roomList);
								scene.getStylesheets().add(getClass().getResource("/Css/app.css").toString());
								WaitingRoomsManagerController roomManagerController = loader.getController();
								roomManagerController.setPrimaryStage(primaryStage);
								primaryStage.setScene(scene);
								primaryStage.show();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						if (ServerClient.SERVERCONNECTIONFAIL == client.getIsServerConnected()) {
							btnLogin.setText("Connect");
							btnLogin.requestLayout();

						} else if (ServerClient.SERVERCONNECTIONSUCCESS == client.getIsServerConnected()) {
							if (!btnLogin.getText().equals("Start"))
								btnLogin.setText("Start");
						}
						btnLogin.requestLayout();
					}
				});

			}
		}, 50, 50, TimeUnit.MILLISECONDS);

		client.setLoginController(this);

	}
	/**
	 * hand btn key event
	 * @param e
	 */
    public void handleBtnKeyEvent(KeyEvent e) {
        switch (e.getCode()) {
            case ENTER:
            	handleBtnLogin(null);
                break;
                
            default:
                e.consume();
                break;
        }
    }

	/**
	 * set the primary stage
	 * 
	 * @param primaryStage
	 */
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	/**
	 * get the primary stage
	 * 
	 * @return
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}

	/**
	 * register client (edit id)
	 */
	private javafx.scene.control.TextField txtFieldUserId;

	/**
	 * register client (edit password)
	 */
	private javafx.scene.control.TextField txtFieldUsePassWord;

	/**
	 * register client (check password)
	 */
	private javafx.scene.control.TextField txtFieldUsePassWordCheck;

	/**
	 * for dialog
	 */
	private Stage dialog;

	/**
	 * new client registration window
	 */
	public void handleNewClient(ActionEvent event) {
		try {
			dialog = new Stage(StageStyle.UTILITY);
			dialog.initModality(Modality.WINDOW_MODAL);
			dialog.initOwner(primaryStage);
			dialog.setTitle("Sign Up");

			Parent parent;
			parent = FXMLLoader.load(getClass().getResource("/Fxml/custom_register.fxml"));

			Button btnOk = (Button) parent.lookup("#btnOk");
			btnOk.setOnAction(e -> handleBtnRegister(event));

			Button btnCancel = (Button) parent.lookup("#btnCancel");
			btnCancel.setOnAction(e -> dialog.close());

			txtFieldUserId = (javafx.scene.control.TextField) parent.lookup("#textEditUserName");
			txtFieldUserId.setPromptText("ID 7 String");
			txtFieldUserId.addEventFilter(KeyEvent.KEY_TYPED, letter_Validation(Settings.nIDMaximumLenght));

			txtFieldUsePassWord = (javafx.scene.control.TextField) parent.lookup("#textEditUserPassWord");
			txtFieldUsePassWord.setPromptText("PASSWORD 7 String");
			txtFieldUsePassWord.addEventFilter(KeyEvent.KEY_TYPED, numeric_Validation(Settings.nPasswordMaximumLength));

			txtFieldUsePassWordCheck = (javafx.scene.control.TextField) parent.lookup("#textEditUserPassWordCheck");
			txtFieldUsePassWordCheck.setPromptText("CHECK PASSWORD");
			txtFieldUsePassWordCheck.addEventFilter(KeyEvent.KEY_TYPED,
					numeric_Validation(Settings.nPasswordMaximumLength));

			Scene scene = new Scene(parent);

			scene.getStylesheets().add(getClass().getResource("/Css/app.css").toString());

			dialog.setScene(scene);
			dialog.setResizable(false);
			dialog.show();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * check the client information and if the information did not exist in the
	 * database system. server sends message (success) but exist server sends
	 * message (fail)
	 * 
	 * @param packet
	 */
	public void checkTheNewUserRegisterSuccess(String packet[]) {
		if (Boolean.parseBoolean(packet[1])) {
			Platform.runLater(() -> handlePopup("Register Success!!"));
			Platform.runLater(() -> dialog.close());
		} else {
			Platform.runLater(() -> handlePopup("There are already Registered."));
		}
	}

	/**
	 * click the register button
	 * 
	 * @param event
	 */
	public void handleBtnRegister(ActionEvent event) {

		if (txtFieldUsePassWord.getText().equals(txtFieldUsePassWordCheck.getText())) {
			client.sendPacket(Settings._REQUEST_REGISTER_NEW_USER + "", txtFieldUserId.getText(),
					txtFieldUsePassWord.getText());
		} else
			Platform.runLater(() -> handlePopup("Wrong password."));

	}

	/**
	 * popUp windows
	 * 
	 * @param text
	 */
	public void handlePopup(String text) {
		Platform.runLater(() -> {

			try {
				Popup popup = new Popup();

				Parent parent;

				parent = FXMLLoader.load(getClass().getResource("/Fxml/popup.fxml"));
				ImageView imageView = (ImageView) parent.lookup("#imgMessage");
				imageView.setImage(new Image(getClass().getResource("/Css/dialog-info.png").toString()));
				imageView.setOnMouseClicked(event -> popup.hide());
				Label lblMessage = (Label) parent.lookup("#lblMessage");
				lblMessage.setText(text);

				// set the popup window position
				popup.setX(primaryStage.getX());
				popup.setY(primaryStage.getY());

				popup.getContent().add(parent);
				popup.setAutoHide(true);
				popup.show(primaryStage);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});
	}

	/**
	 * check the password
	 * 
	 * @param max_Lengh
	 * @return
	 */
	public EventHandler<KeyEvent> numeric_Validation(final Integer max_Lengh) {
		return new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {

				TextField txt_TextField = (TextField) e.getSource();
				if (txt_TextField.getText().length() >= max_Lengh) {
					e.consume();
				}

				if (e.getCharacter().matches("[$]"))
					e.consume();
			}
		};
	}

	/**
	 * check the client name. client must start the name using character (exp:
	 * assss or a1234)
	 * 
	 * @param max_Lengh
	 * @return
	 */
	public EventHandler<KeyEvent> letter_Validation(final Integer max_Lengh) {
		return new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {

				TextField txt_TextField = (TextField) e.getSource();
				if (txt_TextField.getText().length() >= max_Lengh) {
					e.consume();
				}

				if (e.getCharacter().matches("[A-Za-z]") && txt_TextField.getText().length() < 1) {
				} else if (txt_TextField.getText().length() < 1) {
					e.consume();
				}

			}
		};
	}

	/**
	 * if click the connection button then this method is called ( for connect
	 * server)
	 * 
	 * @param event
	 */
	public void handleBtnLogin(ActionEvent event) {
		if (btnLogin.getText().equals("Start")) {

			if (ServerClient.SERVERCONNECTIONFAIL == client.getIsServerConnected()) {
				btnLogin.setText("Connect");
			} else {
				if (Settings.isClientVersionCheck)
					client.sendPacket(Settings._REQUEST_LOGIN + "", txtID.getText(), txtPassWord.getText());
				else
					Platform.runLater(() -> handlePopup("you must update your Client Version"));
			}
		} else if (btnLogin.getText().equals("Connect")) {
			client.startClient();
		}

	}

	/**
	 * client version check module
	 * @param packet
	 */
	public void clientVersionCheck(String[] packet) {
		boolean isCheck = Boolean.parseBoolean(packet[1]);

		if (isCheck) {
			{
				defaultVersionSet();
			}
		} else {
			Settings.isClientVersionCheck = false;
			Platform.runLater(() -> versionID.setText("wrong version"));
		}
	}
	
	/**
	 * defeault version set
	 */
	public void defaultVersionSet()
	{
		Settings.isClientVersionCheck = true;
		Platform.runLater(() -> versionID.setText(Settings.clientVersion));
		Platform.runLater(() -> versionID.setLayoutX(545));
	}

	/**
	 * login to main robby using client information if your information is
	 * correct. set the flat is true, but is unCorrect the program make popUp
	 * for notification
	 * 
	 * @param packet
	 */
	public void loginToTheMainRobby(String[] packet) {
		flag = Boolean.parseBoolean(packet[1]);

		if (flag == false)
			Platform.runLater(() -> handlePopup("incorrect login."));
	}

	/**
	 * if click the cancel button. then this window is terminate
	 * 
	 * @param event
	 */
	public void handleBtnCancel(ActionEvent event) {
		System.out.println("Client Terminate");
		System.exit(0);

	}

	/**
	 * check the continuous server connection
	 * @author KJW
	 *
	 */
	class TimeService extends Service<Integer> {
		@Override
		protected Task<Integer> createTask() {
			Task<Integer> task = new Task<Integer>() {
				@Override
				protected Integer call() throws Exception {

					while (true) {
						if (isCancelled()) {
							break;
						}
						System.out.println(client.getIsServerConnected());

						if (ServerClient.SERVERCONNECTIONSUCCESS == client.getIsServerConnected()) {
							succeeded();
							break;
						} else if (ServerClient.SERVERCONNECTIONFAIL == client.getIsServerConnected()) {
							this.cancel();
							break;
						}

						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							if (isCancelled()) {
								break;
							}
						}
					}

					return 1;
				}
			};
			return task;
		}

		@Override
		protected void succeeded() {
			btnLogin.setText("Start");
		}

		@Override
		protected void cancelled() {
			btnLogin.setText("Connect");
		}

		@Override
		protected void failed() {
			btnLogin.setText("Fail");
		}

	}

	/**
	 * get the client singleton Design Pattern
	 * 
	 * @return
	 */
	public static ServerClient getClient() {
		return LoginController.client;
	}

}
