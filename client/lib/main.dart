import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:barcode_scan/barcode_scan.dart';
import 'package:flutter/services.dart';
import 'package:http/http.dart' as http;
import 'package:auto_size_text/auto_size_text.dart';
import 'package:flutter/foundation.dart';

void main() => runApp(SouffleurClient());

class SouffleurClient extends StatefulWidget {
  @override
  State<SouffleurClient> createState() => _SouffleurClientState();
}

class _SouffleurClientState extends State<SouffleurClient> {
  String baseUrl = "";
  Future<SlideNotes> currentSlideNotes;
  var notesGroup = AutoSizeGroup();

  @override
  void initState() {
    super.initState();
    currentSlideNotes = fetchSlideNotes();
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
                //alignment: Alignment.center,
                child: FutureBuilder<SlideNotes>(
                  future: currentSlideNotes,
                  builder: (context, snapshot) {
                    if (snapshot.hasData) {
                      return _getNotes(snapshot.data.notes);
                    } else {
                      return FlatButton(
                        child: Text(
                          "Scan",
                          style: TextStyle(fontSize: 72, color: Colors.black),
                        ),
                        onPressed: _onPressed,
                      );
                    }
                  },
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _getNotes(List<String> notes) {
    List<Widget> result = [];
    notes.forEach((note) => result.add(Padding(
        padding: EdgeInsets.only(bottom: 16),
        child: AutoSizeText(
          note,
          group: notesGroup,
          style: TextStyle(fontSize: 72, color: Colors.black),
          maxLines: 3,
        ))));
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisSize: MainAxisSize.min,
      children: result,
    );
  }

  Future _onPressed() async {
    try {
      String barcode = await BarcodeScanner.scan();
      setState(
          () => {baseUrl = barcode, currentSlideNotes = fetchSlideNotes()});
    } on PlatformException catch (e) {
      debugPrint('$e');
    }
  }

  Future<SlideNotes> fetchSlideNotes() async {
    try {
      final response = await http.get(baseUrl + "next");
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
