# Multiplayer Drawing & Guessing Game 🎨🕵️

A JavaFX-based multiplayer game inspired by Skribbl.io, where one player draws a word and others try to guess it in real time. Designed with a client-server architecture, the game uses JavaFX for GUI, multithreading for handling concurrent players, and Gson (via Jackson) for JSON communication.

---

## 🛠 Features

- Multiplayer support over a local network
- Real-time drawing and guessing
- Voting mechanism to skip or guess the word
- Simple and intuitive JavaFX UI

---

## ⚙️ Requirements

- **Java**: Version 23 or higher (Tested on Java 23.0.1)
- **Maven**: For managing JavaFX and Jackson dependencies
- **JavaFX SDK**: Used by the application (automatically handled by Maven if using IntelliJ)
- **Jackson**: For serializing/deserializing commands (via Gson-compatible APIs)

---

## 🚀 How to Run the Project

### Option 1: Using IntelliJ IDEA (Recommended)

1. **Clone the Repository**  

2. **Open in IntelliJ IDEA**  
- IntelliJ will automatically detect the Maven project and download dependencies.

3. **Run the Game**  
- Launch `Main.java`
- Run one instance as **host/server**, others as **clients**

> ✅ No need to manually set JavaFX paths if using IntelliJ with Maven.

---

### Option 2: Using Command Line (Manual Setup)

1. **Download JavaFX SDK**  
- [https://openjfx.io](https://openjfx.io)
- Extract it and note the path to the `lib` folder.

2. **Download Dependencies**  
- Download `jackson-core` and `jackson-databind` from [https://mvnrepository.com](https://mvnrepository.com)
- Place the `.jar` files in a `libs/` folder.

3. **Compile and Run**
```bash
javac --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml \
      -cp ".:libs/*" -d out $(find src -name "*.java")

java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml \
     -cp "out:libs/*" main.Main
