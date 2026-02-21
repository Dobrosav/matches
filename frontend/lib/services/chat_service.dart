import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/chat_message_model.dart';
import 'auth_service.dart';

class ChatService {
  final String baseUrl = 'http://localhost:8080/api/v1';
  final AuthService _authService = AuthService();

  Future<List<ChatMessageModel>> getChatHistory(int matchId) async {
    if (!_authService.isAuthenticated) {
      throw Exception('Not authenticated');
    }

    final response = await http.get(
      Uri.parse('$baseUrl/matches/$matchId/messages'),
      headers: <String, String>{
        'Content-Type': 'application/json; charset=UTF-8',
        'Authorization': 'Bearer ${_authService.accessToken}',
      },
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((json) => ChatMessageModel.fromJson(json)).toList();
    } else {
      throw Exception('Failed to load chat history: ${response.statusCode}');
    }
  }

  Future<ChatMessageModel> sendMessage(
    int matchId,
    int senderId,
    String content,
  ) async {
    if (!_authService.isAuthenticated) {
      throw Exception('Not authenticated');
    }

    final response = await http.post(
      Uri.parse('$baseUrl/matches/$matchId/messages'),
      headers: <String, String>{
        'Content-Type': 'application/json; charset=UTF-8',
        'Authorization': 'Bearer ${_authService.accessToken}',
      },
      body: jsonEncode(<String, dynamic>{
        'senderId': senderId,
        'content': content,
      }),
    );

    if (response.statusCode == 200) {
      return ChatMessageModel.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Failed to send message: ${response.statusCode}');
    }
  }
}
