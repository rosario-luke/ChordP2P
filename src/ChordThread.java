import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by lucas on 3/25/15.
 */
public class ChordThread implements Runnable {

    protected ArrayList<Integer> keys;
    protected int identifier;
    protected SynchronousQueue<ThreadMessage> inputQueue;
    protected Finger[] fingers;
    protected ChordThread predecessor;


    public ChordThread(int p, ChordThread helper){
        identifier = p;
        keys = new ArrayList<Integer>();
        inputQueue = new SynchronousQueue<ThreadMessage>();
        setUpFingerTable(helper);
    }

    public void setUpFingerTable(ChordThread helper){
        fingers = new Finger[9]; // not using the first entry, only 1-8
        if(helper == null){ // Node 0
            predecessor = this;
            for(int i  = 1; i<9; i++){
                fingers[i] = new Finger();
                fingers[i].node = this;
            }
            for(int i = 0; i < 256; i++){
                keys.add(i);
            }
        } else {
            // Ask the helper for each key to set it up
            // Tell other nodes to update their tables
        }

    }

    public void run(){
        String message = null;
        while(true){

            try{
                message = inputQueue.take();
            } catch(InterruptedException e){
                System.out.println("Interrupt occurred for thread " + identifier);
                e.printStackTrace();
                continue;
            }


            print(message);


        }

    }

    public ChordThread findSuccessor(int id){
        ChordThread n = findPredecessor(id);
        return n.fingers[1].node;
    }

    public ChordThread findPredecessor(int id){
        ChordThread m = this;
        while(!betweenEndInclusive(id, m.identifier, m.fingers[1].node.identifier)){
            ClosestProFingerCommand c = new ClosestProFingerCommand(id);
            ThreadMessage message = new ThreadMessage(c, this, null);
            m.inputQueue.add(message);
            m = inputQueue.take().getReturnThread();

        }
        return m;
    }

    public boolean betweenEndInclusive(int id, int start, int end){
        if(start == end){
            return id == start;
        } else if(start > end){
            return (id >start || id <= end);
        } else{
            return id>start && id<=end;
        }
    }


    public void print(String toPrint){
        System.out.println(toPrint);
    }
}
