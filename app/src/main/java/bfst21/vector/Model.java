package bfst21.vector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Model implements Iterable<Line> {
    List<Line> lines;
    List<Runnable> observers = new ArrayList<>();

    public Model(String filename) throws IOException {
        long time = -System.nanoTime();
        lines = Files.lines(Path.of(filename)).map(Line::new).collect(Collectors.toList());
        time += System.nanoTime();
        Logger.getGlobal().info(String.format("Load time: %dms", time / 1000000));
    }

    void addObserver(Runnable observer) {
        observers.add(observer);
    }

    void notifyObservers() {
        for (var observer : observers) observer.run();
    }

    @Override
    public Iterator<Line> iterator() {
        return lines.iterator();
    }

	public void add(Line line) {
        lines.add(line);
        notifyObservers();
	}
}
