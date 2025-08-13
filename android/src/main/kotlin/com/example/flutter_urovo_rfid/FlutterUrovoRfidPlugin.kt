package com.example.flutter_urovo_rfid

import android.content.Context
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/**
 * FlutterUrovoRfidPlugin
 *
 * This class acts as the bridge between Flutter and the native Urovo RFID SDK.
 * It exposes methods over a [MethodChannel] and streams tag scan events over
 * an [EventChannel].  See [RfidManagerHelper] for the detailed SDK calls.
 */
class FlutterUrovoRfidPlugin : FlutterPlugin, MethodCallHandler {
    // Android application context, assigned when attached to engine.
    private lateinit var context: Context

    // Method channel used to handle method invocations from Dart.
    private lateinit var channel: MethodChannel

    // Event sink for streaming tag events back to Flutter.
    private var eventSink: EventChannel.EventSink? = null

    // Helper that wraps the Urovo SDK.
    private lateinit var rfidHelper: RfidManagerHelper

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        Log.d("FlutterUrovoRfidPlugin", "onAttachedToEngine")
        context = flutterPluginBinding.applicationContext
        rfidHelper = RfidManagerHelper(context, this)

        // Set up MethodChannel
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_urovo_rfid")
        channel.setMethodCallHandler(this)

        // Set up EventChannel for tag events
        EventChannel(flutterPluginBinding.binaryMessenger, "flutter_urovo_rfid_plugin/scan").setStreamHandler(
            object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    eventSink = events
                    Log.d("FlutterUrovoRfidPlugin", "EventChannel onListen")
                }

                override fun onCancel(arguments: Any?) {
                    eventSink = null
                    Log.d("FlutterUrovoRfidPlugin", "EventChannel onCancel")
                }
            }
        )
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        Log.d("FlutterUrovoRfidPlugin", "onMethodCall: ${call.method}")
        when (call.method) {
            "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
            "openRfid" -> rfidHelper.openRfid(result)
            "closeRfid" -> rfidHelper.closeRfid(result)
            "startReading" -> rfidHelper.startReading(result)
            "stopReading" -> rfidHelper.stopReading(result)
            "inventorySingle" -> rfidHelper.inventorySingle(result)
            "getFirmwareVersion" -> rfidHelper.getFirmwareVersion(result)
            "writeTagByTid" -> {
                // Extract arguments from the call.  Expect a map with keys matching
                // parameter names.  Provide sensible defaults if missing.
                val tid = call.argument<String>("tid") ?: ""
                val mem = call.argument<Int>("mem") ?: 1
                val wordPtr = call.argument<Int>("wordPtr") ?: 2
                val password = call.argument<String>("password") ?: "00000000"
                val data = call.argument<String>("data") ?: ""
                rfidHelper.writeTagByTid(tid, mem, wordPtr, password, data, result)
            }
            "writeEpcString" -> {
                val epc = call.argument<String>("epc") ?: ""
                val password = call.argument<String>("password") ?: "00000000"
                rfidHelper.writeEpcString(epc, password, result)
            }
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        Log.d("FlutterUrovoRfidPlugin", "onDetachedFromEngine")
        channel.setMethodCallHandler(null)
        rfidHelper.dispose()
        eventSink = null
    }

    /**
     * Invoked by [RfidManagerHelper] whenever a tag is read.  Wraps the event and
     * passes it to Flutter via the event sink.
     *
     * @param tag a map containing the tag EPC, TID and RSSI values
     */
    fun sendRfidScanToFlutter(tag: Map<String, Any?>) {
        eventSink?.success(tag)
        Log.d("FlutterUrovoRfidPlugin", "sendRfidScanToFlutter: $tag")
    }
}