import java.util.Scanner;

/**
 * Created by lucas on 3/25/15.
 */
public class ProcessCoordinator extends Thread {

    private ChordThread[] myThreads;
    private ChordThread zero;
    public static void main(String[] args) {
       System.out.println("Started Coordinator Thread");
        new ProcessCoordinator().startSystem();
    }

    public ProcessCoordinator(){
        myThreads = new ChordThread[256];
        zero = new ChordThread(0, null);
        myThreads[0] = zero;
        zero.run();
    }

    public void startSystem(){

        Scanner input = new Scanner(System.in);
        String curLine, command;

        while(!(curLine = input.nextLine()).equals("exit")){

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
            n.run();
        } else {
            print("Node " + p + " already in the network");
        }

    }

    public void find(int p, int k){

    }

    public void leave(int p){

    }

    public void show(int p){

    }

    public void showAll(){

    }
}
