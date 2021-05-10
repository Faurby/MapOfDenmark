package bfst21.models;

import java.util.HashMap;


/**
 * DisplayOptions is a singleton.
 * Used to determine if certain elements should be visible to the user.
 */
public class DisplayOptions {

    private final HashMap<DisplayOption, Boolean> options = new HashMap<>();
    private static DisplayOptions instance;

    /**
     * @return current boolean value of specific DisplayOption.
     * All settings will return true by default unless
     * something else has been specified in the constructor.
     */
    public boolean getBool(DisplayOption displayOption) {
        if (options.containsKey(displayOption)) {
            return options.get(displayOption);
        }
        return true;
    }

    /**
     * Toggle specific DisplayOption
     */
    public void toggle(DisplayOption displayOption) {
        options.put(displayOption, !getBool(displayOption));
    }

    /**
     * Creates an instance of DisplayOptions if it does not exist yet
     *
     * @return singleton instance of DisplayOptions
     */
    public static DisplayOptions getInstance() {
        if (instance == null) {
            instance = new DisplayOptions();
        }
        return instance;
    }
}
