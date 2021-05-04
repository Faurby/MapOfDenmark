package bfst21.view.controllers;


public abstract class SubController extends BaseController {

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