/**
 * @author Atakan Filgöz 151101073
 * @author Enes Karanfil 151101046
 */
package rdt;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.TimerTask;

class TimeoutHandler extends TimerTask {
    RDT.RDTBuffer sndBuf;
    RDTSegment seg;
    DatagramSocket socket;
    InetAddress dst_ip;
    int dst_port;

    TimeoutHandler(RDT.RDTBuffer sndBuf_, RDTSegment s, DatagramSocket sock,
                   InetAddress ip_addr, int p) {
        sndBuf = sndBuf_;
        seg = s;
        socket = sock;
        dst_ip = ip_addr;
        dst_port = p;
    }

    public void run() {
        System.out.println(System.currentTimeMillis() + ":Timeout for seg: " + seg.seqNum);
        System.out.flush();

        Utility.udp_send(seg, socket, dst_ip, dst_port, true);
    }
} // end TimeoutHandler class

