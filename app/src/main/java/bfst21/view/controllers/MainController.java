package bfst21.view.controllers;

import bfst21.data.BinaryFileManager;
import bfst21.exceptions.MapDataNotLoadedException;
import bfst21.models.*;
import bfst21.osm.Node;
import bfst21.osm.UserNode;
import bfst21.osm.Way;
import bfst21.view.ColorMode;
import bfst21.view.MapCanvas;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventType;
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
import java.io.IOException;
import java.util.Objects;


public class MainController {

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
    private TextField userNodeTextField;
    @FXML
    private VBox userNodeClickedVBox;
    @FXML
    private Text userNodeClickedText;
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

    private boolean resetDjikstra = true;

    private boolean userNodeToggle = false;
    ImageCursor userNodeCursorImage = new ImageCursor(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("cursor_transparent.png"))));
    UserNode currentUserNode = null;

    private Model model;
    private Point2D lastMouse;
    private final DisplayOptions displayOptions = DisplayOptions.getInstance();

    public void updateZoomBox() {
        debugBoxController.setZoomPercent("Zoom percent: " + canvas.getZoomPercent());
        debugBoxController.setZoomText("Zoom level: " + canvas.getZoomLevelText());
        updateAverageRepaintTime();
        updateNodeSkipAmount();
    }

    public void updateNodeSkipAmount() {
        debugBoxController.setNodeSkipAmount("Node skip: " + Way.getNodeSkipAmount(canvas.getZoomLevel()));
    }

    public void updateAverageRepaintTime() {
        debugBoxController.setRepaintTime("Repaint time: " + canvas.getAverageRepaintTime());
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
        //StackPane.setAlignment(startBox, Pos.BOTTOM_RIGHT);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        updateZoomBox();

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.D && event.isControlDown()) {
                showHideDebug();
            }
        });
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
            float[] queryCoords = new float[]{(float) point.getX(), (float) point.getY()};
            canvas.runNearestNeighborTask(queryCoords);
        }

        if (userNodeToggle && mouseEvent.isSecondaryButtonDown()) {
            userNodeVBox.setVisible(true);
            userNodeTextField.requestFocus();
            scene.setCursor(Cursor.DEFAULT);
        }

        if (mouseEvent.isShiftDown() && mouseEvent.isPrimaryButtonDown()) {
            Point2D point = canvas.mouseToModelCoords(lastMouse);
            float[] queryCoords = new float[]{(float) point.getX(), (float) point.getY()};
            float[] nearestCoords = model.getMapData().kdTreeNearestNeighborSearch(queryCoords);

            if (resetDjikstra) {
                resetDjikstra = false;
                model.getMapData().originCoords = nearestCoords;
                model.getMapData().destinationCoords = null;
            } else {
                model.getMapData().destinationCoords = nearestCoords;
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
            startBox.setVisible(false);
            startBox.setManaged(false);
            loadingText.setVisible(false);
            searchBoxController.setVisible(true);
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
        fileChooser.setInitialDirectory(new File("./"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Obj file, OSM file, ZIP file", "*.obj; *.osm; *.zip")
        );
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
                startBox.setVisible(false);
                startBox.setManaged(false);
                loadingText.setVisible(false);
                searchBoxController.setVisible(true);
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
            scene.setCursor(userNodeCursorImage);
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
        Point2D point = canvas.mouseToModelCoords(lastMouse);
        UserNode userNode = new UserNode((float) point.getX(), (float) point.getY(), userNodeTextField.getText());

        model.getMapData().addUserNode(userNode);
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
    public void saveObjFile(ActionEvent actionEvent) throws Exception {
        String fileName = model.getFileName();
        if(fileName.endsWith(".obj")) {
            PopupControl popupBox = new PopupControl();
            Text warningText = new Text("You're currently using an OBJ file. Are you sure you want to save another OBJ file?");
            Button continueButton = new Button("Continue");
            Button cancelButton = new Button("Cancel");
            popupBox.show(new Stage());

            String msg = "Cannot save OBJ file when the loaded file is OBJ";
            System.out.println(msg);
            //throw new Exception(msg);
        }
        else {
            FileChooser fileSaver = new FileChooser();
            fileSaver.setTitle("Save to OBJ");
            fileSaver.setInitialDirectory(new File("./"));
            fileSaver.getExtensionFilters().addAll((
                    new FileChooser.ExtensionFilter("OBJ file", ".obj"))
            );
            File file = fileSaver.showSaveDialog(new Stage());
            if(file != null) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws IOException {
                        BinaryFileManager binaryFileManager = new BinaryFileManager();
                        MapData mapData = model.getMapData();

                        long time = -System.nanoTime();
                        //TODO: Måske vi skal ændre i saveOBJ() metoden så man ikke skal give en tom String med
                        binaryFileManager.saveOBJ(file.getAbsolutePath(), "", mapData);
                        time += System.nanoTime();
                        System.out.println("Saved .obj file in: " + time / 1_000_000 + "ms to " + file.getAbsolutePath());
                        return null;
                    }
                };
                task.setOnFailed(event -> task.getException().printStackTrace());
                Thread thread = new Thread(task);
                thread.start();
            }
        }
    }

    public void checkDebug(ActionEvent actionEvent){
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
}