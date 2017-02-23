package uk.ac.st_andrews.distributo.lib.sender;

import uk.ac.st_andrews.distributo.lib.protocol.FileSplitter;
import uk.ac.st_andrews.distributo.lib.protocol.Packet;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class Sender implements Runnable {

    private MulticastSocket dataSocket;
    private ServerSocket controlSocket;

    private int dataPort, controlPort;
    private InetAddress dataGroup;

    private File _file;
    private FileSplitter splitter;

    private volatile boolean servingData, servingControl;

    private Map<InetAddress, Socket> clients;
    private int maxClients, seenClients;
    private boolean limitClients;

    public Sender(String groupHostname, int dataPort, int controlPort, String file, int maxClients) throws IOException {
        dataGroup = InetAddress.getByName(groupHostname);
        this.dataPort = dataPort;
        this.controlPort = controlPort;
        //check that the file exists etc.
        File f = new File(file);
        if (!f.exists())
            throw new IOException("file \"" + file + "\" doesn't exist");
        if (f.isDirectory())
            throw new IOException("file \"" + file + "\" is a directory, currently not supported");
        this._file = f;
        this.splitter = new FileSplitter(f);
        this.maxClients = maxClients;
        limitClients = maxClients > 0;
        servingData = false;
        clients = new HashMap<>();
        //add a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("received shutdown signal");
                stopServingControl();
                stopServingData();
            } catch (IOException e) {
                System.err.println("error when shutting down");
                e.printStackTrace();
            }
        }));
    }

    public Sender(String groupHostname, int dataPort, int controlPort, String file) throws IOException {
        this(groupHostname, dataPort, controlPort, file, 0);
    }

    /**
     * Stop serving data if the server already is.
     */
    private void stopServingData() throws IOException {
        servingData = false;
        if (dataSocket != null && !dataSocket.isClosed())
            dataSocket.close();
    }

    private void stopServingControl() throws IOException {
        servingControl = false;
        if (!controlSocket.isClosed())
            controlSocket.close();
    }

    @Override
    public void run() {
        //start listening for clients
        try {
            serveControl();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Package-local method to safely add clients to the registered list
     * @param client the client to be added to the registered list
     */
    synchronized void registerClient(Socket client) throws IOException {
        clients.put(client.getInetAddress(), client);
        //if this is the first client, start serving data (if not already)
        if (clients.size() >= 1 && !servingData) {
            //instantiate our threads
            new Thread(() -> {
                try {
                    serveData();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }).start();
        }
        //printConnectedClients();
    }

    /**
     * Package-local method to safely deregister clients
     * @param address the address of the client to be deregistered
     */
    synchronized void deregisterClient(InetAddress address) throws IOException {
        clients.remove(address);
        if (clients.size() <= 0)
            stopServingData();
        //printConnectedClients();
        if (limitClients && (maxClients - ++seenClients <= 0)) {
            //this will call the shutdown hook to cleanup
            System.out.println("serviced max client count");
            System.exit(0);
        }
        if (limitClients)
            System.out.println("serviced " + seenClients + "/" + maxClients + " clients");
    }

    /**
     * Begin serving control information for clients.
     */
    private void serveControl() throws IOException {
        try {
            System.out.println("setting up control server on " + controlPort);
            controlSocket = new ServerSocket(controlPort);
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
        servingControl = true;
        while(servingControl) try {
            //System.out.println("awaiting control requests");
            Socket client = controlSocket.accept();
            Thread handler = new Thread(new ReceiverControlHandler(client, this));
            new Thread(handler).start();
        } catch (SocketException e) {
            //we expect this, but probably want to log it anyway
            System.out.println("control socket closed");
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Begin serving data to clients.
     */
    private void serveData() throws IOException {
        servingData = true;
        dataSocket = new MulticastSocket();
        System.out.printf("joining group: %s on %s\n", dataGroup.toString(), dataPort);
        dataSocket.joinGroup(dataGroup);
        int i = 0;
        while (servingData) try {
            String msg = _file.getName();
            //continually go round the file, like a 'data carousel'
            //(Gemmell, Schooler, Gray) "Fcast multicast file distribution" IEEE Network. Mag. 2000. 14(1): p. 58-68
            Packet p = splitter.nextDataPacket();
            //System.out.println(p.packetno());
            byte[] data = p.marshal();
            DatagramPacket datagram = new DatagramPacket(data, data.length, dataGroup, dataPort);
            dataSocket.send(datagram);
        }catch (SocketException e) {
            //we expect this, but probably want to log it anyway
            System.out.println("data socket closed");
        } catch (IOException e) {
            System.err.println("error when serving data");
            stopServingData();
            throw e;
        }
    }

    private void printConnectedClients() {
        if (clients.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("connected clients");
            for (InetAddress client : clients.keySet())
                sb.append("\n- ").append(client.getHostName());
            System.out.println(sb.toString());
        } else
            System.out.println("no clients connected");
    }

    public File file() {
        return _file;
    }
}
