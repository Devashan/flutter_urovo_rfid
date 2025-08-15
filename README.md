# Flutter Urovo RFID

[![pub package](https://img.shields.io/pub/v/flutter_urovo_rfid.svg)](https://pub.dev/packages/flutter_urovo_rfid)
[![License: MIT](https://img.shields.io/badge/license-MIT-purple.svg)](https://opensource.org/licenses/MIT)

A Flutter plugin that provides seamless integration with Urovo's Android RFID hardware, enabling Flutter applications to interact with Urovo RFID readers. The plugin wraps Urovo's native Android SDK, exposing a clean and easy-to-use Dart API.

## Features

- Power on/off and connect to Urovo RFID readers
- Start and stop continuous inventory scanning
- Perform single inventory scans
- Retrieve hardware and firmware information
- Real-time tag events via stream (EPC, TID, RSSI)
- Optimized for performance with minimal battery impact
- Built with null safety

## Prerequisites

- Flutter 3.0.0 or later
- Android 5.0 (API level 21) or later
- Urovo Android device with RFID capabilities

## Installation

Add the following to your `pubspec.yaml`:

```yaml
dependencies:
  flutter_urovo_rfid: ^0.1.0
```

Then run:
```bash
flutter pub get
```

## Usage

### Import the package

```dart
import 'package:flutter_urovo_rfid/flutter_urovo_rfid.dart';
```

### Initialize the RFID Reader

```dart
// Power on and connect to the RFID module
final state = await FlutterUrovoRfid.openRfid();
print('RFID module state: $state');
```

### Listen for Tag Scans

```dart
// Subscribe to tag events
final subscription = FlutterUrovoRfid.scanStream.listen((tag) {
  final epc = tag['epc'];  // Electronic Product Code
  final tid = tag['tid'];  // Tag ID
  final rssi = tag['rssi'] as int;  // Signal strength
  
  print('Tag detected - EPC: $epc, TID: $tid, RSSI: $rssi');
});

// Start scanning
await FlutterUrovoRfid.startReading();

// Stop scanning
await FlutterUrovoRfid.stopReading();

// Don't forget to cancel the subscription when done
await subscription.cancel();
```

### Single Inventory Scan

```dart
// Perform a single inventory scan
final tags = await FlutterUrovoRfid.singleInventory();
print('Found ${tags.length} tags: $tags');
```

### Retrieve Hardware Information

```dart
final fwVersion = await FlutterUrovoRfid.getFirmwareVersion();
print('Firmware version: $fwVersion');
```

### Clean Up

```dart
// Power off the RFID module when done
await FlutterUrovoRfid.closeRfid();
```

### Example Usage

```dart
void initReader() async {
  // Power on and connect
  final state = await FlutterUrovoRfid.openRfid();
  print('RFID state: $state');

  // Listen to tag events
  FlutterUrovoRfid.scanStream.listen((event) {
    final epc = event['epc'];
    final tid = event['tid'];
    final rssi = event['rssi'];
    print('Tag: EPC=$epc TID=$tid RSSI=$rssi');
  });
}

void startScanning() async {
  await FlutterUrovoRfid.startReading();
}
```

## API Reference

### Methods

| Method | Description | Returns |
|--------|-------------|---------|
| `openRfid()` | Powers on and initializes the RFID module | `Future<String>` - Status message |
| `closeRfid()` | Powers off the RFID module | `Future<void>` |
| `startReading()` | Starts continuous inventory scanning | `Future<void>` |
| `stopReading()` | Stops continuous inventory scanning | `Future<void>` |
| `singleInventory()` | Performs a single inventory scan | `Future<List<Map<String, dynamic>>>` - List of scanned tags |
| `getFirmwareVersion()` | Retrieves the firmware version | `Future<String>` - Firmware version string |

### Streams

| Stream | Description | Event Type |
|--------|-------------|------------|
| `scanStream` | Stream of tag scan events | `Map<String, dynamic>` with keys: `epc`, `tid`, `rssi` |

## Error Handling

All methods may throw a `PlatformException` if the operation fails. Make sure to handle potential errors:

```dart
try {
  await FlutterUrovoRfid.openRfid();
} on PlatformException catch (e) {
  print('Failed to initialize RFID: ${e.message}');
}
```

## Permissions

Add the following permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

For Android 12 and above, you'll also need to add these to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.BLUETOOTH_SCAN"
    android:usesPermissionFlags="neverForLocation"
    tools:targetApi="s" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
```

## Troubleshooting

### Common Issues

1. **RFID module not initializing**
   - Ensure the device has RFID capabilities
   - Check if the RFID module is properly connected
   - Verify that all required permissions are granted

2. **No tags being detected**
   - Ensure tags are within the reader's range
   - Check if the RFID antenna is properly connected
   - Verify that the correct frequency is being used for your tags

## Android Setup

This plugin depends on the proprietary Urovo RFID SDK. To compile and run successfully, you must obtain the SDK JAR/AAR files from Urovo and place them into the plugin's `android/src/main/libs` directory (create it if it doesn't exist). The Gradle script is configured to include any `.jar` or `.aar` files found there.

### Adding Urovo SDK

1. Obtain the Urovo RFID SDK (`.aar` or `.jar` files) from Urovo
2. Create the directory if it doesn't exist:
   ```bash
   mkdir -p android/src/main/libs
   ```
3. Copy the Urovo SDK files to `android/src/main/libs/`
4. The plugin will automatically include these files in the build

### ProGuard/R8 Configuration

Because the Urovo SDK uses reflection and dynamic loading, ProGuard rules have been provided in `android/proguard-rules.pro` to preserve required classes. If you're using ProGuard or R8 in your app, make sure to include these rules.

## Example App

See the example app in the `example/` directory for a complete demonstration of how to use this plugin in a Flutter application.

## Permissions

Add the following permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

For Android 12 and above, you'll also need to add these to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.BLUETOOTH_SCAN"
    android:usesPermissionFlags="neverForLocation"
    tools:targetApi="s" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Urovo Technology Co., Ltd. for their Android RFID SDK
- The Flutter team for their excellent plugin system
