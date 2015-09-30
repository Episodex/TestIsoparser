package com.example.episodex.testisoparser;

import android.util.Log;

public class UncaughtExceptionHandler  implements java.lang.Thread.UncaughtExceptionHandler  {
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.e("IsoparserTestUncaught", ex.getMessage(), ex);
    }
}
