package com.Implementation;

import com.Abstraction.AbstractApplicationFactory;
import com.Abstraction.Application;
import com.Implementation.CLI.CmdArgs;
import com.Implementation.CLI.ConsoleArgs;
import org.apache.commons.cli.*;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

public class Main
{

	public static final String NOTIFICATION_PATH = "/sound/messageNotification/";

	public static void main(String[] args) throws InterruptedException
	{
		AbstractApplicationFactory factory = factoryFromCmd(args);
		if(factory == null){
			return;
		}

		try {
			SwingUtilities.invokeAndWait(() -> {
				Application application = new Application(factory);
				application.start();
			});

		} catch (InterruptedException | InvocationTargetException e) {
			e.printStackTrace();
			System.err.println("Swing fucked up in thread invocation");
		}

		//to let server thread start otherwise jvm will be terminated
		if (factory instanceof DesktopApplicationFactoryConsole) {
			TimeUnit.SECONDS.sleep(5);
		}
	}

	private static AbstractApplicationFactory factoryFromCmd(String[] args)
	{
		boolean logEnable;
		boolean consoleApp;
		boolean tcpTransport;
		boolean useEncryption;
		int sampleSize = 16;
		int sampleRate = 20_000;
		int port = 8188;

		Options options = new Options();
		options.addOption(new Option(ConsoleArgs.HELP.cmdKey, "Display help info and exit"));
		options.addOption(new Option(ConsoleArgs.LOG_OFF.cmdKey, "Disable logging"));
		options.addOption(new Option(ConsoleArgs.CONSOLE_APP.cmdKey, "Start web server without GUI"));
		options.addOption(new Option(ConsoleArgs.UDP_PROTOCOL.cmdKey, "Use UDP protocol instead of TCP for audio transfer"));
		options.addOption(new Option(ConsoleArgs.USE_ENCRYPTION.cmdKey, "Use encryption for all the data"));
		options.addOption(Option.builder(ConsoleArgs.SAMPLE_SIZE.cmdKey).hasArg().
				argName("size").desc("8 or 16 bits").converter(Integer::parseInt).type(Integer.class).build());
		options.addOption(Option.builder(ConsoleArgs.SAMPLE_RATE.cmdKey).hasArg().
				argName("rate").desc("from 4000 to 48000").converter(Integer::parseInt).build());
		options.addOption(Option.builder(ConsoleArgs.PORT.cmdKey).hasArg().
				argName("rate").desc("port to listen for new connections").converter(Integer::parseInt).build());

		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine commandLine = parser.parse(options, args);

			if (commandLine.hasOption(ConsoleArgs.HELP.cmdKey)) {
				new HelpFormatter().printHelp(" ", options);
				return null;
			}

			logEnable = !commandLine.hasOption(ConsoleArgs.LOG_OFF.cmdKey);
			consoleApp = commandLine.hasOption(ConsoleArgs.CONSOLE_APP.cmdKey);
			tcpTransport = !commandLine.hasOption(ConsoleArgs.UDP_PROTOCOL.cmdKey);
			useEncryption = commandLine.hasOption(ConsoleArgs.USE_ENCRYPTION.cmdKey);

			if (commandLine.hasOption(ConsoleArgs.SAMPLE_SIZE.cmdKey)) {
				sampleSize = commandLine.getParsedOptionValue(ConsoleArgs.SAMPLE_SIZE.cmdKey);
			}
			if (commandLine.hasOption(ConsoleArgs.SAMPLE_RATE.cmdKey)) {
				sampleRate = commandLine.getParsedOptionValue(ConsoleArgs.SAMPLE_RATE.cmdKey);
			}
			if (commandLine.hasOption(ConsoleArgs.PORT.cmdKey)) {
				port = commandLine.getParsedOptionValue(ConsoleArgs.PORT.cmdKey);
			}

			CmdArgs.getInstance().setArgs(sampleSize, sampleRate, !tcpTransport, useEncryption, port);

		} catch (Exception e) {
			System.out.println(e.getMessage());
			new HelpFormatter().printHelp(" ", options);
			return null;
		}

		AbstractApplicationFactory result = new DesktopApplicationFactory();

		if (consoleApp) {
			result = new DesktopApplicationFactoryConsole(result);
		}

		if (logEnable) {
			result = new DesktopApplicationFactoryLog(result);
		}

		return result;
	}
}