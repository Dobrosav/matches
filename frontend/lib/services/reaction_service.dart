import 'dart:convert';
import 'package:http/http.dart' as http;
import 'auth_service.dart';

class ReactionService {
  final String baseUrl = 'http://localhost:8080/api/v1';
  final AuthService _authService = AuthService();

  Future<void> react({
    required String fromUserEmail,
    required String toUserEmail,
    required String reaction,
  }) async {
    if (!_authService.isAuthenticated) {
      throw Exception('Not authenticated');
    }

    final uri = Uri.parse('$baseUrl/reactions');

    final response = await http.post(
      uri,
      headers: <String, String>{
        'Content-Type': 'application/json; charset=UTF-8',
        'Authorization': 'Bearer ${_authService.accessToken}',
      },
      body: jsonEncode(<String, String>{
        'fromUserEmail': fromUserEmail,
        'toUserEmail': toUserEmail,
        'reaction': reaction,
      }),
    );

    if (response.statusCode != 200 && response.statusCode != 201) {
      throw Exception('Failed to send reaction: ${response.statusCode}');
    }
  }
}
