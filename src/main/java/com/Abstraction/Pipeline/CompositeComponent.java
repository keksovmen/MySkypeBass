package com.Abstraction.Pipeline;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Util.Interfaces.Registration;

/**
 * Observer as subject for ButtonHandler and observer for LogicObserver + Chain of Responsibility pattern
 */

public interface CompositeComponent extends SimpleComponent, Registration<ButtonsHandler> {
}
