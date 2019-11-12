
/**
 * Control everything in our AI program 
 * <p>
 * each turn it goes on run() so it is our main method.
 * <p>
 * gc and bc are our main connection to outer classes.
 * 
 * @author      MohammadReza DaneshvarAmoli <dos.moreda @ gmail.com>
 * @version     1.3.0                 (current version number of program)
 * @since       1.0.0          (the version of the package this class was first added to)
 */

import java.util.ArrayList;

import bc.*;

public class Controller {

	public GameController gc;
	public Graph g;
	public Order o;

	/**
	 * starting Controller
	 * <p>
	 * set gc from player class
	 * <p>
	 * also make Graph
	 * <p>
	 * 
	 * @param gc
	 *            gameController pointer
	 */
	public Controller(GameController gc) {
		this.gc = gc;
		o = new Order(gc);
		g = new Graph(gc, o);
		o.setG(g);
	}

	/**
	 * main method
	 * <p>
	 * run each turn for both mars and earth
	 */
	// TODO is "directionTo" good ? No BFS PLS
	// TODO unit == unit in all turn?!
	// NOTE unit has to many things just write it.
	public void run() {
		if (g.planeti == Planet.Mars) {
			g.update();
			setStrategy();
			setJobs();
			mstructures();
			moving();
			lastWork();

		} else {
			// long millis = System.currentTimeMillis();
			// update Turn
			g.update();
			// System.out.println("Time Left 1 RUN: " + (System.currentTimeMillis() -
			// millis));

			// Set Strategy
			setStrategy();

			// Managing researches
			researches();
			// System.out.println("Time Left 2 RUN: " + (System.currentTimeMillis() -
			// millis));

			// Set Jobs to units
			setJobs();

			// Structure Work
			structures();
			// System.out.println("Time Left 3 RUN: " + (System.currentTimeMillis() -
			// millis));

			// System.out.println("Time Left 4 RUN: " + (System.currentTimeMillis() -
			// millis));
			// Side work
			// workers();
			// System.out.println("Time Left 5 RUN: " + (System.currentTimeMillis() -
			// millis));
			// Defenders
			// defenders();
			// System.out.println("Time Left 6 RUN: " + (System.currentTimeMillis() -
			// millis));
			// move to work
			moving();
			// System.out.println("Time Left 7 RUN: " + (System.currentTimeMillis() -
			// millis));
			// Side work if not did it yet
			lastWork();
			// System.out.println("Time Left 8 RUN: " + (System.currentTimeMillis() -
			// millis));
			// Attackers
			// attackers();
			// System.out.println("Time Left 9 RUN: " + (time - (System.currentTimeMillis()
			// - millis)));
			// unloads
			// unloads();
		}
	}

	private void mstructures() {
		for (Integer id : g.unitIds) {
			Unit unit = g.unit(id);
			if (unit == null)
				continue;
			Location loc = unit.location();
			UnitType ut = unit.unitType();
			if (loc.isInGarrison() || loc.isInSpace())
				continue;

			if (ut == UnitType.Rocket) {
				MapLocation ml = loc.mapLocation();
				VecUnitID vu = unit.structureGarrison();
				Direction dir = g.nearEmpty(g.mlocation(ml));
				int jj = (int) vu.size();
				while (jj > 0 && dir != null) {
					--jj;
					o.unload(id, dir);
					UnitType utt = g.unit(g.mlto(g.mlocation(ml), dir)).unitType();
					dir = g.nearEmpty(g.mlocation(ml));
				}
			}
		}
		// TODO need for new units or not ?
		// THEY CANT MOVE NOW
		// g.update();
		// System.out.println("m: " + g.mage + " k: " + g.knight);
	}

	private void setStrategy() {
		if (g.strategy == "NOTHING") {
			g.strategy = "POPSTART";
		} else if (g.strategy == "POPSTART" && g.round > 40) {
			g.strategy = "RUSH";
		} else if (g.strategy == "RUSH" && g.round > 650) {
			g.strategy = "RUNTOMARS";
			g.sstep = 0;
			g.sstepv = -1;
		} else if (g.strategy == "RUNTOMARS" && g.round > 750) {
			g.strategy = "EARTHISDEAD";
		}
	}

