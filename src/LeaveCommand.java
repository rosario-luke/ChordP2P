/**
 * Created by Yuriy on 4/4/2015.
 */
public class LeaveCommand extends Command {

    private int process;
    public LeaveCommand(int p){
        super("leave " + p);
        process = p;
    }
    public int getProcess(){
        return process;
    }
}
