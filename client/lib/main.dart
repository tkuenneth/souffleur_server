import 'dart:io';

import 'package:barcode_scan2/barcode_scan2.dart';
import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:url_launcher/url_launcher.dart';

const String _souffleurHomepage = "https://www.thomaskuenneth.eu/souffleur";
const String _keyLastKnownUrl = 'lastKnownUrl';
const String _appName = "Souffleur";
const String protocolHttp = "http";

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
      updateLastKnownUrl(prefs.getString(_keyLastKnownUrl), false);
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
    return lastKnownUrl != null && lastKnownUrl.contains(protocolHttp);
  }

  void updateLastKnownUrl(String url, bool shouldUpdatePrefs) async {
    if (url == null) url = "";
    setState(() {
      lastKnownUrl = url;
      _sendCommandHello();
    });
    if (shouldUpdatePrefs) {
      SharedPreferences.getInstance().then((prefs) {
        prefs.setString(_keyLastKnownUrl, url);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        debugShowCheckedModeBanner: false,
        theme: theme,
        localizationsDelegates: AppLocalizations.localizationsDelegates,
        supportedLocales: AppLocalizations.supportedLocales,
        home: Builder(builder: (BuildContext context) {
          return Scaffold(
              appBar: AppBar(
                title: const Text(_appName),
                actions: [
                  PopupMenuButton(itemBuilder: (context) {
                    return [
                      PopupMenuItem<int>(
                        value: 0,
                        child: Text(AppLocalizations.of(context).scan),
                      ),
                      PopupMenuItem<int>(
                        value: 1,
                        child: Text(AppLocalizations.of(context).unlink),
                      ),
                    ];
                  }, onSelected: (value) {
                    switch (value) {
                      case 0:
                        _scanQRCode();
                        break;
                      case 1:
                        updateLastKnownUrl("", true);
                        break;
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
    else {
      final TextTheme theme = Theme.of(context).textTheme;
      return SingleChildScrollView(child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          _createTextWithMaxWidth(AppLocalizations.of(context).welcome,
              TextAlign.center, theme.headline4,
              padding: EdgeInsets.zero),
          _createTextWithMaxWidth(AppLocalizations.of(context).not_linked,
              TextAlign.center, theme.bodyLarge),
          _createTextWithMaxWidth(AppLocalizations.of(context).instructions_01,
              TextAlign.center, theme.bodyLarge),
          GestureDetector(
            child: Padding(
                padding: EdgeInsets.only(top: 16),
                child: Text(
                  _souffleurHomepage,
                  style: theme.bodyLarge
                      .copyWith(decoration: TextDecoration.underline),
                )),
            onTap: () async {
              try {
                await launchUrl(Uri.parse(_souffleurHomepage));
              } catch (err) {
                debugPrint('Something bad happened');
              }
            },
          ),
          _createTextWithMaxWidth(AppLocalizations.of(context).instructions_02,
              TextAlign.center, theme.bodyLarge),
          _createTextWithMaxWidth(AppLocalizations.of(context).instructions_03,
              TextAlign.center, theme.bodyLarge),
          _createTextWithMaxWidth(AppLocalizations.of(context).instructions_04,
              TextAlign.center, theme.bodyLarge,
              padding: EdgeInsets.only(top: 16, bottom: 24)),
          TextButton(
              onPressed: _scanQRCode,
              child: Text(AppLocalizations.of(context).scan)),
        ],
      ));
    }
  }

  Widget _createTextWithMaxWidth(
      String text, TextAlign textAlign, TextStyle style,
      {EdgeInsets padding = const EdgeInsets.only(top: 16)}) {
    return Container(
        constraints: BoxConstraints(maxWidth: 400),
        child: Padding(
            padding: padding,
            child: Text(text, textAlign: textAlign, style: style)));
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
      String strResult = scanResult.rawContent;
      if (strResult.isNotEmpty) {
        updateLastKnownUrl(scanResult.rawContent, true);
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

  SouffleurHttpOverrides(this.state);

  @override
  HttpClient createHttpClient(SecurityContext context) {
    return super.createHttpClient(context)
      ..badCertificateCallback =
          (X509Certificate cert, String host, int port) =>
              state.lastKnownUrl.contains(host);
  }
}
