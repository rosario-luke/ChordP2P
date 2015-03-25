import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by lucas on 3/25/15.
 */
public class ChordThread implements Runnable {

    protected ArrayList<Integer> keys;
    protected int identifier;
    protected SynchronousQueue<String> inputQueue;
    protected Finger[] fingers;
    protected ChordThread predecessor;

    public ChordThread(int p, ChordThread helper){
        identifier = p;
        keys = new ArrayList<Integer>();
        inputQueue = new SynchronousQueue<String>();
        setUpFingerTable(helper);
    }

    public void setUpFingerTable(ChordThread helper){
        fingers = new Finger[9]; // not using the first entry, only 1-8
        if(helper == null){
            predecessor = this;
            for(int i  = 1; i<9; i++){
                fingers[i] = new Finger();
                fingers[i].node = this;
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


    public void print(String toPrint){
        System.out.println(toPrint);
    }
}
