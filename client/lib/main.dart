import 'package:flutter/material.dart';
import 'package:barcode_scan/barcode_scan.dart';
import 'package:flutter/services.dart';
import 'package:http/http.dart' as http;
import 'dart:async';
import 'dart:convert';

void main() => runApp(SouffleurClient());

class SouffleurClient extends StatefulWidget {
  @override
  State<SouffleurClient> createState() => _SouffleurClientState();
}

class _SouffleurClientState extends State<SouffleurClient> {
  String baseUrl = "";
  Future<SlideNotes> currentSlideNotes;

  @override
  void initState() {
    super.initState();
    currentSlideNotes = fetchSlideNotes();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(color: Colors.green),
      child: Directionality(
        textDirection: TextDirection.ltr,
        child: Column(
          children: <Widget>[
            Expanded(
              child: Align(
                alignment: Alignment.center,
                child: FutureBuilder<SlideNotes>(
                  future: currentSlideNotes,
                  builder: (context, snapshot) {
                    if (snapshot.hasData) {
                      return Text(snapshot.data.name);
                    } else {
                      return FlatButton(
                        child: Text("Scan QR-Code"),
                        onPressed: _onPressed,
                      );
                    }
                    return CircularProgressIndicator();
                  },
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Future _onPressed() async {
    try {
      String barcode = await BarcodeScanner.scan();
      setState(
          () => {baseUrl = barcode, currentSlideNotes = fetchSlideNotes()});
    } on PlatformException catch (e) {}
  }

  Future<SlideNotes> fetchSlideNotes() async {
    try {
      final response = await http.get(baseUrl);
      if (response.statusCode == 200) {
        return SlideNotes.fromJson(json.decode(response.body));
      }
    } on Exception catch (e) {}
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
