package bfst21.models;


/**
 * TransportOptions is a singleton.
 * Used to determine which TransportOption is currently selected by the user.
 * TransportOption.CAR is the default enabled option.
 */
public class TransportOptions {

    private TransportOption currentlyEnabled = TransportOption.CAR;
    private static TransportOptions instance;

    public TransportOption getCurrentlyEnabled() {
        return currentlyEnabled;
    }

    public void setCurrentlyEnabled(TransportOption currentlyEnabled) {
        this.currentlyEnabled = currentlyEnabled;
    }

    /**
     * Creates an instance of TransportOptions if it does not exist yet
     *
     * @return singleton instance of TransportOptions
     */
    public static TransportOptions getInstance() {
        if (instance == null) {
            instance = new TransportOptions();
        }
        return instance;
    }
}
