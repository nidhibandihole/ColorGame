import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class ColorGame extends JFrame {
    private static final ArrayList<String> COLORS = new ArrayList<>(); // The colors are stored in an ArrayList
    private static int score = 0;
    private static int timeLeft = 30;
    private JLabel label;
    private JLabel scoreLabel;
    private JLabel timeLabel;
    private JTextField inputField;
    private Timer timer;

    public ColorGame() {
        // Initialize the colors
        COLORS.add("Red");
        COLORS.add("Blue");
        COLORS.add("Green");
        COLORS.add("Yellow");
        COLORS.add("Orange");
        COLORS.add("Purple");
        COLORS.add("Pink");
        COLORS.add("Black");

        // Set up the main game window
        setTitle("Color Game");
        setSize(400, 400); // Adjusted size for better layout
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        // Timer and instruction panel at the top
        JPanel topPanel = new JPanel(new BorderLayout());
        timeLabel = new JLabel("Time left: " + timeLeft, JLabel.CENTER);
        timeLabel.setFont(new Font("Helvetica", Font.BOLD, 24));
        timeLabel.setOpaque(true);
        timeLabel.setBackground(Color.LIGHT_GRAY);
        timeLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        topPanel.add(timeLabel, BorderLayout.NORTH);

        JLabel instrLabel = new JLabel("Type the color you see, not the word!", JLabel.CENTER);
        instrLabel.setFont(new Font("Helvetica", Font.ITALIC, 14));
        instrLabel.setOpaque(true);
        instrLabel.setBackground(Color.WHITE);
        instrLabel.setForeground(Color.DARK_GRAY);
        instrLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        topPanel.add(instrLabel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Main text display
        label = new JLabel("", JLabel.CENTER);
        label.setFont(new Font("Helvetica", Font.BOLD, 48));
        add(label, BorderLayout.CENTER);

        // Input field and score display at the bottom
        JPanel bottomPanel = new JPanel(new BorderLayout());
        scoreLabel = new JLabel("Score: " + score, JLabel.CENTER);
        scoreLabel.setFont(new Font("Helvetica", Font.PLAIN, 16));
        bottomPanel.add(scoreLabel, BorderLayout.NORTH);

        inputField = new JTextField();
        inputField.setFont(new Font("Helvetica", Font.PLAIN, 16));
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.addActionListener(e -> checkInput());
        bottomPanel.add(inputField, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        // Shuffle colors and set up the first display
        shuffleColors();
        updateColor();

        // Start the count-down timer
        startTimer();
    }

    private void shuffleColors() {
        Collections.shuffle(COLORS); // Shuffle is a method in Collections class
    }

    private void updateColor() {
        label.setForeground(Color.decode(getColorHex(COLORS.get(1)))); // The actual color of the text is the 2nd element
        label.setText(COLORS.get(0)); // The word displayed is the 1st element
    }

    private void checkInput() {
        if (timeLeft > 0) {
            String userInput = inputField.getText().trim().toLowerCase();
            String correctColor = COLORS.get(1).toLowerCase();

            if (userInput.equals(correctColor)) {
                score++;
                scoreLabel.setText("Score: " + score);
                inputField.setText(""); // Clearing text for next 
                shuffleColors();
                updateColor();
            } else {
                // End the game immediately on wrong input
                timer.stop();
                showGameOverDialog("Wrong input! Game Over!");
            }
        }
    }

    private void startTimer() { // Timer is a method in javax.swing.Timer class
        timer = new Timer(1000, e -> {
            if (timeLeft > 0) {
                timeLeft--;
                timeLabel.setText("Time left: " + timeLeft);
            } else {
                timer.stop();
                showGameOverDialog("Time's up! Game Over!");
            }
        });
        timer.start();
    }

    private void showGameOverDialog(String message) {
        recordHighScore();
        int highestScore = loadHighScore();

        int choice = JOptionPane.showOptionDialog(
                this,
                message + "\nYour Score: " + score + "\nHighest Score: " + highestScore + "\nDo you want to play again?",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Restart", "Exit"},
                "Restart"
        );

        if (choice == JOptionPane.YES_OPTION) {
            restartGame();
        } else {
            System.exit(0);
        }
    }

    private void restartGame() {
        score = 0;
        timeLeft = 30;
        scoreLabel.setText("Score: 0");
        timeLabel.setText("Time left: 30");
        inputField.setText("");
        shuffleColors();
        updateColor();
        startTimer();
    }

    private void recordHighScore() {
        int highestScore = loadHighScore();
        if (score > highestScore) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("highest_score.txt"))) {
                writer.write(String.valueOf(score));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private int loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader("highest_score.txt"))) {
            String data = reader.readLine();
            if (data != null && !data.isEmpty()) {
                return Integer.parseInt(data); // Convert string to int
            }
        } catch (IOException | NumberFormatException ex) {
            // File not found or invalid data like the first time playing the game
        }
        return 0;
    }

    private String getColorHex(String colorName) {
        switch (colorName.toLowerCase()) {
            case "red": return "#FF0000";
            case "blue": return "#0000FF";
            case "green": return "#008000";
            case "yellow": return "#FFFF00";
            case "orange": return "#FFA500";
            case "purple": return "#800080";
            case "pink": return "#FFC0CB";
            case "black": return "#000000";
            default: return "#000000";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ColorGame game = new ColorGame();
            game.setVisible(true);
        });
    }
}
