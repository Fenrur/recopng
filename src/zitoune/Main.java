package zitoune;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
//        Viewer2D.exec(Utils.medianFilter(ImageLoader.exec("/Users/livio/Downloads/motos/189.jpg")), "MEDIAN");
//        Viewer2D.exec((ImageLoader.exec("/Users/livio/Downloads/motos/189.jpg")), "NORMAL");
//        Utils.buildRGBtoHSV();

        final Image cross = ImageLoader.exec("/Users/livio/Downloads/motos/095.jpg");

        final Path folderMoto = Path.of("/Users/livio/Downloads/motos");

        final List<Utils.ImageWithPath> imageWithPaths = Utils.loadImagesFromFolder(folderMoto);

        final RecoService recoService = new RecoService(new RecoRepository(), 25);

        imageWithPaths
                .parallelStream()
                .forEach(imageWithPath -> recoService.put(imageWithPath.path().getFileName().toString(), imageWithPath.image()));

        final Scanner scanner = new Scanner(System.in);


        System.out.print("Nom de l'image (sans jpg): ");
        while (scanner.hasNextLine()) {
            try {
                final String s = scanner.nextLine();
                final Path moto = folderMoto.resolve(s + ".jpg");

                final var imageNames = recoService
                        .rankBy(ImageLoader.exec(moto.toAbsolutePath().toString()), 10)
                        .stream()
                        .map(rank -> rank.entry().imageName())
                        .toList();

                for (int i = 0; i < imageNames.size(); i++) {
                    final String imageName = imageNames.get(i);
                    System.out.println((i + 1) + ") " + imageName);
                }
            } catch (Exception ignored) {

            }

            System.out.print("Nom de l'image (sans jpg): ");
        }
    }
}