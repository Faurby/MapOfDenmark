package bfst21.vector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Model implements Iterable<Line> {
    List<Line> lines;

    public Model(String filename) throws IOException {
        long time = -System.nanoTime();
        lines = Files.lines(Path.of(filename)).map(Line::new).collect(Collectors.toList());
        time += System.nanoTime();
        Logger.getGlobal().info(String.format("Load time: %dms", time / 1000000));
    }

    @Override
    public Iterator<Line> iterator() {
        return lines.iterator();
    }
}
