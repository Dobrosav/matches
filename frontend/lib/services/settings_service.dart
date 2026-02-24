import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SettingsService extends ChangeNotifier {
  static final SettingsService _instance = SettingsService._internal();

  factory SettingsService() {
    return _instance;
  }

  SettingsService._internal() {
    _loadSettings();
  }

  static const String _themeKey = 'theme_mode';
  static const String _textScaleKey = 'text_scale_factor';

  ThemeMode _themeMode = ThemeMode.system;
  double _textScaleFactor = 1.0;

  ThemeMode get themeMode => _themeMode;
  double get textScaleFactor => _textScaleFactor;

  Future<void> _loadSettings() async {
    final prefs = await SharedPreferences.getInstance();
    
    // Load Theme Mode
    final themeString = prefs.getString(_themeKey);
    if (themeString != null) {
      _themeMode = ThemeMode.values.firstWhere(
        (e) => e.toString() == themeString,
        orElse: () => ThemeMode.system,
      );
    }

    // Load Text Scale
    _textScaleFactor = prefs.getDouble(_textScaleKey) ?? 1.0;

    notifyListeners();
  }

  Future<void> updateThemeMode(ThemeMode newThemeMode) async {
    if (newThemeMode == _themeMode) return;
    
    _themeMode = newThemeMode;
    notifyListeners();

    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_themeKey, newThemeMode.toString());
  }

  Future<void> updateTextScaleFactor(double newFactor) async {
    if (newFactor == _textScaleFactor) return;

    _textScaleFactor = newFactor;
    notifyListeners();

    final prefs = await SharedPreferences.getInstance();
    await prefs.setDouble(_textScaleKey, newFactor);
  }
}
