package com.udacity.project4.utils

import android.util.Log
import com.udacity.project4.BuildConfig


internal object LogUtils {

    private var DEBUG = BuildConfig.DEBUG

    private const val TRACE_METHOD = "trace"

    private const val START_LOG_METHOD = "startLogMethod"
    private const val END_LOG_METHOD = "endLogMethod"

    private const val CLASS_NAME_INDEX = 0

    private const val METHOD_NAME_INDEX = 1

    /**
     *
     * @param debugMode Boolean input value of debugMode.
     */
    fun setDebugMode(debugMode: Boolean) {
        DEBUG = debugMode
    }

    /**
     * Send an information log message.
     *
     * @param content The message you would like logged.
     */
    fun i(content: String) {
        if (DEBUG) {
            val msg = trace()
            if (msg != null) {
                i(
                    msg[CLASS_NAME_INDEX],
                    msg[METHOD_NAME_INDEX] + content
                )
            }
        }
    }

    /**
     * Lazy debug to reduce toString() calls and String concatenating
     *
     * @param getContent function to get message
     * @receiver
     */
    fun l(getContent: () -> String) {
        if (DEBUG) {
            val msg = trace()
            if (msg != null) {
                d(
                    msg[CLASS_NAME_INDEX],
                    "[ " + msg[METHOD_NAME_INDEX] + " ] " + getContent()
                )
            }
        }
    }

    private fun i(tag: String, content: String) {
        if (DEBUG) {
            Log.i(tag, content)
        }
    }

    /**
     * Send an error log message.
     *
     * @param content The message you would like logged.
     */
    fun e(content: String) {
        if (DEBUG) {
            val msg = trace()
            if (msg != null) {
                e(
                    msg[CLASS_NAME_INDEX],
                    msg[METHOD_NAME_INDEX] + content
                )
            }
        }
    }

    private fun e(tag: String, content: String) {
        if (DEBUG) {
            Log.e(tag, content)
        }
    }

    /**
     * Send an debug log message.
     *
     * @param content The message you would like logged.
     */
    fun d(content: String) {
        if (DEBUG) {
            val msg = trace()
            if (msg != null) {
                d(
                    msg[CLASS_NAME_INDEX],
                    "[ " + msg[METHOD_NAME_INDEX] + " ] " + content
                )
            }
        }
    }

    /**
     * Send an warning log message.
     *
     * @param content The message you would like logged.
     */
    fun w(content: String) {
        if (DEBUG) {
            val msg = trace()
            if (msg != null) {
                w(
                    msg[CLASS_NAME_INDEX],
                    "[ " + msg[METHOD_NAME_INDEX] + " ] " + content
                )
            }
        }
    }

    private fun d(tag: String, content: String) {
        if (DEBUG) {
            Log.d(tag, content)
        }
    }

    private fun w(tag: String, content: String) {
        if (DEBUG) {
            Log.w(tag, content)
        }
    }

    private fun trace(): Array<String>? {
        var index = 0
        val stackTraceElements = Thread.currentThread().stackTrace
        for (i in stackTraceElements.indices) {
            val ste = stackTraceElements[i]
            if ((ste.className == LogUtils::class.java.name) && (ste.methodName.contains(
                    TRACE_METHOD
                ))
            ) {
                index = i + 2 // index for startEndMethodLog method
                val isStartLogMethod =
                    index < stackTraceElements.size && stackTraceElements[index].methodName.contains(
                        START_LOG_METHOD
                    )
                val isEndLogMethod =
                    index < stackTraceElements.size && stackTraceElements[index].methodName.contains(
                        END_LOG_METHOD
                    )
                if (isStartLogMethod || isEndLogMethod) {
                    break
                }
                index = i + 1 // index for d method
                break
            }
        }

        index++ // index for method call d or startEndMethodLog method

        return if ((stackTraceElements.size >= index) && (stackTraceElements[index] != null)) {
            arrayOf(
                stackTraceElements[index].fileName,
                stackTraceElements[index].methodName + "[" + stackTraceElements[index].lineNumber + "] "
            )
        } else null
    }
}