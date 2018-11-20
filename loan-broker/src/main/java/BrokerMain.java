import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class BrokerMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        // load FXML file is in bank/src/main/resources/bank.fxml
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("broker.fxml"));
        primaryStage.setTitle("BROKER");
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.exit();
                System.exit(0);
            }
        });

        primaryStage.setScene(new Scene(root, 500,300));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
