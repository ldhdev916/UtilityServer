import 'package:flutter/material.dart';

void main() {
  runApp(const NamelessWeb());
}

class NamelessWeb extends StatelessWidget {
  const NamelessWeb({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
        title: "Nameless Admin Page", home: NamelessPage());
  }
}

class NamelessPage extends StatelessWidget {
  const NamelessPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
        body: Center(
            child: TextField(
                decoration: InputDecoration(
                    labelText: "Password", border: OutlineInputBorder()))));
  }
}
