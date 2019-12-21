package com.Abstraction.Pipeline;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Util.Interfaces.Registration;

public interface CompositeComponent extends SimpleComponent, Registration<ButtonsHandler> {
}
