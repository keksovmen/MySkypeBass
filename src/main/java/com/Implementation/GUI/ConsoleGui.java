package com.Implementation.GUI;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Model.UnEditableModel;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Pipeline.CompositeComponent;
import com.Implementation.CLI.CmdArgs;
import com.Implementation.Client.Client;

import java.util.ArrayList;
import java.util.List;

public class ConsoleGui implements CompositeComponent
{
	private final List<ButtonsHandler> buttonsHandlers;


	public ConsoleGui()
	{
		buttonsHandlers = new ArrayList<>();
	}


	@Override
	public void handleRequest(BUTTONS button, Object[] data)
	{
		buttonsHandlers.forEach(buttonsHandler -> buttonsHandler.handleRequest(button, data));
	}

	@Override
	public void observe(ACTIONS action, Object[] data)
	{
		switch (action)
		{
			case PORT_ALREADY_BUSY:
				System.out.println("Port is already occupied, failed to start server");
				System.exit(-1);
				break;

			case INVALID_AUDIO_FORMAT:
				System.out.println("Invalid audio format change something");
				System.exit(-2);
				break;

			case SERVER_CREATED:
				System.out.println("Server created, leeeeets goooooo to siiiiiiiiiiix");
				break;
		}
	}

	@Override
	public void modelObservation(UnEditableModel model)
	{
		System.out.println("Model update");
	}

	@Override
	public void attach(ButtonsHandler listener)
	{
		buttonsHandlers.add(listener);
		if(listener instanceof Client){
			handleRequest(BUTTONS.CREATE_SERVER, new Object[]{
					String.valueOf(CmdArgs.getInstance().getPort()),
					String.valueOf(CmdArgs.getInstance().getSampleRate()),
					String.valueOf(CmdArgs.getInstance().getSampleSize()),
					CmdArgs.getInstance().isUseEncryption(),
					CmdArgs.getInstance().isUseUdp()
			});
		}
	}

	@Override
	public void detach(ButtonsHandler listener)
	{
		buttonsHandlers.remove(listener);
	}
}
