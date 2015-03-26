import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by lucas on 3/25/15.
 */
public class ProcessCoordinator extends Thread {

    private ChordThread[] myThreads;
    private ChordThread zero;
    public static void main(String[] args) {
        System.out.println("Started Coordinator Thread");
        new ProcessCoordinator().run();
    }



    public void run(){
        myThreads = new ChordThread[256];
        zero = new ChordThread(0, null);
        myThreads[0] = zero;
        zero.start();
        print("Starting system");
        Scanner input = new Scanner(System.in);
        String curLine, command;
        print("Listening for Commands");
        while(!(curLine = input.nextLine()).equals("exit")){
            print("Recieved command " + curLine);
            command = curLine.split(" ")[0];

            if(command.equals("join")){
                int process = Integer.parseInt(curLine.split(" ")[1]);
                join(process);
            } else if(command.equals("find")){
                int process = Integer.parseInt(curLine.split(" ")[1]);
                int key = Integer.parseInt(curLine.split(" ")[2]);
                find(process, key);
            } else if(command.equals("leave")){
                int process = Integer.parseInt(curLine.split(" ")[1]);
                leave(process);
            } else if(command.equals("show")){
                int process = Integer.parseInt(curLine.split(" ")[1]);
                show(process);
            }else if(command.equals("show all")){
                showAll();
            }
        }
        System.out.println("Exiting System");
    }

    public void print(String toPrint){
        System.out.println(toPrint);
    }

    public void join(int p){
        if(myThreads[p] == null) {
            ChordThread n = new ChordThread(p, zero);
            myThreads[p] = n;
            n.start();
        } else {
            print("Node " + p + " already in the network");
        }

    }

    public void find(int p, int k){
        if(myThreads[p] != null){
            print("Sending command");
            FindCommand fc = new FindCommand(k);
            myThreads[p].inputQueue.add(new ThreadMessage(fc, null, null));
        } else {
            print("Node " + p + " does not exist");
        }
    }

    public void leave(int p){

    }

    public void show(int p){
        if(myThreads[p] != null) {
            ArrayList<Integer> keyList= myThreads[p].getKeys();
            for (Integer key: keyList){
                print(key.toString());
            }
        } else {
            print("Node " + p + " does not exist in the network");
        }

    }

    public void showAll(){
        for (int i=0; i<256; i++){
            if (myThreads[i]!=null){
                print("Keys for node "+i);
                show(i);
            }
        }

    }
}
