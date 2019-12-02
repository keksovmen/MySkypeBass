package com.Client;

import com.Model.ChangeableModel;
import com.Networking.Readers.BaseReader;
import com.Networking.Utility.Users.ClientUser;
import com.Networking.Writers.ClientWriter;
import com.Pipeline.ACTIONS;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractClient implements Logic {


    protected final ChangeableModel model;
    private final List<LogicObserver> observerList;

    public AbstractClient(ChangeableModel model) {
        this.model = model;
        observerList = new ArrayList<>();
    }

    public abstract ClientUser authenticate(BaseReader reader, ClientWriter writer, String myName);

    @Override
    public void notifyObservers(ACTIONS action, Object[] data) {
        observerList.forEach(logicObserver -> logicObserver.observe(action, data));
    }

    @Override
    public void attach(LogicObserver listener) {
        if (!observerList.contains(listener)) {
            observerList.add(listener);
        }
    }

    @Override
    public void detach(LogicObserver listener) {
        observerList.remove(listener);
    }

    public ChangeableModel getModel() {
        return model;
    }
}
