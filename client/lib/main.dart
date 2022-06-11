import 'dart:async';
import 'dart:convert';
import 'dart:ffi';

import 'package:flutter/material.dart';
import 'package:barcode_scan/barcode_scan.dart';
import 'package:http/http.dart' as http;
import 'package:auto_size_text/auto_size_text.dart';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:swipedetector/swipedetector.dart';

void main() => runApp(SouffleurClient());

class SouffleurClient extends StatefulWidget {
  @override
  State<SouffleurClient> createState() => _SouffleurClientState();
}

class _SouffleurClientState extends State<SouffleurClient> {
  var notesGroup = AutoSizeGroup();

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return SwipeDetector(
      onSwipeLeft: () => setState(() {
        fetchSlideNotes("next");
      }),
      onSwipeRight: () => setState(() {
        fetchSlideNotes("previous");
      }),
      child: Container(
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
                        future: fetchSlideNotes("next"),
                        builder: (context, snapshot) {
                          if (snapshot.hasData) {
                            return Text("ok");
                          } else {
                            if (snapshot.connectionState ==
                                ConnectionState.done) {
                              return FlatButton(
                                child: Text(
                                  "Scan",
                                  style: TextStyle(
                                      fontSize: 72, color: Colors.black),
                                ),
                                onPressed: _onPressed,
                              );
                            } else {
                              return CircularProgressIndicator();
                            }
                          }
                        }),
                  ),
                ),
                FutureBuilder<bool>(
//                    future: currentSlideNotes,
                    builder: (context, snapshot) {
                      if (snapshot.hasData) {
                        var slideNotes = snapshot.data;
                        return Padding(
                            padding: EdgeInsets.only(bottom: 4),
                            child: Text(
                              "super",
                              style: TextStyle(
                                  fontSize: 32, color: Colors.black38),
                            ));
                      } else {
                        return SizedBox.shrink();
                      }
                    }),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Future _onPressed() async {
    try {
      String lastKnownUrl = await BarcodeScanner.scan().toString();
      SharedPreferences.getInstance().then((prefs) {
        prefs.setString('lastKnownUrl', lastKnownUrl);
        setState(() {
          fetchSlideNotes("start");
        });
      });
    } on Exception catch (e) {
      debugPrint('$e');
    }
  }

  Future<bool> fetchSlideNotes(String suffix) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final lastKnownUrl = prefs.getString('lastKnownUrl');
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
