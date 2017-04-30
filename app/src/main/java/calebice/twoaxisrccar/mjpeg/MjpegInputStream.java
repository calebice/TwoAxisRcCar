package calebice.twoaxisrccar.mjpeg;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

/**
 * Class that accesses the stream of a given url and reads out individual frames from the
 * available data converting these into a bitmap to be painted onto a canvas
 */
public class MjpegInputStream extends DataInputStream {
    /*Representative Bytes for the start of an image*/
    private final byte[] SOI_MARKER = { (byte) 0xFF, (byte) 0xD8 };
    /*Indicates the End of a frame*/
    private final byte[] EOF_MARKER = { (byte) 0xFF, (byte) 0xD9 };
    private final String CONTENT_LENGTH = "Content-Length";
    private final static int HEADER_MAX_LENGTH = 100;
    private final static int FRAME_MAX_LENGTH = 40000 + HEADER_MAX_LENGTH;
    private int mContentLength = -1;

    /**
     * attempts to connect to a URL and returns the available stream
     * @param urlString the url to connect to
     * @return a MjpegInputStream connected to URL or null if none available
     */
    public static MjpegInputStream read(String urlString) {
        HttpURLConnection conn;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();

            return new MjpegInputStream(conn.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Called from read(String urlString) builds a MjpegInputStream using InputStream information
     * @param in The url.openConnection(): Should be a Mjpeg format stream
     */
    public MjpegInputStream(InputStream in) { super(new BufferedInputStream(in, FRAME_MAX_LENGTH)); }

    /**
     * Takes in a stream of bytes and a desired sequence and returns the end position of that
     * Sequence
     * @param in BufferedInputStream from URL
     * @param sequence desired sequence (either SOI or EOF)
     * @return the end position of a given sequence, or -1 if that does not exist
     * @throws IOException If there is not a valid InputStream
     */
    private int getEndOfSequence(DataInputStream in, byte[] sequence) throws IOException {
        int seqIndex = 0;
        byte c;
        for(int i=0; i < FRAME_MAX_LENGTH; i++) {
            c = (byte) in.readUnsignedByte();
            if(c == sequence[seqIndex]) {
                seqIndex++;
                if(seqIndex == sequence.length) return i + 1;
            } else seqIndex = 0;
        }
        return -1;
    }

    /**
     * Finds the end of the desired sequence and then sets the starting position off of it
     * @param in BufferedInputStream from specified URL
     * @param sequence
     * @return
     * @throws IOException
     */
    private int getStartOfSequence(DataInputStream in, byte[] sequence) throws IOException {
        int end = getEndOfSequence(in, sequence);
        return (end < 0) ? (-1) : (end - sequence.length);
    }

    /**
     * Passses the header message into a Properties hashtable that
     * @param headerBytes the bytes in the header
     * @return the length of the message
     * @throws IOException If there is no stream available
     * @throws NumberFormatException if the file is incorrect
     */
    private int parseContentLength(byte[] headerBytes) throws IOException, NumberFormatException {
        //Creates a byte array stream out of the header bytes in a jpeg
        ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
        Properties props = new Properties();
        //adds key and element pairs from header bytes, in order to access length of jpeg byte sequence
        props.load(headerIn);
        return Integer.parseInt(props.getProperty(CONTENT_LENGTH));
    }

    /**
     * Reads in a single frame of max length 40100 bytes and then converts message into bitmap
     * @return converted ByteStream into a bitmap (grid of pixels)
     * @throws IOException If the stream is null
     */
    public Bitmap readMjpegFrame() throws IOException {
        //Sets up maximum frame length
        mark(FRAME_MAX_LENGTH);
        //Finds the beginning point of the image (0xFF 0xD8
        int headerLen = getStartOfSequence(this, SOI_MARKER);
        reset();
        //Resets the stream to the beginning
        byte[] header = new byte[headerLen];
        readFully(header);
        //puts header bytes into byte array
        try {
            mContentLength = parseContentLength(header);
        } catch (NumberFormatException nfe) {
            mContentLength = getEndOfSequence(this, EOF_MARKER);
        }
        //Puts the stream marker to the beginning of the sequence
        reset();
        byte[] frameData = new byte[mContentLength];
        //Skips the header information
        skipBytes(headerLen);
        //Reads the byte stream into the byte array frameData
        readFully(frameData);
        //converts the frameData into a bitmap in order to draw onto frame
        return BitmapFactory.decodeStream(new ByteArrayInputStream(frameData));
    }

}

