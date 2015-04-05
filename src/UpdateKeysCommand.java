/**
 * Created by Yuriy on 4/4/2015.
 */
public class UpdateKeysCommand extends Command {
    private int start;
    private int end;

    public UpdateKeysCommand(int s, int e){
        super("UpdateKeys");
        start=s;
        end=e;
    }

    public int getStart(){return start;}
    public int getEnd(){return end;}
}
