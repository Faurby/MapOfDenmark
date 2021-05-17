package bfst21.view.controllers;

import bfst21.data.BinaryFileManager;
import bfst21.models.*;
import bfst21.osm.*;
import bfst21.pathfinding.DirectedGraph;
import bfst21.pathfinding.Edge;
import bfst21.view.ColorMode;
import bfst21.view.MapCanvas;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class MainController extends BaseController {

    @FXML
    private AnchorPane navigationBox;
    @FXML
    private HBox menuBarHBox;
    @FXML
    private MapCanvas canvas;
    @FXML
    private StackPane stackPane;
    @FXML
    private VBox debugBox;
    @FXML
    private VBox startBox;
    @FXML
    private Scene scene;
    @FXML
    private VBox loadingScreenBox;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private VBox userNodeVBox;
    @FXML
    private ListView<String> userNodeListView;
    @FXML
    private VBox newUserNodeVBox;
    @FXML
    private TextField userNodeNameText;
    @FXML
    private TextField userNodeDescriptionText;
    @FXML
    private VBox userNodeClickedVBox;
    @FXML
    private Text userNodeClickedCoords;
    @FXML
    private Text userNodeClickedName;
    @FXML
    private Text userNodeClickedDescription;
    @FXML
    private VBox userNodeNewNameVBox;
    @FXML
    private TextField userNodeNewNameText;
    @FXML
    private VBox userNodeNewDescriptionVBox;
    @FXML
    private TextField userNodeNewDescriptionText;
    @FXML
    private NavigationBoxController navigationBoxController;
    @FXML
    private DebugBoxController debugBoxController;
    @FXML
    private StartBoxController startBoxController;
    @FXML
    private Text zoomPercentText;
    @FXML
    private StackPane zoomBox_outer;
    @FXML
    private GridPane footerGridPane;
    @FXML
    private Text nearestRoadText;

    private int counter;
    private String nameWithHighestCount = "";

    private boolean resetDijkstra = true;

    private boolean userNodeToggle = false;
    private UserNode currentUserNode = null;

    private final ImageCursor userNodeCursorImage = new ImageCursor(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("images/cursor_orange.png"))));
    private final ObservableList<UserNode> userNodeListItems = FXCollections.observableArrayList();
    private HashMap<String, UserNode> userNodesMap = new HashMap<>();

    private Model model;
    private Point2D lastMouse;

    private Point2D currentMousePos;
    private Task<Void> roadTask;

    private final DisplayOptions displayOptions = DisplayOptions.getInstance();

    private void updateZoomBox() {
        int nodeSkip = MapWay.getNodeSkipAmount(canvas.getZoomLevel());

        zoomPercentText.setText(canvas.getZoomPercent());
        debugBoxController.setZoomText("Zoom level: " + canvas.getZoomLevelText());
        debugBoxController.setNodeSkipAmount("Node skip: " + nodeSkip);

        updateAverageRepaintTime();
    }

    public void changeZoomToShowPoints(float[] startCoords, float[] destinationCoords) {
        float dx = Math.abs(startCoords[0] - destinationCoords[0]);
        float dy = Math.abs(startCoords[1] - destinationCoords[1]);
        double distPoints = Math.sqrt((dx * dx) + (dy * dy));

        float screenMinY = canvas.getScreenBoundingBox(false).getMinY();
        float screenMaxY = canvas.getScreenBoundingBox(false).getMaxY();

        double screenHeight = screenMinY - screenMaxY;
        double zf = Math.abs(screenHeight) / (distPoints * 1.8);
        Point2D point = new Point2D(stackPane.getWidth() / 2, stackPane.getHeight() / 2);

        canvas.zoom(zf, point, false);
        updateZoomBox();
    }

    private void updateAverageRepaintTime() {
        debugBoxController.setRepaintTime("Repaint time: " + canvas.getAverageRepaintTime());
    }

    private void updateMouseCoords(Point2D currentMousePos) {
        double currentPosX = canvas.mouseToModelCoords(currentMousePos).getX();
        double currentPosY = canvas.mouseToModelCoords(currentMousePos).getY();

        String xFormatted = String.format(Locale.ENGLISH, "%.7f", currentPosX);
        String yFormatted = String.format(Locale.ENGLISH, "%.7f", currentPosY);

        debugBoxController.setMouseCoords("Mouse coords: " + xFormatted + ", " + yFormatted);

        String yFormattedRealWorld = String.format(Locale.ENGLISH, "%.7f", -1 * currentPosY * 0.56);
        debugBoxController.setMouseCoordsRealWorld("Real coords: " + xFormatted + ", " + yFormattedRealWorld);
    }

    public MapCanvas getCanvas() {
        return canvas;
    }

    public void init(Model model) {
        this.model = model;
        canvas.init(model);

        navigationBoxController.setController(this);
        debugBoxController.setController(this);
        startBoxController.setController(this);

        StackPane.setAlignment(debugBox, Pos.TOP_RIGHT);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        updateZoomBox();

        userNodeListView.setOnMouseClicked(event -> {
            userNodeClickedInListView(userNodeListView.getSelectionModel().getSelectedItem());
        });
    }

    private void userNodeClickedInListView(String userNodeName) {
        if (userNodeName != null) {
            UserNode clickedUserNode = userNodesMap.get(userNodeName);
            Pin.USER_NODE.setCoords(clickedUserNode.getCoords());
            Pin.USER_NODE.setVisible(true);

            canvas.changeView(clickedUserNode.getX(), clickedUserNode.getY());
            userNodeListView.getSelectionModel().clearSelection();
        }
    }

    @FXML
    public void toggleDebugVisibility() {
        debugBox.setVisible(!debugBox.isVisible());
    }

    public void onWindowResize(Stage stage) {
        StackPane.setAlignment(debugBox, Pos.TOP_RIGHT);
        stage.getHeight();
        canvas.repaint();
        navigationBoxController.onWindowResize(stage);
    }

    @FXML
    public void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.D && event.isControlDown()) {
            toggleDebugVisibility();

        } else if (event.getCode() == KeyCode.ESCAPE) {
            if (userNodeToggle) {
                userNodeToggle = false;
                scene.setCursor(Cursor.DEFAULT);
            }
        }
    }

    @FXML
    public void onScroll(ScrollEvent scrollEvent) {
        double deltaY;

        //Limit delta Y to avoid a rapid zoom update
        if (scrollEvent.getDeltaY() > 0.0D) {
            deltaY = 32.0D;
        } else {
            deltaY = -32.0D;
        }
        double factor = Math.pow(1.01D, deltaY);

        Point2D point = new Point2D(scrollEvent.getX(), scrollEvent.getY());

        canvas.zoom(factor, point, false);
        updateZoomBox();
    }

    @FXML
    public void onMouseReleased() {
        canvas.runRangeSearchTask();

        if (model.getMapData() != null) {
            if (!model.getMapData().getUserNodes().isEmpty()) {

                Point2D point = canvas.mouseToModelCoords(lastMouse);
                Node nodeAtMouse = new Node((float) point.getX(), (float) point.getY());
                UserNode closestNode = null;

                for (UserNode userNode : model.getMapData().getUserNodes()) {
                    //If closestNode is null, any Node could be the closest node
                    if (closestNode == null || nodeAtMouse.distTo(userNode) < nodeAtMouse.distTo(closestNode)) {
                        closestNode = userNode;
                    }
                }
                if (closestNode != null) {
                    //We need to use getTrans().determinant() so it works for different zoom levels
                    double distanceRequired = 840.0D * (1.0D / Math.sqrt(canvas.getTrans().determinant()));

                    //Is user clicking near a UserNode?
                    if (nodeAtMouse.distTo(closestNode) < distanceRequired) {
                        userNodeClickedVBox.setVisible(true);
                        userNodeClickedVBox.requestFocus();
                        userNodeClickedName.setText(closestNode.getName());
                        userNodeClickedDescription.setText((closestNode.getDescription().equals("") ? "No description entered" : closestNode.getDescription()));
                        String x = String.format(Locale.ENGLISH, "%.6f", closestNode.getX());
                        String y = String.format(Locale.ENGLISH, "%.6f", (-1f * closestNode.getY() * 0.56f)); //Convert to "real" coords
                        userNodeClickedCoords.setText(x + ", " + y);
                        currentUserNode = closestNode;
                    }
                }
            }
        }
    }

    @FXML
    public void onMouseDragged(MouseEvent mouseEvent) {
        double dx = mouseEvent.getX() - lastMouse.getX();
        double dy = mouseEvent.getY() - lastMouse.getY();

        if (mouseEvent.isPrimaryButtonDown()) {
            canvas.pan(dx, dy);
        }
        lastMouse = new Point2D(mouseEvent.getX(), mouseEvent.getY());
        updateAverageRepaintTime();
    }

    @FXML
    public void onMousePressed(MouseEvent mouseEvent) {
        lastMouse = new Point2D(mouseEvent.getX(), mouseEvent.getY());
        updateAverageRepaintTime();

        if (userNodeToggle && mouseEvent.isPrimaryButtonDown()) {
            newUserNodeVBox.setVisible(true);
            scene.setCursor(Cursor.DEFAULT);
        }

        if (mouseEvent.isShiftDown() && mouseEvent.isPrimaryButtonDown()) {
            TransportOptions transportOptions = TransportOptions.getInstance();
            TransportOption currentTransportOption = transportOptions.getCurrentlyEnabled();

            Point2D point = canvas.mouseToModelCoords(lastMouse);
            float[] queryCoords = new float[]{(float) point.getX(), (float) point.getY()};
            float[] nearestCoords = model.getMapData().kdTreeNearestNeighborSearch(queryCoords, currentTransportOption);

            if (resetDijkstra) {
                canvas.resetCurrentRoute();
                navigationBoxController.clearRoute();

                resetDijkstra = false;
                canvas.originCoords = nearestCoords;
                canvas.destinationCoords = null;

                Pin.ORIGIN.setVisible(true);
                Pin.ORIGIN.setCoords(nearestCoords);

                Pin.DESTINATION.setVisible(false);
                canvas.repaint();

            } else {
                canvas.destinationCoords = nearestCoords;
                navigationBoxController.runDijkstraTask();
                resetDijkstra = true;
            }
        }
    }

    @FXML
    public void loadDefault() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                startLoadingFile();
                canvas.load(true);
                updateZoomBox();
                return null;
            }
        };
        task.setOnSucceeded(e -> finishedLoadingFile());
        task.setOnFailed(e -> task.getException().printStackTrace());
        Thread thread = new Thread(task);
        thread.start();
    }

    @FXML
    public void loadNewFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load new map segment");
        fileChooser.setInitialDirectory(new File("./"));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("OBJ file, OSM file, ZIP file", "*.obj; *.osm; *.zip")
        );
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            String filename = file.getAbsolutePath();
            model.setFileName(filename);

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    startLoadingFile();
                    canvas.load(false);
                    updateZoomBox();
                    return null;
                }
            };
            task.setOnSucceeded(e -> finishedLoadingFile());
            task.setOnFailed(e -> task.getException().printStackTrace());
            Thread thread = new Thread(task);
            thread.start();
        }
    }

    private void startLoadingFile() {
        userNodeVBox.setVisible(false);
        loadingScreenBox.setVisible(true);
        menuBarHBox.setVisible(false);
        footerGridPane.setVisible(false);
        zoomBox_outer.setVisible(false);

        Pin.disableAllPins();
    }

    private void finishedLoadingFile() {
        if (!model.failedToLoadFile()) {
            startBox.setVisible(false);
            startBox.setManaged(false);
            loadingScreenBox.setVisible(false);
            navigationBoxController.setSearchBoxVisible(true);
            userNodeVBox.setVisible(true);
            footerGridPane.setVisible(true);
            zoomBox_outer.setVisible(true);
            canvas.runRangeSearchTask();
            menuBarHBox.setVisible(true);

            if (model.getMapData() != null) {
                updateUserNodeList();
            }
            canvas.repaint();

        } else {
            displayAlert(Alert.AlertType.ERROR, "Error", "Failed to load file. File does not exist!");
        }
    }

    @FXML
    public void zoomButtonClicked(ActionEvent actionEvent) {
        Point2D point = new Point2D(stackPane.getWidth() / 2.0D, stackPane.getHeight() / 2.0D);

        if (actionEvent.toString().toLowerCase().contains("zoomin")) {
            canvas.zoom(2.0D, point, false);
        } else {
            canvas.zoom(0.5D, point, false);
        }
        updateZoomBox();
    }

    @FXML
    public void changeColorMode(ActionEvent actionEvent) {
        String text = actionEvent.toString().toLowerCase();

        for (ColorMode colorMode : ColorMode.values()) {
            String optionText = colorMode.toString().toLowerCase().replaceAll("_", "");

            if (text.contains(optionText)) {
                canvas.setColorMode(colorMode);
                canvas.repaint();
                break;
            }
        }

        if (text.contains("standard")) {
            scene.getStylesheets().setAll(
                    getClass().getResource("/styles/userNodes_default.css").toExternalForm(),
                    getClass().getResource("/styles/misc_default.css").toExternalForm()
            );
            debugBox.getStylesheets().setAll(getClass().getResource("/styles/debug_default.css").toExternalForm());
            navigationBox.getStylesheets().setAll(getClass().getResource("/styles/navigation_default.css").toExternalForm());

        } else if (text.contains("darkmode")) {
            scene.getStylesheets().setAll(
                    getClass().getResource("/styles/userNodes_darkmode.css").toExternalForm(),
                    getClass().getResource("/styles/misc_darkmode.css").toExternalForm()
            );
            debugBox.getStylesheets().setAll(getClass().getResource("/styles/debug_darkmode.css").toExternalForm());
            navigationBox.getStylesheets().setAll(getClass().getResource("/styles/navigation_darkmode.css").toExternalForm());

        } else if (text.contains("colorblind")) {
            //TODO: add color blind colors
        }
    }

    @FXML
    public void userNodeButtonClicked() {
        if (model.getMapData() == null) {
            displayAlert(Alert.AlertType.ERROR,
                    "Error",
                    "ERROR: MapData is null",
                    "No MapData has been loaded.");
        }
        if (userNodeToggle) {
            userNodeToggle = false;
            scene.setCursor(Cursor.DEFAULT);
        } else {
            userNodeToggle = true;
            scene.setCursor(userNodeCursorImage);
        }
    }

    @FXML
    public void userNodeClickedVBoxOnKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            userNodeClickedVBox.setVisible(false);

        } else if (keyEvent.getCode() == KeyCode.DELETE
                || keyEvent.getCode() == KeyCode.BACK_SPACE) {

            userNodeDeleteClicked();
        }
    }

    @FXML
    public void userNodeTextKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            newUserNodeCheckNameAndSave();

        } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            newUserNodeVBox.setVisible(false);
            scene.setCursor(Cursor.DEFAULT);
            userNodeToggle = false;
        }
    }

    @FXML
    public void userNodeSaveClicked() {
        newUserNodeCheckNameAndSave();
    }

    private void newUserNodeCheckNameAndSave() {
        String textField = userNodeNameText.getText();
        if (textField.isEmpty()) {
            displayAlert(Alert.AlertType.INFORMATION,
                    "Error",
                    "A name is required");
            userNodeNameText.requestFocus();

        } else if (textField.length() > 15) {
            displayAlert(Alert.AlertType.INFORMATION,
                    "Error",
                    "Names must be no longer than 15 characters");
            userNodeNameText.requestFocus();

        } else if (userNodesMap.containsKey(textField)) {
            displayAlert(Alert.AlertType.INFORMATION,
                    "Error",
                    "Point of Interest names must be unique");
            userNodeNameText.requestFocus();

        } else {
            saveUserNode();
        }
    }

    @FXML
    public void userNodeCancelClicked() {
        newUserNodeVBox.setVisible(false);
        scene.setCursor(Cursor.DEFAULT);
        userNodeToggle = false;
    }

    private void saveUserNode() {
        Point2D point = canvas.mouseToModelCoords(lastMouse);

        double x = point.getX() + (15.0D / canvas.getZoomLevel());
        double y = point.getY() + (30.0D / canvas.getZoomLevel());
        UserNode userNode = new UserNode((float) x, (float) y, userNodeNameText.getText(), userNodeDescriptionText.getText());

        model.getMapData().addUserNode(userNode);
        model.getMapData().updateUserNodesMap();

        scene.setCursor(Cursor.DEFAULT);
        userNodeToggle = false;
        newUserNodeVBox.setVisible(false);
        updateUserNodeList();
        userNodeNameText.setText("");
        canvas.repaint();
    }

    private void updateUserNodeList() {
        userNodeListItems.setAll(model.getMapData().getUserNodes());
        ObservableList<String> tempList = FXCollections.observableArrayList();

        userNodesMap = model.getMapData().getUserNodesMap();

        for (UserNode userNode : userNodeListItems) {
            tempList.add(userNode.getName());
        }
        userNodeListView.setItems(tempList);
        userNodeListView.setVisible(!userNodeListItems.isEmpty());

        if (userNodeListItems.size() == 1) {
            userNodeListView.setMaxHeight(27.0D);
            userNodeListView.setMinHeight(27.0D);

        } else if (userNodeListItems.size() < 4) {
            userNodeListView.setMaxHeight(userNodeListItems.size() * 25.0D);
            userNodeListView.setMinHeight(userNodeListItems.size() * 25.0D);

        } else {
            userNodeListView.setMaxHeight(85.0D);
            userNodeListView.setMinHeight(85.0D);
        }
    }

    @FXML
    public void userNodeDeleteClicked() {
        if (currentUserNode != null) {

            //Set Pin.USER_NODE to invisible if the coords are the same as the UserNode coords.
            if (Pin.USER_NODE.isVisible()) {
                float[] pinCoords = Pin.USER_NODE.getCoords();
                float[] userNodeCoords = currentUserNode.getCoords();

                if (pinCoords[0] == userNodeCoords[0] && pinCoords[1] == userNodeCoords[1]) {
                    Pin.USER_NODE.setVisible(false);
                }
            }

            model.getMapData().getUserNodes().remove(currentUserNode);
            model.getMapData().updateUserNodesMap();
            currentUserNode = null;
            userNodeClickedVBox.setVisible(false);
            updateUserNodeList();
            canvas.repaint();
        }
    }

    @FXML
    public void userNodeCloseClicked() {
        userNodeClickedVBox.setVisible(false);
        currentUserNode = null;
    }

    @FXML
    public void userNodeChangeNameClicked() {
        userNodeNewNameVBox.setVisible(true);
        userNodeNewNameText.requestFocus();
    }

    @FXML
    public void userNodeChangeDescriptionClicked() {
        userNodeNewDescriptionVBox.setVisible(true);
        userNodeNewDescriptionText.requestFocus();
    }

    @FXML
    public void userNodeNewNameTextKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            userNodeNewNameCheckNameAndSave();

        } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            userNodeNewNameVBox.setVisible(false);
        }
    }

    @FXML
    public void userNodeNewDescTextKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            currentUserNode.setDescription(userNodeNewDescriptionText.getText());
            userNodeNewDescriptionVBox.setVisible(false);
            userNodeNewDescriptionText.setText("");
            userNodeClickedDescription.setText(currentUserNode.getDescription());

        } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            userNodeNewDescriptionVBox.setVisible(false);
        }
    }

    @FXML
    public void userNodeNewNameCancelClicked() {
        userNodeNewNameVBox.setVisible(false);
    }

    @FXML
    public void userNodeNewDescCancelClicked() {
        userNodeNewDescriptionVBox.setVisible(false);
    }

    @FXML
    public void userNodeNewNameSaveClicked() {
        userNodeNewNameCheckNameAndSave();
    }

    private void userNodeNewNameCheckNameAndSave() {
        String textField = userNodeNewNameText.getText();

        if (textField.isEmpty()) {
            displayAlert(Alert.AlertType.INFORMATION,
                    "Error",
                    "A name is required");
            userNodeNewNameText.requestFocus();

        } else if (textField.length() > 15) {
            displayAlert(Alert.AlertType.INFORMATION,
                    "Error",
                    "Names must be no longer than 15 characters");
            userNodeNewNameText.requestFocus();

        } else if (userNodesMap.containsKey(textField)) {
            displayAlert(Alert.AlertType.INFORMATION,
                    "Error", "Point of Interest names must be unique");
            userNodeNewNameText.requestFocus();

        } else {
            currentUserNode.setName(userNodeNewNameText.getText());
            userNodeNewNameVBox.setVisible(false);
            userNodeNewNameText.setText("");
            userNodeClickedName.setText(currentUserNode.getName());
            updateUserNodeList();
            model.getMapData().getUserNodesMap().put(currentUserNode.getName(), currentUserNode);
        }
    }

    @FXML
    public void userNodeNewDescSaveClicked() {
        currentUserNode.setDescription(userNodeNewDescriptionText.getText());
        userNodeNewDescriptionVBox.setVisible(false);
        userNodeNewDescriptionText.setText("");
        userNodeClickedDescription.setText(currentUserNode.getDescription());
    }

    @FXML
    public void preSaveObjFile() {
        String fileName = model.getFileName();

        if (fileName.endsWith(".obj")) {
            String contentText = "You're currently using an OBJ file. Are you sure you want to save another OBJ file?";
            Alert alert = displayAlert(Alert.AlertType.CONFIRMATION, "Confirmation", contentText);

            if (alert.getResult() == ButtonType.OK) {
                saveObjFile();
            }
        } else {
            saveObjFile();
        }
    }

    private void saveObjFile() {
        FileChooser fileSaver = new FileChooser();
        fileSaver.setTitle("Save to OBJ");
        fileSaver.setInitialDirectory(new File("./"));
        fileSaver.getExtensionFilters().addAll((
                new FileChooser.ExtensionFilter("OBJ file", ".obj")));

        File file = fileSaver.showSaveDialog(new Stage());

        if (file != null) {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws IOException {
                    BinaryFileManager binaryFileManager = new BinaryFileManager();
                    MapData mapData = model.getMapData();

                    long time = -System.nanoTime();
                    binaryFileManager.saveOBJ(file.getAbsolutePath(), mapData);
                    time += System.nanoTime();
                    System.out.println("Saved .obj file in: " + time / 1_000_000L + "ms to " + file.getAbsolutePath());
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                displayAlert(Alert.AlertType.INFORMATION,
                        "Success",
                        "Successfully saved OBJ");
            });
            task.setOnFailed(event -> task.getException().printStackTrace());
            Thread thread = new Thread(task);
            thread.start();
        }
    }

    public void checkDebug(ActionEvent actionEvent) {
        String text = actionEvent.toString().toLowerCase();

        for (DisplayOption displayOption : DisplayOption.values()) {
            String optionText = displayOption.toString().toLowerCase().replaceAll("_", " ");

            if (text.contains(optionText)) {
                displayOptions.toggle(displayOption);
                canvas.repaint();
                break;
            }
        }
    }

    @FXML
    public void onMouseMoved(MouseEvent mouseEvent) {

        currentMousePos = new Point2D(mouseEvent.getX(), mouseEvent.getY());
        updateMouseCoords(currentMousePos);

        if (model.getMapData() != null) {
            if (counter == 5) {
                updateRoadTask();
            }
            counter++;
        }
    }

    private void updateRoadTask() {
        if (roadTask != null) {
            if (roadTask.isRunning()) {
                roadTask.cancel();
            }
        }
        roadTask = new Task<>() {

            @Override
            protected Void call() {
                Point2D point = canvas.mouseToModelCoords(currentMousePos);
                float[] queryCoords = new float[]{(float) point.getX(), (float) point.getY()};
                float[] nearestCoords = model.getMapData().kdTreeNearestNeighborSearch(queryCoords, TransportOption.ALL);

                DirectedGraph graph = canvas.getModel().getMapData().getDirectedGraph();

                int nearestVertex = graph.getVertexID(nearestCoords);
                List<Edge> edgeList = graph.getAdjacentEdges(nearestVertex);
                Map<String, Integer> countMap = new HashMap<>();

                for (Edge edge : edgeList) {
                    if (edge.getName() != null) {
                        int count = 0;
                        if (countMap.containsKey(edge.getName())) {
                            count = countMap.get(edge.getName());
                        }
                        count++;
                        countMap.put(edge.getName(), count);
                    }
                }
                int highestCount = 0;
                nameWithHighestCount = "";

                for (String name : countMap.keySet()) {
                    int count = countMap.get(name);
                    if (count > highestCount) {
                        highestCount = count;
                        nameWithHighestCount = name;
                    }
                }
                counter = 0;
                return null;
            }
        };
        roadTask.setOnSucceeded(e -> nearestRoadText.setText(nameWithHighestCount));
        roadTask.setOnFailed(e -> roadTask.getException().printStackTrace());
        Thread thread = new Thread(roadTask);
        thread.start();
    }
}