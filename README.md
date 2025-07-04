# Multiplayer Drawing & Guessing Game 🎨🕵️

<!--toc:start-->
- [Multiplayer Drawing & Guessing Game 🎨🕵️](#multiplayer-drawing-guessing-game-🎨🕵️)
  - [🛠 Features](#🛠-features)
  - [⚙️ Requirements](#️-requirements)
  - [🚀 How to Run the Project](#🚀-how-to-run-the-project)
    - [Option 1: Using IntelliJ IDEA (Recommended)](#option-1-using-intellij-idea-recommended)
    - [Option 2: Using Command Line (Manual Setup)](#option-2-using-command-line-manual-setup)
<!--toc:end-->

A JavaFX-based multiplayer game inspired by Skribbl.io, where one player draws a word and others try to guess it in real time. Designed with a client-server architecture, the game uses JavaFX for GUI, multithreading for handling concurrent players, and Jackson for JSON communication.

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
- **Jackson**: For serializing/deserializing commands
- **OS**: Linux(Recommended), Windows(Need to enable broacasting on server and allow udp/tcp ports on client)

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

1. **Install Maven**  
   - [Download Maven](https://maven.apache.org/download.cgi) and follow the installation instructions.  
   - Verify installation by running:  

     ```bash
     mvn -v
     ```

2. **Clone the Repository**

   ```bash
   git clone https://github.com/piyush-1337/Game.git
   ```

3. **Run the Game**

   ```bash
   cd Game
   mvn clean javafx:run
   ```
