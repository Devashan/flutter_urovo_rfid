import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_urovo_rfid/flutter_urovo_rfid.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String _rfidState = 'INACTIVE';
  final List<Map<String, dynamic>> _tags = [];
  StreamSubscription<dynamic>? _subscription;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion = await FlutterUrovoRfid.getPlatformVersion() ?? 'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  /// Opens the RFID reader and sets up the stream listener.
  Future<void> _openRfid() async {
    final state = await FlutterUrovoRfid.openRfid();
    setState(() {
      _rfidState = state ?? 'ERROR';
    });
    // Subscribe to tag stream
    _subscription?.cancel();
    _subscription = FlutterUrovoRfid.scanStream.listen((event) {
      setState(() {
        _tags.add(Map<String, dynamic>.from(event));
      });
    });
  }

  /// Closes the RFID reader and cancels the stream listener.
  Future<void> _closeRfid() async {
    final state = await FlutterUrovoRfid.closeRfid();
    setState(() {
      _rfidState = state ?? 'ERROR';
    });
    await _subscription?.cancel();
    _subscription = null;
  }

  Future<void> _startReading() async {
    await FlutterUrovoRfid.startReading();
  }

  Future<void> _stopReading() async {
    await FlutterUrovoRfid.stopReading();
  }

  Future<void> _clearTags() async {
    setState(() {
      _tags.clear();
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Urovo RFID Plugin Example'),
        ),
        body: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Platform version: $_platformVersion'),
              const SizedBox(height: 8),
              Text('RFID state: $_rfidState'),
              const SizedBox(height: 16),
              Wrap(
                spacing: 8,
                children: [
                  ElevatedButton(
                    onPressed: _openRfid,
                    child: const Text('Open'),
                  ),
                    ElevatedButton(
                    onPressed: _closeRfid,
                    child: const Text('Close'),
                  ),
                    ElevatedButton(
                    onPressed: _startReading,
                    child: const Text('Start Reading'),
                  ),
                    ElevatedButton(
                    onPressed: _stopReading,
                    child: const Text('Stop Reading'),
                  ),
                    ElevatedButton(
                    onPressed: _clearTags,
                    child: const Text('Clear Tags'),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              const Text('Scanned Tags:'),
              Expanded(
                child: ListView.builder(
                  itemCount: _tags.length,
                  itemBuilder: (context, index) {
                    final tag = _tags[index];
                    return ListTile(
                      title: Text(tag['epc'] ?? ''),
                      subtitle: Text('TID: ${tag['tid']}  RSSI: ${tag['rssi']}'),
                    );
                  },
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
