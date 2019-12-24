package com.Abstraction.Pipeline;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Model.ModelObserver;

/**
 * Observer + Chain of Responsibility patterns
 */

public interface SimpleComponent extends ModelObserver, LogicObserver, ButtonsHandler {
}
