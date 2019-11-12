
/**
 * Job is something unit planed to do
 * <p>
 * about what unit most do in next turns
 * <p>
 * gc and bc are our main connection to outer classes.
 * 
 * @author MohammadReza DaneshvarAmoli <dos.moreda @ gmail.com>
 * @version 1.5.0 (current version number of program)
 * @since 1.2.0 (the version of the package this class was first added to)
 */
public class Job {
	public boolean hasJob = false;
	public int id = -1;
	public int target = -1;
	public String type = "NOTHING";
	public String arg = "NOTHING";
	public int karbonite;
	public int target_id;
	public int needDistance;

	public Job(int id) {
		this.id = id;
	}
	// public final Queue<Consumer<GameController>> delayedMoves = new
	// ArrayDeque<>();

}
