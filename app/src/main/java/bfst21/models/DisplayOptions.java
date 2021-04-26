package bfst21.models;

import java.util.HashMap;


public class DisplayOptions {

    private final HashMap<DisplayOption, Boolean> options = new HashMap<>();

    private static DisplayOptions instance;

    private DisplayOptions() {
        options.put(DisplayOption.DISPLAY_KD_TREE, false);
        options.put(DisplayOption.DISPLAY_GRAPH, false);
        options.put(DisplayOption.DISPLAY_DIJKSTRA, false);
        options.put(DisplayOption.SAVE_OBJ_FILE, false);
    }

    //Returns the current value for a specific option
    //All settings are true by default
    public boolean getBool(DisplayOption displayOption) {
        if (options.containsKey(displayOption)) {
            return options.get(displayOption);
        }
        return true;
    }

    public void toggle(DisplayOption displayOption) {
        options.put(displayOption, !getBool(displayOption));
    }

    /**
     * Creates an instance of Options if it does not exist yet
     * @return singleton instance of Options
     */
    public static DisplayOptions getInstance() {
        if (instance == null) {
            instance = new DisplayOptions();
        }
        return instance;
    }
}
