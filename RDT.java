/**
 * @author Atakan Filgöz 151101073
 * @author Enes Karanfil 151101046
 */
package rdt;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import static rdt.RDTSegment.*;

public class RDT {
    public static int MSS = 100; // Max segment size in bytes
    public static final int RTO = 500; // Retransmission Timeout in msec
    public static final int ERROR = -1;
    public static final int MAX_BUF_SIZE = 3;
    public static final int GBN = 1;   // Go back N protocol
    public static final int SR = 2;    // Selective Repeat
    public static int protocol = GBN;
    public static double lossRate = 0.0;
    public static Random random = new Random();
    public static Timer timer = new Timer();

    private DatagramSocket socket;
    private InetAddress dst_ip;
    private int dst_port;
    private int local_port;

    private ReceiverThread rcvThread;

    public int sequence_number = 0;

    RDTBuffer sndBuf;
    RDTBuffer rcvBuf;

    RDT(String dst_hostname_, int dst_port_, int local_port_) {
        local_port = local_port_;
        dst_port = dst_port_;

        try {
            socket = new DatagramSocket(local_port);
            dst_ip = InetAddress.getByName(dst_hostname_);
        } catch (IOException e) {
            System.out.println("RDT constructor: " + e);
        }

        sndBuf = new RDTBuffer(MAX_BUF_SIZE);

        rcvThread = new ReceiverThread(rcvBuf, sndBuf, socket, dst_ip, dst_port);
        rcvThread.start();
    }

    RDT(String dst_hostname_, int dst_port_, int local_port_, int sndBufSize, int rcvBufSize) {
        local_port = local_port_;
        dst_port = dst_port_;

        try {
            socket = new DatagramSocket(local_port);
            dst_ip = InetAddress.getByName(dst_hostname_);
        } catch (IOException e) {
            System.out.println("RDT constructor: " + e);
        }

        sndBuf = new RDTBuffer(sndBufSize);
        rcvBuf = new RDTBuffer(rcvBufSize);

        rcvThread = new ReceiverThread(rcvBuf, sndBuf, socket, dst_ip, dst_port);
        rcvThread.start();

    }
    //setting the simulated rate
    public static void setLossRate(double rate) {
        lossRate = rate;
    }

    //this method provides setting maximum size of body
    public static void setMSS(int maxSize) {
        MSS = maxSize;
    }


    //this method provides us to allocate a place segmentsinto a send buffer
    public int send(byte[] data, int size) {

        if (protocol == GBN) {
            if (size > MSS) {
                double numSegments = Math.ceil((double) size / MSS);
                for (int i = 0; i < numSegments; i++) {
                    RDTSegment seg = new RDTSegment();
                    byte segData[] = new byte[MSS];
                    int index = 0;
                    for (int j = MSS * i; j < (MSS * i) + MSS; j++) {
                        if (j < size) {
                            segData[index] = data[j];
                        } else {
                            segData[index] = 0;  // Filled last segment with zeros
                        }
                        index++;
                    }

                    seg.seqNum = sequence_number;
                    sequence_number++;
                    helperMethod(seg, segData, MSS);
                }
            } else {
                RDTSegment seg = new RDTSegment();
                seg.seqNum = sequence_number;
                sequence_number++;
                byte segData[] = new byte[size];
                for (int i = 0; i < size; i++)
                    segData[i] = data[i];

                helperMethod(seg, segData, size);
            }
        } else if (protocol == SR) {
            RDTSegment seg = new RDTSegment();
            seg.seqNum = sequence_number;
            sequence_number++;
            byte segData[] = new byte[size];
            for (int i = 0; i < size; i++) {
                segData[i] = data[i];
            }
            seg.length = size;
            seg.setData(segData);
            seg.checksum = seg.computeChecksum();

            sndBuf.putNext(seg); // Put segment into send buffer

            Utility.udp_send(seg, socket, dst_ip, dst_port, false);

            TimeoutHandler timeoutHandler = new TimeoutHandler(sndBuf, seg, socket, dst_ip, dst_port);
            seg.timeoutHandler = timeoutHandler;
            seg.timer.schedule(timeoutHandler, RTO, RTO); // Timer will be cancelled when ack is received

            sndBuf.nextSeqNum++;
        }

        return size;
    }

