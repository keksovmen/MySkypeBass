package com.Implementation;

import com.Abstraction.AbstractApplicationFactory;
import com.Abstraction.Application;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

public class Main
{

	public static final String NOTIFICATION_PATH = "/sound/messageNotification/";

	public static void main(String[] args) throws InterruptedException
	{
		AbstractApplicationFactory factory = factoryFromCmd(args);
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
			TimeUnit.SECONDS.sleep(3);
		}
	}

	private static AbstractApplicationFactory factoryFromCmd(String[] args)
	{
		AbstractApplicationFactory result = new DesktopApplicationFactory();
		boolean logE = true;
		boolean console = false;

		for (String arg : args) {
			switch (arg) {
				case "-logD":
					logE = false;
					break;

				case "-cmd":
					console = true;
					break;
			}
		}

		if (console) {
			result = new DesktopApplicationFactoryConsole(result);
		}

		if (logE) {
			result = new DesktopApplicationFactoryLog(result);
		}

		return result;
	}
}