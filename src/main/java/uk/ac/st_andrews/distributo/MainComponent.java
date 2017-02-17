package uk.ac.st_andrews.distributo;

import org.apache.commons.cli.ParseException;
import uk.ac.st_andrews.distributo.lib.node.ReceiverNode;
import uk.ac.st_andrews.distributo.lib.node.FileServerNode;

import java.io.IOException;
import java.util.Arrays;

public class MainComponent {

    public static String DESCRIPTION_HEADER = "Distributo: distribute a file to multiple machines at once via multicast";
    public static String DESCRIPTION_FOOTER = "Requires Java 8";

    private static String USAGE = "usage: dst <send | receive> <args>";

    public static void main(String[] args) throws IOException, InterruptedException {
        MainComponent m = new MainComponent();
        if (args.length <= 0) {
            System.err.println(USAGE + "\n" + DESCRIPTION_HEADER + "\n" + DESCRIPTION_FOOTER);
            System.exit(1);
        }
        //take all arguments after the first, pass them to a sub-command
        String[] rest = Arrays.copyOfRange(args, 1, args.length);
        if (args[0].equals("send")) {
            m.send(rest);
        } else if (args[0].equals("receive")) {
            m.receive(rest);
        } else {
            System.err.println(USAGE + "\n" + DESCRIPTION_HEADER + "\n" + DESCRIPTION_FOOTER);
            System.exit(1);
        }
    }

    private void send(String[] args) throws InterruptedException, IOException {
        SenderOptions opts = new SenderOptions();
        try {
            opts.parseArgs(args);
        } catch (ParseException e) {
            opts.printHelp();
        }
        Thread t = new Thread(new FileServerNode(opts.GROUP_HOST, opts.GROUP_PORT, opts.CONTROL_PORT, opts.FILE));
        t.start();
        t.join();
    }

    private void receive(String[] args) throws InterruptedException, IOException {
        ReceiverOptions opts = new ReceiverOptions();
        try {
            opts.parseArgs(args);
        } catch (ParseException e) {
            opts.printHelp();
        }
        Thread t = new Thread(new ReceiverNode(opts.GROUP_HOST, opts.GROUP_PORT, opts.CONTROL_HOST, opts.CONTROL_PORT));
        t.start();
        t.join();
    }
}
