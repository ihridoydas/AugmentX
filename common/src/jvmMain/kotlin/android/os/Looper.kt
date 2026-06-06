package android.os

class Looper private constructor(private val thread: Thread) {
    companion object {
        private val mainThread = Thread.currentThread()
        private val instance = Looper(mainThread)

        @JvmStatic
        fun getMainLooper(): Looper = instance

        @JvmStatic
        fun myLooper(): Looper? {
            return if (Thread.currentThread() == mainThread) instance else null
        }
    }

    fun getThread(): Thread = thread

    fun quit() {}
    fun quitSafely() {}
}
