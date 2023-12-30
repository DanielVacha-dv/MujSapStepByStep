package com.sap.stepbystep.kmf.store.model;

import com.sap.stepbystep.kmf.store.interfac.IKMFStreamHandler;

import java.util.HashMap;
import java.util.Map;

public class KMFStreamHandlersBuffer {
    private Map<String, IKMFStreamHandler> mStreamHandlers = new HashMap<>();

    public void add(String key, IKMFStreamHandler streamHandler) {
        this.mStreamHandlers.put(key, streamHandler);
    }

    public IKMFStreamHandler get(String key) {
        IKMFStreamHandler streamHandler = this.mStreamHandlers.get(key);
        this.mStreamHandlers.remove(key);
        return streamHandler;
    }

    public boolean hasKey(String key) {
        return this.mStreamHandlers.containsKey(key);
    }
}
