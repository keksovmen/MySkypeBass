package com.Implementation.GUI;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Model.UnEditableModel;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Pipeline.CompositeComponent;
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
		System.out.printf("Button %s\n", button.name());
		buttonsHandlers.forEach(buttonsHandler -> buttonsHandler.handleRequest(button, data));
	}

	@Override
	public void observe(ACTIONS action, Object[] data)
	{
		System.out.printf("Action %s\n", action.name());
	}

	@Override
	public void modelObservation(UnEditableModel model)
	{
		System.out.printf("Model update");
	}

	@Override
	public void attach(ButtonsHandler listener)
	{
		buttonsHandlers.add(listener);
		if(listener instanceof Client){
			handleRequest(BUTTONS.CREATE_SERVER, new Object[]{
					"8188",
					"20000",
					"8",
					Boolean.TRUE,
					Boolean.TRUE
			});
		}
	}

	@Override
	public void detach(ButtonsHandler listener)
	{
		buttonsHandlers.remove(listener);
	}

}
