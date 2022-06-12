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
        child: Padding(
          padding: EdgeInsets.only(left: 16, right: 16),
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
                          return Text("ok",
                              style:
                                  TextStyle(fontSize: 72, color: Colors.black));
                        } else {
                          return TextButton(
                            child: Text(
                              "Scan",
                              style:
                                  TextStyle(fontSize: 72, color: Colors.black),
                            ),
                            onPressed: _scanQRCode,
                          );
                        }
                      }),
                ),
              ),
              FutureBuilder<bool>(
//                    future: currentSlideNotes,
                  builder: (context, snapshot) {
                if (snapshot.hasData) {
//                  var slideNotes = snapshot.data;
                  return Padding(
                      padding: EdgeInsets.only(bottom: 4),
                      child: Text(
                        "super",
                        style: TextStyle(fontSize: 32, color: Colors.black38),
                      ));
                } else {
                  return SizedBox.shrink();
                }
              }),
            ],
          ),
        ),
      ),
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
}
