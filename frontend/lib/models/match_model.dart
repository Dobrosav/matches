import 'user_model.dart';

class MatchModel {
  final int matchId;
  final UserModel otherUser;

  MatchModel({required this.matchId, required this.otherUser});

  factory MatchModel.fromJson(Map<String, dynamic> json) {
    return MatchModel(
      matchId: json['matchId'],
      otherUser: UserModel.fromJson(json['otherUser']),
    );
  }
}
