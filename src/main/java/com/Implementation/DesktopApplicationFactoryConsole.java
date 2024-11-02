package com.Implementation;

import com.Abstraction.AbstractApplicationFactory;
import com.Abstraction.ApplicationFactoryDecorator;
import com.Abstraction.Pipeline.CompositeComponent;
import com.Implementation.GUI.ConsoleGui;

public class DesktopApplicationFactoryConsole extends ApplicationFactoryDecorator
{


	public DesktopApplicationFactoryConsole(AbstractApplicationFactory child)
	{
		super(child);
	}

	@Override
	public CompositeComponent createGUI()
	{
		return new ConsoleGui();
	}
}
