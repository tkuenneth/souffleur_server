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

  @override
  void initState() {
    super.initState();
    SharedPreferences.getInstance().then((prefs) {
      setState(() {
        lastKnownUrl = prefs.getString('lastKnownUrl');
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(color: Colors.white),
      child: Directionality(
        textDirection: TextDirection.ltr,
        child: Column(
          children: <Widget>[
            Expanded(
              child: Align(
                alignment: Alignment.center,
                child: FutureBuilder<bool>(
                    future: _getFromServer(lastKnownUrl, "hello"),
                    builder: (context, snapshot) {
                      if (snapshot.connectionState != ConnectionState.done) {
                        return CircularProgressIndicator();
                      } else if (snapshot.data == true) {
                        return _createButtons();
                      } else {
                        return TextButton(
                          child: Text(
                            "Scan",
                            style: TextStyle(fontSize: 72, color: Colors.black),
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
    );
  }

  Widget _createButtons() {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        TextButton(onPressed: _sendCommandHome, child: Text("Home")),
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            TextButton(onPressed: _sendCommandPrevious, child: Text("Prev")),
            TextButton(onPressed: _sendCommandNext, child: Text("Next")),
          ],
        ),
        TextButton(onPressed: _sendCommandEnd, child: Text("End")),
      ],
    );
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

  void _sendCommand(String cmd) async{
    await _getFromServer(lastKnownUrl, cmd);
  }
}
