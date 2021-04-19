package bfst21.view;

import bfst21.address.Address;
import bfst21.exceptions.MapDataNotLoadedException;
import bfst21.models.*;
import bfst21.osm.Node;
import bfst21.osm.UserNode;
import bfst21.osm.Way;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Objects;


public class Controller {

    @FXML
    private MapCanvas canvas;
    @FXML
    private VBox searchAddressVbox;
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
    private TextArea startingPoint;
    @FXML
    private TextArea destinationPoint;
    @FXML
    private VBox DestinationBox;
    @FXML
    private HBox expandAndSearchButtons;
    @FXML
    private Text startingPointText;
    @FXML
    private Scene scene;
    @FXML
    private VBox loadingText;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Button expandButton;
    @FXML
    private Button collapseButton;
    @FXML
    private VBox userNodeVBox;
    @FXML
    private TextField userNodeTextField;
    @FXML
    private Button switchButton;
    @FXML
    private Button searchButton;
    @FXML
    private Button searchButtonExpanded;
    @FXML
    private ToggleButton CAR;
    @FXML
    private ToggleButton BIKE;
    @FXML
    private ToggleButton WALK;
    @FXML
    private VBox userNodeClickedVBox;
    @FXML
    private Text userNodeClickedText;
    @FXML
    private VBox userNodeNewDescriptionVBox;
    @FXML
    private TextField userNodeNewDescriptionTextField;

