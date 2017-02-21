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

    private Thread dataThread;

    private volatile boolean servingData, servingControl;

    private Map<InetAddress, Socket> clients;

    public Sender(String groupHostname, int dataPort, int controlPort, String file) throws IOException {
        dataGroup = InetAddress.getByName(groupHostname);
        this.dataPort = dataPort;
        this.controlPort = controlPort;
        dataSocket = new MulticastSocket();
        //check that the file exists etc.
        File f = new File(file);
        if (!f.exists())
            throw new IOException("file \"" + file + "\" doesn't exist");
        if (f.isDirectory())
            throw new IOException("file \"" + file + "\" is a directory, currently not supported");
        this._file = f;
        this.splitter = new FileSplitter(f);
        //instantiate our threads
        dataThread = new Thread(() -> {
            try {
                serveData();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        });
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

    /**
     * Stop serving data if the server already is.
     */
    private void stopServingData() throws IOException {
        boolean servingBefore = servingData;
        servingData = false;
        if (!dataSocket.isClosed())
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
            System.err.println(e.getMessage());
        }
    }

    /**
     * Package-local method to safely add clients to the registered list
     * @param client the client to be added to the registered list
     */
    void registerClient(Socket client) throws IOException {
        clients.put(client.getInetAddress(), client);
        //if this is the first client, start serving data (if not already)
        if (clients.size() >= 1 && !servingData)
            dataThread.start();
        //System.out.println(clients);
    }

    /**
     * Package-local method to safely deregister clients
     * @param address the address of the client to be deregistered
     */
    void deregisterClient(InetAddress address) throws IOException {
        clients.remove(address);
        if (clients.size() <= 0)
            stopServingData();
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
        System.out.println("awaiting control requests");
        while(servingControl) try {
            Socket client = controlSocket.accept();
            System.out.println("found client " + client.getInetAddress().toString() + ":" + client.getLocalPort());
            new Thread(new ReceiverControlHandler(client, this)).start();
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Begin serving data to clients.
     */
    private void serveData() throws IOException {
        servingData = true;
        System.out.printf("joining group: %s on %s\n", dataGroup.toString(), dataPort);
        dataSocket.joinGroup(dataGroup);
        int i = 0;
        while (servingData) try {
            //todo break file into packets
            String msg = _file.getName();
            //go round the file, like a "data carousel"
            //"Fcast multicast file distribution" (Gemmell, Schooler, Gray)
            Packet p = splitter.nextDataPacket();
            byte[] data = p.marshal();
            DatagramPacket datagram = new DatagramPacket(data, data.length, dataGroup, dataPort);
            dataSocket.send(datagram);
            if (i++ % 1 == 0) {
                System.out.println("sending packets...");
                Thread.sleep(1000);
            }
        } catch (IOException e) {
            System.err.println("error when serving data");
            stopServingData();
            throw e;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public File file() {
        return _file;
    }
}
