import Callbacks.MessageCallback;
import Tiles.TileFactory;

import java.util.Scanner;

public class CLI {
    private static final String VALID_ACTION_KEYS = "wasdeq";
    private static final int MIN_INDEX_CHARACTER = 1;

    private MessageCallback mcb;
    private Scanner scanner;
    private InputReader input;

    /**
     * Initializes the CLI, message callback, and input reader.
     */
    public CLI() {
        scanner = new Scanner(System.in);
        mcb = (s) -> print(s);
        input = () -> getInput();
    }

    /**
     * Returns the message callback for printing messages.
     * @return the message callback
     */
    public MessageCallback getMcb() { return mcb; }

    /**
     * Prints the given string to the console.
     * @param s the string to print
     */
    public void print(String s) { System.out.println(s); }

    /**
     * Prompts the user to select a player character and returns the selection.
     * @return the selected player character index
     */
    public int getCharacterSelection() {
        return getPlayerChoice();
    }

    /**
     * Prompts the user for an action and returns the valid action character.
     * @return the action character entered by the user
     */
    public char getUserAction() {
        String userInput = "";
        while(!(isValidActionChar(userInput))) {
            userInput = input.read().toLowerCase();
        }
        return userInput.charAt(0);
    }


    /**
     * Prompts the user to select a valid player character index.
     * @return the selected player character index
     */
    private int getPlayerChoice() {
        int userChoice = -1;
        while (isInvalidCharacterChoice(userChoice)){
            selectionPrint();
            try {
                userChoice = Integer.parseInt(input.read());
                if(isInvalidCharacterChoice(userChoice)) {
                    System.out.printf("Invalid number. Please choose between 1 and %d.%n",TileFactory.getPlayerCount());
                }
            } catch (NumberFormatException e) {
                System.out.println("Not a number.");
            }
        }
        return userChoice;
    }

    /**
     * Prints the player selection prompt and available player descriptions.
     */
    private void selectionPrint() {
        print("Select player:");
        print(TileFactory.PlayersDescription());
    }

    /**
     * Checks if the given string is a valid action character.
     * @param str the input string to check
     * @return true if the string is a valid action character, false otherwise
     */
    private boolean isValidActionChar(String str) {
        return str != null && str.length() == 1 && VALID_ACTION_KEYS.indexOf(str.charAt(0)) != -1;
    }

    /**
     * Checks if the given choice is an invalid player character index.
     * @param choice the player character index to check
     * @return true if the choice is invalid, false otherwise
     */
    private boolean isInvalidCharacterChoice(int choice) {
        return choice < MIN_INDEX_CHARACTER || choice > TileFactory.getPlayerCount();
    }

    /**
     * Reads and returns the next line of user input.
     * @return the user input as a string
     */
    private String getInput() { return scanner.nextLine(); }
}