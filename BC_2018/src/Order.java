import java.util.Map;

import bc.*;

public class Order {

	private GameController gc;
	private Graph g;

	public Order(GameController gc) {
		this.gc = gc;
	}

	public void setG(Graph g) {
		this.g = g;
	}

	// refresh
	private void refresh(int unit_id) {
		g.unitById.remove(unit_id);
		g.unitById.put(unit_id, gc.unit(unit_id));
	}

	public void blueprint(int worker_id, UnitType structure_type, Direction direction) {
		// System.out
		// .println(worker_id + " is blueprinting " + structure_type + " - " + direction
		// + " -kk- " + g.karbonite);
		gc.blueprint(worker_id, structure_type, direction);
		g.pkarbonite -= bc.bcUnitTypeBlueprintCost(structure_type);
		g.karbonite = (int) gc.karbonite() - g.pkarbonite;
		g.unitActed.remove(worker_id);
		g.unitActed.put(worker_id, g.round);
		MLocation ml = g.mlto(g.mlocation(g.unit(worker_id).location().mapLocation()), direction);
		g.unitAt.put(ml, gc.senseUnitAtLocation(g.maplocation(ml)).id());
		g.unitById.put(gc.senseUnitAtLocation(g.maplocation(ml)).id(), gc.senseUnitAtLocation(g.maplocation(ml)));
	}

	public void build(int worker_id, int blueprint_id) {
		// System.out.println(worker_id + " is building this " + blueprint_id + " -hh- "
		// + gc.unit(blueprint_id).health());
		gc.build(worker_id, blueprint_id);
		g.unitActed.remove(worker_id);
		g.unitActed.put(worker_id, g.round);
		refresh(blueprint_id);
	}

	public void harvest(int worker_id, Direction direction) {
		// System.out.println(worker_id + " is harvesting " + direction + " -kk- " +
		// g.karbonite + " -Cap- "
		// +
		// gc.karboniteAt(gc.unit(worker_id).location().mapLocation().add(direction)));
		gc.harvest(worker_id, direction);
		g.unitActed.remove(worker_id);
		g.unitActed.put(worker_id, g.round);
		g.karbonite = (int) gc.karbonite() - g.pkarbonite;
	}

	public void repair(int worker_id, int structure_id) {
		// System.out.println(worker_id + " is repairing this " + structure_id + " -hh-
		// " + gc.unit(structure_id).health());
		if (g.round == 745)
			System.out.println(g.unit(worker_id) + " - " + g.unit(structure_id));
		gc.repair(worker_id, structure_id);
		g.unitActed.remove(worker_id);
		g.unitActed.put(worker_id, g.round);
		refresh(structure_id);
	}

	// TODO act or not?
	public void load(int structure_id, int robot_id) {
		// System.out.println(robot_id + " is loading in " + structure_id);
		gc.load(structure_id, robot_id);
		MLocation ml = g.mlocation(g.unitById.get(robot_id).location().mapLocation());
		refresh(robot_id);
		g.unitAt.remove(ml);
	}

	public void moveRobot(int robot_id, Direction direction) {
		// System.out.println(robot_id + " is going to " + direction);
		gc.moveRobot(robot_id, direction);
		MLocation ml = g.mlocation(g.unitById.get(robot_id).location().mapLocation());
		refresh(robot_id);
		g.unitAt.remove(ml);
		g.unitAt.put(g.mlto(ml, direction), robot_id);
	}

	public void unload(int structure_id, Direction direction) {
		// System.out.println(structure_id + " factory unload : " + direction);
		gc.unload(structure_id, direction);
		MLocation ml = g.mlocation(g.unitById.get(structure_id).location().mapLocation());
		Unit unit = gc.senseUnitAtLocation(g.maplocation(g.mlto(ml, direction)));
		refresh(unit.id());
		g.unitAt.put(g.mlto(ml, direction), unit.id());
		// TODO NEW ? CAN DO SHIT ?
		g.unitActed.remove(unit.id());
		g.unitActed.put(unit.id(), g.round);

	}

	public void produceRobot(int factory_id, UnitType robot_type) {
		// System.out.println(factory_id + " factory produce : " + robot_type + " -kk- "
		// + g.karbonite);
		gc.produceRobot(factory_id, robot_type);
		g.karbonite = (int) gc.karbonite() - g.pkarbonite;
	}

	public void replicate(int worker_id, Direction direction) {
		// System.out.println(worker_id + " Replicate ! : " + direction);
		gc.replicate(worker_id, direction);
		MLocation ml = g.mlocation(g.unitById.get(worker_id).location().mapLocation());
		Unit unit = gc.senseUnitAtLocation(g.maplocation(g.mlto(ml, direction)));

		g.unitAt.put(g.mlto(ml, direction), unit.id());
		g.unitById.put(unit.id(), unit);
		g.pkarbonite -= bc.bcUnitTypeReplicateCost(UnitType.Worker);
		g.karbonite = (int) gc.karbonite() - g.pkarbonite;

		g.unitActed.remove(worker_id);
		g.unitActed.put(worker_id, g.round);
		// if (!g.unitIds.contains(unit.id())) {
		// // g.unitIds.add(unit.id());
		// // g.unitJob.put(unit.id(), new Job(unit.id()));
		// // g.unitActed.put(unit.id(), g.round);
		// } else
		// System.out.println("NO! PLS!");
	}

	public void attack(int robot_id, int target_unit_id) {
		// System.out.println(g.unit(robot_id));
		if (g.round == 499)
			System.out.println("id: " + robot_id + " - " + g.unit(robot_id) + "-" + gc.unit(robot_id));
		gc.attack(robot_id, target_unit_id);

		MLocation ml = g.mlocation(target_unit_id);
		// KILL ?
		if (gc.hasUnitAtLocation(g.maplocation(g.mlocation(target_unit_id)))) {
			Unit unit = gc.senseUnitAtLocation(g.maplocation(g.mlocation(target_unit_id)));
			g.unitById.remove(target_unit_id);
			g.unitById.put(target_unit_id, unit);
			if (unit.health() == 0) {
				g.unitById.remove(target_unit_id);
				g.unitAt.remove(ml);
				// System.out.println("dead");
			}
		} else {
			g.unitById.remove(target_unit_id);
			g.unitAt.remove(ml);
			// System.out.println("DEAD");
		}
		refresh(robot_id);

		g.unitActed.remove(robot_id);
		g.unitActed.put(robot_id, g.round);
	}

	public void launchRocket(int rocket_id, MapLocation location) {
		MLocation ml = g.mlocation(g.unitById.get(rocket_id).location().mapLocation());
		if (g.round == 392)
			System.out.println(ml + " - " + g.unit(g.mlto(ml, Direction.East)) + " - "
					+ gc.unit(g.unit(g.mlto(ml, Direction.East)).id()));

		gc.launchRocket(rocket_id, location);
		// ?

		for (Direction dir : Direction.values()) {
			if (dir != Direction.Center && g.valid(g.mlto(ml, dir)) && g.isUnit(g.mlto(ml, dir))) {
				if (g.unit(g.mlto(ml, dir)).health() <= 100) {
					g.unitById.remove(g.unit(g.mlto(ml, dir)).id());
					g.unitAt.remove(g.mlto(ml, dir));
				} else {
					int ii = g.unit(g.mlto(ml, dir)).id();
					g.unitById.remove(ii);
					g.unitById.put(ii, gc.unit(ii));
				}
			}
		}

		refresh(rocket_id);
		g.unitAt.remove(ml);

	}
}
