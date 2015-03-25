/**
 * Created by lucas on 3/25/15.
 */
public class FindCommand extends Command {

    private int key;

    public FindCommand(int k){
        super("find " + k);
        key = k;
    }

    public int getKey(){
        return key;
    }
}
