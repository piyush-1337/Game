module com.piyush.game {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.piyush.game to javafx.fxml;
    exports com.piyush.game;
    exports com.piyush.game.controllers;
    opens com.piyush.game.controllers to javafx.fxml;
    opens com.piyush.game.controllers.server to javafx.fxml;
    opens com.piyush.game.controllers.client to javafx.fxml;
    opens com.piyush.game.controllers.game to javafx.fxml;

}