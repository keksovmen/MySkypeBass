package com.Abstraction;

import com.Abstraction.Audio.Factory.AudioFactory;
import com.Abstraction.Audio.Helper.AudioHelper;
import com.Abstraction.Client.AbstractClient;
import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Model.ChangeableModel;
import com.Abstraction.Pipeline.CompositeComponent;
import com.Abstraction.Pipeline.SimpleComponent;
import com.Abstraction.Util.Logging.LogManagerHelper;
import com.Abstraction.Util.Resources.AbstractResources;

public class ApplicationFactoryDecorator extends AbstractApplicationFactory
{
	private final AbstractApplicationFactory child;

	public ApplicationFactoryDecorator(AbstractApplicationFactory child)
	{
		this.child = child;
	}

	@Override
	public AudioHelper createAudioHelper()
	{
		return getChild().createAudioHelper();
	}

	@Override
	public AbstractResources createResources()
	{
		return getChild().createResources();
	}

	@Override
	public AbstractClient createClient(ChangeableModel model)
	{
		return getChild().createClient(model);
	}

	@Override
	public CompositeComponent createGUI()
	{
		return getChild().createGUI();
	}

	@Override
	public SimpleComponent createAudio(ButtonsHandler helpHandler, AudioFactory factory)
	{
		return getChild().createAudio(helpHandler, factory);
	}

	@Override
	public AudioFactory createAudioFactory()
	{
		return getChild().createAudioFactory();
	}

	@Override
	public LogManagerHelper createLogManager()
	{
		return getChild().createLogManager();
	}

	public AbstractApplicationFactory getChild()
	{
		return child;
	}
}
