import 'package:flutter/material.dart';

void main() => runApp(SouffleurClient());

class SouffleurClient extends StatelessWidget {
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
                onPressed: onPressed,
              ),
            ),
          ],
        ),
      ),
    );
  }

  onPressed() {}
}
