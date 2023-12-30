package com.sap.stepbystep.kmf.store.interfac;

import java.io.InputStream;

public interface IKMFStreamHandler {
    void handle(InputStream stream);
    Object getResult();
    Exception getException();
}
