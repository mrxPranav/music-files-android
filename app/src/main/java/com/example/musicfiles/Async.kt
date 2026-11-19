package com.example.musicfiles

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

private val backgroundExecutor = Executors.newSingleThreadExecutor()
private val mainHandler = Handler(Looper.getMainLooper())

/** Runs [work] off the main thread, then calls [done] on the main thread. */
fun <T> runAsync(work: () -> T, done: (T) -> Unit) {
    backgroundExecutor.execute {
        val result = work()
        mainHandler.post { done(result) }
    }
}
