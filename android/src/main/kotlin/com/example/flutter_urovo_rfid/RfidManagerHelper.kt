package com.example.flutter_urovo_rfid

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ubx.usdk.RFIDSDKManager
import io.flutter.plugin.common.MethodChannel

/**
 * Helper class that encapsulates interactions with Urovo's RFID SDK.
 *
 * This class manages powering the module on/off, connecting/disconnecting,
 * starting and stopping inventory, and relaying tag callbacks back to the
 * [FlutterUrovoRfidPlugin].
 */
class RfidManagerHelper(
    private val context: Context,
    private val plugin: FlutterUrovoRfidPlugin
) {
    // We store the RFID manager as a generic reference to avoid compile-time
    // dependencies on the vendor class.  Methods are invoked via reflection.
    private var rfidManager: Any? = null

    // Dynamic proxy used to receive inventory events from the SDK.  We avoid
    // referencing the IRfidCallback interface at compile time by using
    // reflection.  At runtime we look up the callback interface by name and
    // create a proxy that implements it.  When onInventoryTag and
    // onInventoryTagEnd are invoked, the proxy forwards the calls to the
    // plugin.
    private val rfidCallback: Any by lazy {
        try {
            val clazz = Class.forName("com.ubx.usdk.rfid.aidl.IRfidCallback")
            java.lang.reflect.Proxy.newProxyInstance(
                clazz.classLoader,
                arrayOf(clazz)
            ) { proxy, method, args ->
                try {
                    when (method.name) {
                        "onInventoryTag" -> {
                            // args: [String EPC, String TID, String RSSI]
                            val epc = args?.getOrNull(0) as? String
                            val tid = args?.getOrNull(1) as? String
                            val rssi = args?.getOrNull(2) as? String
                            val event: MutableMap<String, Any?> = HashMap()
                            event["epc"] = epc ?: ""
                            event["tid"] = tid ?: ""
                            event["rssi"] = rssi ?: ""
                            // Ensure event delivery on the main thread, as Flutter platform
                            // channels must be called on the UI thread.  Use Handler to post.
                            Handler(Looper.getMainLooper()).post {
                                plugin.sendRfidScanToFlutter(event)
                            }
                            return@newProxyInstance null
                        }
                        "onInventoryTagEnd" -> {
                            // End of inventory; no action needed
                            return@newProxyInstance null
                        }
                        // The following methods are invoked implicitly on binder proxies.  If
                        // a null return is provided for hashCode(), the Binder will attempt
                        // to unbox null into an int, causing a NPE.  Return a stable int.
                        "hashCode" -> {
                            return@newProxyInstance System.identityHashCode(proxy)
                        }
                        "toString" -> {
                            return@newProxyInstance "IRfidCallbackProxy"
                        }
                        "equals" -> {
                            // Identity equality
                            return@newProxyInstance proxy === args?.getOrNull(0)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("RfidManagerHelper", "Callback invocation error", e)
                }
                return@newProxyInstance null
            }
        } catch (e: Exception) {
            Log.e("RfidManagerHelper", "Failed to create callback proxy", e)
            Any()
        }
    }

    /**
     * Powers on the RFID module and establishes a connection.  This method
     * performs blocking work on a background thread to avoid blocking the main
     * thread.  Upon completion the supplied [result] is invoked on the main
     * thread.
     */
    fun openRfid(result: MethodChannel.Result) {
        try {
            val sdkManager = RFIDSDKManager.getInstance()
            sdkManager.power(true)  // Power on the module

            // Connect in background thread because it may block for a second or two.
            Thread {
                var success = false
                try {
                    // Give the module time to power on.
                    Thread.sleep(1500)
                    success = sdkManager.connect()
                    if (success) {
                        // Obtain the RfidManager instance via reflection to avoid compile-time
                        // dependencies.  The SDK exposes getRfidManager() in most versions.
                        try {
                            val methodGet = sdkManager.javaClass.getMethod("getRfidManager")
                            rfidManager = methodGet.invoke(sdkManager)
                        } catch (e: Exception) {
                            Log.e("RfidManagerHelper", "getRfidManager() via reflection failed", e)
                        }
                        // Register callback to receive inventory events using reflection.
                        try {
                            rfidManager?.let { manager ->
                                val method = manager.javaClass.methods.firstOrNull { it.name == "registerCallback" }
                                method?.invoke(manager, rfidCallback)
                            }
                        } catch (e: Exception) {
                            Log.e("RfidManagerHelper", "registerCallback via reflection failed", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("RfidManagerHelper", "openRfid error during connect", e)
                }
                // Return result on main thread
                Handler(Looper.getMainLooper()).post {
                    if (success) {
                        result.success("ACTIVE")
                    } else {
                        result.error("ERROR", "RFID connect failed", null)
                    }
                }
            }.start()
        } catch (e: Exception) {
            Log.e("RfidManagerHelper", "openRfid error", e)
            result.error("ERROR", e.toString(), null)
        }
    }

    /**
     * Disconnects from the RFID module and powers it off.
     */
    fun closeRfid(result: MethodChannel.Result) {
        try {
            val sdkManager = RFIDSDKManager.getInstance()
            // Disconnect from device
            try {
                sdkManager.disConnect()
            } catch (_: Exception) {
            }
            // Power off module
            try {
                sdkManager.power(false)
            } catch (_: Exception) {
            }
            rfidManager = null
            result.success("INACTIVE")
        } catch (e: Exception) {
            Log.e("RfidManagerHelper", "closeRfid error", e)
            result.error("ERROR", e.toString(), null)
        }
    }

    /**
     * Starts continuous inventory reading.
     */
    fun startReading(result: MethodChannel.Result) {
        try {
            if (rfidManager == null) {
                result.error("ERROR", "RFID manager is null, call openRfid() first", null)
                return
            }
            try {
                rfidManager?.let { manager ->
                    val method = manager.javaClass.getMethod("startRead")
                    method.invoke(manager)
                }
                result.success("true")
            } catch (e: Exception) {
                Log.e("RfidManagerHelper", "startReading call error", e)
                result.error("ERROR", e.toString(), null)
            }
        } catch (e: Exception) {
            Log.e("RfidManagerHelper", "startReading error", e)
            result.error("ERROR", e.toString(), null)
        }
    }

    /**
     * Stops continuous inventory reading.
     */
    fun stopReading(result: MethodChannel.Result) {
        try {
            if (rfidManager == null) {
                result.error("ERROR", "RFID manager is null, call openRfid() first", null)
                return
            }
            try {
                rfidManager?.let { manager ->
                    val method = manager.javaClass.getMethod("stopInventory")
                    method.invoke(manager)
                }
                result.success("true")
            } catch (e: Exception) {
                Log.e("RfidManagerHelper", "stopReading call error", e)
                result.error("ERROR", e.toString(), null)
            }
        } catch (e: Exception) {
            Log.e("RfidManagerHelper", "stopReading error", e)
            result.error("ERROR", e.toString(), null)
        }
    }

    /**
     * Performs a single inventory scan.  The SDK will invoke the
     * [IRfidCallback.onInventoryTag] method for any tags discovered.
     */
    fun inventorySingle(result: MethodChannel.Result) {
        try {
            if (rfidManager == null) {
                result.error("ERROR", "RFID manager is null, call openRfid() first", null)
                return
            }
            try {
                rfidManager?.let { manager ->
                    val method = manager.javaClass.getMethod("inventorySingle")
                    method.invoke(manager)
                }
                result.success("true")
            } catch (e: Exception) {
                Log.e("RfidManagerHelper", "inventorySingle call error", e)
                result.error("ERROR", e.toString(), null)
            }
        } catch (e: Exception) {
            Log.e("RfidManagerHelper", "inventorySingle error", e)
            result.error("ERROR", e.toString(), null)
        }
    }

    /**
     * Retrieves the firmware version string.
     */
    fun getFirmwareVersion(result: MethodChannel.Result) {
        try {
            if (rfidManager == null) {
                result.error("ERROR", "RFID manager is null, call openRfid() first", null)
                return
            }
            try {
                var firmware: Any? = null
                rfidManager?.let { manager ->
                    // Try to call getFirmwareVersion() method if present
                    val method = manager.javaClass.methods.firstOrNull { it.name.equals("getFirmwareVersion", ignoreCase = true) }
                    firmware = method?.invoke(manager)
                    // Some SDKs expose firmwareVersion as a field property getter
                    if (firmware == null) {
                        val field = manager.javaClass.fields.firstOrNull { it.name.equals("firmwareVersion", ignoreCase = true) }
                        firmware = field?.get(manager)
                    }
                }
                result.success(firmware as? String)
            } catch (e: Exception) {
                Log.e("RfidManagerHelper", "getFirmwareVersion error", e)
                result.error("ERROR", e.toString(), null)
            }
        } catch (e: Exception) {
            Log.e("RfidManagerHelper", "getFirmwareVersion error", e)
            result.error("ERROR", e.toString(), null)
        }
    }

    /**
     * Writes data to a tag identified by its TID.  The caller must supply
     * the TID string, memory bank (0 = password, 1 = EPC, 2 = TID, 3 = USER),
     * word pointer, password and data (hex string).  Returns the SDK return
     * code: 0 for success, negative values for errors.
     */
    fun writeTagByTid(tid: String, mem: Int, wordPtr: Int, password: String, data: String, result: MethodChannel.Result) {
        try {
            if (rfidManager == null) {
                result.error("ERROR", "RFID manager is null, call openRfid() first", null)
                return
            }
            var ret: Any? = null
            try {
                rfidManager?.let { manager ->
                    val method = manager.javaClass.getMethod(
                        "writeTagByTid",
                        String::class.java,
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType,
                        String::class.java,
                        String::class.java
                    )
                    ret = method.invoke(manager, tid, mem, wordPtr, password, data)
                }
                result.success(ret)
            } catch (e: Exception) {
                Log.e("RfidManagerHelper", "writeTagByTid error", e)
                result.error("ERROR", e.toString(), null)
            }
        } catch (e: Exception) {
            Log.e("RfidManagerHelper", "writeTagByTid error", e)
            result.error("ERROR", e.toString(), null)
        }
    }

    /**
     * Writes a new EPC value to a tag.  The caller must provide the EPC
     * string and the access password.  Returns the SDK return code.
     */
    fun writeEpcString(epc: String, password: String, result: MethodChannel.Result) {
        try {
            if (rfidManager == null) {
                result.error("ERROR", "RFID manager is null, call openRfid() first", null)
                return
            }
            var ret: Any? = null
            try {
                rfidManager?.let { manager ->
                    val method = manager.javaClass.getMethod(
                        "writeEpcString",
                        String::class.java,
                        String::class.java
                    )
                    ret = method.invoke(manager, epc, password)
                }
                result.success(ret)
            } catch (e: Exception) {
                Log.e("RfidManagerHelper", "writeEpcString error", e)
                result.error("ERROR", e.toString(), null)
            }
        } catch (e: Exception) {
            Log.e("RfidManagerHelper", "writeEpcString error", e)
            result.error("ERROR", e.toString(), null)
        }
    }

    /**
     * Unregisters the RFID callback and clears references.  Called when the
     * plugin is detached from the Flutter engine.
     */
    fun dispose() {
        // Clear reference to manager and callback.  Some SDKs expose
        // unRegisterCallback, but to avoid compilation errors when it is absent,
        // we omit explicit calls here.  The manager will drop the callback when
        // it is garbage-collected or the connection is closed.
        rfidManager = null
    }
}