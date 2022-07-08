import 'dart:io';

import 'package:barcode_scan2/barcode_scan2.dart';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  runApp(SouffleurClient());
}

class SouffleurClient extends StatefulWidget {
  @override
  State<SouffleurClient> createState() => _SouffleurClientState();
}

class _SouffleurClientState extends State<SouffleurClient>
    with WidgetsBindingObserver {
  String lastKnownUrl = "";
  ThemeData theme;
  BuildContext scaffoldContext;

  @override
  void initState() {
    super.initState();
    HttpOverrides.global = SouffleurHttpOverrides(this);
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
//      updateLastKnownUrl(prefs.getString('lastKnownUrl'));
      updateLastKnownUrl(
          "https://192.168.178.33:8087/souffleur/d66b1991-d02e-4bfb-af6a-9835ee7b71a8/");
    });
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) _sendCommandHello();
  }

  bool isLastKnownUrlValid() {
    return lastKnownUrl.contains("http");
  }

  void updateLastKnownUrl(String url) {
    setState(() {
      lastKnownUrl = url;
      _sendCommandHello();
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        theme: theme,
        home: Builder(builder: (BuildContext context) {
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
              body: Builder(builder: (BuildContext context) {
                scaffoldContext = context;
                return _createBody(context);
              }));
        }));
  }

  Widget _createBody(BuildContext context) {
    return SafeArea(
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
                        child: _createButtons(context),
                      ),
                    ),
                  ],
                ),
              ),
            )));
  }

  Widget _createButtons(BuildContext context) {
    if (isLastKnownUrlValid())
      return Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Expanded(
            child: _createRoundedButton(_sendCommandHome, "\u23ee", context),
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
                              _sendCommandPrevious, "\u25c0", context)),
                      Expanded(
                          child: Padding(
                              child: _createRoundedButton(
                                  _sendCommandNext, "\u25b6", context),
                              padding: EdgeInsets.only(left: 8))),
                    ],
                  ),
                  padding: EdgeInsets.only(top: 8, bottom: 8))),
          Expanded(
            child: _createRoundedButton(_sendCommandEnd, "\u23ed", context),
          )
        ],
      );
    else
      return Text("Please scan QR code",
          textAlign: TextAlign.center,
          style: TextStyle(
            fontSize: 48,
          ));
  }

  Widget _createRoundedButton(
      void command(), String text, BuildContext context) {
    var color = theme?.colorScheme?.primary;
    return TextButton(
        onPressed: command,
        child: FittedBox(
            fit: BoxFit.contain,
            child: Text(text,
                style: TextStyle(
                    fontSize: 72,
                    color: color,
                    textBaseline: TextBaseline.ideographic))),
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
      String strResult = scanResult.rawContent;
      if (strResult.isNotEmpty) {
        prefs.setString('lastKnownUrl', strResult);
        updateLastKnownUrl(scanResult.rawContent);
      }
    } on Exception catch (e) {
      debugPrint('$e');
    }
  }

  void _getFromServer(String lastKnownUrl, String suffix) async {
    if (isLastKnownUrlValid()) {
      try {
        final response = await http
            .get(Uri.parse(lastKnownUrl + suffix))
            .timeout(const Duration(seconds: 2));
        if (response.statusCode == 200) return;
      } on Exception catch (e) {
        debugPrint('$e');
      }
      final snackBar = SnackBar(content: const Text("Server did not respond"));
      ScaffoldMessenger.of(scaffoldContext).showSnackBar(snackBar);
    }
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
    _getFromServer(lastKnownUrl, cmd);
  }

  void _updateThemeData(Brightness brightness) {
    theme = ThemeData(brightness: brightness);
  }
}

class SouffleurHttpOverrides extends HttpOverrides {
  _SouffleurClientState state;

  SouffleurHttpOverrides(_SouffleurClientState this.state);

  @override
  HttpClient createHttpClient(SecurityContext context) {
    return super.createHttpClient(context)
      ..badCertificateCallback =
          (X509Certificate cert, String host, int port) =>
              state.lastKnownUrl.contains(host);
  }
}
