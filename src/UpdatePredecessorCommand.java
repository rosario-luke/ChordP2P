/**
 * Created by Yuriy on 4/4/2015.
 */
public class UpdatePredecessorCommand extends Command {
    private ChordThread predecessor;
    private int fingerIndex;

    public UpdatePredecessorCommand(ChordThread p){
        super("UpdatePredecessor");
        predecessor=p;
    }

    public ChordThread getPredecessor(){return predecessor;}
}
