package com.Abstraction.Audio.Misc;

/**
 * Represent independent audio format
 *
 * ENCODING IS PCM 16BIT
 *
 * Little note: Android AudioFormat is SIGNED and LITTLE_ENDIAN for 16BIT frame size for 8 doesn't work any combination
 */

public class AbstractAudioFormat {

    //May not be used by android

    /**
     * Default channel amount
     */

    public static final int CHANNELS = 1;

    /**
     * Default signed state
     */

    public static final boolean SIGNED = true;

    /**
     * Default endian state
     */

    public static final boolean BIG_ENDIAN = false;


    private final int sampleRate;
    private final int sampleSizeInBits;
    private final int channelsAmount;
    private final boolean isSigned;
    private final boolean isBigEndian;


    public AbstractAudioFormat(int sampleRate, int sampleSizeInBits) {
        this.sampleRate = sampleRate;
        this.sampleSizeInBits = sampleSizeInBits;
        channelsAmount = CHANNELS;
        isSigned = SIGNED;
        isBigEndian = BIG_ENDIAN;
    }

    public AbstractAudioFormat(int sampleRate, int sampleSizeInBits, int channelsAmount, boolean isSigned, boolean isBigEndian) {
        this.sampleRate = sampleRate;
        this.sampleSizeInBits = sampleSizeInBits;
        this.channelsAmount = channelsAmount;
        this.isSigned = isSigned;
        this.isBigEndian = isBigEndian;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getSampleSizeInBits() {
        return sampleSizeInBits;
    }

    public int getChannelsAmount() {
        return channelsAmount;
    }

    public boolean isSigned() {
        return isSigned;
    }

    public boolean isBigEndian() {
        return isBigEndian;
    }

    public int getFrameSize(){
        return (sampleSizeInBits / 8) * channelsAmount;
    }
}
