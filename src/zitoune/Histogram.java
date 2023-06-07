package zitoune;

import fr.unistra.pelican.Image;

public record Histogram(double[][] values) {

    public static Histogram calculateHistogram(Image from) {
        double[][] histogram = new double[3][256];

        for (int b = 0; b < from.bdim; b++) {
            for (int x = 0; x < from.getXDim(); x++) {
                for (int y = 0; y < from.getYDim(); y++) {
                    final int pixelValue = from.getPixelXYBByte(x, y, b);
                    histogram[b][pixelValue] = histogram[b][pixelValue] + 1;
                }
            }
        }
        return new Histogram(histogram);
    }

    private static double[][] deepCopy(double[][] original) {
        if (original == null) {
            return null;
        }

        double[][] copy = new double[original.length][];

        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }

        return copy;
    }

    public Histogram discretize(int batch) {
        final int batching = (int) Math.ceil(256.0 / (double) batch);

        double[][] discreteHistogram = new double[3][batching];

        for (int dim = 0; dim < dims(); dim++) {
            for (int indexBatching = 0; indexBatching < batching; indexBatching++) {
                for (int indexIntoBatching = 0; indexIntoBatching < batch; indexIntoBatching++) {
                    try {
                        final int index = indexBatching * batch + indexIntoBatching;
                        discreteHistogram[dim][indexBatching] = discreteHistogram[dim][indexBatching] + dim(dim)[index];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        break;
                    }
                }
            }
        }

        return new Histogram(discreteHistogram);
    }

    public Histogram normalize(double numberOfPixels) {
        final double[][] h = deepCopy(values);
        for (double[] values : h) {
            for (int i = 0; i < values.length; i++) {
                values[i] = values[i] / numberOfPixels;
            }
        }

        return new Histogram(h);
    }

    public double[] dim(int bDim) {
        return values[bDim];
    }

    public int dims() {
        return values.length;
    }

    public double calculateEuclideanDistance(Histogram histogramToCompare) {
        double distance = 0;

        for (int dim = 0; dim < dims(); dim++) {
            final double[] values = dim(dim);
            final double[] valuesToCompare = histogramToCompare.dim(dim);

            for (int i = 0; i < values.length; i++) {
                final double h1 = values[i];
                final double h2 = valuesToCompare[i];

                final double pow = Math.pow(h1 - h2, 2);
                final double val = Math.sqrt(pow);

                distance += val;
            }
        }

        return distance;
    }
}
