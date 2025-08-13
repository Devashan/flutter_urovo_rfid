import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_urovo_rfid_platform_interface.dart';

/// An implementation of [FlutterUrovoRfidPlatform] that uses method channels.
class MethodChannelFlutterUrovoRfid extends FlutterUrovoRfidPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_urovo_rfid');

  /// Event channel for receiving scanned tag events.  Tags are streamed
  /// whenever continuous inventory is running.
  @visibleForTesting
  final EventChannel eventChannel = const EventChannel('flutter_urovo_rfid_plugin/scan');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<String?> openRfid() async {
    final result = await methodChannel.invokeMethod<String>('openRfid');
    return result;
  }

  @override
  Future<String?> closeRfid() async {
    final result = await methodChannel.invokeMethod<String>('closeRfid');
    return result;
  }

  @override
  Future<String?> startReading() async {
    final result = await methodChannel.invokeMethod<String>('startReading');
    return result;
  }

  @override
  Future<String?> stopReading() async {
    final result = await methodChannel.invokeMethod<String>('stopReading');
    return result;
  }

  @override
  Future<dynamic> inventorySingle() async {
    final result = await methodChannel.invokeMethod<dynamic>('inventorySingle');
    return result;
  }

  @override
  Future<String?> getFirmwareVersion() async {
    final result = await methodChannel.invokeMethod<String>('getFirmwareVersion');
    return result;
  }

  @override
  Future<dynamic> writeTagByTid({
    required String tid,
    required int mem,
    required int wordPtr,
    required String password,
    required String data,
  }) async {
    final params = <String, dynamic>{
      'tid': tid,
      'mem': mem,
      'wordPtr': wordPtr,
      'password': password,
      'data': data,
    };
    final result = await methodChannel.invokeMethod<dynamic>('writeTagByTid', params);
    return result;
  }

  @override
  Future<dynamic> writeEpcString({
    required String epc,
    required String password,
  }) async {
    final params = <String, dynamic>{
      'epc': epc,
      'password': password,
    };
    final result = await methodChannel.invokeMethod<dynamic>('writeEpcString', params);
    return result;
  }

  @override
  Stream<dynamic> get scanStream {
    return eventChannel.receiveBroadcastStream();
  }
}
