package com.example.core.firebase

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Await extension for Google Play Services / Firebase Tasks with safety timeout.
 */
suspend fun <T> Task<T>.await(timeoutMs: Long = 5000L): T = withTimeout(timeoutMs) {
    suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (cont.isActive) {
                if (task.isSuccessful) {
                    cont.resume(task.result)
                } else {
                    cont.resumeWithException(
                        task.exception ?: RuntimeException("Firebase operation failed")
                    )
                }
            }
        }
    }
}
