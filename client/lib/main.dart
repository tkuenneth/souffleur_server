import 'package:barcode_scan/barcode_scan.dart';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

void main() => runApp(SouffleurClient());

class SouffleurClient extends StatefulWidget {
  @override
  State<SouffleurClient> createState() => _SouffleurClientState();
}

enum _Status { waiting, ok, not_successful }

class _SouffleurClientState extends State<SouffleurClient> {
  _Status status = _Status.waiting;
  String lastKnownUrl = "";
  ThemeData theme;

  @override
  void initState() {
    super.initState();
    var window = WidgetsBinding.instance?.window;
    if (window != null) {
      _updateThemeData(window.platformBrightness);
      window.onPlatformBrightnessChanged = () {
        setState(() {
          _updateThemeData(window.platformBrightness);
        });
      };
    }
    SharedPreferences.getInstance().then((prefs) {
      // updateLastKnownUrl(prefs.getString('lastKnownUrl'));
      updateLastKnownUrl(
          "http://192.168.178.33:8087/souffleur/d66b1991-d02e-4bfb-af6a-9835ee7b71a8/");
    });
  }

  bool isLastKnownUrlValid() {
    return lastKnownUrl.contains("http");
  }

  void updateLastKnownUrl(String url) {
    setState(() {
      lastKnownUrl = url;
      if (!isLastKnownUrlValid()) {
        status = _Status.waiting;
      } else
        _sendCommandHello();
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        theme: theme,
        home: Builder(builder: (BuildContext context) {
          return _getScaffold();
        }));
  }

  Widget _getScaffold() {
    return Scaffold(
        appBar: AppBar(
          title: const Text("Souffleur"),
          actions: [
            PopupMenuButton(itemBuilder: (context) {
              return [
                PopupMenuItem<int>(
                  value: 0,
                  child: Text("Scan"),
                ),
              ];
            }, onSelected: (value) {
              if (value == 0) {
                _scanQRCode();
              }
            }),
          ],
        ),
        body: SafeArea(
            child: Container(
                decoration: BoxDecoration(color: theme?.colorScheme?.surface),
                child: Padding(
                  padding: EdgeInsets.all(16),
                  child: Directionality(
                    textDirection: TextDirection.ltr,
                    child: Column(
                      children: <Widget>[
                        Expanded(
                          child: Align(
                            alignment: Alignment.center,
                            child: _createContentArea(),
                          ),
                        ),
                      ],
                    ),
                  ),
                ))));
  }

  Widget _createContentArea() {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Expanded(
          child: _createRoundedButton(_sendCommandHome, "\u23ee"),
        ),
        Expanded(
            flex: 3,
            child: Padding(
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Expanded(
                        child: _createRoundedButton(
                            _sendCommandPrevious, "\u25c0")),
                    Expanded(
                        child: Padding(
                            child: _createRoundedButton(
                                _sendCommandNext, "\u25b6"),
                            padding: EdgeInsets.only(left: 8))),
                  ],
                ),
                padding: EdgeInsets.only(top: 8, bottom: 8))),
        Expanded(
          child: _createRoundedButton(_sendCommandEnd, "\u23ed"),
        ),
        Padding(
          padding: EdgeInsets.only(top: 16, left: 16, right: 16),
          child: Center(
              child: Text(
            status.name,
          )),
        )
      ],
    );
  }

  Widget _createRoundedButton(VoidCallback onPressed, String text) {
    var color = theme?.colorScheme?.primary;
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
      updateLastKnownUrl(scanResult.rawContent);
    } on Exception catch (e) {
      debugPrint('$e');
    }
  }

  void _getFromServer(String lastKnownUrl, String suffix) async {
    _Status newStatus = _Status.not_successful;
    try {
      final response = await http
          .get(Uri.parse(lastKnownUrl + suffix))
          .timeout(const Duration(seconds: 5));
      if (response.statusCode == 200) {
        newStatus = _Status.ok;
      }
    } on Exception catch (e) {
      debugPrint('$e');
    }
    setState(() {
      status = newStatus;
    });
  }

  void _sendCommandHello() {
    _sendCommand("hello");
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
    if (isLastKnownUrlValid()) _getFromServer(lastKnownUrl, cmd);
  }

  void _updateThemeData(Brightness brightness) {
    theme = ThemeData(brightness: brightness);
  }
}
