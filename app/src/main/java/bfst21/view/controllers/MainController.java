package bfst21.view.controllers;

import bfst21.data.BinaryFileManager;
import bfst21.models.*;
import bfst21.osm.Node;
import bfst21.osm.Pin;
import bfst21.osm.UserNode;
import bfst21.osm.Way;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class MainController extends BaseController {

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
    private VBox loadingText;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private VBox userNodeVBox;
    @FXML
    private ListView<String> userNodeListView;
    @FXML
    private VBox newUserNodeVBox;
    @FXML
    private TextField userNodeNameTextField;
    @FXML
    private TextField userNodeDescriptionTextField;
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
    private TextField userNodeNewNameTextField;
    @FXML
    private VBox userNodeNewDescriptionVBox;
    @FXML
    private TextField userNodeNewDescriptionTextField;
    @FXML
    private SearchBoxController searchBoxController;
    @FXML
    private NavigationBoxController navigationBoxController;
    @FXML
    private DebugBoxController debugBoxController;
    @FXML
    private StartBoxController startBoxController;
    @FXML
    private Text zoomPercent;
    @FXML
    private GridPane footBox;
    @FXML
    private Text nearestRoadText;

    private int counter;
    private String nameWithHighestCount = "";

    private boolean resetDijkstra = true;

    private boolean userNodeToggle = false;
    private UserNode currentUserNode = null;

    private final ImageCursor userNodeCursorImage = new ImageCursor(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("cursor_transparent.png"))));
    private final ObservableList<UserNode> userNodeListItems = FXCollections.observableArrayList();
    private HashMap<String, UserNode> userNodesMap = new HashMap<>();

    private Model model;
    private Point2D lastMouse;
    private Task<Void> roadTask;

    private final DisplayOptions displayOptions = DisplayOptions.getInstance();

    public void updateZoomBox() {
        int nodeSkip = Way.getNodeSkipAmount(canvas.getZoomLevel());

        zoomPercent.setText(canvas.getZoomPercent());
        debugBoxController.setZoomText("Zoom level: " + canvas.getZoomLevelText());
        debugBoxController.setNodeSkipAmount("Node skip: " + nodeSkip);

        updateAverageRepaintTime();
    }

    public void changeZoomToShowPoints(float[] startCoords, float[] destinationCoords) {
        float dx = Math.abs(startCoords[0] - destinationCoords[0]);
        float dy = Math.abs(startCoords[1] - destinationCoords[1]);
        double distPoints = Math.sqrt((dx*dx)+(dy*dy));

        double screenHeight = getCanvas().getScreenBoundingBox(false).getMinY() - getCanvas().getScreenBoundingBox(false).getMaxY();
        double zf = Math.abs(screenHeight) / (distPoints * 1.8);
        Point2D point = new Point2D(stackPane.getWidth() / 2, stackPane.getHeight() / 2);

        getCanvas().zoom(zf, point, false);
        updateZoomBox();
    }

    public void updateAverageRepaintTime() {
        debugBoxController.setRepaintTime("Repaint time: " + canvas.getAverageRepaintTime());
    }

    public void updateMouseCoords(Point2D currentMousePos) {
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

        searchBoxController.setController(this);
        navigationBoxController.setController(this);
        debugBoxController.setController(this);
        startBoxController.setController(this);

        StackPane.setAlignment(debugBox, Pos.TOP_RIGHT);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        updateZoomBox();

        scene.setOnMouseMoved(event -> {
            Point2D currentMousePos = new Point2D(event.getX(), event.getY());
            updateMouseCoords(currentMousePos);
        });
        userNodeListView.setOnMouseClicked(event -> {
            userNodeClickedInListView(userNodeListView.getSelectionModel().getSelectedItem());
        });
    }

    private void userNodeClickedInListView(String userNodeName) {
        if (userNodeName != null) {
            UserNode clickedUserNode = userNodesMap.get(userNodeName);
            Pin.USER_NODE.setCoords(clickedUserNode.getX(), clickedUserNode.getY());
            Pin.USER_NODE.setVisible(true);

            canvas.changeView(clickedUserNode.getX(), clickedUserNode.getY());
            userNodeListView.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void showHideDebug() {
        debugBox.setVisible(!debugBox.isVisible());
    }

    public void onWindowResize(Stage stage) {
        StackPane.setAlignment(debugBox, Pos.TOP_RIGHT);
        stage.getHeight();
        canvas.repaint();
        searchBoxController.onWindowResize(stage);
        navigationBoxController.onWindowResize(stage);
    }

    @FXML
    public void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.D && event.isControlDown()) {
            showHideDebug();

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
                    // 300 is a number chosen by trial and error. It seems to fit perfectly.
                    if (nodeAtMouse.distTo(closestNode) < 300 * (1 / Math.sqrt(canvas.getTrans().determinant()))) {
                        userNodeClickedVBox.setVisible(true);
                        userNodeClickedName.setText(closestNode.getName());
                        userNodeClickedDescription.setText((closestNode.getDescription().equals("") ? "No description entered" : closestNode.getDescription()));
                        String x = String.format(Locale.ENGLISH, "%.6f", closestNode.getX());
                        String y = String.format(Locale.ENGLISH, "%.6f", (-1 * closestNode.getY() * 0.56));
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
            userNodeNameTextField.requestFocus();
            scene.setCursor(Cursor.DEFAULT);
        }

        if (mouseEvent.isShiftDown() && mouseEvent.isPrimaryButtonDown()) {
            Point2D point = canvas.mouseToModelCoords(lastMouse);
            float[] queryCoords = new float[]{(float) point.getX(), (float) point.getY()};
            float[] nearestCoords = model.getMapData().kdTreeNearestNeighborSearch(queryCoords);

            if (resetDijkstra) {
                resetDijkstra = false;
                model.getMapData().originCoords = nearestCoords;
                model.getMapData().destinationCoords = null;
            } else {
                model.getMapData().destinationCoords = nearestCoords;
                model.getMapData().runDijkstra();
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
        loadingText.setVisible(true);
    }

    private void finishedLoadingFile() {
        startBox.setVisible(false);
        startBox.setManaged(false);
        loadingText.setVisible(false);
        searchBoxController.setVisible(true);
        userNodeVBox.setVisible(true);
        footBox.setVisible(true);
        canvas.runRangeSearchTask();

        if (model.getMapData() != null) {
            updateUserNodeList();
        }
    }

    @FXML
    public void zoomButtonClicked(ActionEvent actionEvent) {
        Point2D point = new Point2D(stackPane.getWidth() / 2, stackPane.getHeight() / 2);

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
    public void userNodeTextFieldKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            newUserNodeCheckNameAndSave();
        } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            newUserNodeVBox.setVisible(false);
            scene.setCursor(userNodeCursorImage);
        }
    }

    @FXML
    public void userNodeSaveClicked() {
        newUserNodeCheckNameAndSave();
    }

    private void newUserNodeCheckNameAndSave() {
        String textField = userNodeNameTextField.getText();
        if (textField.isEmpty()) {
            displayAlert(Alert.AlertType.INFORMATION,
                    "Error",
                    "A name is required");

        } else if (textField.length() > 20) {
            displayAlert(Alert.AlertType.INFORMATION,
                    "Error",
                    "Names must be no longer than 20 characters");

        } else if (userNodesMap.containsKey(textField)) {
            displayAlert(Alert.AlertType.INFORMATION,
                    "Error",
                    "Point of Interest names must be unique");

        } else {
            saveUserNode();
        }
    }

    @FXML
    public void userNodeCancelClicked() {
        newUserNodeVBox.setVisible(false);
        scene.setCursor(userNodeCursorImage);
    }

    private void saveUserNode() {
        Point2D point = canvas.mouseToModelCoords(lastMouse);
        UserNode userNode = new UserNode((float) point.getX(), (float) point.getY(), userNodeNameTextField.getText(), userNodeDescriptionTextField.getText());

        model.getMapData().addUserNode(userNode);
        model.getMapData().updateUserNodesMap();

        scene.setCursor(Cursor.DEFAULT);
        userNodeToggle = false;
        newUserNodeVBox.setVisible(false);
        updateUserNodeList();
        userNodeNameTextField.setText("");
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

        if(userNodeListItems.size() == 1) {
            userNodeListView.setMaxHeight(27);
            userNodeListView.setMinHeight(27);

        } else if(userNodeListItems.size() < 4) {
            userNodeListView.setMaxHeight(userNodeListItems.size() * 25);
            userNodeListView.setMinHeight(userNodeListItems.size() * 25);
        } else {
            userNodeListView.setMaxHeight(85);
            userNodeListView.setMinHeight(85);
        }
    }

    @FXML
    public void userNodeDeleteClicked() {
        if (currentUserNode != null) {

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
        userNodeNewNameTextField.requestFocus();
    }

    @FXML
    public void userNodeChangeDescriptionClicked() {
        userNodeNewDescriptionVBox.setVisible(true);
        userNodeNewDescriptionTextField.requestFocus();
    }

    @FXML
    public void userNodeNewNameTextFieldKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            userNodeNewNameCheckNameAndSave();

        } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            userNodeNewNameVBox.setVisible(false);
        }
    }

    @FXML
    public void userNodeNewDescTextFieldKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            currentUserNode.setDescription(userNodeNewDescriptionTextField.getText());
            userNodeNewDescriptionVBox.setVisible(false);
            userNodeNewDescriptionTextField.setText("");
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
        String textField = userNodeNewNameTextField.getText();

        if (textField.isEmpty()) {
            displayAlert(Alert.AlertType.INFORMATION,
                    "Error",
                    "A name is required");

        } else if (textField.length() > 20) {
            displayAlert(Alert.AlertType.INFORMATION,
                    "Error",
                    "Names must be no longer than 20 characters");

        } else if (userNodesMap.containsKey(textField)) {
            displayAlert(Alert.AlertType.INFORMATION,
                    "Error", "Point of Interest names must be unique");

        } else {
            currentUserNode.setName(userNodeNewNameTextField.getText());
            userNodeNewNameVBox.setVisible(false);
            userNodeNewNameTextField.setText("");
            userNodeClickedName.setText(currentUserNode.getName());
            updateUserNodeList();
            model.getMapData().getUserNodesMap().put(currentUserNode.getName(), currentUserNode);
        }
    }

    @FXML
    public void userNodeNewDescSaveClicked() {
        currentUserNode.setDescription(userNodeNewDescriptionTextField.getText());
        userNodeNewDescriptionVBox.setVisible(false);
        userNodeNewDescriptionTextField.setText("");
        userNodeClickedDescription.setText(currentUserNode.getDescription());
    }

    public void setNavigationBoxVisible(boolean visible) {
        navigationBoxController.setVisible(visible);
    }

    public void setSearchBoxVisible(boolean visible) {
        searchBoxController.setVisible(visible);
    }

    public void setNavigationBoxAddressText(String address) {
        navigationBoxController.transferAddressText(address);
    }

    public void setSearchBoxAddressText(String address) {
        searchBoxController.transferAddressText(address);
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
                    System.out.println("Saved .obj file in: " + time / 1_000_000 + "ms to " + file.getAbsolutePath());
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
        lastMouse = new Point2D(mouseEvent.getX(), mouseEvent.getY());
        if (model.getMapData() != null) {
            if (counter == 5) {
                updateRoadTask();
            }
            counter++;
        }
    }

    public void updateRoadTask() {
        if (roadTask != null) {
            if (roadTask.isRunning()) {
                roadTask.cancel();
            }
        }
        roadTask = new Task<>() {

            @Override
            protected Void call() {
                Point2D point = canvas.mouseToModelCoords(lastMouse);
                float[] queryCoords = new float[]{(float) point.getX(), (float) point.getY()};
                float[] nearestCoords = model.getMapData().kdTreeNearestNeighborSearch(queryCoords);

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