import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_urovo_rfid_method_channel.dart';

abstract class FlutterUrovoRfidPlatform extends PlatformInterface {
  /// Constructs a FlutterUrovoRfidPlatform.
  FlutterUrovoRfidPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterUrovoRfidPlatform _instance = MethodChannelFlutterUrovoRfid();

  /// The default instance of [FlutterUrovoRfidPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterUrovoRfid].
  static FlutterUrovoRfidPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterUrovoRfidPlatform] when
  /// they register themselves.
  static set instance(FlutterUrovoRfidPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  /// Powers on and connects to the RFID module.
  ///
  /// Returns a string representing the module state (e.g. "ACTIVE" on
  /// success). Implementations should throw or return an error string
  /// if the operation fails.
  Future<String?> openRfid() {
    throw UnimplementedError('openRfid() has not been implemented.');
  }

  /// Disconnects and powers off the RFID module.
  ///
  /// Returns a string representing the new state or an error on failure.
  Future<String?> closeRfid() {
    throw UnimplementedError('closeRfid() has not been implemented.');
  }

  /// Starts continuous inventory reading.
  ///
  /// Returns a string such as "true" or "false" depending on whether the
  /// underlying SDK call succeeded. When reading is started, tag events
  /// will be streamed on [scanStream].
  Future<String?> startReading() {
    throw UnimplementedError('startReading() has not been implemented.');
  }

  /// Stops continuous inventory reading.
  ///
  /// Returns a string indicating success.
  Future<String?> stopReading() {
    throw UnimplementedError('stopReading() has not been implemented.');
  }

  /// Performs a single inventory scan.  Implementations should return a
  /// platform specific value – typically a boolean or list of tags.
  Future<dynamic> inventorySingle() {
    throw UnimplementedError('inventorySingle() has not been implemented.');
  }

  /// Requests the firmware version string from the reader.
  Future<String?> getFirmwareVersion() {
    throw UnimplementedError('getFirmwareVersion() has not been implemented.');
  }

  /// Writes data to a tag identified by its TID.  [tid] is the tag's TID
  /// string, [mem] is the memory bank (0=password, 1=EPC, 2=TID, 3=USER),
  /// [wordPtr] is the starting word address, [password] is the access
  /// password (8 hexadecimal characters), and [data] is the hex string to
  /// write.  Returns an integer SDK return code.
  Future<dynamic> writeTagByTid({
    required String tid,
    required int mem,
    required int wordPtr,
    required String password,
    required String data,
  }) {
    throw UnimplementedError('writeTagByTid() has not been implemented.');
  }

  /// Writes a new EPC value to a tag.  [epc] is the EPC string and
  /// [password] is the access password.  Returns an integer SDK return code.
  Future<dynamic> writeEpcString({
    required String epc,
    required String password,
  }) {
    throw UnimplementedError('writeEpcString() has not been implemented.');
  }

  /// Broadcast stream of tag scan events.
  ///
  /// Each event is expected to be a `Map<String, dynamic>` containing the
  /// EPC, TID and RSSI keys.  Implementations must deliver a broadcast
  /// stream so that multiple listeners can subscribe simultaneously.
  Stream<dynamic> get scanStream {
    throw UnimplementedError('scanStream has not been implemented.');
  }
}
