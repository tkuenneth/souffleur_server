import 'package:flutter/material.dart';
import 'package:barcode_scan/barcode_scan.dart';
import 'package:flutter/services.dart';

void main() => runApp(SouffleurClient());

class SouffleurClient extends StatefulWidget {
  @override
  State<SouffleurClient> createState() => _SouffleurClientState();
}

class _SouffleurClientState extends State<SouffleurClient> {

  String baseUrl = "";

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
                child: Text("Hello", textScaleFactor: 2.0),
              ),
            ),
            Align(
              alignment: Alignment.bottomRight,
              child: FlatButton(
                child: Text("Scan QR-Code"),
                onPressed: _onPressed,
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
      setState(() => {
        baseUrl = barcode
      });
    } on PlatformException catch (e) {}
  }
}
