import java.io.PrintWriter;
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
    private boolean outputToFile = false;
    private PrintWriter printWriter;

    public ChordThread(int p, ChordThread helper, PrintWriter pW){
        identifier = p;
        keys = new ArrayList<Integer>();
        inputQueue = new SynchronousQueue<ThreadMessage>();
        setUpFingerTable(helper);
       /* for(int i = 1; i<9; i++){
            print("Finger[" + i + "] = node(" + fingers[i].node.identifier + ") for start " + fingers[i].start);
        }*/
        getKeysFromSuccessor();
        print("Node " + identifier + " successfully joined");
        if(pW != null){
            printWriter = pW;
            outputToFile = true;
        }

    }

    public void setUpFingerTable(ChordThread helper){
        fingers = new Finger[9]; // not using the first entry, only 1-8
        if(helper == null){ // Node 0
            predecessor = this;
            for(int i  = 1; i<9; i++){
                fingers[i] = new Finger();
                fingers[i].node = this;
                fingers[i].start = (identifier + (int)Math.pow(2,(i-1))) % 256;
                fingers[i].interval = new Interval(fingers[i].start, fingers[i].start);
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

            //should we use messages for these?
            UpdatePredecessorCommand p= new UpdatePredecessorCommand(this);
            sendUpdateMessage(fingers[1].node, this, p);
            predecessor= helper.findPredecessor(identifier);
            predecessor.fingers[1].node = this;


            // like this:
            //fingers[1].node.updatePredecessor(this);
            //UpdatePredecessorCommand g= new getPredecessorCommand(this);
            //sendUpdateMessage(fingers[1].node, this, g);
            /*UpdateSuccessorCommand s= new UpdateSuccessorCommand(this);
            sendUpdateMessage(predecessor, this, s);*/

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
                    if(betweenStartInclusive(identifier, fingers[i+1].start, ret.getReturnThread().identifier)){
                        fingers[i+1].node = this;
                    } else {
                        fingers[i + 1].node = ret.getReturnThread();
                    }
                }
            }
            updateOthers(true);


        }

    }

    public void getKeysFromSuccessor(){
        //print("Node " + identifier + " removing keys from predecessor " + predecessor.identifier);
        for(int i = predecessor.identifier + 1; i<identifier + 1; i++){
            fingers[1].node.keys.remove((Integer)(i));
            keys.add(i);
        }

    }

    public void removeKey(int k){
        keys.remove((Object)(k));
    }

    public void updateOthers(Boolean insert){
        for (int i=1; i<9; i++){
            int logTraversal=(int)Math.pow(2,i-1);
            int newIdentifier;
            if (logTraversal>identifier){
                newIdentifier=255+identifier-logTraversal;
            }
            else{
                newIdentifier=identifier-logTraversal;
            }
            //print("Updating for id (" + identifier + " - " + logTraversal + ") = " + newIdentifier);
            ChordThread p= findPredecessor(newIdentifier);

            //print("findPredecessor(" + newIdentifier + ") = " +  p.identifier);
            if(p.identifier != this.identifier) {
                //print("Sending updatemessage to node " + p.identifier + " with node " + identifier + " for index " + i + " for identifier " + newIdentifier);
                UpdateFingerTableCommand c;
                if (insert) {
                     c= new UpdateFingerTableCommand(this, i, insert);
                }
                else{
                    c= new UpdateFingerTableCommand(fingers[1].node, i, insert);
                }
                sendUpdateMessage(p, this, c);
                //updateFingerTable(p, this, i);
            }
        }
    }

    public void sendUpdateMessage(ChordThread target, ChordThread s, Command c){
       // print("Sending update message to node " + target.identifier + " from node " + identifier);
        if(target == s){ return;}
        ThreadMessage nMessage = new ThreadMessage(c,this,null);
        boolean caughtError = true;
        while(caughtError){
            try{
                target.inputQueue.add(nMessage);
                caughtError = false;
            } catch(IllegalStateException e){

            }
        }
        try{
            ThreadMessage response = inputQueue.take();
        } catch(InterruptedException e){

        }
        //print("FINISHED");
    }
    public void updateFingerTable(ChordThread s, int i, boolean insert){
        //print("Node " + identifier + " recieved updatefingertablecommand about node  " + identifier);
        if (insert) {
            if (identifier == s.identifier) {
                return;
            }
            if (betweenStartInclusive(s.identifier, identifier, fingers[i].node.identifier)) {
                //print("Node " + identifier + " updating finger " + i + " to node " + s.identifier);
                fingers[i].node = s;
                fingers[i].interval = new Interval(fingers[i].start, s.identifier);
                ChordThread n = predecessor;
                // isnt this a complete traversal?
                UpdateFingerTableCommand c = new UpdateFingerTableCommand(s, i, insert);
                sendUpdateMessage(n, s, c);
                //updateFingerTable(n, s,i);

            }
        }
        else{
            fingers[i].node=s;
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
                boolean caughtError = true;
                while(caughtError){
                    try{
                        message.getOrigin().inputQueue.add(r);
                        caughtError = false;
                    } catch(IllegalStateException e){

                    }
                }

            } else if(c instanceof FindCommand){
                /*if(message.getOrigin() == null) {
                    for (int i = 1; i < 9; i++) {
                        print("Finger[" + i + "] = node(" + fingers[i].node.identifier + ") for start " + fingers[i].start);
                    }
                }*/
                int id = ((FindCommand) c).getKey();
                ChordThread hasKey = findSuccessor(id);
                if(message.getOrigin() != null){
                    ThreadMessage r = new ThreadMessage(c, this, hasKey);
                    boolean caughtError = true;
                    while(caughtError) {
                        try {
                            message.getOrigin().inputQueue.add(r);
                            caughtError = false;
                        } catch(IllegalStateException e){
                            print("Caught error");
                        }
                    }
                } else {

                    print("Thread " + identifier + " found key " + id + " at node " + hasKey.identifier);
                    // Wake up ProcessCoordinator
                    synchronized(c){
                        c.notifyAll();
                    }
                }
            } else if(c instanceof UpdateFingerTableCommand){
                UpdateFingerTableCommand ftc = (UpdateFingerTableCommand)c;
                updateFingerTable(ftc.getFinger(), ftc.getFingerIndex(), ftc.isInsert());
                ThreadMessage m = new ThreadMessage(new Command("Acknowledgement"), this, null);
                boolean errorCaught = true;
                while(errorCaught){
                    try{
                        message.getOrigin().inputQueue.add(m);
                        errorCaught = false;
                    } catch(IllegalStateException e){

                    }
                }
            } else if(c instanceof UpdateKeysCommand){
                UpdateKeysCommand keysCommand= (UpdateKeysCommand)c;
                updateKeys(keysCommand.getStart(), keysCommand.getEnd());
                ThreadMessage m = new ThreadMessage(new Command("Acknowledgement"), this, null);
                boolean errorCaught = true;
                while(errorCaught){
                    try{
                        message.getOrigin().inputQueue.add(m);
                        errorCaught = false;
                    } catch(IllegalStateException e){

                    }
                }
            }  else if(c instanceof UpdatePredecessorCommand){
                UpdatePredecessorCommand predecessorCommand= (UpdatePredecessorCommand)c;
                updatePredecessor(predecessorCommand.getPredecessor());
                ThreadMessage m = new ThreadMessage(new Command("Acknowledgement"), this, null);
                boolean errorCaught = true;
                while(errorCaught){
                    try{
                        message.getOrigin().inputQueue.add(m);
                        errorCaught = false;
                    } catch(IllegalStateException e){

                    }
                }
            }
            else if(c instanceof LeaveCommand){
                removeSelf();
                ThreadMessage m = new ThreadMessage(new Command("Acknowledgement"), this, null);
                print("Thread "+identifier+" has left the system");
                // Wake up ProcessCoordinator
                synchronized(c){
                    c.notifyAll();
                }
            }



        }

    }

    public ChordThread findSuccessor(int id){
        if(keys.contains(id)) { return this;}
        ChordThread n = findPredecessor(id);
        //print("Find successor returned node " + n.identifier + " with successor " + n.fingers[1].node.identifier);
        return n.fingers[1].node;
    }

    public ChordThread getClosestPrecedingFinger(int id){

        for(int i = 8; i > 0; i--){

            if(inBetween(fingers[i].node.identifier, identifier, id)){
                return fingers[i].node;
            }
        }
        return this;
    }

    public boolean inBetween(int id, int start, int end){
        if(start > end){
            return id > start || id < end;
        } else {
            return id > start && id < end;
        }
    }

    public ChordThread findPredecessor(int id){

        ChordThread m = this;
        while(!betweenEndInclusive((id % 256), m.identifier, m.fingers[1].node.identifier)){
            if(m!= this) {
                ClosestPreFingerCommand c = new ClosestPreFingerCommand(id);
                ThreadMessage message = new ThreadMessage(c, this, null);
                boolean caughtError = true;
                while(caughtError){
                    try{
                        m.inputQueue.add(message);
                        caughtError = false;
                    } catch(IllegalStateException e){

                    }
                }


                try{
                    m = inputQueue.take().getReturnThread();
                } catch(InterruptedException e){
                    System.out.println("Return thread failed");
                    e.printStackTrace();
                    continue;
                }
            } else {
                m = getClosestPrecedingFinger(id);
            }



        }
        return m;
    }

    public boolean betweenEndInclusive(int id, int start, int end){
        if(start == end){
            return true;
        } else if(start > end){
            return (id >start || id <= end);
        } else{
            return id>start && id<=end;
        }
    }
    public boolean betweenStartInclusive(int id, int start, int end){
        if(start == end){
            return true;
        } else if(start > end){
            return (id >=start || id < end);
        } else{
            return id>=start && id<end;
        }
    }
    public ArrayList<Integer> getKeys(){
        return keys;
    }
    public void updateKeys(int start, int end){
        if (end<start) {
            for (int i = start; i < 256; i++) {
                keys.add(i);
            }
            for (int i = 0; i < end; i++) {
                keys.add(i);
            }
        }
        else{
            for (int i= start; i<end; i++){
                keys.add(i);
            }
        }

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
    public void removeSelf(){
        UpdateKeysCommand k= new UpdateKeysCommand(predecessor.identifier, identifier+1);
        sendUpdateMessage(fingers[1].node, this, k);
        updateOthers(false);
        UpdatePredecessorCommand p= new UpdatePredecessorCommand(predecessor);
        sendUpdateMessage(fingers[1].node, this, p);
        keys.clear();
        fingers=null;
        predecessor=null;
    }
    public void start() {

        if (t == null) {
            t = new Thread(this, "Node:" + identifier);
        }
        t.start();
    }
    public void print(String toPrint){
        if(outputToFile){
            printWriter.println(toPrint);
        }
        // Ouput to STDOUT anyway just so we can see what's happening
        System.out.println(toPrint);
    }
}