    private void helperMethod(RDTSegment seg, byte[] segData, int mss) {
        seg.length = mss;
        seg.setData(segData);
        seg.checksum = seg.computeChecksum();

        sndBuf.putNext(seg); // Put segment into send buffer

        Utility.udp_send(seg, socket, dst_ip, dst_port, false);

        if (sndBuf.base == sndBuf.nextSeqNum) {
            System.out.println(System.currentTimeMillis() + ":" + " START TIMER: " +
                    "base=" + sndBuf.base + " nextSeqNum=" + sndBuf.nextSeqNum);
            sndBuf.runTimerTask(socket, dst_ip, dst_port);
        }

        sndBuf.nextSeqNum++;
    }
    //takes packets and delivers them to application layer
    public int receive(byte[] buf, int size) {
        RDTSegment seg = rcvBuf.getNext();

        if (seg != null && seg.containsData()) {
            for (int i = 0; i < seg.length; i++) {
                buf[i] = seg.getData()[i];
            }
            String segData = Utility.dataToString(seg.getData());

            if (protocol == GBN) {
                System.out.println(System.currentTimeMillis() + ":" + " RECEIVED SEGMENT: " +
                        "SeqNum=" + seg.seqNum + " Checksum=" + seg.checksum + " Data=" + segData);
            } else {
                System.out.println(System.currentTimeMillis() + ":" + " SEGMENT DELIVERED TO UPPER LAYER: " +
                        "SeqNum=" + seg.seqNum + " Checksum=" + seg.checksum + " Data=" + segData);
            }
        } else {
            return 0;
        }

        return seg.length;
    }

    /** Closes the connection gracefully, using TCP teardown
     *
     */
    public void close() {
        // OPTIONAL: close the connection gracefully
        // you can use TCP-style connection termination process
    }

    class RDTBuffer {
        RDTSegment[] buf;
        int size;
        int next = 0;
        int base;
        int nextSeqNum;
        Semaphore semMutex; // For mutual exclusion
        Semaphore semFull;  // # of full slots
        Semaphore semEmpty; // # of Empty slots
        boolean receivedFirst = false; // initial value at first time is false

        RDTBuffer(int bufSize) {
            buf = new RDTSegment[bufSize];

            for (int i = 0; i < bufSize; i++) {
                buf[i] = null;
            }

            size = bufSize;
            base = 1;
            nextSeqNum = 1;
            semMutex = new Semaphore(1, true);
            semFull = new Semaphore(0, true);
            semEmpty = new Semaphore(bufSize, true);
            receivedFirst = false;
        }

        public void putNext(RDTSegment seg) {
            try {
                semEmpty.acquire(); // Wait for an empty slot
                semMutex.acquire(); // Acquire exclusive lock

                buf[nextSeqNum % size] = seg;
                next++;
                semMutex.release();
                semFull.release();
            } catch (InterruptedException e) {
                System.out.println("Buffer putNext(): " + e);
            }
        }

        public void putSeqNum(RDTSegment seg) {
            int value = seg.seqNum % size;
            this.buf[value] = seg;
        }

