
import 'flutter_urovo_rfid_platform_interface.dart';

/// Dart-facing API for the Urovo RFID plugin.
///
/// This class forwards calls to the underlying platform implementation via
/// [FlutterUrovoRfidPlatform].  Tag data is streamed over [scanStream].
class FlutterUrovoRfid {
  /// Returns a broadcast stream of tag scan events.
  ///
  /// Each event is expected to be a `Map<String, dynamic>` containing
  /// `epc`, `tid` and `rssi` keys.  See the README for usage examples.
  static Stream<dynamic> get scanStream {
    return FlutterUrovoRfidPlatform.instance.scanStream;
  }

  /// Returns the platform version string.
  static Future<String?> getPlatformVersion() {
    return FlutterUrovoRfidPlatform.instance.getPlatformVersion();
  }

  /// Powers on and connects to the RFID module.
  static Future<String?> openRfid() {
    return FlutterUrovoRfidPlatform.instance.openRfid();
  }

  /// Disconnects and powers off the RFID module.
  static Future<String?> closeRfid() {
    return FlutterUrovoRfidPlatform.instance.closeRfid();
  }

  /// Starts continuous inventory scanning.
  static Future<String?> startReading() {
    return FlutterUrovoRfidPlatform.instance.startReading();
  }

  /// Stops continuous inventory scanning.
  static Future<String?> stopReading() {
    return FlutterUrovoRfidPlatform.instance.stopReading();
  }

  /// Performs a single inventory scan.
  static Future<dynamic> inventorySingle() {
    return FlutterUrovoRfidPlatform.instance.inventorySingle();
  }

  /// Retrieves the firmware version string.
  static Future<String?> getFirmwareVersion() {
    return FlutterUrovoRfidPlatform.instance.getFirmwareVersion();
  }

  /// Writes data to a tag identified by its TID.  See
  /// [FlutterUrovoRfidPlatform.writeTagByTid] for parameter descriptions.
  static Future<dynamic> writeTagByTid({
    required String tid,
    required int mem,
    required int wordPtr,
    required String password,
    required String data,
  }) {
    return FlutterUrovoRfidPlatform.instance.writeTagByTid(
      tid: tid,
      mem: mem,
      wordPtr: wordPtr,
      password: password,
      data: data,
    );
  }

  /// Writes a new EPC value to a tag.
  static Future<dynamic> writeEpcString({
    required String epc,
    required String password,
  }) {
    return FlutterUrovoRfidPlatform.instance.writeEpcString(
      epc: epc,
      password: password,
    );
  }
}
