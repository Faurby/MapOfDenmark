package bfst21.view;

public abstract class SubController {

    protected MainController mainController;
    protected boolean isVisible;

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setController(MainController mainController) {
        this.mainController = mainController;
    }
}