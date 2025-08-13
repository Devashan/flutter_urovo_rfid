import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_urovo_rfid/flutter_urovo_rfid.dart';
import 'package:flutter_urovo_rfid/flutter_urovo_rfid_platform_interface.dart';
import 'package:flutter_urovo_rfid/flutter_urovo_rfid_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterUrovoRfidPlatform
    with MockPlatformInterfaceMixin
    implements FlutterUrovoRfidPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final FlutterUrovoRfidPlatform initialPlatform = FlutterUrovoRfidPlatform.instance;

  test('$MethodChannelFlutterUrovoRfid is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterUrovoRfid>());
  });

  test('getPlatformVersion', () async {
    FlutterUrovoRfid flutterUrovoRfidPlugin = FlutterUrovoRfid();
    MockFlutterUrovoRfidPlatform fakePlatform = MockFlutterUrovoRfidPlatform();
    FlutterUrovoRfidPlatform.instance = fakePlatform;

    expect(await flutterUrovoRfidPlugin.getPlatformVersion(), '42');
  });
}
