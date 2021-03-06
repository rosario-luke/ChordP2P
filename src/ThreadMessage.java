/**
 * Created by lucas on 3/25/15.
 */
public class ThreadMessage {

    private Command command;
    private ChordThread origin;
    private ChordThread returnThread;

    public ThreadMessage(Command c, ChordThread o, ChordThread rt){
        command = c;
        origin = o;
        returnThread = rt;
    }

    public Command getCommand(){
        return command;
    }

    public ChordThread getOrigin(){
        return origin;
    }

    public ChordThread getReturnThread(){
        return returnThread;
    }
}
