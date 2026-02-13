import 'package:flutter/material.dart';
import '../auth/login_screen.dart';

class WelcomeScreen extends StatelessWidget {
  const WelcomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            colors: [Color(0xFFFDEFEF), Color(0xFFE8E5FF)],
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
          ),
        ),
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 24.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                const Icon(Icons.favorite, size: 80, color: Colors.pinkAccent),
                const SizedBox(height: 24),
                const Text(
                  'For Every Story.',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 36,
                    fontWeight: FontWeight.bold,
                    fontFamily: 'Georgia',
                    color: Color(0xFF333333),
                  ),
                ),
                const SizedBox(height: 16),
                const Text(
                  'We believe that every person carries a unique story worth sharing. '
                  'Our mission is to create a space where authenticity is valued, '
                  'where deeper conversations are encouraged, and where every connection has the potential '
                  'to become something more. Forget about superficiality, find someone '
                  'who understands your chapter.',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 18,
                    height: 1.5,
                    color: Color(0xFF555555),
                  ),
                ),
                const SizedBox(height: 48),
                ElevatedButton(
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => const LoginScreen(),
                      ),
                    );
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.pinkAccent,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(
                      horizontal: 40,
                      vertical: 15,
                    ),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(30.0),
                    ),
                    elevation: 5,
                  ),
                  child: const Text(
                    'Get Started',
                    style: TextStyle(fontSize: 18),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
