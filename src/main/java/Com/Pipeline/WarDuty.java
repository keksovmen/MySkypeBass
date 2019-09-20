package Com.Pipeline;

public interface WarDuty {

    /**
     * Check values in BUTTONS enum because most of the time they are null
     *
     * @param button what button was pressed
     * @param plainData use instance of to define what it is
     * @param stringData data as string
     * @param integerData data as int
     */

    void fight(BUTTONS button, Object plainData, String stringData, int integerData);
}
