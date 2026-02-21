import 'package:shared_preferences/shared_preferences.dart';

class FilterService {
  static const String _keyGender = 'filter_gender';
  static const String _keyMinAge = 'filter_min_age';
  static const String _keyMaxAge = 'filter_max_age';
  static const String _keyLocation = 'filter_location';

  Future<void> saveFilters({
    String? gender,
    int? minAge,
    int? maxAge,
    String? location,
  }) async {
    final prefs = await SharedPreferences.getInstance();

    if (gender != null) {
      await prefs.setString(_keyGender, gender);
    } else {
      await prefs.remove(_keyGender);
    }

    if (minAge != null) {
      await prefs.setInt(_keyMinAge, minAge);
    } else {
      await prefs.remove(_keyMinAge);
    }

    if (maxAge != null) {
      await prefs.setInt(_keyMaxAge, maxAge);
    } else {
      await prefs.remove(_keyMaxAge);
    }

    if (location != null) {
      await prefs.setString(_keyLocation, location);
    } else {
      await prefs.remove(_keyLocation);
    }
  }

  Future<Map<String, dynamic>> loadFilters() async {
    final prefs = await SharedPreferences.getInstance();

    return {
      'gender': prefs.getString(_keyGender),
      'minAge': prefs.getInt(_keyMinAge),
      'maxAge': prefs.getInt(_keyMaxAge),
      'location': prefs.getString(_keyLocation),
    };
  }

  Future<void> clearFilters() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_keyGender);
    await prefs.remove(_keyMinAge);
    await prefs.remove(_keyMaxAge);
    await prefs.remove(_keyLocation);
  }
}
