package bfst21.view.controllers;


public abstract class SubController extends BaseController {

    protected MainController mainController;

    public void setController(MainController mainController) {
        this.mainController = mainController;
    }
}