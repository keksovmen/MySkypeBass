package com.Implementation.CLI;

public enum ConsoleArgs
{
	LOG_OFF("log-off"),
	CONSOLE_APP("cmd"),
	SAMPLE_SIZE("sample_size"),
	SAMPLE_RATE("sample_rate"),
	UDP_PROTOCOL("udp"),
	USE_ENCRYPTION("enc"),
	HELP("help"),
	PORT("port"),
	;

	ConsoleArgs(String cmd)
	{
		cmdKey = cmd;
	}

	public final String cmdKey;
}
