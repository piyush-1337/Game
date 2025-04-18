module com.piyush.game {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;


    opens com.piyush.game to javafx.fxml;
    exports com.piyush.game;
    exports com.piyush.game.controllers;
    exports com.piyush.game.GameTools to com.fasterxml.jackson.core, com.fasterxml.jackson.databind;
    opens com.piyush.game.controllers to javafx.fxml;
    opens com.piyush.game.controllers.server to javafx.fxml;
    opens com.piyush.game.controllers.client to javafx.fxml;
    opens com.piyush.game.controllers.game to javafx.fxml;

}