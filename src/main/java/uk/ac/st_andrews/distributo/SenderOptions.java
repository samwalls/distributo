package uk.ac.st_andrews.distributo;

import org.apache.commons.cli.*;

public class SenderOptions {

    public static String GROUP_HOST = null;
    public static int GROUP_PORT = 8532;
    public static int CONTROL_PORT = 8532;
    public static String FILE = null;

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
            GROUP_HOST = cmd.getOptionValue("gp");
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
        return o;
    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("dst send", MainComponent.DESCRIPTION_HEADER, options, MainComponent.DESCRIPTION_FOOTER, true);
    }
}
