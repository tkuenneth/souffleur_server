import 'dart:async';

import 'package:barcode_scan/barcode_scan.dart';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

void main() => runApp(SouffleurClient());

class SouffleurClient extends StatefulWidget {
  @override
  State<SouffleurClient> createState() => _SouffleurClientState();
}

class _SouffleurClientState extends State<SouffleurClient> {
  var lastKnownUrl = "";
  ThemeData theme;

  @override
  void initState() {
    super.initState();
    var window = WidgetsBinding.instance.window;
    _updateThemeData(window.platformBrightness);
    window.onPlatformBrightnessChanged = () {
      setState(() {
        _updateThemeData(window.platformBrightness);
      });
    };
    SharedPreferences.getInstance().then((prefs) {
      setState(() {
        lastKnownUrl = prefs.getString('lastKnownUrl');
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        theme: theme,
        home: Scaffold(
            appBar: AppBar(title: const Text("Souffleur")),
            body: SafeArea(
                child: Container(
                    decoration: BoxDecoration(color: theme.colorScheme.surface),
                    child: Padding(
                      padding: EdgeInsets.all(16),
                      child: Directionality(
                        textDirection: TextDirection.ltr,
                        child: Column(
                          children: <Widget>[
                            Expanded(
                              child: Align(
                                alignment: Alignment.center,
                                child: FutureBuilder<bool>(
                                    future:
                                        _getFromServer(lastKnownUrl, "hello"),
                                    builder: (context, snapshot) {
                                      if (snapshot.connectionState !=
                                          ConnectionState.done) {
                                        return CircularProgressIndicator();
                                      } else if (snapshot.data == true) {
                                        return _createButtons();
                                      } else {
                                        return TextButton(
                                          child: Text(
                                            "Scan",
                                            style: TextStyle(fontSize: 72),
                                          ),
                                          onPressed: _scanQRCode,
                                        );
                                      }
                                    }),
                              ),
                            ),
                          ],
                        ),
                      ),
                    )))));
  }

  Widget _createButtons() {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Expanded(
          child: _roundButton(_sendCommandHome, "\u23ee"),
        ),
        Expanded(
            flex: 3,
            child: Padding(
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Expanded(
                        child: _roundButton(_sendCommandPrevious, "\u25c0")),
                    Expanded(
                        child: Padding(
                            child: _roundButton(_sendCommandNext, "\u25b6"),
                            padding: EdgeInsets.only(left: 8))),
                  ],
                ),
                padding: EdgeInsets.only(top: 8, bottom: 8))),
        Expanded(
          child: _roundButton(_sendCommandEnd, "\u23ed"),
        ),
      ],
    );
  }

  Widget _roundButton(VoidCallback onPressed, String text) {
    var color = theme.colorScheme.primary;
    return TextButton(
        onPressed: onPressed,
        child: Text(text,
            style: TextStyle(
                fontSize: 72,
                color: color,
                textBaseline: TextBaseline.ideographic)),
        style: ButtonStyle(
            shape: MaterialStateProperty.all<RoundedRectangleBorder>(
                RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(18.0),
                    side: BorderSide(color: color)))));
  }

  void _scanQRCode() async {
    try {
      var scanResult = await BarcodeScanner.scan();
      var prefs = await SharedPreferences.getInstance();
      prefs.setString('lastKnownUrl', scanResult.rawContent);
      setState(() {
        lastKnownUrl = scanResult.rawContent;
      });
    } on Exception catch (e) {
      debugPrint('$e');
    }
  }

  Future<bool> _getFromServer(String lastKnownUrl, String suffix) async {
    try {
      if (!lastKnownUrl.contains("http")) return false;
      final response = await http
          .get(Uri.parse(lastKnownUrl + suffix))
          .timeout(const Duration(seconds: 5));
      if (response.statusCode == 200) {
        return true;
      }
    } on Exception catch (e) {
      debugPrint('$e');
    }
    return false;
  }

  void _sendCommandHome() {
    _sendCommand("home");
  }

  void _sendCommandEnd() {
    _sendCommand("end");
  }

  void _sendCommandPrevious() {
    _sendCommand("previous");
  }

  void _sendCommandNext() {
    _sendCommand("next");
  }

  void _sendCommand(String cmd) async {
    await _getFromServer(lastKnownUrl, cmd);
  }

  void _updateThemeData(Brightness brightness) {
    theme = ThemeData(brightness: brightness);
  }
}
