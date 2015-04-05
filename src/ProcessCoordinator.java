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
            } else if(curLine.equals("show")){
                int process = Integer.parseInt(curLine.split(" ")[1]);
                show(process);
            }else if(curLine.equals("show all")){
                showAll();
            }
        }
        System.out.println("Exiting System");
    }

    public void print(String toPrint){
        System.out.println(toPrint);
    }

    public void printOneLine(String toPrint){ System.out.print(toPrint);}

    public void join(int p){
        if(myThreads[p] == null) {
            ChordThread n = new ChordThread(p, zero);
            print("Haven't put into queue yet");
            myThreads[p] = n;
            n.start();

        } else {
            print("Node " + p + " already in the network");
        }

    }

    public void find(int p, int k){
        if(myThreads[p] != null){

            FindCommand fc = new FindCommand(k);
            myThreads[p].inputQueue.add(new ThreadMessage(fc, null, null));
            synchronized (fc){
                try{
                    fc.wait();
                } catch(InterruptedException e){
                    print("Error occurred while waiting for find command");
                }
            }
        } else {
            print("Node " + p + " does not exist");
        }
    }

    public void leave(int p){
        if(myThreads[p] != null){
            LeaveCommand fc = new LeaveCommand(p);
            myThreads[p].inputQueue.add(new ThreadMessage(fc, null, null));
            synchronized (fc){
                try{
                    fc.wait();
                } catch(InterruptedException e){
                    print("Error occurred while waiting for leave command");
                }
            }
            myThreads[p]=null;
        } else {
            print("Node " + p + " does not exist");
        }
    }

    public void show(int p){
        if(myThreads[p] != null) {
            ArrayList<Integer> keyList= myThreads[p].getKeys();
            for (Integer key: keyList){
                printOneLine(key.toString() + ",");
            }
            print("");
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
