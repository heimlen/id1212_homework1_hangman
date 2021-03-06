package se.kth.id1212.heimlen.homework1.view;

import se.kth.id1212.heimlen.homework1.controller.Controller;
import se.kth.id1212.heimlen.homework1.exceptions.BadFormattedInputException;
import se.kth.id1212.heimlen.homework1.exceptions.UnknownCommandException;
import se.kth.id1212.heimlen.homework1.model.OutputHandler;

import java.io.IOException;
import java.util.Scanner;

/**
 * This class read and interpret user commands. This interpreter will run in a separate thread to allow multiple
 * users to simultaneously give commands to the interpreter. These threads will be picked from the existing thread pool in JDK.
 */
public class InputInterpreter implements Runnable {
    private static final String PROMPT = "> ";
    private final Scanner clientInput = new Scanner(System.in);
    private boolean acceptingClientCommands = false;
    private Controller controller;
    private final ThreadSafeStdOut msgOut = new ThreadSafeStdOut();

    /**
     * Starts the interpreter. The interpreter will be waiting for user input when this method
     * returns. Calling <code>start</code> on an interpreter that is already started has no effect.
     */
    public void start() {
        if (acceptingClientCommands) {
            return;
        }
        System.out.println(welcomeMsg());
        acceptingClientCommands = true;
        controller = new Controller();
        new Thread(this).start();
    }

    @Override
    public void run() {
        while(acceptingClientCommands) {
            try {
                UserInput userInput = new UserInput(readNextLine());
                switch(userInput.getUserCommand()) {
                    case QUIT :
                        acceptingClientCommands = false;
                        controller.disconnect();
                        break;
                    case CONNECT :
                        controller.connectToServer(userInput.getFirstParam(),
                                Integer.parseInt(userInput.getSecondParam()),
                                new ServerOutput());
                        break;
                    case GUESS :
                        controller.sendInput(userInput.getFirstParam());
                        break;
                }
            } catch (UnknownCommandException | BadFormattedInputException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("thanks for playing the game!");
            }
        }
    }

    private String readNextLine() {
        msgOut.print(PROMPT);
        return clientInput.nextLine();
    }

    private class ServerOutput implements OutputHandler {
        @Override
        public void printServerOutput(String output) {
            msgOut.println(output);
            msgOut.print(PROMPT);

        }
    }

    private String welcomeMsg() {
        return "Welcome to the hangman game!\n" +
                "please start off by connecting to a server, this is done by writing connect \"ip-adress\" \"port\"\n" +
                "you can then start guessing for the word by typing guess \"letter/word you want to guess\"\n" +
                "you can at any time quit the game by typing quit!";
    }
}
