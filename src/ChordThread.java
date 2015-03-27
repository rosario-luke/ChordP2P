import java.util.ArrayList;
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
    private Thread t;

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
            for(int i = 1; i<9; i++){
                fingers[i] = new Finger();
                fingers[i].start = (identifier + (int)Math.pow(2,(i-1))) % 256;
            }
            for(int i = 1; i<9; i++){
                if(i == 8){
                    fingers[i].interval = new Interval(fingers[i].start, fingers[1].start);
                } else {
                    fingers[i].interval = new Interval(fingers[i].start, fingers[i + 1].start);
                }
            }

            fingers[1].node= helper.findSuccessor(fingers[1].start);
            fingers[1].interval = new Interval(fingers[1].start, fingers[1].node.identifier);

            predecessor= fingers[1].node.getPredecessor();
            fingers[1].node.updatePredecessor(this);
            for (int i=1; i<8 ;i++){ // only up to 8 since we're doing 2-step accesses
                if (betweenStartInclusive(fingers[i+1].start, identifier, fingers[i].node.getIdentifier())){
                    fingers[i+1].node=fingers[i].node;
                }
                else{
                    FindCommand fc = new FindCommand(fingers[i+1].start);
                    ThreadMessage m = new ThreadMessage(fc, this, null);
                    helper.inputQueue.add(m);
                    ThreadMessage ret = null;
                    try {
                        ret = inputQueue.take();
                    } catch(InterruptedException e){
                        e.printStackTrace();
                        print("Error asking helper for id: " + fingers[i + 1].start);
                        continue;
                    }
                    fingers[i+1].node = ret.getReturnThread();
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
        ThreadMessage message = null;
        while(true){

            try{
                message = inputQueue.take();
            } catch(InterruptedException e){
                System.out.println("Interrupt occurred for thread " + identifier);
                e.printStackTrace();
                continue;
            }

            Command c = message.getCommand();
            if(c instanceof ClosestPreFingerCommand){
                int id = ((ClosestPreFingerCommand) c).getId();
                ChordThread toReturn = getClosestPrecedingFinger(id);
                ThreadMessage r = new ThreadMessage(c, this, toReturn);
                message.getOrigin().inputQueue.add(r);
            } else if(c instanceof FindCommand){
                int id = ((FindCommand) c).getKey();
                ChordThread hasKey = findSuccessor(id);
                if(message.getOrigin() != null){
                    ThreadMessage r = new ThreadMessage(c, this, hasKey);
                    message.getOrigin().inputQueue.add(r);
                } else {
                    print("Thread " + hasKey.identifier + " found key " + id);
                }
            } else if(c instanceof JoinCommand){

            }



        }

    }

    public ChordThread findSuccessor(int id){
        if(keys.contains(id)) { return this;}
        ChordThread n = findPredecessor(id);
        return n.fingers[1].node;
    }

    public ChordThread getClosestPrecedingFinger(int id){

        for(int i = 8; i > 0; i--){
            if(fingers[i].node.identifier > identifier && fingers[i].node.identifier < id){
                return fingers[i].node;
            }
        }
        return this;
    }

    public ChordThread findPredecessor(int id){

        ChordThread m = this;
        while(!betweenEndInclusive((id % 256), m.identifier, m.fingers[1].node.identifier)){
            ClosestPreFingerCommand c = new ClosestPreFingerCommand(id);
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

    public void start() {

        if (t == null) {
            t = new Thread(this, "Node:" + identifier);
        }
        t.start();
    }

    public void print(String toPrint){
        System.out.println(toPrint);
    }
}
