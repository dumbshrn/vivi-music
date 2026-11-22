package android.util

/**
 * Desktop shims for the two Android `util` helpers used by the shared
 * Innertube module. They keep the ported sources byte-for-byte identical
 * to the Android originals.
 */
object Log {
    @JvmStatic
    fun d(tag: String, message: String): Int {
        println("D/$tag: $message")
        return message.length
    }

    @JvmStatic
    fun d(tag: String, message: String, throwable: Throwable?): Int {
        println("D/$tag: $message")
        throwable?.printStackTrace()
        return message.length
    }

    @JvmStatic
    fun i(tag: String, message: String): Int {
        println("I/$tag: $message")
        return message.length
    }

    @JvmStatic
    fun i(tag: String, message: String, throwable: Throwable?): Int {
        println("I/$tag: $message")
        throwable?.printStackTrace()
        return message.length
    }

    @JvmStatic
    fun w(tag: String, message: String): Int {
        println("W/$tag: $message")
        return message.length
    }

    @JvmStatic
    fun w(tag: String, message: String, throwable: Throwable?): Int {
        println("W/$tag: $message")
        throwable?.printStackTrace()
        return message.length
    }

    @JvmStatic
    fun e(tag: String, message: String): Int {
        System.err.println("E/$tag: $message")
        return message.length
    }

    @JvmStatic
    fun e(tag: String, message: String, throwable: Throwable?): Int {
        System.err.println("E/$tag: $message")
        throwable?.printStackTrace()
        return message.length
    }

    @JvmStatic
    fun e(tag: String, throwable: Throwable?): Int {
        System.err.println("E/$tag: ${throwable?.message}")
        throwable?.printStackTrace()
        return 0
    }
}

object Base64 {
    const val DEFAULT = 0
    const val NO_WRAP = 2
    const val NO_PADDING = 1
    const val URL_SAFE = 8

    @JvmStatic
    fun decode(str: String, flags: Int): ByteArray =
        if (flags and URL_SAFE != 0) {
            java.util.Base64.getUrlDecoder().decode(str)
        } else {
            java.util.Base64.getDecoder().decode(str)
        }

    @JvmStatic
    fun decode(str: String, flags: Int, offset: Int, length: Int): ByteArray =
        decode(str.substring(offset, offset + length), flags)

    @JvmStatic
    fun encodeToString(bytes: ByteArray, flags: Int): String {
        val encoded =
            if (flags and URL_SAFE != 0) {
                java.util.Base64.getUrlEncoder()
            } else {
                java.util.Base64.getEncoder()
            }
        val text = encoded.encodeToString(bytes)
        return if (flags and NO_WRAP != 0) text else text + "\n"
    }
}
