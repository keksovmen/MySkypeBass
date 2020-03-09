package com.Abstraction.Util.Logging;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;

import static com.Abstraction.Util.Logging.LoggerUtils.clientLogger;

public class TreeMapProxy<K, V> extends TreeMap<K, V> {


    public TreeMapProxy() {
    }

    public TreeMapProxy(Comparator<? super K> comparator) {
        super(comparator);
    }

    public TreeMapProxy(Map<? extends K, ? extends V> m) {
        super(m);
    }

    public TreeMapProxy(SortedMap<K, ? extends V> m) {
        super(m);
    }

    @Override
    public V get(Object key) {
        V v = super.get(key);
        if (v == null)
            clientLogger.logp(Level.FINER, getClass().getName(), "get", "Trying to get a dude with key - " + key + " but he is null");
        return v;
    }
}