	private void researches() {
		if (!gc.researchInfo().hasNextInQueue() && g.round < 100) {
			gc.queueResearch(UnitType.Worker);// 25
			gc.queueResearch(UnitType.Knight);// 25
			gc.queueResearch(UnitType.Mage);// 25
			gc.queueResearch(UnitType.Knight);// 75
			gc.queueResearch(UnitType.Mage);// 75
			gc.queueResearch(UnitType.Rocket);// 50
			g.rocketReady = 275;
			gc.queueResearch(UnitType.Mage);// 100
			gc.queueResearch(UnitType.Healer);// 25
			gc.queueResearch(UnitType.Healer);// 100
			gc.queueResearch(UnitType.Healer);// 100
			gc.queueResearch(UnitType.Rocket);// 100
			gc.queueResearch(UnitType.Rocket);// 100
			gc.queueResearch(UnitType.Ranger);// 25
			gc.queueResearch(UnitType.Ranger);// 100
			gc.queueResearch(UnitType.Ranger);// 200
		}
	}

	private void setJobs() {
		for (Integer id : g.unitIds) {
			Unit unit = g.unit(id);
			Location loc = unit.location();
			UnitType ut = unit.unitType();
			if (loc.isInGarrison() || loc.isInSpace())
				continue;
			if (ut == UnitType.Factory || ut == UnitType.Rocket) {
				// TODO
			} else if (ut == UnitType.Worker) {
				g.workerJob(id);
				// System.out.println(id + " - " + g.unitJob.get(id).type + " - " +
				// g.v_to_mloc(g.unitJob.get(id).target));
			} else if (ut == UnitType.Knight || ut == UnitType.Ranger || ut == UnitType.Mage || ut == UnitType.Healer)
				g.armyJob(id);
		}
	}

	private void structures() {
		for (Integer id : g.unitIds) {
			Unit unit = g.unit(id);
			if (unit == null)
				continue;
			Location loc = unit.location();
			UnitType ut = unit.unitType();
			if (loc.isInGarrison() || loc.isInSpace())
				continue;
			if (((g.strategy == "RUSH" && g.karbonite >= 2 * bc.bcUnitTypeFactoryCost(UnitType.Ranger))
					|| (g.strategy == "RUNTOMARS" && g.karbonite >= bc.bcUnitTypeBlueprintCost(UnitType.Factory)))
					&& ut == UnitType.Factory && unit.structureIsBuilt() == 1) {
				MapLocation ml = loc.mapLocation();
				VecUnitID vu = unit.structureGarrison();
				Direction dir = g.nearEmpty(g.mlocation(ml));

				if (vu.size() > 0 && dir != null) {
					o.unload(id, dir);
					UnitType utt = g.unit(g.mlto(g.mlocation(ml), dir)).unitType();
					if (utt == UnitType.Worker) {
						--g.worker;
						--g.fworker;
					} else if (utt == UnitType.Knight) {
						--g.knight;
						--g.fknight;
					} else if (utt == UnitType.Ranger) {
						--g.ranger;
						--g.franger;
					} else if (utt == UnitType.Mage) {
						--g.mage;
						--g.fmage;
					}

				}

				// TODO fuck you dont make worker
				if (g.karbonite >= bc.bcUnitTypeFactoryCost(UnitType.Worker) && (g.avk / 400 >= g.worker - 1)
						&& unit.isFactoryProducing() != 1
						&& unit.structureMaxCapacity() != unit.structureGarrison().size()) {
					o.produceRobot(unit.id(), UnitType.Worker);
					++g.worker;
					++g.fworker;
				} else if (g.karbonite >= bc.bcUnitTypeFactoryCost(UnitType.Knight) && unit.isFactoryProducing() != 1
						&& unit.structureMaxCapacity() != unit.structureGarrison().size()
						&& (g.knight <= g.ranger && g.knight <= g.mage)) {
					o.produceRobot(unit.id(), UnitType.Knight);
					++g.knight;
					++g.fknight;
				} else if (g.karbonite >= bc.bcUnitTypeFactoryCost(UnitType.Ranger) && unit.isFactoryProducing() != 1
						&& unit.structureMaxCapacity() != unit.structureGarrison().size()
						&& (g.ranger <= g.knight && g.ranger <= g.mage)) {
					o.produceRobot(unit.id(), UnitType.Ranger);
					++g.ranger;
					++g.franger;
				} else if (g.karbonite >= bc.bcUnitTypeFactoryCost(UnitType.Mage) && unit.isFactoryProducing() != 1
						&& unit.structureMaxCapacity() != unit.structureGarrison().size()
						&& (g.mage <= g.ranger && g.mage <= g.knight)) {
					// System.out.println(g.karbonite + " - " + gc.karbonite() + " - " +
					// g.pkarbonite);
					o.produceRobot(unit.id(), UnitType.Mage);
					++g.mage;
					++g.fmage;
				}

			} else if (ut == UnitType.Rocket && unit.structureIsBuilt() == 1
					&& (unit.structureGarrison().size() == unit.structureMaxCapacity() || g.round > 748)) {
				MapLocation ml = new MapLocation(Planet.Mars, 0, 0);
				o.launchRocket(id, ml);
			}
		}
		// TODO need for new units or not ?
		// THEY CANT MOVE NOW
		// g.update();
		// System.out.println("m: " + g.mage + " k: " + g.knight);
	}

