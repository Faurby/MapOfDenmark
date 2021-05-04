package bfst21.view.controllers;

import bfst21.address.TST;
import bfst21.models.MapData;
import bfst21.models.Model;
import bfst21.osm.OsmAddress;
import bfst21.view.MapCanvas;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class NavigationSubController extends SubController {

    protected boolean isVisible;

    private List<OsmAddress> allSuggestions = new ArrayList<>();
    private List<OsmAddress> allSuggestionsDestSpecific = new ArrayList<>();
    private List<String> shownSuggestions = new ArrayList<>();
    private List<String> shownSuggestionsDestSpecific = new ArrayList<>();
    private TST<List<OsmAddress>> addressTries;
    private Task<Void> addressSuggestionTask;

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public boolean isVisible() {
        return isVisible;
    }

    protected void displayAddressSuggestions(VBox suggestions, TextArea textArea, boolean extended) {
        int count = 0;
        suggestions.getChildren().clear();

        List<String> localShownSuggestions;
        if (extended){
            localShownSuggestions = shownSuggestionsDestSpecific;
        } else {
            localShownSuggestions = shownSuggestions;
        }

        for (String s : localShownSuggestions) {
            if (count <= 500) {
                Label b = new Label(s);
                b.setPrefWidth(800);
                b.setOnMouseClicked((event) -> {
                    textArea.setText(b.getText());
                    suggestions.getChildren().clear();
                    textArea.requestFocus();
                });
                b.setOnMouseEntered((event) -> {
                    b.setStyle("-fx-background-color:#dae7f3;");
                });
                b.setOnMouseExited((event) -> {
                    b.setStyle("-fx-background-color: transparent;");
                });
                suggestions.getChildren().add(b);
                count++;
            }
        }
    }

    protected void checkTries(){
        if (addressTries == null) {
            activateTries();
        }
    }

    private void activateTries() {
        MapCanvas mapCanvas = mainController.getCanvas();
        Model model = mapCanvas.getModel();
        MapData mapData = model.getMapData();
        addressTries = mapData.getAddressTries();
    }

    protected void runAddressSuggestionTask(VBox suggestions, TextArea textArea, boolean extended) {
        if (addressSuggestionTask != null) {
            if (addressSuggestionTask.isRunning()) {
                addressSuggestionTask.cancel();
            }
        }
        addressSuggestionTask = new Task<>() {
            @Override
            protected Void call() {
                if (addressTries == null) {
                    activateTries();
                }

                List<OsmAddress> localAllSuggestions;
                if (extended) {
                    shownSuggestionsDestSpecific = new ArrayList<>();
                    localAllSuggestions = allSuggestionsDestSpecific;
                } else {
                    shownSuggestions = new ArrayList<>();
                    localAllSuggestions = allSuggestions;
                }

                String input = textArea.getText();
                String addressInput = input.replace(" ", "").toLowerCase();

                Iterator<String> it = addressTries.keysWithPrefix(addressInput).iterator();

                //If the string doesn't return matches, find a substring that does
                if (!it.hasNext()) {
                    addressInput = longestSubstringWithMatches(addressInput);
                    it = addressTries.keysWithPrefix(addressInput).iterator();
                }

                //A check to see whether a valid street name has been typed
                //This determines whether the suggested addresses are of streets or house numbers
                String streetName = null;
                if (it.hasNext()) {
                    String streetInfo = addressTries.keysWithPrefix(addressInput).iterator().next();
                    streetName = streetInfo.substring(0, streetInfo.length() - 4);
                }

                if (addressInput.equals(streetName)) {

                    if (it.hasNext()) {
                        localAllSuggestions = new ArrayList<>();
                        localAllSuggestions = addressTries.get(it.next());
                    }

                    if (localAllSuggestions.size() > 0) {
                        for (OsmAddress osmAddress : localAllSuggestions) {
                            String address = osmAddress.toString();

                            if (extended) {
                                shownSuggestionsDestSpecific.add(address);
                            } else {
                                shownSuggestions.add(address);
                            }
                        }
                    }
                } else {

                    localAllSuggestions = new ArrayList<>();

                    while (it.hasNext()) {
                        localAllSuggestions.add(addressTries.get(it.next()).get(0));
                    }
                    for (OsmAddress osmAddress : localAllSuggestions) {
                        String address = osmAddress.omitHouseNumberToString();

                        if (extended) {
                            shownSuggestionsDestSpecific.add(address);
                        } else {
                            shownSuggestions.add(address);
                        }
                    }
                }
                if (extended) {
                    allSuggestionsDestSpecific = localAllSuggestions;
                } else {
                    allSuggestions = localAllSuggestions;
                }
                return null;
            }
        };

        addressSuggestionTask.setOnSucceeded(e -> displayAddressSuggestions(suggestions, textArea, extended));
        addressSuggestionTask.setOnFailed(e -> addressSuggestionTask.getException().printStackTrace());
        Thread thread = new Thread(addressSuggestionTask);
        thread.start();
    }

    private String longestSubstringWithMatches(String addressInput) {
        Iterator<String> modifiedIt;
        boolean hasResults = true;
        int counter = 1;

        while (hasResults && counter < addressInput.length()) {
            modifiedIt = addressTries.keysWithPrefix(addressInput.substring(0, counter)).iterator();
            if (!modifiedIt.hasNext()) {
                hasResults = false;
                counter--;
            } else {
                counter++;
            }
        }

        return addressInput.substring(0, counter);
    }

    public List<String> getShownSuggestions() {
        return shownSuggestions;
    }

    public List<OsmAddress> getAllSuggestions() {
        return allSuggestions;
    }
    public List<OsmAddress> getAllSuggestionsDestSpecific() {
        return allSuggestionsDestSpecific;
    }

    public void setAllSuggestions(List<OsmAddress> allSuggestions) {
        this.allSuggestions = allSuggestions;
    }

    public void setAllSuggestionsDestSpecific(List<OsmAddress> allSuggestionsDestSpecific) {
        this.allSuggestionsDestSpecific = allSuggestionsDestSpecific;
    }
}
