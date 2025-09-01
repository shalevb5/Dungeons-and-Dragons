import javax.swing.*;


public class Main {
    public static void main(String[] args) {
        String levelsPath;

        if (args.length < 1) {
            int result = JOptionPane.showConfirmDialog(
                    null,
                    "No path provided.\nWould you like to use the default 'levels' directory?",
                    "Missing Levels Path",
                    JOptionPane.YES_NO_OPTION
            );

            if (result == JOptionPane.YES_OPTION) {
                levelsPath = "levels"; // assuming the "levels" directory is in the project root
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "The game cannot start without a levels directory.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
                return;
            }
        } else {
            levelsPath = args[0];
        }

        GameManager gameManager = new GameManager();
        gameManager.initializeGame(levelsPath);
        gameManager.start();
    }
}
