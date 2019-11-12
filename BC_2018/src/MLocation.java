import bc.MapLocation;
import bc.Planet;

public class MLocation {
	public int x;
	public int y;
	public Planet planet;

	public MLocation(MapLocation ml) {
		x = ml.getX();
		y = ml.getY();
		planet = ml.getPlanet();
	}

	public MLocation(Planet planet, int x, int y) {
		this.planet = planet;
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return "MLocation { planet: " + planet + ", x: " + x + ", y:" + y + " }";
	}
}
