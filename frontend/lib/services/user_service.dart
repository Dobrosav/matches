import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/user_model.dart';
import '../models/match_model.dart';
import 'auth_service.dart';

class UserService {
  final String baseUrl = 'http://localhost:8080/api/v1';
  final AuthService _authService = AuthService();

  Future<List<UserModel>> getFilteredFeed(
    String email, {
    String? gender,
    int? minAge,
    int? maxAge,
    String? location,
  }) async {
    if (!_authService.isAuthenticated) {
      throw Exception('Not authenticated');
    }

    final queryParameters = <String, String>{};
    if (gender != null) queryParameters['gender'] = gender;
    if (minAge != null) queryParameters['minAge'] = minAge.toString();
    if (maxAge != null) queryParameters['maxAge'] = maxAge.toString();
    if (location != null) queryParameters['location'] = location;

    final uri = Uri.parse(
      '$baseUrl/users/$email/filtered-feed',
    ).replace(queryParameters: queryParameters);

    final response = await http.get(
      uri,
      headers: <String, String>{
        'Content-Type': 'application/json; charset=UTF-8',
        'Authorization': 'Bearer ${_authService.accessToken}',
      },
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((json) => UserModel.fromJson(json)).toList();
    } else {
      throw Exception('Failed to load feed: ${response.statusCode}');
    }
  }

  Future<List<MatchModel>> getMatches(String email) async {
    if (!_authService.isAuthenticated) {
      throw Exception('Not authenticated');
    }

    final response = await http.get(
      Uri.parse('$baseUrl/users/$email/matches'),
      headers: <String, String>{
        'Content-Type': 'application/json; charset=UTF-8',
        'Authorization': 'Bearer ${_authService.accessToken}',
      },
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((json) => MatchModel.fromJson(json)).toList();
    } else {
      throw Exception('Failed to load matches: ${response.statusCode}');
    }
  }

  Future<void> setPremium(String email, bool isPremium) async {
    if (!_authService.isAuthenticated) {
      throw Exception('Not authenticated');
    }

    final response = await http.put(
      Uri.parse('$baseUrl/users/$email/premium?isPremium=$isPremium'),
      headers: <String, String>{
        'Content-Type': 'application/json; charset=UTF-8',
        'Authorization': 'Bearer ${_authService.accessToken}',
      },
    );

    if (response.statusCode != 200) {
      throw Exception(
        'Failed to update premium status: ${response.statusCode}',
      );
    }
  }
}
