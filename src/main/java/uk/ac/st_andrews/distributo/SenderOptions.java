package uk.ac.st_andrews.distributo;

import org.apache.commons.cli.*;

public class SenderOptions {

    public static String GROUP_HOST = null;
    public static int GROUP_PORT = 8532;
    public static int CONTROL_PORT = 8532;
    public static String FILE = null;
    public static int CLIENT_THRESHOLD = -1;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;

    public SenderOptions() {
        options = makeOptions();
        parser = new DefaultParser();
    }

    public void parseArgs(String[] args) throws ParseException {
        cmd = parser.parse(options, args);
        //handle the arguments
        if (cmd.hasOption("g"))
            GROUP_HOST = cmd.getOptionValue("g");
        if (cmd.hasOption("gp"))
            try {
                GROUP_PORT = Integer.parseInt(cmd.getOptionValue("gp"));
            } catch(NumberFormatException e) {
                throw new ParseException(e.getMessage());
            }
        if (cmd.hasOption("cp"))
            try {
                CONTROL_PORT = Integer.parseInt(cmd.getOptionValue("cp"));
            } catch(NumberFormatException e) {
                throw new ParseException(e.getMessage());
            }
        if (cmd.hasOption("f"))
            FILE = cmd.getOptionValue("f");
        if (cmd.hasOption("t"))
            try {
                CLIENT_THRESHOLD = Integer.parseInt(cmd.getOptionValue("t"));
            } catch(NumberFormatException e) {
                throw new ParseException(e.getMessage());
            }
    }

    private Options makeOptions() {
        Options o = new Options();
        o.addOption(OptionBuilder
                .withArgName("host")
                .hasArg()
                .isRequired()
                .withDescription("the hostname of the multicast group to send data over")
                .withLongOpt("group")
                .create("g"));
        o.addOption(OptionBuilder
                .withArgName("port")
                .hasArg()
                .withDescription("the port of the multicast group to send data over")
                .withLongOpt("group-port")
                .create("gp"));
        o.addOption(OptionBuilder
                .withArgName("port")
                .hasArg()
                .withDescription("the port to host the control server over")
                .withLongOpt("control-port")
                .create("cp"));
        o.addOption(OptionBuilder
                .withArgName("path")
                .hasArg()
                .isRequired()
                .withDescription("the complete path to the file to send")
                .withLongOpt("file")
                .create("f"));
        o.addOption(OptionBuilder
                .withArgName("n")
                .hasArg()
                .withDescription("the maximum number of clients to serve before stopping, will serve for infinity by" +
                        "default")
                .withLongOpt("threshold")
                .create("t"));
        return o;
    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("dst send", MainComponent.DESCRIPTION_HEADER, options, MainComponent.DESCRIPTION_FOOTER, true);
    }
}
