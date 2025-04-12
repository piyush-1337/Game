package com.piyush.game.drawing;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class DrawingTools {

    private final Canvas canvas;

    public double lastX;
    public double lastY;
    public final List<Draw> drawHistory = new ArrayList<>();

    public DrawingTools(Canvas canvas) {
        this.canvas = canvas;
    }

    public interface Draw {
        void execute(GraphicsContext gc);
    }

    // Example line command implementation
    public record LineCommand(double x1, double y1, double x2, double y2) implements Draw {
        @Override
        public void execute(GraphicsContext gc) {
            gc.strokeLine(x1, y1, x2, y2);
        }
    }

    public void redraw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        // Clear the canvas
        gc.clearRect(0, 0, width, height);
        // Fill with blue color
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);

        // Redraw any existing drawing commands if needed
        for (Draw command : drawHistory) {
            command.execute(gc);
        }

    }

    public void clearCanvas() {
        drawHistory.clear();
        redraw();
    }
}
