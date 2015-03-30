/**
 * Created by lucas on 3/29/15.
 */
public class UpdateFingerTableCommand extends Command {

    private ChordThread finger;
    private int fingerIndex;

    public UpdateFingerTableCommand(ChordThread f, int i){
        super("UpdateFingerTable");
        finger = f;
        fingerIndex = i;
    }

    public ChordThread getFinger(){return finger;}

    public int getFingerIndex(){ return fingerIndex;}
}
