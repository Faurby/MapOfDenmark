package bfst21.view.controllers;


public abstract class SubController {

    protected MainController mainController;
    protected boolean isVisible;

    public void setController(MainController mainController) {
        this.mainController = mainController;
    }
}