        public RDTSegment getNext() {
            RDTSegment segment = new RDTSegment();
            try {
                semFull.acquire();
                semMutex.acquire();
                segment = buf[nextSeqNum % size];
                nextSeqNum++;
                int ind = base % size;
                buf[ind] = null;
                base++;
                semMutex.release();
                semEmpty.release();
            } catch (InterruptedException e) {
                System.out.println("Buffer put(): " + e);
            }
            return isSegNull(segment) ? null : segment;
        }
        //returns boolean value for isSegment null this is used in upper method check it
        public boolean isSegNull(RDTSegment seg) {
            boolean value = false;
            if (seg == null) {
                value = true;
            } else {
                value = false;
            }
            return value;
        }
        //control method for if seq number is already been contains
        public boolean contains(int target) {
            boolean value = false;
            for (int i = 0; i < this.size; i++) {
                if (this.buf[i] != null) {
                    if (this.buf[i].seqNum == target) {
                        return value = true;
                    }
                }
            }
            return value;
        }
        //for GBN
        public void runTimerTask(DatagramSocket socket, InetAddress dst_ip, int dst_port) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    int i = base % size;
                    System.out.println(System.currentTimeMillis() + ":" + " Restart Timer: " + "Resend all un-acked packets starting from base=" + base);
                    if (size > 1) {
                        while (i < (nextSeqNum % size)) {
                            Utility.udp_send(buf[i], socket, dst_ip, dst_port, true);
                            i++;
                        }
                    } else {
                        Utility.udp_send(buf[0], socket, dst_ip, dst_port, true);
                    }
                }
            };
            timer = new Timer();
            timer.schedule(task, RTO, RTO);
        }

    }


    //deliver from packets from network
    class ReceiverThread extends Thread {
        private RDTBuffer rcvBuf, sndBuf;
        private DatagramSocket socket;
        private InetAddress dst_ip;
        private int dst_port;
        private int expectedSeqNum = 0;
        private int lastRecvdSeqNum = 0;

        ReceiverThread(RDTBuffer rcv_buf, RDTBuffer snd_buf, DatagramSocket s,
                       InetAddress dst_ip_, int dst_port_) {
            rcvBuf = rcv_buf;
            sndBuf = snd_buf;
            socket = s;
            dst_ip = dst_ip_;
            dst_port = dst_port_;
        }

        @Override
        public void run() {

            while (true) {
                byte[] buffer = new byte[MSS + HDR_SIZE];
                DatagramPacket pkt = new DatagramPacket(buffer, MSS + HDR_SIZE);

                try {
                    socket.receive(pkt);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                RDTSegment seg = new RDTSegment();
                makeSegment(seg, pkt.getData());
                if (protocol == GBN) {
                    if (seg.containsAck()) {
                        if (seg.ackNum == -1) {
                            System.out.println(System.currentTimeMillis() + ":" + " Received ACK with the ACK No : " + seg.ackNum);
                            timer.cancel();
                            sndBuf.runTimerTask(socket, dst_ip, dst_port);
                            continue;
                        }

                        if (sndBuf.buf[seg.ackNum % sndBuf.size].flags < FLAGS_ACKED) {
                            sndBuf.buf[seg.ackNum % sndBuf.size].flags = FLAGS_ACKED;
                        }

                        System.out.println(System.currentTimeMillis() + ":" + " RECEIVED ACK: " +
                                "Segment number " + seg.ackNum);

                        if (sndBuf.size > 1) {
                            if (seg.ackNum == sndBuf.base) {
                                sndBuf.base = seg.ackNum + 1;
                                sndBuf.semEmpty.release();
                            } else if (seg.ackNum > sndBuf.base) {
                                int numSlots = seg.ackNum - sndBuf.base;

                                for (int i = 0; i < numSlots; i++) {
                                    sndBuf.base++;
                                    sndBuf.semEmpty.release();
                                }
                            }
                        } else {
                            sndBuf.base = seg.ackNum + 1;
                            sndBuf.semEmpty.release();
                        }

                        if (sndBuf.base == sndBuf.nextSeqNum) {
                            try {
                                timer.cancel();
                                System.out.println(System.currentTimeMillis() + ":" + " Timer Cancelled...");
                            } catch (IllegalStateException e) {
                                System.out.println(e);
                            }
                        } else {
                            timer.cancel();
                            sndBuf.runTimerTask(socket, dst_ip, dst_port);
                        }
                    }

                    if (seg.containsData()) {
                        RDTSegment ack_seg = new RDTSegment();
                        boolean resend = false;

                        if (seg.seqNum == expectedSeqNum) {
                            ack_seg.ackNum = seg.seqNum;
                            expectedSeqNum++;
                            lastRecvdSeqNum = seg.seqNum;
                            rcvBuf.putNext(seg);
                            rcvBuf.receivedFirst = true;
                        } else {
                            if (rcvBuf.receivedFirst) {
                                ack_seg.ackNum = lastRecvdSeqNum;
                            } else {
                                ack_seg.ackNum = -1;
                            }
                            System.out.println(System.currentTimeMillis() + ":" + " Received out of order packet: " +
                                    "SeqNum=" + seg.seqNum);
                            resend = true;
                        }
                        ack_seg.flags = FLAGS_ACK;
                        Utility.udp_send(ack_seg, socket, dst_ip, dst_port, resend);
                    }
                }

                if (protocol == SR) {
                    if (seg.containsAck()) {
                        if (seg.ackNum < sndBuf.base) {
                            continue;
                        }
                        try {
                            sndBuf.semMutex.acquire();

                            sndBuf.buf[seg.ackNum % sndBuf.size].flags = FLAGS_ACKED;
                            sndBuf.buf[seg.ackNum % sndBuf.size].timer.cancel();
                            System.out.println(System.currentTimeMillis() + ":" + " Received ACK with the seg No : " + seg.ackNum);

                            if (seg.ackNum == sndBuf.base) {
                                sndBuf.base++;
                                sndBuf.semEmpty.release();

                                for (int i = sndBuf.base % sndBuf.size; i < sndBuf.size; i++) {
                                    if (sndBuf.buf[i] != null && sndBuf.buf[i].flags == FLAGS_ACKED) {
                                        sndBuf.base++;
                                        sndBuf.buf[i].timer.cancel();
                                        sndBuf.semEmpty.release();
                                    } else {
                                        break;
                                    }
                                }
                            }
                            sndBuf.semMutex.release();
                        } catch (InterruptedException e) {
                            System.out.println(e);
                        }
                    }

                    if (seg.containsData()) {
                        System.out.println(System.currentTimeMillis() + ":" + " Received Segment with the seg No : " + seg.seqNum);
                        RDTSegment ack_seg = new RDTSegment();
                        ack_seg.ackNum = seg.seqNum;
                        ack_seg.flags = FLAGS_ACK;
                        Utility.udp_send(ack_seg, socket, dst_ip, dst_port, false);

                        if (seg.seqNum >= rcvBuf.base && seg.seqNum < (rcvBuf.base + rcvBuf.size)) {
                            rcvBuf.putSeqNum(seg);
                            if (seg.seqNum == rcvBuf.base) {
                                int numToDeliver = 1;

                                try {
                                    rcvBuf.semMutex.acquire();

                                    rcvBuf.nextSeqNum = seg.seqNum;
                                    int next = rcvBuf.nextSeqNum;

                                    while (rcvBuf.contains(++next)) {
                                        numToDeliver++;
                                    }
                                    rcvBuf.semFull.release(numToDeliver);

                                    rcvBuf.semMutex.release();

                                    rcvBuf.base += numToDeliver;
                                } catch (InterruptedException e) {
                                    System.out.println(e);
                                }
                            }
                        }
                    }
                }
            }
        }

        public void makeSegment(RDTSegment seg, byte[] payload) {
            seg.seqNum = Utility.byteToInt(payload, RDTSegment.SEQ_NUM_OFFSET);
            seg.ackNum = Utility.byteToInt(payload, RDTSegment.ACK_NUM_OFFSET);
            seg.flags = Utility.byteToInt(payload, RDTSegment.FLAGS_OFFSET);
            seg.checksum = Utility.byteToInt(payload, RDTSegment.CHECKSUM_OFFSET);
            seg.rcvWin = Utility.byteToInt(payload, RDTSegment.RCV_WIN_OFFSET);
            seg.length = Utility.byteToInt(payload, RDTSegment.LENGTH_OFFSET);

            byte segData[] = new byte[seg.length];

            for (int i = 0; i < seg.length; i++) {
                segData[i] = payload[i + RDTSegment.HDR_SIZE];
            }

            seg.setData(segData);
        }

    } // end ReceiverThread class

}  // end RDT class
