import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
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
            new ProcessCoordinator().run(args[1], false);
        } else if(args.length == 3){
            new ProcessCoordinator().run(args[1], true);
        } else {
            new ProcessCoordinator().run("", false);
        }

    }



    public void run(String outputFile, boolean tf){
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
        if(!tf) {
            print("Starting system");
            Scanner input = new Scanner(System.in);
            String curLine, command;
            print("Listening for Commands");
            while (!(curLine = input.nextLine()).equals("exit")) {
                print("Recieved command " + curLine);
                command = curLine.split(" ")[0];

                if (command.equals("join")) {
                    int process = Integer.parseInt(curLine.split(" ")[1]);
                    join(process);
                } else if (command.equals("find")) {
                    int process = Integer.parseInt(curLine.split(" ")[1]);
                    int key = Integer.parseInt(curLine.split(" ")[2]);
                    find(process, key);
                } else if (command.equals("leave")) {
                    int process = Integer.parseInt(curLine.split(" ")[1]);
                    leave(process);
                } else if (curLine.startsWith("show") && !curLine.equals("show all") && !curLine.equals("show count")) {
                    int process = Integer.parseInt(curLine.split(" ")[1]);
                    show(process);
                } else if (curLine.equals("show all")) {
                    showAll();
                } else if (curLine.equals("show count")) {
                    showMessageCount();
                } else if (curLine.equals("reset count")) {
                    resetMessageCount();
                }
            }
        } else {
            // Test Stuff
            int P = 30;
            int F = 68;
            Random rand = new Random(System.currentTimeMillis());
            ArrayList<Integer> nodeList = new ArrayList<Integer>();
            //nodeList.add(new Integer(0));

            // Add P Nodes
            for(int i = 0; i< P; i++){
                Integer k = rand.nextInt(255) + 1;
                while(nodeList.contains(k)){
                    k = rand.nextInt(255) + 1;
                }
                nodeList.add(k);
            }
            for(Integer k : nodeList){
                join(k);
            }

            int p1Count = showMessageCount();
            resetMessageCount();

            for(int i = 0; i < F; i++){
                int p = rand.nextInt(P);
                int k = rand.nextInt(255);
                find(nodeList.get(p),k);
                try {
                    Thread.sleep(100);
                } catch(Exception e){

                }
            }

            int p2Count = showMessageCount();
            resetMessageCount();

            print("Test Summary");
            print("P = " + P);
            print("F = " + F);
            print("Phase 1 Total: " + p1Count);
            print("Phase 2 Total: " + p2Count);

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

    public int showMessageCount(){
        int total = 0;
        for(int i = 0; i< 256; i++){
            if(myThreads[i] != null){
                total += myThreads[i].getNumMessagesSent();
            }
        }
        print("Total # of Messages = " + total);
        return total;
    }

    public void resetMessageCount(){
        for(int i = 0; i < 256; i++){
            if(myThreads[i] != null){
                myThreads[i].resetNumMessagesSent();
            }
        }
        print("Reset MessageCounters to 0");
    }
}
