package bfst21.view.controllers;


public abstract class SubController {

    protected MainController mainController;

    public void setController(MainController mainController) {
        this.mainController = mainController;
    }
}