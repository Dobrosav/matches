import 'package:flutter/material.dart';
import 'features/welcome/welcome_screen.dart';
import 'services/settings_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final settingsService = SettingsService();

  // Wait a bit for initial load to prevent initial flicker (optional but good practice)
  await Future.delayed(const Duration(milliseconds: 100));

  runApp(MyApp(settingsService: settingsService));
}

class MyApp extends StatelessWidget {
  final SettingsService settingsService;

  const MyApp({super.key, required this.settingsService});

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: settingsService,
      builder: (BuildContext context, Widget? child) {
        return MaterialApp(
          title: 'Matches',
          themeMode: settingsService.themeMode,
          theme: ThemeData(
            colorScheme: ColorScheme.fromSeed(seedColor: Colors.pinkAccent),
            useMaterial3: true,
            focusColor: Colors.pink.withOpacity(0.3),
            inputDecorationTheme: InputDecorationTheme(
              filled: true,
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
              ),
              focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: const BorderSide(
                  color: Colors.pinkAccent,
                  width: 2.5,
                ),
              ),
            ),
          ),
          darkTheme: ThemeData.dark().copyWith(
            colorScheme: ColorScheme.fromSeed(
              seedColor: Colors.pinkAccent,
              brightness: Brightness.dark,
            ),
            useMaterial3: true,
            focusColor: Colors.pinkAccent.withOpacity(0.4),
            inputDecorationTheme: InputDecorationTheme(
              filled: true,
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
              ),
              focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: const BorderSide(
                  color: Colors.pinkAccent,
                  width: 2.5,
                ),
              ),
            ),
          ),
          debugShowCheckedModeBanner: false,
          builder: (context, child) {
            // Apply text scaling globally
            final data = MediaQuery.of(context);
            return MediaQuery(
              data: data.copyWith(
                textScaler: TextScaler.linear(settingsService.textScaleFactor),
              ),
              child: child!,
            );
          },
          home: const WelcomeScreen(),
        );
      },
    );
  }
}
