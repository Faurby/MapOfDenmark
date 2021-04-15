package bfst21.view;

import bfst21.address.Address;
import bfst21.models.*;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;


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
    private TextArea startingPoint;
    @FXML
    private TextArea destinationPoint;
    @FXML
    private VBox getDestinationBox;
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

    private Model model;
    private Point2D lastMouse;
    private final Options options = Options.getInstance();

    public void updateZoomBox() {
        zoomPercent.setText("Zoom percent: " + canvas.getZoomPercent());
        zoomText.setText("Zoom level: " + canvas.getZoomLevel());
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
        searchAddressVbox.setMaxWidth(stage.getWidth() * 0.25);
        canvas.repaint();
    }

    @FXML
    private void onScroll(ScrollEvent e) {
        double deltaY;
        if (e.getDeltaY() > 0) {
            deltaY = 32;
        } else {
            deltaY = -32;
        }
        double factor = Math.pow(1.01, deltaY);
        canvas.preZoom(factor, new Point2D(e.getX(), e.getY()));
        updateZoomBox();
    }

    @FXML
    public void onMouseReleased() {
        canvas.runRangeSearchTask();
    }

    @FXML
    private void onMouseDragged(MouseEvent e) {
        double dx = e.getX() - lastMouse.getX();
        double dy = e.getY() - lastMouse.getY();

        if (e.isPrimaryButtonDown()) {
            canvas.pan(dx, dy);

        } else {
            Point2D from = canvas.mouseToModelCoords(lastMouse);
            Point2D to = canvas.mouseToModelCoords(new Point2D(e.getX(), e.getY()));
            model.add(new Line(from, to));
            canvas.repaint();
        }
        onMousePressed(e);
    }

    @FXML
    private void onMousePressed(MouseEvent e) {
        lastMouse = new Point2D(e.getX(), e.getY());
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
        task.setOnSucceeded(e -> loadingText.setVisible(false));
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
                    System.out.println(Thread.currentThread().getName());
                    canvas.load(false);
                    updateZoomBox();
                    return null;
                }
            };
            task.setOnSucceeded(e -> loadingText.setVisible(false));
            Thread thread = new Thread(task);
            thread.start();
        }
    }

    @FXML
    public void zoomButtonClicked(ActionEvent actionEvent) {

        if (actionEvent.toString().contains("zoomIn")) {
            canvas.preZoom(2.0, new Point2D(stackPane.getWidth() / 2, stackPane.getHeight() / 2));
        } else {
            canvas.preZoom(0.50, new Point2D(stackPane.getWidth() / 2, stackPane.getHeight() / 2));
        }

        //TODO her er begyndelsen på et fix af zoomIndikatoren oppe til højre når man bruger zoom knappen.
        // PT er den ret scuffed. Jeg er ikke sikker på functionen af 'factor'.
        double factor = 1;
        canvas.preZoom(factor, new Point2D(stackPane.getWidth() / 2, stackPane.getHeight() / 2));
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
            getDestinationBox.setVisible(true);
            getDestinationBox.setManaged(true);
            startingPointText.setVisible(true);
            startingPointText.setManaged(true);
            startingPoint.setPromptText("From:");
            expandAndSearchButtons.setVisible(false);
            expandAndSearchButtons.setManaged(false);
            //To avoid any TextArea being activated
            getDestinationBox.requestFocus();
        } else {
            if (!destinationPoint.getText().equals("") && startingPoint.getText().equals("")) {
                startingPoint.setText(destinationPoint.getText());
            }
            getDestinationBox.setVisible(false);
            getDestinationBox.setManaged(false);
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
        } else if (actionEvent.getSource().toString().contains("BIKE")) {
            transOptions.chooseType(TransportationOption.BIKE);
        } else {
            transOptions.chooseType(TransportationOption.CAR);
        }
        System.out.println(transOptions.returnType().toString());
    }

    public void tabCheck(KeyEvent event) {
        if (event.getCode() == KeyCode.TAB) {
            if (event.getSource().toString().contains("startingPoint")) {
                startingPoint.setText(startingPoint.getText().trim());
                if (getDestinationBox.isVisible()) {
                    destinationPoint.requestFocus();
                } else {
                    expandButton.requestFocus();
                }
            } else if (event.getSource().toString().contains("destinationPoint")) {
                destinationPoint.setText(destinationPoint.getText().trim());
                startingPoint.setText(startingPoint.getText().trim());
                collapseButton.requestFocus();
            }
        }
    }
}
