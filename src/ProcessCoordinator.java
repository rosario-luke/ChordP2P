import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by lucas on 3/25/15.
 */
public class ProcessCoordinator extends Thread {

    private ChordThread[] myThreads;
    private ChordThread zero;
    private String oFile;
    private boolean outputToFile = false;
    private PrintWriter printWriter;
    public static void main(String[] args) {
        System.out.println("Started Coordinator Thread");
        if(args.length == 2){
            new ProcessCoordinator().run(args[1]);
        }
        new ProcessCoordinator().run("");

    }



    public void run(String outputFile){
        if(!outputFile.equals("")){
            oFile = outputFile;
            outputToFile = true;
            try {
                printWriter = new PrintWriter(outputFile, "UTF-8");
            } catch(Exception e){
                System.out.println("FATAL ERROR ---- Error occured while opening file: " + outputFile);
                System.exit(1);
            }
        }
        myThreads = new ChordThread[256];
        zero = new ChordThread(0, null, printWriter);
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
            } else if(curLine.startsWith("show") && !curLine.equals("show all")){
                int process = Integer.parseInt(curLine.split(" ")[1]);
                show(process);
            }else if(curLine.equals("show all")){
                showAll();
            }
        }
        if(outputToFile){
            printWriter.close();
        }
        System.out.println("Exiting System");
        System.exit(0);
    }

    public void print(String toPrint){
            if(outputToFile){
                printWriter.println(toPrint);
            }
            // Ouput to STDOUT anyway just so we can see what's happening
            System.out.println(toPrint);

    }

    public void printOneLine(String toPrint){
        if(outputToFile){
            printWriter.print(toPrint);
        }
        System.out.print(toPrint);}

    public void join(int p){
        if(myThreads[p] == null) {
            ChordThread n = new ChordThread(p, zero, printWriter);

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
            print("Showing for p: " + p);
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
