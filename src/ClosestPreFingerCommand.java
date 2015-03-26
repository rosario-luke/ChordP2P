/**
 * Created by lucas on 3/25/15.
 */
public class ClosestPreFingerCommand extends Command{

    private int id;

    public ClosestPreFingerCommand(int i){
        super("closestprofinger " + i);
        id = i;
    }

    public int getId(){
        return id;
    }
}
