package com.Pipeline;

import com.Client.ButtonsHandler;
import com.Util.Interfaces.Registration;

public interface CompositeComponent extends SimpleComponent, Registration<ButtonsHandler> {
}
