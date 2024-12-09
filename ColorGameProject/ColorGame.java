
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.*;

@SuppressWarnings("serial")
public class ColorGame extends JFrame {
    private static final ArrayList<String> COLORS = new ArrayList<>();
    private static int score = 0;
    private static int timeLeft = 30;
    private JLabel label;
    private JLabel scoreLabel;
    private JLabel timeLabel;
    private JTextField inputField;
    private Timer timer;

    // MySQL Database credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/colorgame_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

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
        setSize(400, 400);
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
        Collections.shuffle(COLORS);
    }

    private void updateColor() {
        label.setForeground(Color.decode(getColorHex(COLORS.get(1))));
        label.setText(COLORS.get(0));
    }

    private void checkInput() {
        if (timeLeft > 0) {
            String userInput = inputField.getText().trim().toLowerCase();
            String correctColor = COLORS.get(1).toLowerCase();

            if (userInput.equals(correctColor)) {
                score++;
                scoreLabel.setText("Score: " + score);
                inputField.setText("");
                shuffleColors();
                updateColor();
                updateColorStats(COLORS.get(1), true);
            } else {
                timer.stop();
                updateColorStats(COLORS.get(1), false);
                showGameOverDialog("Wrong input! Game Over!");
            }
        }
    }

    private void startTimer() {
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
        recordPlayerAndSession();
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

    private void recordPlayerAndSession() {
        String playerName = JOptionPane.showInputDialog(this, "Enter your name:");
        String playerEmail = JOptionPane.showInputDialog(this, "Enter your email:");

        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Anonymous";
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Insert player details
            int playerId;
            String playerQuery = "INSERT INTO players (name, email) VALUES (?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name)";
            try (PreparedStatement ps = conn.prepareStatement(playerQuery, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, playerName);
                ps.setString(2, playerEmail);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        playerId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve player ID.");
                    }
                }
            }

            // Insert game session
            String sessionQuery = "INSERT INTO game_sessions (player_id, score, start_time, end_time) VALUES (?, ?, NOW(), NOW())";
            try (PreparedStatement ps = conn.prepareStatement(sessionQuery)) {
                ps.setInt(1, playerId);
                ps.setInt(2, score);
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private int loadHighScore() {
        int highestScore = 0;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Use JOIN to fetch player details with their highest score
            String query = "SELECT MAX(gs.score) FROM game_sessions gs " +
                           "JOIN players p ON p.id = gs.player_id";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    highestScore = rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return highestScore;
    }

    private void updateColorStats(String colorName, boolean correctGuess) {
        String column = correctGuess ? "correct_guesses" : "incorrect_guesses";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO color_stats (color_name, " + column + ") " +
                           "VALUES (?, 1) " +
                           "ON DUPLICATE KEY UPDATE " + column + " = " + column + " + 1";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, colorName);
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
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
