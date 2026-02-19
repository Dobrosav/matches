import 'dart:convert';
import 'package:http/http.dart' as http;

class AuthService {
  static const String baseUrl = 'http://localhost:8080/api/v1';

  static final AuthService _instance = AuthService._internal();

  factory AuthService() {
    return _instance;
  }

  AuthService._internal();

  String? _accessToken;

  bool get isAuthenticated => _accessToken != null;

  String? get accessToken => _accessToken;

  Future<Map<String, dynamic>> login(String email, String password) async {
    final response = await http.post(
      Uri.parse('$baseUrl/auth/login'),
      headers: <String, String>{
        'Content-Type': 'application/json; charset=UTF-8',
      },
      body: jsonEncode(<String, String>{'email': email, 'password': password}),
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      _accessToken = data['access_token'];
      return data;
    } else {
      _handleError(response);
      throw Exception('Failed to login'); // Should be unreachable
    }
  }

  Future<Map<String, dynamic>> register({
    required String name,
    required String surname,
    required String email,
    required String username,
    required String password,
    required String sex,
    required DateTime dateOfBirth,
    required String disabilities,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/auth/register'),
      headers: <String, String>{
        'Content-Type': 'application/json; charset=UTF-8',
      },
      body: jsonEncode(<String, dynamic>{
        'name': name,
        'surname': surname,
        'email': email,
        'username': username,
        'password': password,
        'sex': sex,
        'dateOfBirth': dateOfBirth.toIso8601String(),
        'disabilities': disabilities,
      }),
    );

    if (response.statusCode == 200) {
      // The backend returns AuthenticationResponse which has access_token
      // However, check if the response body is directly the token or a JSON object
      // Based on UserController.java: return ResponseEntity.ok(authenticationService.register(request));
      // And AuthenticationService returns AuthenticationResponse
      final data = jsonDecode(response.body);
      if (data['access_token'] != null) {
        _accessToken = data['access_token'];
      }
      return data;
    } else {
      _handleError(response);
      throw Exception('Failed to register');
    }
  }

  Future<Map<String, dynamic>> getProfile() async {
    if (_accessToken == null) {
      throw Exception('Not authenticated');
    }

    final response = await http.get(
      Uri.parse('$baseUrl/profile/me'),
      headers: <String, String>{
        'Content-Type': 'application/json; charset=UTF-8',
        'Authorization': 'Bearer $_accessToken',
      },
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      _handleError(response);
      throw Exception('Failed to get profile');
    }
  }

  Future<Map<String, dynamic>> updateProfile({
    String? bio,
    String? interests,
    String? location,
  }) async {
    if (_accessToken == null) {
      throw Exception('Not authenticated');
    }

    final response = await http.put(
      Uri.parse('$baseUrl/profile/me'),
      headers: <String, String>{
        'Content-Type': 'application/json; charset=UTF-8',
        'Authorization': 'Bearer $_accessToken',
      },
      body: jsonEncode(<String, dynamic>{
        'bio': bio,
        'interests': interests,
        'location': location,
      }),
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      _handleError(response);
      throw Exception('Failed to update profile');
    }
  }

  void logout() {
    _accessToken = null;
  }

  void _handleError(http.Response response) {
    try {
      final errorData = jsonDecode(response.body);
      if (errorData is Map<String, dynamic> &&
          errorData.containsKey('message')) {
        throw Exception(errorData['message']);
      }
    } catch (e) {
      // If parsing fails or message is missing, fallback to raw body or status code
      if (e is! FormatException) {
        throw e; // Rethrow the specific exception from above
      }
    }
    throw Exception(
      'Request failed with status: ${response.statusCode}. Body: ${response.body}',
    );
  }
}
