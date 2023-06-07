package zitoune;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.util.Color;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class Utils {

    record ImageWithPath(Image image, Path path) {
    }

    public static Image buildRGBtoHSV(Image imageRGB) {
        final ByteImage imageHSB = new ByteImage(imageRGB.getXDim(), imageRGB.getYDim(), 1, 1, 3);
        float[] toHSBValues = new float[3];

        for (int x = 0; x < imageRGB.getXDim(); x++) {
            for (int y = 0; y < imageRGB.getYDim(); y++) {
                Color.RGBtoHSB(
                        imageRGB.getPixelXYBByte(x, y, 0),
                        imageRGB.getPixelXYBByte(x, y, 1),
                        imageRGB.getPixelXYBByte(x, y, 2),
                        toHSBValues
                );

                imageHSB.setPixelXYBByte(x, y, 0, (int) (toHSBValues[0] * 255.0));
                imageHSB.setPixelXYBByte(x, y, 1, (int) (toHSBValues[1] * 255.0));
                imageHSB.setPixelXYBByte(x, y, 2, (int) (toHSBValues[2] * 255.0));
            }
        }

        return imageHSB;
    }

    public static List<ImageWithPath> loadImagesFromFolder(Path pathFolder) {
        if (!Files.isDirectory(pathFolder)) {
            return List.of();
        }

        try {
            return Files.list(pathFolder)
                    .map(path -> new ImageWithPath(ImageLoader.exec(path.toAbsolutePath().toString()), path))
                    .toList();
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static double calculateMedian(List<Integer> values) {
        Collections.sort(values);

        if (values.size() % 2 == 1) return values.get((values.size() + 1) / 2 - 1);
        else {
            double lower = values.get(values.size() / 2 - 1);
            double upper = values.get(values.size() / 2);

            return (lower + upper) / 2.0;
        }
    }


    public static ByteImage medianFilter(Image input) {
        int width = input.getXDim();
        int height = input.getYDim();

        ByteImage newImgMedian = new ByteImage(input);

        for (int b = 0; b < input.getBDim(); b++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    List<Integer> list = new ArrayList<>();

                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (x + dx >= 0 && x + dx < width && y + dy >= 0 && y + dy < height) {
                                int neighboringValue = input.getPixelXYBByte(x + dx, y + dy, b);

                                list.add(neighboringValue);
                            }
                        }
                    }
                    int medianValue = (int) calculateMedian(list);
                    newImgMedian.setPixelXYBByte(x, y, b, medianValue);
                }
            }
        }


        return newImgMedian;
    }

    public static ByteImage stretching(Image from) {
        final ByteImage img = new ByteImage(from);

        double gMin = 255;
        double gMax = 0;

        for (int x = 0; x < img.getXDim(); x++) {
            for (int y = 0; y < img.getYDim(); y++) {
                final int pixelValue = img.getPixelXYBByte(x, y, 0);
                if (pixelValue > gMax) {
                    gMax = pixelValue;
                } else if (pixelValue < gMin) {
                    gMin = pixelValue;
                }
            }
        }

        final double diffExtremum = gMax - gMin;

        for (int x = 0; x < img.getXDim(); x++) {
            for (int y = 0; y < img.getYDim(); y++) {
                final double currentPixelValue = img.getPixelXYBByte(x, y, 0);

                final double newPixelValue = 255.0 * (currentPixelValue - gMin) / diffExtremum;
                img.setPixelXYBByte(x, y, 0, (int) newPixelValue);
            }
        }

        return img;
    }

    public static ByteImage buildByThreshold(Image from, int threshold) {
        final ByteImage img = new ByteImage(from);

        for (int b = 0; b < img.getBDim(); b++) {
            for (int x = 0; x < img.getXDim(); x++) {
                for (int y = 0; y < img.getYDim(); y++) {
                    final int pixelValue = img.getPixelXYBByte(x, y, b);
                    if (pixelValue <= threshold) {
                        img.setPixelXYBByte(x, y, b, 0);
                    } else {
                        img.setPixelXYBByte(x, y, b, 255);
                    }
                }
            }
        }

        return img;
    }

    public static ByteImage buildToGrey(Image from) {
        final int xDim = from.getXDim();
        final int yDim = from.getYDim();
        final ByteImage img = new ByteImage(xDim, yDim, 1, 1, 1);


        for (int x = 0; x < img.getXDim(); x++) {
            for (int y = 0; y < img.getYDim(); y++) {
                img.setPixelXYBByte(x, y, 0, from.getPixelXYBByte(x, y, 0));
            }
        }

        return img;
    }

    public static void plotHistogram(double[] histogram, String title) throws IOException {
        XYSeries myseries = new XYSeries("Nombre de pixels");
        for (int i = 0; i < histogram.length; i++) {
            myseries.add(i, histogram[i]);
        }
        XYSeriesCollection myseriescollection = new XYSeriesCollection(myseries);

        JFreeChart jfreechart = ChartFactory.createXYBarChart("Histogramme de l'image", "Niveaux de gris", false, "Nombre de pixels", myseriescollection, PlotOrientation.VERTICAL, true, false, false);
        jfreechart.setBackgroundPaint(Color.white);
        XYPlot xyplot = jfreechart.getXYPlot();

        xyplot.setBackgroundPaint(Color.lightGray);
        xyplot.setRangeGridlinePaint(Color.white);
        NumberAxis axis = (NumberAxis) xyplot.getDomainAxis();

        axis.setLowerMargin(0);
        axis.setUpperMargin(0);

        // create and display a frame...
        ChartFrame frame = new ChartFrame(title, jfreechart);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void saveHistogram(double[] histogram, String pathToSave) throws IOException {
        XYSeries myseries = new XYSeries("Nombre de pixels");
        for (int i = 0; i < histogram.length; i++) {
            myseries.add(i, histogram[i]);
        }
        XYSeriesCollection myseriescollection = new XYSeriesCollection(myseries);

        JFreeChart jfreechart = ChartFactory.createXYBarChart("Histogramme de l'image", "Niveaux de gris", false, "Nombre de pixels", myseriescollection, PlotOrientation.VERTICAL, true, false, false);
        jfreechart.setBackgroundPaint(Color.white);
        XYPlot xyplot = jfreechart.getXYPlot();

        xyplot.setBackgroundPaint(Color.lightGray);
        xyplot.setRangeGridlinePaint(Color.white);
        NumberAxis axis = (NumberAxis) xyplot.getDomainAxis();

        axis.setLowerMargin(0);
        axis.setUpperMargin(0);

        if (pathToSave != null) ChartUtilities.saveChartAsPNG(new File(pathToSave), jfreechart, 900, 600);
    }

    public static Regions calculateRegions(Image from, List<Point> s, int threshold) {
        int[][] copyGreyPixels = copyGreyPixels(from);

        final Regions regions = new Regions();

        for (Point seed : s) {
            regions.putPoint(seed, seed);

            Queue<Point> queue = new LinkedList<>();
            queue.add(seed);

            while (!queue.isEmpty()) {
                final Point p = queue.poll();
                final double averageGreyColorValue = regions
                        .getRegion(seed)
                        .parallelStream()
                        .mapToInt(value -> copyGreyPixels[value.x()][value.y()])
                        .average()
                        .orElseThrow();

                final int x = p.x();
                final int y = p.y();

                final List<PointWithValue> n = Stream.of(
                                getPoint(copyGreyPixels, x + 1, y),
                                getPoint(copyGreyPixels, x - 1, y),
                                getPoint(copyGreyPixels, x, y + 1),
                                getPoint(copyGreyPixels, x, y - 1)
//                                ,getPoint(copyImagePixelValues, x + 1, y + 1),
//                                getPoint(copyImagePixelValues, x + 1, y - 1),
//                                getPoint(copyImagePixelValues, x - 1, y - 1),
//                                getPoint(copyImagePixelValues, x - 1, y + 1)
                        )
                        .flatMap(Optional::stream)
                        .toList();

                for (PointWithValue pn : n) {
                    final Point pnp = pn.toPoint();
                    if (!regions.pointContains(pnp) && nearGreyColor((int) averageGreyColorValue, pn.greyColor(), threshold)) {
                        regions.putPoint(seed, pnp);
                        queue.add(pnp);
                    }
                }
            }
        }

        return regions;
    }

    private static int[][] copyGreyPixels(Image from) {
//        if (from.getBDim() != 1) throw new RuntimeException("Only 1 dim");
        int[][] copyImagePixelValues = new int[from.getXDim()][from.getYDim()];
        for (int x = 0; x < from.getXDim(); x++) {
            for (int y = 0; y < from.getYDim(); y++) {
                copyImagePixelValues[x][y] = from.getPixelXYBByte(x, y, 0);
            }
        }
        return copyImagePixelValues;
    }

    public static boolean nearGreyColor(int greyColor, int greyColorCompare, int threshold) {
        final int minimum = greyColor - threshold;
        final int maximum = greyColor + threshold;

        return minimum < greyColorCompare && greyColorCompare < maximum;
    }

    public static Optional<PointWithValue> getPoint(int[][] in, int x, int y) {
        try {
            return Optional.of(new PointWithValue(x, y, in[x][y]));
        } catch (Exception e) {

        }
        return Optional.empty();
    }

    public static Optional<PointWithValue> getPoint(Image image, int x, int y, int b) {
        try {
            final int greyValue = image.getPixelXYBByte(x, y, b);
            return Optional.of(new PointWithValue(x, y, greyValue));
        } catch (Exception ignored) {

        }
        return Optional.empty();
    }

    public static double[] calculateHistogram(Image from) {
        final double[] histogram = new double[256];

        for (int x = 0; x < from.getXDim(); x++) {
            for (int y = 0; y < from.getYDim(); y++) {
                final int pixelValue = from.getPixelXYBByte(x, y, 0);
                histogram[pixelValue] = histogram[pixelValue] + 1;
            }
        }
        return histogram;
    }

    public static double[] calculateCumulativeHistogram(Image from) {
        final double[] cumulativeHistogram = calculateHistogram(from);

        for (int i = 1; i < cumulativeHistogram.length; i++) {
            cumulativeHistogram[i] = cumulativeHistogram[i - 1] + cumulativeHistogram[i];
        }

        return cumulativeHistogram;
    }

    public static ByteImage equalizeHistogram(Image from) {
        final ByteImage img = new ByteImage(from);
        final double[] cumulativeHistogram = calculateCumulativeHistogram(from);
        final double numberOfPixels = from.getXDim() * from.getYDim();

        final Set<Integer> allGreyColor = new HashSet<>();


        for (int x = 0; x < img.getXDim(); x++) {
            for (int y = 0; y < img.getYDim(); y++) {
                final int currentPixelValue = img.getPixelXYBByte(x, y, 0);
                allGreyColor.add(currentPixelValue);
            }
        }

        final double levelOfGrey = allGreyColor.size();

        for (int x = 0; x < img.getXDim(); x++) {
            for (int y = 0; y < img.getYDim(); y++) {
                final int currentPixelValue = img.getPixelXYBByte(x, y, 0);
                final double newPixelValue = ((levelOfGrey - 1.0) * cumulativeHistogram[currentPixelValue] / numberOfPixels);
                System.out.println(newPixelValue);
                img.setPixelXYBByte(x, y, 0, (int) newPixelValue);
            }
        }

        return img;
    }
}