	// do side project when going to job
	private void workers() {
		for (Integer id : g.unitIds) {
			Unit unit = g.unit(id);
			if (unit == null)
				continue;
			Location loc = unit.location();
			Job job = g.unitJob.get(id);
			UnitType ut = unit.unitType();
			if (loc.isInGarrison() || loc.isInSpace())
				continue;
			// TODO not needed but not bad
			// if (!job.hasJob) {
			// g.giveJob(id);
			// g.eupdate();
			// }
			if (ut == UnitType.Worker)
				if (job.hasJob && g.distancevm(job.target, g.mlocation(loc.mapLocation())) >= 9)
					g.mangeNear(id);
		}
	}

	private void defenders() {
		// TODO Auto-generated method stub
	}

	private void moving() {
		ArrayList<Unit> retry = new ArrayList<Unit>();
		for (Integer id : g.unitIds) {
			Unit unit = g.unit(id);
			if (unit == null)
				continue;
			Location loc = unit.location();
			Job job = g.unitJob.get(id);
			UnitType ut = unit.unitType();
			if (loc.isInGarrison() || loc.isInSpace())
				continue;
			// TODO not needed but not bad
			// if (!job.hasJob) {
			// g.giveJob(id);
			// g.eupdate();
			// if (ut == UnitType.Worker && )
			// if (job.hasJob && g.unitActed.get(id) != g.round
			// && g.distancevm(job.target, loc.mapLocation()) >= 9)
			// g.mangeNear(id);
			// g.eupdate();
			// }
			if (ut == UnitType.Worker && job.hasJob) {
				MapLocation ml = loc.mapLocation();
				// TODO what if we must move and do something here ?
				if (g.unitActed.get(id) != g.round && g.distancevm(job.target, g.mlocation(ml)) <= 2) {
					g.doJob(id);
				} else if (gc.isMoveReady(unit.id())) {
					// int go = g.BFS(g.pnum, g.mloc_to_v(ml), job.target);
					int go = g.best(g.pnum, g.mloc_to_v(g.mlocation(ml)), job.target);
					if (go != -1) {
						MapLocation mml = g.maplocation(g.v_to_mloc(go));
						Direction dir = ml.directionTo(mml);
						o.moveRobot(id, dir);
						// TODO unit or id ?
						retry.add(unit);
					} else {
						g.reJob(id);
						// System.out.println(id + " I cant reach my target :" + job.target);
					}
				}
			}

			if ((ut == UnitType.Knight || ut == UnitType.Ranger || ut == UnitType.Mage || ut == UnitType.Healer)
					&& job.hasJob) {
				MapLocation ml = loc.mapLocation();
				// TODO what if we must move and do something here ?
				if (ut == UnitType.Knight && g.unitActed.get(id) != g.round
						&& g.distancevm(job.target, g.mlocation(ml)) <= job.needDistance && unit.attackHeat() < 10)
				// TODO < 10 or <= 10 ???
				{
					g.doArmyJob(id);

				} else if (ut == UnitType.Mage && job.type == "LOADTOROCKET"
						&& g.distancevm(job.target, g.mlocation(ml)) <= 2) {
					g.doArmyJob(id);
				} else if (gc.isMoveReady(unit.id())) {
					// int go = g.BFS(g.pnum, g.mloc_to_v(ml), job.target);
					// if (g.round == 656)
					// System.out.println(g.mloc_to_v(g.mlocation(ml)) + " - " + job.target);
					int go = g.best(g.pnum, g.mloc_to_v(g.mlocation(ml)), job.target);
					if (go != -1) {
						MapLocation mml = g.maplocation(g.v_to_mloc(go));
						Direction dir = ml.directionTo(mml);
						o.moveRobot(id, dir);
						// TODO unit or id ?
						retry.add(unit);
					} else {
						// g.reJob(id);
						// System.out.println(id + " I cant reach my target :" + job.target);
					}
				}
			}

		}
		// move act here
		// for (Unit unit : retry) {
		// int id = unit.id();
		// Location loc = unit.location();
		// Job job = g.unitJob.get(id);
		// if (loc.isInGarrison() || loc.isInSpace())
		// continue;
		// if (ut == UnitType.Worker && && job.hasJob) {
		// // TODO what if we must move and do something here ?
		// if (g.unitActed.get(id) != g.round && g.distancevm(job.target,
		// loc.mapLocation()) <= 2)
		// g.doJob(id);
		// }
		// g.eupdate();
		// }
	}

