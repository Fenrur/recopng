package zitoune;

public record PointWithValue(int x, int y, int greyColor) {

    public Point toPoint() {
        return new Point(x, y);
    }
}
