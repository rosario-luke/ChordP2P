/**
 * Created by lucas on 3/25/15.
 */
public class ClosestProFingerCommand extends Command{

    private int id;

    public ClosestProFingerCommand(int i){
        super("closestprofinger " + i);
        id = i;
    }

    public int getId(){
        return id;
    }
}
