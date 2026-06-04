package android.os

class Looper private constructor() {
    companion object {
        private val instance = Looper()
        @JvmStatic
        fun getMainLooper(): Looper = instance
        @JvmStatic
        fun myLooper(): Looper? = instance
    }
}
