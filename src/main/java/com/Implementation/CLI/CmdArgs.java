package com.Implementation.CLI;

public class CmdArgs
{
	private static CmdArgs instance;

	private int sampleSize;
	private int sampleRate;
	private int port;
	private boolean useUdp;
	private boolean useEncryption;



	private CmdArgs()
	{
	}

	public static CmdArgs getInstance()
	{
		if(instance == null){
			instance = new CmdArgs();
		}

		return instance;
	}

	public void setArgs(int sampleSize, int sampleRate, boolean useUdp, boolean useEncryption, int port)
	{
		if(sampleSize != 8 && sampleSize != 16){
			throw new IllegalArgumentException("Sample size must be 8 or 16");
		}

		if(sampleRate < 4000 || sampleRate > 48000){
			throw new IllegalArgumentException("Sample rate must be in range [4000, 48000]");
		}

		if(port < 0 || port > Short.MAX_VALUE * 2 - 1){
			throw new IllegalArgumentException("Illegal port value");
		}

		this.sampleSize = sampleSize;
		this.sampleRate = sampleRate;
		this.useUdp = useUdp;
		this.useEncryption = useEncryption;
		this.port = port;
	}

	public int getSampleSize()
	{
		return sampleSize;
	}

	public int getSampleRate()
	{
		return sampleRate;
	}

	public boolean isUseUdp()
	{
		return useUdp;
	}

	public boolean isUseEncryption()
	{
		return useEncryption;
	}

	public int getPort()
	{
		return port;
	}
}
