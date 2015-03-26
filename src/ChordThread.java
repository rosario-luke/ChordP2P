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
        if (helper!=null) {
            updateOthers();
        }
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
            fingers[1].node= helper.findSuccessor(identifier);
            predecessor= fingers[1].node.getPredecessor();
            fingers[1].node.updatePredecessor(this);
            for (int i=1; i<8 ;i++){ // only up to 8 since we're doing 2-step accesses
                if (betweenStartInclusive(i+1, identifier, fingers[i].node.getIdentifier())){
                    fingers[i+1].node=fingers[i].node;
                }
                else{
                    fingers[i+1].node=helper.findSuccessor(i+1);
                }
            }
            int startKeyPosition;
            // todo update key positions

        }

    }
    public void updateOthers(){
        for (int i=1; i<9; i++){
            int logTraversal=(int)Math.pow(2,i-1);
            int newIdentifier;
            if (logTraversal>identifier){
                newIdentifier=255+identifier-logTraversal;
            }
            else{
                newIdentifier=identifier-logTraversal;
            }

            ChordThread p= findPredecessor(newIdentifier);
            p.updateFingerTable(this, i);
        }
    }
    public void updateFingerTable(ChordThread s, int i){
        if (betweenStartInclusive(s.identifier, this.identifier, i)){
            fingers[i].node=s;
            ChordThread p= predecessor;
            p.updateFingerTable(s,i);

        }
    }
    public void run(){
        Command message = null;
        while(true){

            try{
                message = inputQueue.take().getCommand();
            } catch(InterruptedException e){
                System.out.println("Interrupt occurred for thread " + identifier);
                e.printStackTrace();
                continue;
            }


            print(message.getCommand());


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

            try{
                m = inputQueue.take().getReturnThread();
            } catch(InterruptedException e){
                System.out.println("Return thread failed");
                e.printStackTrace();
                continue;
            }

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
    public boolean betweenStartInclusive(int id, int start, int end){
        if(start == end){
            return id == start;
        } else if(start > end){
            return (id >=start || id < end);
        } else{
            return id>=start && id<end;
        }
    }
    public ArrayList<Integer> getKeys(){
        return keys;
    }
    public int getIdentifier(){
        return identifier;
    }
    public ChordThread getPredecessor(){
        return predecessor;
    }
    public void updatePredecessor(ChordThread newPredecessor){
        predecessor= newPredecessor;
    }


    public void print(String toPrint){
        System.out.println(toPrint);
    }
}
