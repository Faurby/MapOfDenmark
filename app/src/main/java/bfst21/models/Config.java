package bfst21.models;

import java.io.IOException;
import java.util.Properties;


public class Config {

    private final Properties configProps;

    public Config() throws IOException {
        configProps = new Properties();
        configProps.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
    }

    public Properties getProps() {
        return configProps;
    }

    public String getProp(String key) {
        return configProps.getProperty(key);
    }
}
