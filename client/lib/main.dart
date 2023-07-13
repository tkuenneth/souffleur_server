import 'dart:io';

import 'package:barcode_scan2/barcode_scan2.dart';
import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:shake/shake.dart';

const String _urlHomepage = "https://tkuenneth.github.io/souffleur";
const String _keyLastKnownUrl = 'lastKnownUrl';
const String _keyShakeEnabled = "shakeEnabled";
const String _appName = "Souffleur";
const String protocolHttp = "http";

const _symbolNext = Icons.arrow_forward_ios_rounded;
const _symbolPrevious = Icons.arrow_back_ios_rounded;
const _symbolHome = Icons.first_page;
const _symbolEnd = Icons.last_page;

void main() {
  runApp(const SouffleurClient());
}

class SouffleurClient extends StatefulWidget {
  const SouffleurClient({Key? key}) : super(key: key);

  @override
  State<SouffleurClient> createState() => _SouffleurClientState();
}

class _SouffleurClientState extends State<SouffleurClient>
    with WidgetsBindingObserver {
  String lastKnownUrl = "";
  ThemeData? theme;
  BuildContext? scaffoldContext;
  ShakeDetector? detector;
  bool shakeEnabled = false;

  @override
  void initState() {
    super.initState();
    HttpOverrides.global = _SouffleurHttpOverrides(this);
    var window = WidgetsBinding.instance.window;
    _updateThemeData(window.platformBrightness);
    window.onPlatformBrightnessChanged = () {
      setState(() {
        _updateThemeData(window.platformBrightness);
      });
    };
    SharedPreferences.getInstance().then((prefs) {
      updateLastKnownUrl(prefs.getString(_keyLastKnownUrl), false);
      setState(() {
        shakeEnabled = prefs.getBool(_keyShakeEnabled) ?? false;
        _startOrStopListening();
      });
    });
    WidgetsBinding.instance.addObserver(this);
    detector = ShakeDetector.waitForStart(
      onPhoneShake: () {
        _sendCommandNext();
      },
      minimumShakeCount: 1,
      shakeSlopTimeMS: 500,
      shakeCountResetTime: 3000,
      shakeThresholdGravity: 2.7,
    );
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
    return lastKnownUrl.contains(protocolHttp);
  }

  void updateLastKnownUrl(String? url, bool shouldUpdatePrefs) async {
    url ??= "";
    setState(() {
      lastKnownUrl = url ?? "";
      _sendCommandHello();
    });
    if (shouldUpdatePrefs) {
      SharedPreferences.getInstance().then((prefs) {
        prefs.setString(_keyLastKnownUrl, url!);
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
                        child: Text(AppLocalizations.of(context)!.scan),
                      ),
                      PopupMenuItem<int>(
                        value: 1,
                        child: Text(AppLocalizations.of(context)!.unlink),
                      ),
                      PopupMenuItem<int>(
                        value: 2,
                        child: Text(AppLocalizations.of(context)!.unlink),
                      ),
                      shakeEnabled
                          ? PopupMenuItem<int>(
                              value: 2,
                              child: Text(
                                  AppLocalizations.of(context)!.disable_shake),
                            )
                          : PopupMenuItem<int>(
                              value: 2,
                              child: Text(
                                  AppLocalizations.of(context)!.enable_shake),
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
                      case 2:
                        _toggleShakeEnabled();
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
            decoration: BoxDecoration(color: theme?.colorScheme.surface),
            child: Padding(
              padding: const EdgeInsets.all(16),
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
    if (isLastKnownUrlValid()) {
      return Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Expanded(
            child: _createRoundedButton(_sendCommandHome, _symbolHome, context),
          ),
          Expanded(
              flex: 3,
              child: Padding(
                  padding: const EdgeInsets.only(top: 8, bottom: 8),
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Expanded(
                          child: _createRoundedButton(
                              _sendCommandPrevious, _symbolPrevious, context)),
                      Expanded(
                          child: Padding(
                              padding: const EdgeInsets.only(left: 8),
                              child: _createRoundedButton(
                                  _sendCommandNext, _symbolNext, context))),
                    ],
                  ))),
          Expanded(
            child: _createRoundedButton(_sendCommandEnd, _symbolEnd, context),
          )
        ],
      );
    } else {
      final TextTheme theme = Theme.of(context).textTheme;
      return SingleChildScrollView(
          child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          _createTextWithMaxWidth(AppLocalizations.of(context)!.welcome,
              TextAlign.center, theme.headlineMedium!,
              padding: EdgeInsets.zero),
          _createTextWithMaxWidth(AppLocalizations.of(context)!.not_linked,
              TextAlign.center, theme.bodyLarge!),
          _createTextWithMaxWidth(AppLocalizations.of(context)!.instructions_01,
              TextAlign.center, theme.bodyLarge!),
          GestureDetector(
            child: Padding(
                padding: const EdgeInsets.only(top: 16),
                child: Text(
                  _urlHomepage,
                  style: theme.bodyLarge!
                      .copyWith(decoration: TextDecoration.underline),
                )),
            onTap: () async {
              try {
                await launchUrl(Uri.parse(_urlHomepage));
              } catch (err) {
                debugPrint('Something bad happened');
              }
            },
          ),
          _createTextWithMaxWidth(AppLocalizations.of(context)!.instructions_02,
              TextAlign.center, theme.bodyLarge!),
          _createTextWithMaxWidth(AppLocalizations.of(context)!.instructions_03,
              TextAlign.center, theme.bodyLarge!),
          _createTextWithMaxWidth(AppLocalizations.of(context)!.instructions_04,
              TextAlign.center, theme.bodyLarge!,
              padding: const EdgeInsets.only(top: 16, bottom: 24)),
          TextButton(
              onPressed: _scanQRCode,
              child: Text(AppLocalizations.of(context)!.scan)),
        ],
      ));
    }
  }

  Widget _createTextWithMaxWidth(
      String text, TextAlign textAlign, TextStyle style,
      {EdgeInsets padding = const EdgeInsets.only(top: 16)}) {
    return Container(
        constraints: const BoxConstraints(maxWidth: 400),
        child: Padding(
            padding: padding,
            child: Text(text, textAlign: textAlign, style: style)));
  }

  Widget _createRoundedButton(
      void Function() command, IconData iconData, BuildContext context) {
    var color = theme!.colorScheme.primary;
    return IconButton(
      icon: Icon(iconData),
      onPressed: command,
      style: ButtonStyle(
          shape: MaterialStateProperty.all<RoundedRectangleBorder>(
              RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(18.0),
                  side: BorderSide(color: color)))),
    );
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
            .timeout(const Duration(seconds: 10));
        if (response.statusCode == 200) return;
      } on Exception catch (e) {
        debugPrint('$e');
      }
      const snackBar = SnackBar(content: Text("Server did not respond"));
      ScaffoldMessenger.of(scaffoldContext!).showSnackBar(snackBar);
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
    theme = ThemeData(
      brightness: brightness,
      useMaterial3: true,
    );
  }

  void _toggleShakeEnabled() {
    setState(() {
      shakeEnabled = !shakeEnabled;
      SharedPreferences.getInstance().then((prefs) {
        prefs.setBool(_keyShakeEnabled, shakeEnabled);
      });
      _startOrStopListening();
    });
  }

  void _startOrStopListening() {
    if (shakeEnabled) {
      detector?.startListening();
    } else {
      detector?.stopListening();
    }
  }
}

class _SouffleurHttpOverrides extends HttpOverrides {
  _SouffleurClientState state;

  _SouffleurHttpOverrides(this.state);

  @override
  HttpClient createHttpClient(SecurityContext? context) {
    return super.createHttpClient(context)
      ..badCertificateCallback =
          (X509Certificate cert, String host, int port) =>
              state.lastKnownUrl.contains(host);
  }
}
