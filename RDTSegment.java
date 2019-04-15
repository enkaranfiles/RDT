/**
 * @author Atakan Filgöz 151101073
 * @author Enes Karanfil 151101046
 */

package rdt;

import java.util.Timer;


//it represents a UDP segments
public class RDTSegment {
    private byte[] data;

    Timer timer = new Timer();
    TimeoutHandler timeoutHandler;
    int seqNum;
    int ackNum;
    int flags;
    int checksum;
    int rcvWin;
    int length;  // Number of data bytes (<= MSS)
    static final int SEQ_NUM_OFFSET = 0;
    static final int ACK_NUM_OFFSET = 4;
    static final int FLAGS_OFFSET = 8;
    static final int CHECKSUM_OFFSET = 12;
    static final int RCV_WIN_OFFSET = 16;
    static final int LENGTH_OFFSET = 20;
    static final int HDR_SIZE = 24;
    static final int FLAGS_ACK = 1;
    static final int FLAGS_ACKED = 2;
    static final int FLAGS_FIN = 3;

    RDTSegment() {
        seqNum = 0;
        ackNum = 0;
        flags = 0;
        checksum = 0;
        rcvWin = 0;
        length = 0;
        data = new byte[RDT.MSS];
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return this.data;
    }

    public boolean containsAck() {
        if (flags == FLAGS_ACK) {
            return true;
        }
        return false;
    }

    public boolean containsData() {
        if (length > 0) {
            return true;
        }
        return false;
    }

    public int computeChecksum() {
        int check_sum = 0;
        int i = 0;
        check_sum += (0xff & (((length & 0xff000000) >> 24) + ((length & 0x00ff0000) >> 16) + ((length & 0x0000ff00) >> 8) + (length & 0x000000ff)));
        check_sum += (0xff & (((seqNum & 0xff000000) >> 24) + ((seqNum & 0x00ff0000) >> 16) + ((seqNum & 0x0000ff00) >> 8) + (seqNum & 0x000000ff)));
        check_sum += (0xff & (((rcvWin & 0xff000000) >> 24) + ((rcvWin & 0x00ff0000) >> 16) + ((rcvWin & 0x0000ff00) >> 8) + (rcvWin & 0x000000ff)));
        check_sum += (0xff & (((ackNum & 0xff000000) >> 24) + ((ackNum & 0x00ff0000) >> 16) + ((ackNum & 0x0000ff00) >> 8) + (ackNum & 0x000000ff)));
        check_sum += (0xff & (((flags & 0xff000000) >> 24) + ((flags & 0x00ff0000) >> 16) + ((flags & 0x0000ff00) >> 8) + (flags & 0x000000ff)));

        while (i < length) {
            check_sum += (0xff & this.data[i]);
            i++;
        }

        return (0xff & check_sum);
    }

    public boolean isValid() {
        if (this.checksum == computeChecksum()) {
            return true;
        } else {
            return false;
        }
    }

    public void printHeader() {
        System.out.print("SeqNum:" + seqNum + " ");
        System.out.print("ackNum:" + ackNum + " ");
        System.out.print("flags:" + flags + " ");
        System.out.print("checksum:" + checksum + " ");
        System.out.print("rcvWin:" + rcvWin + " ");
        System.out.print("length:" + length + " ");
    }

    public void printData() {
        System.out.println("Data ... ");
        for (int i = 0; i < length; i++)
            System.out.print(data[i]);
        System.out.println(" ");
    }

    public void dump() {
        System.out.print(this + " ");
        printHeader();
        printData();
    }

    public String toString() {
        return "seg@" + Integer.toHexString(System.identityHashCode(this));
    }
} // end RDTSegment class