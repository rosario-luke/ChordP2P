/**
 * Created by lucas on 3/25/15.
 */
public class JoinCommand extends Command {

    private int process;

    public JoinCommand(int p){
        super("join " + p);
        process = p;
    }

    public int getProcess(){
        return process;
    }
}
