@file:JvmName("CoroutineInterop")

package health.ere.ps.zeta.utils

import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object CoroutineInterop {

    @JvmStatic
    fun <T> resumeOk(c: Continuation<T>, value: T) {
        c.resume(value)
    }

    @JvmStatic
    fun <T> resumeErr(c: Continuation<T>, t: Throwable) {
        c.resumeWithException(t)
    }
}

