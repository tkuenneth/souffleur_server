import 'dart:async';
import 'dart:convert';

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
  Future<SlideNotes> currentSlideNotes;
  var notesGroup = AutoSizeGroup();

  @override
  void initState() {
    super.initState();
    currentSlideNotes = fetchSlideNotes("current");
  }

  @override
  Widget build(BuildContext context) {
    return SwipeDetector(
      onSwipeLeft: () => setState(() {
        currentSlideNotes = fetchSlideNotes("next");
      }),
      onSwipeRight: () => setState(() {
        currentSlideNotes = fetchSlideNotes("previous");
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
                    child: FutureBuilder<SlideNotes>(
                        future: currentSlideNotes,
                        builder: (context, snapshot) {
                          if (snapshot.hasData) {
                            return _getNotes(snapshot.data.notes);
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
                FutureBuilder<SlideNotes>(
                    future: currentSlideNotes,
                    builder: (context, snapshot) {
                      if (snapshot.hasData) {
                        var slideNotes = snapshot.data;
                        return Padding(
                            padding: EdgeInsets.only(bottom: 4),
                            child: Text(
                              "${slideNotes.slideNumber} / ${slideNotes.total}",
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

  Widget _getNotes(List<String> notes) {
    List<Widget> result = [];
    notes.forEach((note) {
      if (result.length > 0) {
        result.add(SizedBox(height: 16));
      }
      result.add(AutoSizeText(
        note,
        group: notesGroup,
        style: TextStyle(fontSize: 72, color: Colors.black),
        maxLines: 3,
      ));
    });
    return Align(
        alignment: Alignment.centerLeft,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: result,
        ));
  }

  Future _onPressed() async {
    try {
      String lastKnownUrl = await BarcodeScanner.scan();
      SharedPreferences.getInstance().then((prefs) {
        prefs.setString('lastKnownUrl', lastKnownUrl);
        setState(() {
          currentSlideNotes = fetchSlideNotes("start");
        });
      });
    } on Exception catch (e) {
      debugPrint('$e');
    }
  }

  Future<SlideNotes> fetchSlideNotes(String suffix) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final lastKnownUrl = prefs.getString('lastKnownUrl');
      final response = await http
          .get(lastKnownUrl + suffix)
          .timeout(const Duration(seconds: 5));
      if (response.statusCode == 200) {
        return SlideNotes.fromJson(json.decode(response.body));
      }
    } on Exception catch (e) {
      debugPrint('$e');
    }
    return null;
  }
}

class SlideNotes {
  final String name;
  final List<String> notes;
  final int slideNumber;
  final int total;

  SlideNotes({this.name, this.notes, this.slideNumber, this.total});

  factory SlideNotes.fromJson(Map<String, dynamic> json) {
    return SlideNotes(
      name: json['name'],
      notes: new List<String>.from(json['notes']),
      slideNumber: json['slideNumber'],
      total: json['total'],
    );
  }
}
