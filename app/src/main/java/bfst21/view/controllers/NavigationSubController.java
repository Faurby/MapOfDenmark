package bfst21.view.controllers;

public abstract class NavigationSubController extends SubController{

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public boolean isVisible() {
        return isVisible;
    }

}
