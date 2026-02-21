class ChatMessageModel {
  final int id;
  final int matchId;
  final int senderId;
  final String senderUsername;
  final String content;
  final DateTime timestamp;

  ChatMessageModel({
    required this.id,
    required this.matchId,
    required this.senderId,
    required this.senderUsername,
    required this.content,
    required this.timestamp,
  });

  factory ChatMessageModel.fromJson(Map<String, dynamic> json) {
    return ChatMessageModel(
      id: json['id'],
      matchId: json['matchId'],
      senderId: json['senderId'],
      senderUsername: json['senderUsername'] ?? 'Unknown',
      content: json['content'],
      timestamp: DateTime.parse(json['timestamp']),
    );
  }
}
