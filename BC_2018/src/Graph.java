
/**
 * Make Graph and do calculating
 * <p>
 * make at start and update every turns
 * <p>
 * gc and bc are our main connection to outer classes.
 * 
 * @author      MohammadReza DaneshvarAmoli <dos.moreda @ gmail.com>
 * @version     1.3.0                 (current version number of program)
 * @since       1.1.0          (the version of the package this class was first added to)
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import bc.*;

public class Graph {
	private GameController gc;
	private Order o;

	// public final Map<Integer, Unit> unitById = new HashMap<>();
	public final Map<Integer, Job> unitJob = new HashMap<>();
	public final Map<Integer, Integer> unitActed = new HashMap<>();
	public final Map<Integer, Unit> unitById = new HashMap<>();
	public final Set<Integer> unitIds = new HashSet<>();
	public final Map<MLocation, Integer> unitAt = new HashMap<>(); // mlocation To ID

	public final Map<Integer, MLocation>[] vtml = new Map[] { new HashMap<>(), new HashMap<>() };
	public final Map<Integer, MapLocation>[] vtm = new Map[] { new HashMap<>(), new HashMap<>() };

	public AsteroidPattern asteroidPattern;
	public OrbitPattern orbitPattern;

	public Team teami, teamo;
	public int tnum;
	public Planet planeti, planeto;
	public int pnum;

	public PlanetMap[] map;
	public int[] size;

	public intVec[][] adjacencyList;

	public intVec[] nodeSi;
	public intVec[] nodeSo;

	public int karbonite;
	public int pkarbonite = 0;
	public int round;

	public int factory = 0;
	public int worker = 0;
	public int knight = 0;
	public int ranger = 0;
	public int mage = 0;
	public int healer = 0;
	public int rocket = 0;
	public int ffactory = 0;
	public int fworker = 0;
	public int fknight = 0;
	public int franger = 0;
	public int fmage = 0;
	public int fhealer = 0;
	public int frocket = 0;

	public int[][] distance;
	public int[] distancek;
	public int[] neark;

	public int[] distanceh;
	public int[] nearh;

	public int[] distancet;
	public int[] nearest_knight;
	public int[] nearest_ranger;
	public int[] nearest_enemy0;
	public int[] nearest_enemy1;
	public int[] nearest_rocket;

	public int avk;
	public int rocketReady;
	public int sstep = 0;
	public int sstepv = -1;

	public String strategy = "NOTHING";

	public Graph(GameController gc, Order o) {
		this.gc = gc;
		this.o = o;
		// someShit();
		init();
	}

	private void init() {
		asteroidPattern = gc.asteroidPattern();
		orbitPattern = gc.orbitPattern();

		teami = gc.team();
		teamo = Team.values()[1 - teami.ordinal()];
		tnum = teami.ordinal();
		planeti = gc.planet();
		planeto = bc.bcPlanetOther(planeti);
		pnum = gc.planet().ordinal();

		map = new PlanetMap[2];
		map[0] = gc.startingMap(Planet.Earth);
		map[1] = gc.startingMap(Planet.Mars);

		size = new int[2];
		size[0] = (int) (map[0].getWidth() * map[0].getHeight());
		size[1] = (int) (map[1].getWidth() * map[1].getHeight());
		for (Integer i = 0; i < size[0]; ++i) {
			vtml[0].put(i, new MLocation(Planet.values()[0], v_to_x(0, i), v_to_y(0, i)));
			vtm[0].put(i, new MapLocation(Planet.values()[0], v_to_x(0, i), v_to_y(0, i)));
		}
		for (Integer i = 0; i < size[1]; ++i) {
			vtml[1].put(i, new MLocation(Planet.values()[1], v_to_x(1, i), v_to_y(1, i)));
			vtm[1].put(i, new MapLocation(Planet.values()[1], v_to_x(1, i), v_to_y(1, i)));
		}
		distance = new int[size[pnum]][size[pnum]];
		distancek = new int[size[pnum]];
		neark = new int[size[pnum]];
		distanceh = new int[size[pnum]];
		nearh = new int[size[pnum]];
		nearest_knight = new int[size[pnum]];
		nearest_ranger = new int[size[pnum]];
		nearest_enemy0 = new int[size[pnum]];
		nearest_enemy1 = new int[size[pnum]];
		nearest_rocket = new int[size[pnum]];
		distancet = new int[size[pnum]];
		adjacencyList = new intVec[2][];
		adjacencyList[0] = new intVec[size[0]];
		adjacencyList[1] = new intVec[size[1]];
		for (int i = 0; i < size[0]; ++i)
			adjacencyList[0][i] = new intVec();
		for (int i = 0; i < size[1]; ++i)
			adjacencyList[1][i] = new intVec();

		for (Integer i = 0; i < size[0]; ++i) {
			MLocation ml = v_to_mloc(0, i);
			for (Direction dir : Direction.values()) {
				MLocation mml = mlto(ml, dir);
				if (mml != null && map[0].isPassableTerrainAt(vtm[0].get(mloc_to_v(ml))) == 1)
					adjacencyList[0][i].add(mloc_to_v(mml));
			}
		}
		for (Integer i = 0; i < size[1]; ++i) {
			MLocation ml = v_to_mloc(1, i);
			for (Direction dir : Direction.values()) {
				MLocation mml = mlto(ml, dir);
				if (mml != null && map[1].isPassableTerrainAt(vtm[1].get(mloc_to_v(ml))) == 1)
					adjacencyList[1][i].add(mloc_to_v(mml));
			}
		}
		long millis = System.currentTimeMillis();
		BFS_ALL();// 293 X^X 1225
		// Floyd_Warshall();// 6780 X^X 1225
		System.out.println((System.currentTimeMillis() - millis) + " X^X " + size[pnum]);

		intVec iv = new intVec();
		VecUnit vc = map[pnum].getInitial_units();
		for (Integer i = 0; i < vc.size(); ++i) {
			Unit unit = vc.get(i);
			Integer id = unit.id();
			// ok ?
			iv.add(mloc_to_v(mlocation(unit.location().mapLocation())));
		}
		// MY karboonite ALL
		BFS(iv, null, nearh, distanceh, size[pnum], adjacencyList[pnum]);
		avk = 0;
		for (Integer i = 0; i < size[pnum]; ++i) {
			MLocation ml = v_to_mloc(i);
			MapLocation mal = maplocation(ml);
			if (nearh[i] != -1 && gc.canSenseLocation(maplocation(v_to_mloc(nearh[i])))
					&& gc.senseUnitAtLocation(maplocation(v_to_mloc(nearh[i]))).team() == teami)
				avk += map[pnum].initialKarboniteAt(mal);
		}

		System.out.println("AVK: " + avk);

	}

	// private void someShit() {
	// AsteroidPattern ap = gc.asteroidPattern();
	// System.out.println("Asteroid Pattern : ");
	// for (Integer i = 1; i <= 1000; ++i) {
	// if (ap.hasAsteroid(i))
	// System.out.println("round " + i + " : " + ap.asteroid(i).getKarbonite());
	// }
	// System.out.println("Unit Value? : ");
	// for (UnitType ut : UnitType.values()) {
	// System.out.println(ut + " : " + bc.bcUnitTypeValue(ut));
	// }
	// }

	public void update() {
		karbonite = (int) gc.karbonite();
		round = (int) gc.round();
		factory = ffactory;
		worker = fworker;
		knight = fknight;
		ranger = franger;
		mage = fmage;
		healer = fhealer;
		rocket = frocket;
		unitById.clear();
		unitAt.clear();
		unitIds.clear();
		// TODO which planet?
		// TODO what about dead unit?!
		VecUnit unitsi = gc.units();
		for (Integer i = 0; i < unitsi.size(); ++i) {
			Unit unit = unitsi.get(i);
			Integer id = unit.id();
			Location loc = unit.location();
			UnitType ut = unit.unitType();
			Team team = unit.team();

			unitById.put(id, unit);
			if (!loc.isInGarrison() && !loc.isInSpace())
				unitAt.put(vtml[pnum].get(bml_to_v(loc.mapLocation())), id);
			if (team == teami) {
				unitIds.add(id);
				if (!unitActed.containsKey(id)) {
					unitJob.put(id, new Job(id));
					unitActed.put(id, 0);
				}
				if (ut == UnitType.Factory)
					++factory;
				else if (ut == UnitType.Rocket)
					++rocket;
				else if (ut == UnitType.Worker)
					++worker;
				else if (ut == UnitType.Knight)
					++knight;
				else if (ut == UnitType.Ranger)
					++ranger;
				else if (ut == UnitType.Mage)
					++mage;
				else if (ut == UnitType.Healer)
					++healer;
			}
		}

		System.out.println("Current round : " + round);
		// System.out.println("karbonite : " + karbonite);
		// System.out.println("remain time: " + gc.getTimeLeftMs());
		// System.out.println("units :" + unitsi.size());

		intVec iv = new intVec();
		for (Integer i = 0; i < size[pnum]; ++i) {
			MLocation ml = v_to_mloc(i);
			MapLocation mal = maplocation(ml);
			if ((gc.canSenseLocation(mal) && gc.karboniteAt(mal) > 0)
					|| (!gc.canSenseLocation(mal) && map[pnum].initialKarboniteAt(mal) > 0))
				iv.add(i);
		}

		BFS(iv, null, neark, distancek, size[pnum], adjacencyList[pnum]);

		iv = new intVec();
		VecUnit vc = map[pnum].getInitial_units();
		for (Integer i = 0; i < vc.size(); ++i) {
			Unit unit = vc.get(i);
			Integer id = unit.id();
			// ok ?
			if (!unit.location().isInGarrison() && !unit.location().isInSpace())
				if (unit.team() == teamo)
					iv.add(mloc_to_v(mlocation(unit.location().mapLocation())));
		}
		BFS(iv, null, nearest_enemy0, distancet, size[pnum], adjacencyList[pnum]);

		iv = new intVec();
		vc = gc.units();
		for (Integer i = 0; i < vc.size(); ++i) {
			Unit unit = vc.get(i);
			Integer id = unit.id();
			// ok ?
			if (!unit.location().isInGarrison() && !unit.location().isInSpace())
				if (unit.team() == teamo)
					iv.add(mloc_to_v(mlocation(unit.location().mapLocation())));
		}
		BFS(iv, null, nearest_enemy1, distancet, size[pnum], adjacencyList[pnum]);

		iv = new intVec();
		for (Integer id : unitIds) {
			Unit unit = unit(id);
			// ok ?
			if (!unit.location().isInGarrison() && !unit.location().isInSpace())
				if (unit.unitType() == UnitType.Knight)
					iv.add(mloc_to_v(mlocation(unit.location().mapLocation())));
		}
		BFS(iv, null, nearest_knight, distancet, size[pnum], adjacencyList[pnum]);

		iv = new intVec();
		for (Integer id : unitIds) {
			Unit unit = unit(id);
			// ok ?
			if (!unit.location().isInGarrison() && !unit.location().isInSpace())
				if (unit.unitType() == UnitType.Ranger)
					iv.add(mloc_to_v(mlocation(unit.location().mapLocation())));
		}
		BFS(iv, null, nearest_ranger, distancet, size[pnum], adjacencyList[pnum]);

		iv = new intVec();
		for (Integer id : unitIds) {
			Unit unit = unit(id);
			// ok ?
			if (!unit.location().isInGarrison() && !unit.location().isInSpace())
				if (unit.unitType() == UnitType.Rocket && unit.structureIsBuilt() == 1)
					iv.add(mloc_to_v(mlocation(unit.location().mapLocation())));
		}
		BFS(iv, null, nearest_rocket, distancet, size[pnum], adjacencyList[pnum]);

	}

	public void workerJob(Integer id) {
		Job job = unitJob.get(id);
		MLocation ml = mlocation(id);
		Unit unit = unit(ml);
		// ASSIGN JOB

		// go for resource
		// nearest unassigned karbonite
		// TODO error ?

		// 2: khodesh edame mide zakhire nashode job ghablish hanuz
		// TODO destroyed blue prInteger
		if (!job.hasJob) {
			// TODO kam karbon + ghesmat kochik + fast fight
			if (strategy == "POPSTART" && unit.abilityHeat() < 10
					&& karbonite >= bc.bcUnitTypeReplicateCost(unit.unitType()) && (avk / 400 >= worker - 1)
					&& nearEmpty(ml) != null) {
				job.hasJob = true;
				job.target = mloc_to_v(mlto(ml, nearEmpty(ml)));
				job.type = "REPLICATE";
				pkarbonite += bc.bcUnitTypeReplicateCost(unit.unitType());
				karbonite = (int) gc.karbonite() - pkarbonite;
				++worker;
				++fworker;
				// FAST CAST
			} else if (strategy == "POPSTART" && buildin(ml) != -1) {
				job.hasJob = true;
				job.target = buildin(ml);
				job.type = "BUILD";
				// SLOW CAST
			} else if (blueq && strategy == "POPSTART" && karbonite >= bc.bcUnitTypeBlueprintCost(UnitType.Factory)
					&& (factory - 1 <= avk / 620) && targetBluePrint(ml) != -1) {
				job.hasJob = true;
				job.target = targetBluePrint(ml);
				job.type = "BLUEPRINT";
				job.arg = "FACTORY";
				pkarbonite += bc.bcUnitTypeBlueprintCost(UnitType.Factory);
				karbonite = (int) gc.karbonite() - pkarbonite;
				blueq = false;
				// SLOW CAST
			} else if (strategy == "POPSTART") {
				Integer end = neark[mloc_to_v(ml)];
				if (end != -1) {
					job.hasJob = true;
					job.target = end;
					job.type = "HARVEST";
				} else {
					job.hasJob = true;
					job.target = mloc_to_v(ml);
					job.type = "NOTHING";
				}
			} else if (strategy == "RUSH" && buildin(ml) != -1) {
				job.hasJob = true;
				job.target = buildin(ml);
				job.type = "BUILD";
				// SLOW CAST
			} else if (blueq && strategy == "RUSH" && karbonite >= bc.bcUnitTypeBlueprintCost(UnitType.Factory)
					&& (factory - 1 <= avk / 620) && targetBluePrint(ml) != -1) {
				job.hasJob = true;
				job.target = targetBluePrint(ml);
				job.type = "BLUEPRINT";
				job.arg = "FACTORY";
				pkarbonite += bc.bcUnitTypeBlueprintCost(UnitType.Factory);
				karbonite = (int) gc.karbonite() - pkarbonite;
				blueq = false;
				// SLOW CAST
			} else if (strategy == "RUSH") {
				Integer end = neark[mloc_to_v(ml)];
				if (end != -1) {
					job.hasJob = true;
					job.target = end;
					job.type = "HARVEST";
				} else {
					job.hasJob = true;
					job.target = mloc_to_v(ml);
					job.type = "NOTHING";
				}
			} else if (strategy == "RUNTOMARS") {
				if (strategy == "RUNTOMARS" && buildin(ml) != -1) {
					job.hasJob = true;
					job.target = buildin(ml);
					job.type = "BUILD";
					// SLOW CAST
				} else if (strategy == "RUNTOMARS" && karbonite >= bc.bcUnitTypeBlueprintCost(UnitType.Rocket)
						&& (rocket - 1 <= unitIds.size() / 10) && round > rocketReady && nearEmpty(ml) != null) {
					job.hasJob = true;
					job.target = mloc_to_v(mlto(ml, nearEmpty(ml)));
					job.type = "BLUEPRINT";
					job.arg = "ROCKET";
					pkarbonite += bc.bcUnitTypeBlueprintCost(UnitType.Rocket);
					karbonite = (int) gc.karbonite() - pkarbonite;
					System.out.println(id + " - " + v_to_mloc(job.target));
				} else if (strategy == "RUNTOMARS" && (rocket >= unitIds.size() / 10)
						&& nearest_rocket[mloc_to_v(ml)] != -1) {
					job.hasJob = true;
					job.target = nearest_rocket[mloc_to_v(ml)];
					job.type = "LOADTOROCKET";
				} else if (strategy == "RUNTOMARS") {
					Integer end = neark[mloc_to_v(ml)];
					if (end != -1) {
						job.hasJob = true;
						job.target = end;
						job.type = "HARVEST";
					} else {
						job.hasJob = true;
						job.target = mloc_to_v(ml);
						job.type = "NOTHING";
					}
				}
			} else if (strategy == "EARTHISDEAD") {
				if (unit.abilityHeat() < 10 && karbonite >= bc.bcUnitTypeReplicateCost(unit.unitType())
						&& (avk / 400 >= worker - 1) && nearEmpty(ml) != null) {
					job.hasJob = true;
					job.target = mloc_to_v(mlto(ml, nearEmpty(ml)));
					job.type = "REPLICATE";
					pkarbonite += bc.bcUnitTypeReplicateCost(unit.unitType());
					karbonite = (int) gc.karbonite() - pkarbonite;
					++worker;
					++fworker;
					// FAST CAST
				} else {
					Integer end = neark[mloc_to_v(ml)];
					if (end != -1) {
						job.hasJob = true;
						job.target = end;
						job.type = "HARVEST";
					} else {
						job.hasJob = true;
						job.target = mloc_to_v(ml);
						job.type = "NOTHING";
					}
				}
			}
		} else {
			if (job.type != "BULEPRINT" && strategy == "POPSTART" && unit.abilityHeat() < 10
					&& karbonite >= bc.bcUnitTypeReplicateCost(unit.unitType()) && (avk / 400 >= worker - 1)
					&& nearEmpty(ml) != null) {
				// change Job!
				job.target = mloc_to_v(mlto(ml, nearEmpty(ml)));
				job.type = "REPLICATE";
				pkarbonite += bc.bcUnitTypeReplicateCost(unit.unitType());
				karbonite = (int) gc.karbonite() - pkarbonite;
				++worker;
				++fworker;
				// FAST CAST
			} else if (job.type != "BULEPRINT" && job.type != "BUILD" && job.type != "LOADTOROCKET"
					&& strategy == "RUNTOMARS") {
				if (strategy == "RUNTOMARS" && buildin(ml) != -1) {
					job.hasJob = true;
					job.target = buildin(ml);
					job.type = "BUILD";
					// SLOW CAST
				} else if (strategy == "RUNTOMARS" && karbonite >= bc.bcUnitTypeBlueprintCost(UnitType.Rocket)
						&& (rocket - 1 <= unitIds.size() / 10) && round > rocketReady && nearEmpty(ml) != null) {
					job.target = mloc_to_v(mlto(ml, nearEmpty(ml)));
					job.type = "BLUEPRINT";
					job.arg = "ROCKET";
					pkarbonite += bc.bcUnitTypeBlueprintCost(UnitType.Rocket);
					karbonite = (int) gc.karbonite() - pkarbonite;
					System.out.println(id + " - " + v_to_mloc(job.target));
				} else if (strategy == "RUNTOMARS" && (rocket >= unitIds.size() / 10)
						&& nearest_rocket[mloc_to_v(ml)] != -1) {
					job.target = nearest_rocket[mloc_to_v(ml)];
					job.type = "LOADTOROCKET";
				} else if (strategy == "RUNTOMARS") {
					Integer end = neark[mloc_to_v(ml)];
					if (end != -1) {
						job.target = end;
						job.type = "HARVEST";
					} else {
						job.target = mloc_to_v(ml);
						job.type = "NOTHING";
					}
				}
			} else if (job.type == "BULEPRINT") {
				// check job
				if (unitAt.get(v_to_mloc(job.target)) != null
						&& unit(v_to_mloc(job.target)).unitType() == UnitType.Factory) {
					reJob(id);
				}
			} else if (job.type == "BUILD") {
				if (unitAt.get(v_to_mloc(job.target)) == null
						|| (unit(v_to_mloc(job.target)).unitType() != UnitType.Factory
								&& unit(v_to_mloc(job.target)).unitType() != UnitType.Rocket)
						|| (unit(v_to_mloc(job.target)).structureIsBuilt() == 1))
					reJob(id);
			} else if (job.type == "LOADTOROCKET") {
				if (unitAt.get(v_to_mloc(job.target)) == null
						|| unit(v_to_mloc(job.target)).unitType() != UnitType.Rocket
						|| (unit(v_to_mloc(job.target)).structureIsBuilt() != 1))
					reJob(id);
			}
			// HARVEST
		}

	}

	private int buildin(MLocation iml) {
		int start = mloc_to_v(iml);
		Queue<Integer> queue = new LinkedList<Integer>();
		Queue<Integer> queue2 = new LinkedList<Integer>();
		Integer[] mark = new Integer[size[pnum]];
		Integer ie;
		Integer ie2;
		for (Integer i = 0; i < size[pnum]; ++i) {
			mark[i] = 0;
		}

		queue.add(start);
		queue2.add(0);
		mark[start] = 1;

		while (!queue.isEmpty()) {
			ie = queue.poll();
			ie2 = queue2.poll();
			if (ie2 > 2)
				break;
			for (Integer it : adjacencyList[pnum][ie]) {
				MLocation ml = v_to_mloc(pnum, it);
				MapLocation mal = maplocation(ml);
				// TODO no block just for first move
				if (mark[it] != 1) {
					if (mark[it] == 0) {
						queue.add(it);
						queue2.add(ie2 + 1);
					}
					mark[it] = 1;
					if (unitAt.containsKey(ml)
							&& (unit(ml).unitType() == UnitType.Factory || unit(ml).unitType() == UnitType.Rocket)
							&& unit(ml).structureIsBuilt() != 1)
						return it;
				}
			}
		}
		return -1;
	}

	private int targetBluePrint(MLocation iml) {
		if (sstep % 3 == 0) {
			int start = mloc_to_v(iml);
			Queue<Integer> queue = new LinkedList<Integer>();
			Integer[] mark = new Integer[size[pnum]];
			Integer ie;
			for (Integer i = 0; i < size[pnum]; ++i) {
				mark[i] = 0;
			}

			queue.add(start);
			mark[start] = 1;

			while (!queue.isEmpty()) {
				ie = queue.poll();
				for (Integer it : adjacencyList[pnum][ie]) {
					MLocation ml = v_to_mloc(pnum, it);
					MapLocation mal = maplocation(ml);
					// TODO no block just for first move
					if (mark[it] != 1) {
						if (mark[it] == 0)
							queue.add(it);
						mark[it] = 1;
						int h = 0;
						for (Direction dir : Direction.values()) {
							MapLocation mall = maplocation(mlto(ml, dir));
							if (valid(mlto(ml, dir)) && map[pnum].isPassableTerrainAt(mall) == 1)
								++h;
						}
						if (h > 6 && map[pnum].isPassableTerrainAt(mal) == 1)
							return it;
					}
				}
			}
		} else {
			int p = sstep;
			sstep = 0;
			// System.out.println(p + " - " + sstepv + " - " + iml + " - " +
			// v_to_mloc(sstepv));
			int x = targetBluePrint(v_to_mloc(sstepv));
			sstep = p;
			return x;
		}
		return -1;
	}

	private int cnt = 0;
	private boolean blueq = true;

	public void doJob(Integer id) {
		Unit unit = unit(id);
		Location loc = unit.location();
		Job job = unitJob.get(id);
		MapLocation ml = loc.mapLocation();
		MapLocation mml = maplocation(v_to_mloc(job.target));
		Direction dir = ml.directionTo(mml);
		Integer idd = unitAt.get(mlocation(mml));
		// TODO SEEMS OK FOR WORKER
		if (job.type.equals("BLUEPRINT")) {
			if (job.arg.equals("FACTORY")) {
				if (gc.canBlueprint(id, UnitType.Factory, dir)) {
					o.blueprint(id, UnitType.Factory, dir);
					sstepv = job.target;
					++sstep;
					reJob(id);
				} else {
					if (unitAt.get(v_to_mloc(job.target)) == null
							|| unit(v_to_mloc(job.target)).unitType() != UnitType.Factory || cnt > 2) {
						cnt++;
					} else {
						cnt = 0;
						pkarbonite -= bc.bcUnitTypeBlueprintCost(UnitType.Factory);
						karbonite = (int) gc.karbonite() - pkarbonite;
						reJob(id);
					}
				}
			} else if (job.arg.equals("ROCKET")) {
				if (gc.canBlueprint(id, UnitType.Rocket, dir)) {
					o.blueprint(id, UnitType.Rocket, dir);
					reJob(id);
				} else {
					pkarbonite -= bc.bcUnitTypeBlueprintCost(UnitType.Factory);
					karbonite = (int) gc.karbonite() - pkarbonite;
					reJob(id);
				}
			}
		} else if (job.type.equals("BUILD")) {
			// TODO destroyed
			if (idd != null && gc.canBuild(id, idd))
				o.build(id, idd);
			else
				reJob(id);
		} else if (job.type.equals("HARVEST")) {
			if (gc.canHarvest(id, dir))
				o.harvest(id, dir);
			else
				reJob(id);
		} else if (job.type.equals("REPAIR")) {
			if (idd != null && gc.canRepair(id, idd))
				o.repair(id, idd);
			else
				reJob(id);
		} else if (job.type.equals("LOADTOROCKET")) {
			if (idd != null && gc.canLoad(idd, id))
				o.load(idd, id);
			else
				reJob(id);
		} else if (job.type.equals("REPLICATE")) {
			if (gc.canReplicate(id, dir))
				o.replicate(id, dir);
			else {
				pkarbonite -= bc.bcUnitTypeReplicateCost(UnitType.Worker);
				karbonite = (int) gc.karbonite() - pkarbonite;
			}
			--worker;
			--fworker;
			reJob(id);
		} else if (job.type.equals("NOTHING")) {
			// TODO this reJob just!
			reJob(id);
		} else {
			System.out.println(id + " NOWAY " + job);
		}
	}

	public void reJob(Integer id) {
		// Unit unit = unit(id);
		Job job = unitJob.get(id);
		if (job.type == "BLUEPRINT")
			blueq = true;
		job.hasJob = false;
		// giveJob(id);
		// eupdate();
		// doJobX(id);
		// eupdate();
	}

	// F

	public void armyJob(Integer id) {
		Job job = unitJob.get(id);
		MLocation ml = mlocation(id);
		Unit unit = unit(ml);
		UnitType ut = unit.unitType();
		if (!job.hasJob) {
			if (strategy == "RUNTOMARS" && nearest_rocket[mloc_to_v(ml)] != -1) {
				job.hasJob = true;
				job.target = nearest_rocket[mloc_to_v(ml)];
				job.type = "LOADTOROCKET";
				job.target_id = unit(v_to_mloc(nearest_rocket[mloc_to_v(ml)])).id();
			} else if (ut == UnitType.Knight) {
				// POSTIONING ? ?
				// AGGRO ? ?
				Integer go = nearest_enemy1[mloc_to_v(mlocation(id))];
				if (go != -1) {
					job.hasJob = true;
					job.target = go;
					job.type = "KILL";
					job.needDistance = (int) unit.attackRange();
					job.target_id = unit(v_to_mloc(go)).id();
				} else {
					go = nearest_enemy0[mloc_to_v(mlocation(id))];
					if (go != -1) {
						job.hasJob = true;
						job.target = go;
						job.type = "FKILL";
						// job.target_id = unit(v_to_mloc(go)).id();
					} else {
						job.hasJob = true;
						job.target = mloc_to_v(ml);
						job.type = "NOTHING";
					}
				}
			} else if (ut == UnitType.Ranger) {
				Integer go = nearest_knight[mloc_to_v(mlocation(id))];
				if (go != -1) {
					job.hasJob = true;
					job.target = go;
					job.type = "FOLLOW";
					job.target_id = unit(v_to_mloc(go)).id();
				} else {
					job.hasJob = true;
					job.target = mloc_to_v(ml);
					job.type = "NOTHING";
				}
			} else if (ut == UnitType.Mage) {
				Integer go = nearest_ranger[mloc_to_v(mlocation(id))];
				if (go != -1) {
					job.hasJob = true;
					job.target = go;
					job.type = "FOLLOW";
					job.target_id = unit(v_to_mloc(go)).id();
				} else {
					job.hasJob = true;
					job.target = mloc_to_v(ml);
					job.type = "NOTHING";
				}
			}
		}
		// job.hasJob == true
		else {
			if (strategy == "RUNTOMARS" && nearest_rocket[mloc_to_v(ml)] != -1) {
				job.hasJob = true;
				job.target = nearest_rocket[mloc_to_v(ml)];
				job.type = "LOADTOROCKET";
				job.target_id = unit(v_to_mloc(nearest_rocket[mloc_to_v(ml)])).id();
			} else if (ut == UnitType.Knight) {
				// POSTIONING ? ?
				// AGGRO ? ?
				if (job.type == "KILL") {
					if (unitById.containsKey(job.target_id)) {
						job.target = mloc_to_v(mlocation(job.target_id));
					} else
						reJob(id);
				} else if (job.type == "FKILL") {
					Integer go = nearest_enemy1[mloc_to_v(mlocation(id))];
					if (go != -1) {
						job.hasJob = true;
						job.target = go;
						job.type = "KILL";
						job.needDistance = (int) unit.attackRange();
						job.target_id = unit(v_to_mloc(go)).id();
					}
				}
			} else if (ut == UnitType.Ranger) {
				if (job.type == "FOLLOW") {
					if (unitById.containsKey(job.target_id)) {
						job.target = mloc_to_v(mlocation(job.target_id));
					} else
						reJob(id);
				}
			} else if (ut == UnitType.Mage) {
				if (job.type == "FOLLOW") {
					if (unitById.containsKey(job.target_id)) {
						job.target = mloc_to_v(mlocation(job.target_id));
					} else
						reJob(id);
				}
			}
		}

	}

	public void doArmyJob(Integer id) {
		Unit unit = unit(id);
		Location loc = unit.location();
		Job job = unitJob.get(id);
		MapLocation ml = loc.mapLocation();
		MapLocation mml = maplocation(v_to_mloc(job.target));
		Direction dir = ml.directionTo(mml);
		Integer idd = unitAt.get(mlocation(mml));
		// System.out
		// .println(ml + " - " + id + " X-X-X-X " + mml + " - " + idd + " - " +
		// job.type
		// + " - " + unitAt.size());
		// TODO SEEMS OK FOR WORKER
		if (job.type.equals("KILL")) {
			if (gc.canAttack(id, job.target_id))
				o.attack(id, job.target_id);
			else
				reJob(id);
		} else if (job.type.equals("FKILL")) {
			// reJob(id);
		} else if (job.type.equals("NOTHING")) {
			reJob(id);
		} else if (job.type.equals("FOLLOW")) {
			// reJob(id);
		} else if (job.type.equals("LOADTOROCKET")) {
			// System.out.println(idd + " - " + id);
			if (idd != null && gc.canLoad(idd, id))
				o.load(idd, id);
			else
				reJob(id);
		} else {
			System.out.println(id + " NOWAY " + job);
		}

	}

	// Side working
	public void mangeNear(Integer id) {
		MLocation ml = mlocation(id);
		Direction dir;
		dir = nearNotBuild(ml);
		if (dir != null)
			o.build(id, unit(mlto(ml, dir)).id());
		else {
			dir = nearRepair(ml);
			if (dir != null)
				o.repair(id, unit(mlto(ml, dir)).id());
			else {
				dir = nearKarbonite(ml);
				if (dir != null)
					o.harvest(id, dir);
			}
		}
	}

	// Side Heat
	public void armyAnyAttack(Integer id) {
		MLocation ml = mlocation(id);
		Unit unit = unit(id);
		if (unit.attackHeat() >= 10)
			return;
		Integer go = nearest_enemy1[mloc_to_v(mlocation(id))];
		if (go != -1 && unitAt.containsKey(v_to_mloc(go)) && (unit.unitType() != UnitType.Mage
				|| gc.senseNearbyUnitsByTeam(maplocation(v_to_mloc(go)), 2, teami).size() == 0)) {
			if (unit.attackRange() >= distancevv(pnum, mloc_to_v(ml), go)
					&& gc.canAttack(id, unitAt.get(v_to_mloc(go))))
				o.attack(id, unitAt.get(v_to_mloc(go)));
		}
	}

	// private Direction goodToBuild(MapLocation ml) {
	// if (karbonite < bc.bcUnitTypeblueprintCost(UnitType.Factory))
	// return null;
	// for (Direction dir : Direction.values())
	// if (valid(mlto(ml,dir)) && map[pnum].isPassableTerrainAt(mlto(ml,dir)) == 1
	// && !isUnit(mlto(ml,dir)))
	// return dir;
	// return null;
	// }

	private Direction nearRepair(MLocation ml) {
		for (Direction dir : Direction.values())
			if (valid(mlto(ml, dir)) && isUnit(mlto(ml, dir)) && unit(mlto(ml, dir)).unitType() == UnitType.Factory
					&& unit(mlto(ml, dir)).health() < unit(mlto(ml, dir)).maxHealth()
					&& unit(mlto(ml, dir)).team() == teami)
				return dir;
		return null;
	}

	private Direction nearKarbonite(MLocation ml) {
		for (Direction dir : Direction.values()) {
			MapLocation mal = maplocation(mlto(ml, dir));
			if (valid(mlto(ml, dir)) && gc.karboniteAt(mal) > 0)
				return dir;
		}
		return null;
	}

	private Direction nearNotBuild(MLocation ml) {
		for (Direction dir : Direction.values()) {
			// System.out.println(mlto(ml, dir) + " " + valid(mlto(ml, dir)) + " ");
			// if (valid(mlto(ml, dir)))
			// System.out.println(isUnit(mlto(ml, dir)));
			// if (isUnit(mlto(ml, dir))) {
			// System.out.println(unitAt.get(mlto(ml, dir)) + " - " + unitAt.size());
			// System.out.println(unitById.containsKey(unitAt.get(mlto(ml, dir))) + " -
			// " +
			// unitById.size());
			// if (round > 228 && valid(mlto(ml, dir)) && isUnit(mlto(ml, dir)) &&
			// unitAt.get(mlto(ml, dir)) == 4) {
			// System.out.println(ml);
			// System.out.println(dir);
			// System.out.println(mlto(ml, dir));
			// System.out.println(unit(mlto(ml, dir)));
			// System.out.println(unitAt.get(mlto(ml, dir)) + " - " + unitAt.size());
			// System.out.println(unitById.containsKey(unitAt.get(mlto(ml, dir))) + " -
			// " +
			// unitById.size());
			// // for (Integer ii : unitAt.values())
			// // System.out.println(ii);
			// // for (Integer ii : unitById.keySet())
			// // System.out.println(ii);
			// }

			if (valid(mlto(ml, dir)) && isUnit(mlto(ml, dir)) && unit(mlto(ml, dir)).unitType() == UnitType.Factory
					&& unit(mlto(ml, dir)).structureIsBuilt() != 1 && unit(mlto(ml, dir)).team() == teami)
				return dir;
		}
		return null;
	}

	public boolean valid(MLocation ml) {
		if (ml != null && map[pnum].isPassableTerrainAt(maplocation(ml)) == 1)
			return true;
		return false;
	}

	public Direction nearEmpty(MLocation ml) {
		for (Direction dir : Direction.values()) {
			MapLocation mal = maplocation(mlto(ml, dir));
			if (valid(mlto(ml, dir)) && map[pnum].isPassableTerrainAt(mal) == 1 && !isUnit(mlto(ml, dir)))
				return dir;
		}
		return null;
	}

	public Integer v_to_x(Integer pnum, Integer v) {
		return (int) (v % map[pnum].getWidth());
	}

	public Integer v_to_x(Integer v) {
		return (int) (v % map[pnum].getWidth());
	}

	public Integer v_to_y(Integer pnum, Integer v) {
		return (int) (v / map[pnum].getWidth());
	}

	public Integer v_to_y(Integer v) {
		return (int) (v / map[pnum].getWidth());
	}

	public Integer xy_to_v(Integer pnum, Integer x, Integer y) {
		return (int) (y * map[pnum].getWidth() + x);
	}

	public Integer xy_to_v(Integer x, Integer y) {
		return (int) (y * map[pnum].getWidth() + x);
	}

	public MLocation v_to_mloc(Integer pnum, Integer v) {
		return vtml[pnum].get(v);

	}

	public MLocation v_to_mloc(Integer v) {
		return vtml[pnum].get(v);
	}

	private Integer bml_to_v(MapLocation ml) {
		return xy_to_v(ml.getPlanet().ordinal(), ml.getX(), ml.getY());
	}

	public Integer mloc_to_v(MLocation ml) {
		return xy_to_v(ml.planet.ordinal(), ml.x, ml.y);
	}

	public Integer distancevv(Integer pnum, Integer v1, Integer v2) {
		return distancevm(v1, v_to_mloc(pnum, v2));
	}

	public Integer distancevm(Integer v, MLocation ml) {
		return distancemm(v_to_mloc(ml.planet.ordinal(), v), ml);
	}

	public Integer distancemm(MLocation ml1, MLocation ml2) {
		return (ml1.x - ml2.x) * (ml1.x - ml2.x) + (ml1.y - ml2.y) * (ml1.y - ml2.y);
	}

	/*
	 * Floyd Warshall calculate all pair shortest path on distance and path. O(n^3)
	 */
	// private void Floyd_Warshall() {
	// for (Integer i = 0; i < size[pnum]; ++i)
	// for (Integer j = 0; j < size[pnum]; ++j)
	// distance[i][j] = Integer.MAX_VALUE / 2;
	//
	// for (Integer i = 0; i < size[pnum]; ++i)
	// for (Integer adjNode : adjacencyList[pnum][i])
	// distance[i][adjNode] = 1;
	//
	// for (Integer k = 0; k < size[pnum]; ++k)
	// for (Integer i = 0; i < size[pnum]; ++i)
	// for (Integer j = 0; j < size[pnum]; ++j)
	// if (distance[i][k] + distance[k][j] < distance[i][j])
	// distance[i][j] = distance[i][k] + distance[k][j];
	//
	// }

	/*
	 * bfs all calculate all pair shortest path on distance and path. O(mn) could be
	 * better can not set mark and ....
	 */
	private void BFS_ALL() {
		for (Integer start = 0; start < size[pnum]; ++start) {
			Queue<Integer> queue = new LinkedList<Integer>();
			Integer[] mark = new Integer[size[pnum]];
			Integer ie;
			for (Integer i = 0; i < size[pnum]; ++i) {
				mark[i] = 0;
				distance[start][i] = Integer.MAX_VALUE / 2;
			}
			queue.add(start);
			mark[start] = 1;
			distance[start][start] = 0;

			while (!queue.isEmpty()) {
				ie = queue.poll();
				for (Integer it : adjacencyList[pnum][ie]) {
					if (mark[it] != 1) {
						distance[start][it] = distance[start][ie] + 1;
						if (mark[it] == 0)
							queue.add(it);
						mark[it] = 1;
					}
				}
			}
		}
	}

	/*
	 * Normal BFS with start nodes and block nodes that dosen't pass the block
	 * nodes. parent and near and distance as parameter that set in the BFS.
	 * O(m+n)->O(n)
	 */
	private void BFS(intVec start, intVec block, int[] near, int[] distance, int size, intVec[] adjacencyList) {
		Queue<Integer> queue = new LinkedList<Integer>();
		Integer[] mark = new Integer[size];
		Integer ie;
		for (Integer i = 0; i < size; ++i) {
			mark[i] = 0;
			near[i] = -1;
			distance[i] = Integer.MAX_VALUE / 2;
		}
		for (Integer it : start) {
			queue.add(it);
			mark[it] = 1;
			distance[it] = 0;
			near[it] = it;
		}
		if (block != null)
			for (Integer it : block)
				mark[it] = -1;
		while (!queue.isEmpty()) {
			ie = queue.poll();
			for (Integer it : adjacencyList[ie]) {
				if (mark[it] != 1) {
					near[it] = near[ie];
					distance[it] = distance[ie] + 1;
					if (mark[it] == 0)
						queue.add(it);
					mark[it] = 1;
				}
			}
		}
	}

	// not optimal every time BFS ....
	public Integer BFS(Integer pnum, Integer start, Integer end) {
		Queue<Integer> queue = new LinkedList<Integer>();
		Integer[] mark = new Integer[size[pnum]];
		Integer[] road = new Integer[size[pnum]];
		Integer[] distance = new Integer[size[pnum]];
		Integer ie;
		for (Integer i = 0; i < size[pnum]; ++i) {
			mark[i] = 0;
			distance[i] = Integer.MAX_VALUE / 2;
		}

		queue.add(start);
		mark[start] = 1;
		distance[start] = 0;

		while (!queue.isEmpty()) {
			ie = queue.poll();
			for (Integer it : adjacencyList[pnum][ie]) {
				MLocation ml = v_to_mloc(pnum, it);
				MapLocation mal = maplocation(ml);
				// TODO no block just for first move
				if (mark[it] != 1 && (!gc.canSenseLocation(mal) || !isUnit(ml) || distancevv(pnum, start, it) > 2)) {
					if (ie != start)
						road[it] = road[ie];
					else
						road[it] = it;
					distance[it] = distance[ie] + 1;
					if (mark[it] == 0)
						queue.add(it);
					mark[it] = 1;
					if (it == end) {
						return road[it];
					}
				}
			}
		}
		return -1;
	}

	public Integer best(Integer pnum, Integer start, Integer end) {
		Integer minDis = distance[start][end];
		Integer bestnear = -1;
		for (Integer near : adjacencyList[pnum][start]) {
			// TODO == ??
			if (!isUnit(v_to_mloc(near)) && distance[near][end] <= minDis) {
				minDis = distance[near][end];
				bestnear = near;
			}
		}
		return bestnear;
	}

	// not optimal every time BFS ....
	public Integer WBFS(Integer pnum, Integer start) {
		Queue<Integer> queue = new LinkedList<Integer>();
		Integer[] mark = new Integer[size[pnum]];
		Integer[] road = new Integer[size[pnum]];
		Integer[] distance = new Integer[size[pnum]];
		Integer ie;
		for (Integer i = 0; i < size[pnum]; ++i) {
			mark[i] = 0;
			distance[i] = Integer.MAX_VALUE / 2;
		}

		queue.add(start);
		mark[start] = 1;
		distance[start] = 0;

		while (!queue.isEmpty()) {
			ie = queue.poll();
			for (Integer it : adjacencyList[pnum][ie]) {
				// TODO DELETE MAP
				MLocation ml = v_to_mloc(pnum, it);
				MapLocation mal = maplocation(ml);
				// TODO no block just for first move
				if (mark[it] != 1 && (!gc.canSenseLocation(mal) || !isUnit(ml) || ie != start)) {
					if (ie != start)
						road[it] = road[ie];
					else
						road[it] = it;
					distance[it] = distance[ie] + 1;
					if (mark[it] == 0)
						queue.add(it);
					mark[it] = 1;
					if (gc.canSenseLocation(mal)) {
						if (gc.karboniteAt(mal) > 0 && !isUnit(ml))
							return it;
					} else if (map[pnum].initialKarboniteAt(mal) > 0)
						return it;
				}
			}
		}
		return -1;
	}

	// TAKE CARE about change
	// MAYBE NOT EXIST
	// Unit IDDDDDDDDDDDD not V!!!

	public Unit unit(Integer id) {
		if (id == null)
			return null;
		return unitById.get(id);
	}

	public Unit unit(MLocation ml) {
		if (ml == null)
			return null;
		return unit(unitAt.get(ml));
	}

	public boolean isUnit(MLocation ml) {
		if (ml == null)
			return false;
		return unitAt.containsKey(ml);
	}

	private Integer[] dx = new Integer[] { 0, 1, 1, 1, 0, -1, -1, -1, 0 };
	private Integer[] dy = new Integer[] { 1, 1, 0, -1, -1, -1, 0, 1, 0 };

	public MLocation mlto(MLocation ml, Direction dir) {
		if (ml.x + dx[dir.ordinal()] < 0 || ml.x + dx[dir.ordinal()] >= map[ml.planet.ordinal()].getWidth()
				|| ml.y + dy[dir.ordinal()] < 0 || ml.y + dy[dir.ordinal()] >= map[ml.planet.ordinal()].getHeight())
			return null;
		return v_to_mloc(ml.planet.ordinal(), xy_to_v(ml.x + dx[dir.ordinal()], ml.y + dy[dir.ordinal()]));
	}

	public MLocation mlocation(MapLocation mal) {
		if (mal == null)
			return null;
		return vtml[mal.getPlanet().ordinal()].get(bml_to_v(mal));
	}

	public MLocation mlocation(Integer id) {
		if (id == null)
			return null;
		return mlocation(unit(id).location().mapLocation());
	}

	public MapLocation maplocation(MLocation ml) {
		if (ml == null)
			return null;
		return vtm[ml.planet.ordinal()].get(mloc_to_v(ml));
	}

	public void initial_structure_point() {

	}

}
