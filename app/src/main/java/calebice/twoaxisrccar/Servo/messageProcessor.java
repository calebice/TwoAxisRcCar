package calebice.twoaxisrccar.Servo;


/**
 * Created by Caleb Ice on 4/8/17.
 *
 * Constructs a message to send over UDP determined by one of the two modes (Sport, Surveillance)
 */
public class messageProcessor {
    private String formatMessage;
    private int mode;


    public messageProcessor(){
        this.mode = 1;
    }


    public int getMode() {return mode;}
    public void setMode(int m) {this.mode = m;}

    /**
     * Sets Format like this:
     * x = XXX, y = XXX, z = XXX, d = XXX
     * Raspberry Pi parses each command into
     * x = 30, y = 90, z = 130, d = 1
     * message = "030090130001"
     *
     * @param x - x-axis accelerometer value
     * @param y - y-axis accelerometer value
     * @param z - z-axis accelerometer value
     * @param d - current motor drive state
     *            0 = no movement
     *            1 = Forward
     *            2 = Reverse
     */
    public void setFormatMessage(int x, int y, int z, int d) {
        x = modeCheck("x", x, this.mode);
        y = modeCheck("y", y, this.mode);
        z = modeCheck("z", z, this.mode);
        d = modeCheck("d", d, this.mode);
        int[] values = {x, y, z, d};
        int valueLength;
        String message = "", subMessage;

        //Loops through read in values to construct a String message to send to Server
        for(int c: values) {

            valueLength = String.valueOf(c).length();
            subMessage = String.valueOf(c);

            if(valueLength < 3) {
                while(valueLength < 3) {
                    subMessage = "0" + subMessage;
                    valueLength = valueLength + 1;
                }
            }
            message =  message + subMessage;
        }
        this.formatMessage = message;
    }

    /**
     * Processes what mode the RC car is in:
     * Sport - (x,z) servos locked to 90 degrees (default position), motor operations enabled
     * Surveillance - (y) servo locked to 90 degrees, motor operations disabled
     * @param component (x,y,z,d) which is the field to be determined
     * @param value the field value
     * @param m the current mode state 1 represents sport, 2 represents surveillance
     * @return Either the value received, or the default value (if disabled)
     */
    private int modeCheck(String component, int value, int m){
        value = validRange(value);
        switch(m){
            //Represents Sport Mode, disables camera servos (x,z) in forward direction, operate motor normally
            case 1:
                switch(component) {
                    case "x":
                    case "z":
                        return 9;
                    default:
                        return value;
                }

            //Represents Surveillance Mode, disables turning servo (y) in forward direction and stops motor
            case 2:
                switch(component){
                    case "y":
                        return 9;
                    case "d":
                        return 0;
                    default:
                        return value;
                }
            default:
                return value;
        }

    }

    /**
     * Ensures the value sent will be within acceptable ranges
     * @param axis x,y, or z value to be checked
     * @return 18 if it is above range, 0 if below, value if else
     */
    private int validRange(int axis){
        if(axis>18){
            return 18;
        }
        else if(axis<0)
            return 0;
        else
            return axis;
    }
    /**
     * @return returns the formatted servo controller message
     */
    public String getFormatMessage() {
        return formatMessage;
    }

}
