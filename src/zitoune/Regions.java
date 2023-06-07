package zitoune;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Regions {

    private final Map<Point, Set<Point>> regions = new HashMap<>();

    public boolean pointContains(Point point) {
        for (Set<Point> region : regions.values()) {
            if (region.contains(point)) {
                return true;
            }
        }

        return false;
    }

    public Set<Point> getRegion(Point point) {
        return regions.get(point);
    }

    public void putPoint(Point seedRegionPoint, Point regionPoint) {
        final Set<Point> region = regions.putIfAbsent(seedRegionPoint, new HashSet<>());
        regions.get(seedRegionPoint).add(regionPoint);
    }

    @Override
    public String toString() {
        return "Regions{" +
            "regions=" + regions +
            '}';
    }
}
