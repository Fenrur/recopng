package zitoune;

import fr.unistra.pelican.Image;

import java.util.Comparator;
import java.util.List;

public class RecoService {

    private final RecoRepository repository;
    private final int discretizeBatch;

    public RecoService(RecoRepository repository, int discretizeBatch) {
        this.repository = repository;
        this.discretizeBatch = discretizeBatch;
    }

    public record Rank(double distance, RecoRepository.Entry entry) {}

    private List<Rank> rankBy(Histogram histogram, int count) {
        return repository
                .entries()
                .parallelStream()
                .map(entry -> new Rank(histogram.calculateEuclideanDistance(entry.histogram()), entry))
                .filter(rank -> rank.distance > 0)
                .sorted(Comparator.comparingDouble(o -> o.distance))
                .limit(count)
                .toList();
    }

    private Image transformImage(Image image) {
        return Utils.buildRGBtoHSV(Utils.medianFilter(image));
    }

    public List<Rank> rankBy(Image image, int count) {
        final Histogram histogram = Histogram
                .calculateHistogram(transformImage(image))
                .discretize(discretizeBatch)
                .normalize(image.getXDim() * image.getYDim());

        return rankBy(histogram, count);
    }

    public void put(String imageName, Image image) {
        final Histogram histogram = Histogram
                .calculateHistogram(transformImage(image))
                .discretize(discretizeBatch)
                .normalize(image.getXDim() * image.getYDim());

        repository.put(imageName, histogram, image);
    }
}
