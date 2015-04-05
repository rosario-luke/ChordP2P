/**
 * Created by Yuriy on 4/4/2015.
 */
public class UpdateSuccessorCommand extends Command {
    private ChordThread successor;
    private int fingerIndex;

    public UpdateSuccessorCommand(ChordThread s){
        super("UpdateSuccessor");
        successor=s;
    }

    public ChordThread getSuccessor(){return successor;}
}
