package bfst21.view;

import bfst21.exceptions.MapDataNotLoadedException;
import bfst21.models.*;
import bfst21.osm.Node;
import bfst21.osm.UserNode;
import bfst21.osm.Way;
import bfst21.pathfinding.Vertex;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Map;
import java.util.Objects;


public class Controller {

    @FXML
    private MapCanvas canvas;
    @FXML
    private StackPane stackPane;
    @FXML
    private VBox debugBox;
    @FXML
    private VBox debugOptions;
    @FXML
    private Text zoomText;
    @FXML
    private Text zoomPercent;
    @FXML
    private Text repaintTime;
    @FXML
    private Text nodeSkipAmount;
    @FXML
    private Scene scene;
    @FXML
    private VBox loadingText;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private VBox userNodeVBox;
    @FXML
    private TextField userNodeTextField;
    @FXML
    private VBox userNodeClickedVBox;
    @FXML
    private Text userNodeClickedText;
    @FXML
    private VBox userNodeNewDescriptionVBox;
    @FXML
    private TextField userNodeNewDescriptionTextField;
    private Node source;
    private Node destination;
    private boolean resetDjikstra = true;

    private boolean userNodeToggle = false;
    ImageCursor userNodeCursorImage = new ImageCursor(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("cursor_transparent.png"))));
    UserNode currentUserNode = null;

    @FXML
    private SearchBoxController searchBoxController;

    private Model model;
    private Point2D lastMouse;
    private final DisplayOptions displayOptions = DisplayOptions.getInstance();

    public void updateZoomBox() {
        zoomPercent.setText("Zoom percent: " + canvas.getZoomPercent());
        zoomText.setText("Zoom level: " + canvas.getZoomLevelText());
        updateAverageRepaintTime();
        updateNodeSkipAmount();
    }

    public void updateNodeSkipAmount() {
        nodeSkipAmount.setText("Node skip: " + Way.getNodeSkipAmount(canvas.getZoomLevel()));
    }

    public void updateAverageRepaintTime() {
        repaintTime.setText("Repaint time: " + canvas.getAverageRepaintTime());
    }

    public MapCanvas getCanvas() {
        return canvas;
    }

    public void init(Model model) {
        this.model = model;
        canvas.init(model);

        searchBoxController.setController(this);

        StackPane.setAlignment(debugBox, Pos.TOP_RIGHT);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        updateZoomBox();

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.D && event.isControlDown()) {
                debugOptions.setVisible(!debugOptions.isVisible());
            }
        });
    }

    public void onWindowResize(Stage stage) {
        StackPane.setAlignment(debugBox, Pos.TOP_RIGHT);
        stage.getHeight();
        canvas.repaint();
        searchBoxController.onWindowResize(stage);
        //searchBoxController.setMaxWidth(stage.getWidth() * 0.25D);
    }

    @FXML
    private void onScroll(ScrollEvent scrollEvent) {
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
            float mouseX = (float) canvas.mouseToModelCoords(lastMouse).getX();
            float mouseY = (float) canvas.mouseToModelCoords(lastMouse).getY();

            Node tempNodeAtMouseCoords = new Node(mouseX, -(mouseY * 0.56f));
            UserNode closestNode = null;

            if (!model.getMapData().getUserNodes().isEmpty()) {
                for (UserNode userNode : model.getMapData().getUserNodes()) {
                    if (closestNode == null || tempNodeAtMouseCoords.distTo(userNode) < tempNodeAtMouseCoords.distTo(closestNode)) {
                        closestNode = userNode;
                    }
                }
                if (closestNode != null) {
                    if (tempNodeAtMouseCoords.distTo(closestNode) < 0.025) {
                        userNodeClickedVBox.setVisible(true);
                        userNodeClickedText.setText((closestNode.getDescription().equals("") ? "No description entered" : closestNode.getDescription()));
                        currentUserNode = closestNode;
                    }
                }
            }
        }
    }

    @FXML
    private void onMouseDragged(MouseEvent mouseEvent) {
        double dx = mouseEvent.getX() - lastMouse.getX();
        double dy = mouseEvent.getY() - lastMouse.getY();

        if (mouseEvent.isPrimaryButtonDown()) {
            canvas.pan(dx, dy);
        }
        onMousePressed(mouseEvent);
    }

    @FXML
    private void onMousePressed(MouseEvent mouseEvent) {
        lastMouse = new Point2D(mouseEvent.getX(), mouseEvent.getY());
        updateAverageRepaintTime();

        if (mouseEvent.isSecondaryButtonDown()) {
            Point2D point = canvas.mouseToModelCoords(lastMouse);
            Node node = new Node((float) point.getX(), (float) -point.getY() * 0.56f);
            canvas.runNearestNeighborTask(node);
        }

        if (userNodeToggle && mouseEvent.isSecondaryButtonDown()) {
            userNodeVBox.setVisible(true);
            userNodeTextField.requestFocus();
            scene.setCursor(Cursor.DEFAULT);
        }

        if (mouseEvent.isShiftDown() && mouseEvent.isPrimaryButtonDown()) {
            Point2D point = canvas.mouseToModelCoords(lastMouse);
            Node node = new Node((float) point.getX(), (float) -point.getY() * 0.56f);
            Node nearestNode = model.getMapData().kdTreeNearestNeighborSearch(node);
            Vertex vertex = model.getMapData().getDirectedGraph().getVertex(nearestNode.getX(), nearestNode.getY());
            if (resetDjikstra) {
                resetDjikstra = false;
                model.getMapData().setOriginVertex(vertex);
                model.getMapData().setDestinationVertex(null);
            } else {
                model.getMapData().setDestinationVertex(vertex);
                model.getMapData().runDijkstra();
                resetDjikstra = true;
            }

        }
    }

    @FXML
    public void loadDefault() {

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                loadingText.setVisible(true);
                canvas.load(true);
                updateZoomBox();
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            loadingText.setVisible(false);
            canvas.runRangeSearchTask();
        });
        task.setOnFailed(e -> task.getException().printStackTrace());
        Thread thread = new Thread(task);
        thread.start();
    }

    @FXML
    public void loadNewFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load new map segment");
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            String filename = file.getAbsolutePath();
            model.setFileName(filename);
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    loadingText.setVisible(true);
                    canvas.load(false);
                    updateZoomBox();
                    return null;
                }
            };
            task.setOnSucceeded(e -> {
                loadingText.setVisible(false);
                canvas.runRangeSearchTask();
            });
            task.setOnFailed(e -> task.getException().printStackTrace());
            Thread thread = new Thread(task);
            thread.start();
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
        String buttonClicked = actionEvent.toString().toLowerCase();

        if (buttonClicked.contains("standard")) {
            canvas.setColorMode(ColorMode.STANDARD);

        } else if (buttonClicked.contains("blackandwhite")) {
            canvas.setColorMode(ColorMode.BLACK_WHITE);

        } else if (buttonClicked.contains("colorblind")) {
            canvas.setColorMode(ColorMode.COLOR_BLIND);
        }
        canvas.repaint();
    }

    public void onCheckDebug(ActionEvent actionEvent) {
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
    public void userNodeButtonClicked() throws MapDataNotLoadedException {
        if (model.getMapData() == null) {
            throw new MapDataNotLoadedException("No MapData has been loaded. MapData is null.");
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
        if (keyEvent.getCode().getName().equals("Enter")) {
            saveUserNode();
        } else if (keyEvent.getCode().getName().equals("Esc")) {
            userNodeVBox.setVisible(false);
        }
    }

    @FXML
    public void userNodeCancelClicked() {
        userNodeVBox.setVisible(false);
        scene.setCursor(userNodeCursorImage);
    }

    @FXML
    public void userNodeSaveClicked() {
        saveUserNode();
    }

    private void saveUserNode() {
        float mouseX = (float) canvas.mouseToModelCoords(lastMouse).getX();
        float mouseY = (float) canvas.mouseToModelCoords(lastMouse).getY();
        model.getMapData().addUserNode(new UserNode(mouseX, -(mouseY * 0.56f), userNodeTextField.getText()));
        scene.setCursor(Cursor.DEFAULT);
        userNodeToggle = false;
        userNodeVBox.setVisible(false);
        canvas.repaint();
    }

    @FXML
    public void userNodeDeleteClicked() {
        if (currentUserNode == null) {
            throw new NullPointerException("currentUserNode is null");

        } else {
            model.getMapData().getUserNodes().remove(currentUserNode);
            currentUserNode = null;
            userNodeClickedVBox.setVisible(false);
            canvas.repaint();
        }
    }

    @FXML
    public void userNodeCloseClicked() {
        userNodeClickedVBox.setVisible(false);
        currentUserNode = null;
    }

    @FXML
    public void userNodeChangeDescriptionClicked() {
        userNodeNewDescriptionVBox.setVisible(true);
        userNodeNewDescriptionTextField.requestFocus();
    }

    @FXML
    public void userNodeNewDescTextFieldKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().getName().equals("Enter")) {
            currentUserNode.changeDescription(userNodeNewDescriptionTextField.getText());
            userNodeNewDescriptionVBox.setVisible(false);

        } else if (keyEvent.getCode().getName().equals("Esc")) {
            userNodeNewDescriptionVBox.setVisible(false);
        }
    }

    @FXML
    public void userNodeNewDescCancelClicked() {
        userNodeNewDescriptionVBox.setVisible(false);
    }

    @FXML
    public void userNodeNewDescSaveClicked() {
        currentUserNode.changeDescription(userNodeNewDescriptionTextField.getText());
        userNodeNewDescriptionVBox.setVisible(false);
    }
}