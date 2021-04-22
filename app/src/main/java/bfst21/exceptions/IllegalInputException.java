package bfst21.exceptions;

public class IllegalInputException extends RuntimeException {

    public IllegalInputException(String str) {
        super(str);
    }

    public IllegalInputException(String str, String extra) {
        super(str + ": " + extra);
    }
}