    private boolean userNodeToggle = false;
    ImageCursor userNodeCursorImage = new ImageCursor(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("cursor_transparent.png"))));
    UserNode currentUserNode = null;

    private Model model;
    private Point2D lastMouse;
    private final Options options = Options.getInstance();

    public void updateZoomBox() {
        zoomPercent.setText("Zoom percent: " + canvas.getZoomPercent());
        zoomText.setText("Zoom level: " + canvas.getZoomLevelText());
        updateAverageRepaintTime();
        updateNodeSkipAmount();
    }

    public void updateNodeSkipAmount() {
        nodeSkipAmount.setText("Node skip: "+ Way.getNodeSkipAmount(canvas.getZoomLevel()));
    }

    public void updateAverageRepaintTime() {
        repaintTime.setText("Repaint time: " + canvas.getAverageRepaintTime());
    }

    public void init(Model model) {
        this.model = model;
        canvas.init(model);
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
        searchAddressVbox.setMaxWidth(stage.getWidth() * 0.25D);
        stage.getHeight();
        canvas.repaint();
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
    public void onMouseReleased(MouseEvent e) {
        canvas.runRangeSearchTask();

        float mouseX = (float) canvas.mouseToModelCoords(lastMouse).getX();
        float mouseY = (float) canvas.mouseToModelCoords(lastMouse).getY();
        Node tempNodeAtMouseCoords = new Node(mouseX, -(mouseY * 0.56f));
        UserNode closestNode = null;
        if(!model.getMapData().getUserNodes().isEmpty()) {
            for(UserNode userNode : model.getMapData().getUserNodes()) {
                System.out.println("Distance to nearest UserNode: " + userNode.distance(tempNodeAtMouseCoords)); //TODO: remove this at some point
                if(closestNode == null || tempNodeAtMouseCoords.distance(userNode) < tempNodeAtMouseCoords.distance(closestNode)) {
                    closestNode = userNode;
                }
            }
            if(tempNodeAtMouseCoords.distance(closestNode) < 0.025) {
                System.out.println("close enough to trigger a click :))");  //TODO: remove this at some point
                userNodeClickedVBox.setVisible(true);
                userNodeClickedText.setText((closestNode.getDescription().equals("") ? "No description entered" : closestNode.getDescription()));
                currentUserNode = closestNode;
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
        
        if (userNodeToggle && mouseEvent.isSecondaryButtonDown()) {
            userNodeVBox.setVisible(true);
            userNodeTextField.requestFocus();
            scene.setCursor(Cursor.DEFAULT);
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
        if (actionEvent.toString().contains("zoomIn")) {
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

        } else if (buttonClicked.contains("blackwhite")) {
            canvas.setColorMode(ColorMode.BLACK_WHITE);

        } else if (buttonClicked.contains("colorblind")) {
            canvas.setColorMode(ColorMode.COLOR_BLIND);
        }
        canvas.repaint();
    }

    @FXML
    public void searchAddressString() {
        String sAddress = startingPoint.getText();
        Address parsedSA = Address.parse(sAddress);

        String dAddress = destinationPoint.getText();
        Address parsedDA = Address.parse(dAddress);

        System.out.println(parsedSA.toString());
        System.out.println(parsedDA.toString());
    }

    @FXML
    public void expandSearchView(ActionEvent actionEvent) {
        if (actionEvent.toString().contains("expandButton")) {
            if (startingPoint.getText() != null) {
                destinationPoint.setText(startingPoint.getText());
                startingPoint.setText("");
            }
            DestinationBox.setVisible(true);
            DestinationBox.setManaged(true);
            startingPointText.setVisible(true);
            startingPointText.setManaged(true);
            startingPoint.setPromptText("From:");
            expandAndSearchButtons.setVisible(false);
            expandAndSearchButtons.setManaged(false);
            //To avoid any TextArea being activated
            DestinationBox.requestFocus();
        } else {
            if (!destinationPoint.getText().equals("") && startingPoint.getText().equals("")) {
                startingPoint.setText(destinationPoint.getText());
            }
            DestinationBox.setVisible(false);
            DestinationBox.setManaged(false);
            startingPointText.setVisible(false);
            startingPointText.setManaged(false);
            startingPoint.setPromptText("Choose an address...");
            expandAndSearchButtons.setVisible(true);
            expandAndSearchButtons.setManaged(true);
        }
    }

    public void switchText() {
        String s = startingPoint.getText();
        startingPoint.setText(destinationPoint.getText());
        destinationPoint.setText(s);
    }

    public void onCheckDebug(ActionEvent actionEvent) {
        String text = actionEvent.toString().toLowerCase();

        for (Option option : Option.values()) {
            String optionText = option.toString().toLowerCase().replaceAll("_", " ");
            if (text.contains(optionText)) {
                options.toggle(option);
                canvas.repaint();
                break;
            }
        }
    }

    public void transportationButtonPushed(ActionEvent actionEvent) {
        TransportationOptions transOptions = new TransportationOptions();

        if (actionEvent.getSource().toString().contains("WALK")) {
            transOptions.chooseType(TransportationOption.WALK);
            WALK.setSelected(true);
        } else if (actionEvent.getSource().toString().contains("BIKE")) {
            transOptions.chooseType(TransportationOption.BIKE);
            BIKE.setSelected(true);
        } else {
            transOptions.chooseType(TransportationOption.CAR);
            CAR.setSelected(true);
        }
        System.out.println(transOptions.returnType().toString());

    }

    public void tabEnterCheck(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.TAB) {
            if (keyEvent.getSource().toString().contains("startingPoint")) {
                startingPoint.setText(startingPoint.getText().trim());
                if (!DestinationBox.isVisible()) {
                    expandButton.requestFocus();
                } else {
                    switchButton.requestFocus();
                }
            } else if (keyEvent.getSource().toString().contains("destinationPoint")) {
                destinationPoint.setText(destinationPoint.getText().trim());
                startingPoint.setText(startingPoint.getText().trim());
                collapseButton.requestFocus();
            }
        } else if (keyEvent.getCode() == KeyCode.ENTER) {
            startingPoint.setText(startingPoint.getText().trim());
            destinationPoint.setText(destinationPoint.getText().trim());
            searchAddressString();
            if (DestinationBox.isVisible()) {
                searchButtonExpanded.requestFocus();
            } else {
                searchButton.requestFocus();
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
    public void userNodeDeleteClicked(ActionEvent actionEvent) {
        if(currentUserNode == null) throw new NullPointerException("currentUserNode is null");
        else {
            model.getMapData().getUserNodes().remove(currentUserNode);
            currentUserNode = null;
            userNodeClickedVBox.setVisible(false);
            canvas.repaint();
        }
    }

    @FXML
    public void userNodeCloseClicked(ActionEvent actionEvent) {
        userNodeClickedVBox.setVisible(false);
        currentUserNode = null;
    }

    @FXML
    public void userNodeChangeDescriptionClicked(ActionEvent actionEvent) {
        userNodeNewDescriptionVBox.setVisible(true);
        userNodeNewDescriptionTextField.requestFocus();
    }

    @FXML
    public void userNodeNewDescTextFieldKeyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode().getName().equals("Enter")) {
            currentUserNode.changeDescription(userNodeNewDescriptionTextField.getText());
            userNodeNewDescriptionVBox.setVisible(false);
        } else if (keyEvent.getCode().getName().equals("Esc")) {
            userNodeNewDescriptionVBox.setVisible(false);
        }
    }

    @FXML
    public void userNodeNewDescCancelClicked(ActionEvent actionEvent) {
        userNodeNewDescriptionVBox.setVisible(false);
    }

    @FXML
    public void userNodeNewDescSaveClicked(ActionEvent actionEvent) {
        currentUserNode.changeDescription(userNodeNewDescriptionTextField.getText());
        userNodeNewDescriptionVBox.setVisible(false);
    }
}