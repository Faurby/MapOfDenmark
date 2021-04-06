package bfst21.models;

import java.util.HashMap;

public class Options {

    private final HashMap<Option, Boolean> options = new HashMap<>();

    private static Options instance;

    private Options() {
        options.put(Option.DISPLAY_KD_TREE, false);
    }

    //Returns the current value for a specific option
    //All settings are true by default
    public boolean getBool(Option option) {
        if (options.containsKey(option)) {
            return options.get(option);
        }
        return true;
    }

    public void toggle(Option option) {
        options.put(option, !getBool(option));
    }

    /**
     * Creates an instance of Options if it does not exist yet
     * @return singleton instance of Options
     */
    public static Options getInstance() {
        if (instance == null) {
            instance = new Options();
        }
        return instance;
    }
}
