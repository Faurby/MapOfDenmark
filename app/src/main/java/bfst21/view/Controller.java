package bfst21.view;

import bfst21.address.Address;
import bfst21.models.Option;
import bfst21.models.Options;
import bfst21.models.Model;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

public class Controller {

    private Model model;
    private Point2D lastMouse;
    private final Options options = Options.getInstance();

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

    public void updateZoomBox() {
        zoomPercent.setText("Zoom percent: " + canvas.getZoomPercent());
        zoomText.setText("Zoom level: " + canvas.getZoomLevel());
    }

    public void init(Model model) throws IOException {
        this.model = model;
        canvas.init(model);
        stackPane.setAlignment(debugBox, Pos.TOP_RIGHT);
        updateZoomBox();

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.D && event.isControlDown()) {
                if (debugOptions.isVisible()) {
                    debugOptions.setVisible(false);
                } else {
                    debugOptions.setVisible(true);
                }
            }
        });
    }

    public void onWindowResize(Stage stage) {
        stackPane.setAlignment(debugBox, Pos.TOP_RIGHT);
        searchAddressVbox.setMaxWidth(stage.getWidth() * 0.25);
        System.out.println("StackPane width: " + stackPane.getWidth());
        System.out.println("StackPane height: " + stackPane.getHeight());
        canvas.repaint();
    }

    @FXML
    private void onScroll(ScrollEvent e) {
        double deltaY = 0;
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
    public void loadDefault() throws XMLStreamException, IOException, ClassNotFoundException {
        canvas.load(true);
        updateZoomBox();
    }

    @FXML
    public void loadNewFile() throws IOException, XMLStreamException, ClassNotFoundException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load new map segment");
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            String filename = file.getAbsolutePath();
            model.setFileName(filename);
            canvas.load(false);
            updateZoomBox();
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
        if (actionEvent.toString().contains("expand")) {
            if (startingPoint.getText() != null) {
                destinationPoint.setText(startingPoint.getText());
                startingPoint.setText("");
            }
            getDestinationBox.setVisible(true);
            getDestinationBox.setManaged(true);
            startingPointText.setVisible(true);
            startingPointText.setManaged(true);
            startingPoint.setPromptText("Choose a starting point...");
            expandAndSearchButtons.setVisible(false);
            expandAndSearchButtons.setManaged(false);
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
}