	private void lastWork() {
		for (Integer id : g.unitIds) {
			Unit unit = g.unit(id);
			if (unit == null)
				continue;
			UnitType ut = unit.unitType();
			if (unit.team() == g.teamo) {
				System.out.println(unit);
			}
			Location loc = unit.location();
			if (loc.isInGarrison() || loc.isInSpace() || g.unitActed.get(id) == g.round)
				continue;
			if (ut == UnitType.Worker) {
				if (g.round == 745)
					System.out.println(unit);
				g.mangeNear(id);
			} else if (ut == UnitType.Knight || ut == UnitType.Ranger || ut == UnitType.Mage || ut == UnitType.Healer) {
				g.armyAnyAttack(id);
			}
		}

	}

	private void attackers() {
		// TODO Auto-generated method stub

	}

	// private void unloads() {
	// for (Integer id : g.unitIds) {
	// Unit unit = g.unit(id);
	// Location loc = unit.location();
	// if (loc.isInGarrison() || loc.isInSpace())
	// continue;
	// if (ut == UnitType.Factory && unit.structureIsBuilt() == 1) {
	// MapLocation ml = loc.mapLocation();
	// VecUnitID vu = unit.structureGarrison();
	// Direction dir = g.nearEmpty(ml);
	// if (vu.size() > 0 && dir != null)
	// o.unload(id, dir);
	// if (g.karbonite >= bc.bcUnitTypeFactoryCost(UnitType.Worker) &&
	// unit.isFactoryProducing() != 1
	// && unit.structureMaxCapacity() != unit.structureGarrison().size())
	// o.produceRobot(unit.id(), UnitType.Worker);
	// }
	// }
	// // TODO need for new units or not ?
	// // THEY CANT MOVE NOW
	// // g.update();
	// }
}
