/**
 * Created by lucas on 3/29/15.
 */
public class UpdateFingerTableCommand extends Command {
    private ChordThread finger;
    private int fingerIndex;
    boolean insert;

    public UpdateFingerTableCommand(ChordThread f, int i, boolean in){
        super("UpdateFingerTable");
        finger = f;
        fingerIndex = i;
        insert=in;
    }

    public ChordThread getFinger(){return finger;}

    public int getFingerIndex(){ return fingerIndex;}

    public boolean isInsert(){
        return insert;
    }
}
