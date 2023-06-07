package zitoune;

import fr.unistra.pelican.Image;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RecoRepository {

    public record Entry(Histogram histogram, String imageName, Image image) {

    }

    private final Map<String, Entry> entries;

    public RecoRepository() {
        this.entries = new ConcurrentHashMap<>();
    }

    public void clear() {
        entries.clear();
    }

    public void put(String imageName, Histogram histogram, Image image) {
        entries.put(imageName, new Entry(
                histogram,
                imageName,
                image)
        );
    }

    public Optional<Entry> get(String imageName) {
        return Optional.ofNullable(entries.get(imageName));
    }

    public List<Entry> entries() {
        return List.copyOf(entries.values());
    }
}
