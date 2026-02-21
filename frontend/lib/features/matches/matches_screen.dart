import 'package:flutter/material.dart';
import '../../models/match_model.dart';
import '../../services/user_service.dart';
import 'chat_screen.dart';

class MatchesScreen extends StatefulWidget {
  final String userEmail;

  const MatchesScreen({super.key, required this.userEmail});

  @override
  State<MatchesScreen> createState() => _MatchesScreenState();
}

class _MatchesScreenState extends State<MatchesScreen> {
  final UserService _userService = UserService();
  List<MatchModel> _matches = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadMatches();
  }

  Future<void> _loadMatches() async {
    try {
      final matches = await _userService.getMatches(widget.userEmail);
      if (mounted) {
        setState(() {
          _matches = matches;
          _isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text('Failed to load matches: $e')));
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_matches.isEmpty) {
      return const Center(child: Text('No matches yet'));
    }

    return Scaffold(
      appBar: AppBar(title: const Text('Matches')),
      body: RefreshIndicator(
        onRefresh: _loadMatches,
        child: ListView.builder(
          itemCount: _matches.length,
          itemBuilder: (context, index) {
            final match = _matches[index];
            final otherUser = match.otherUser;

            return ListTile(
              leading: CircleAvatar(
                backgroundColor: Colors.pinkAccent,
                child: Text(
                  otherUser.name.isNotEmpty
                      ? otherUser.name[0].toUpperCase()
                      : '?',
                  style: const TextStyle(color: Colors.white),
                ),
              ),
              title: Text('${otherUser.name} ${otherUser.surname}'),
              subtitle: Text(otherUser.bio ?? ''),
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => ChatScreen(
                      matchId: match.matchId,
                      otherUser: otherUser,
                    ),
                  ),
                );
              },
            );
          },
        ),
      ),
    );
  }
}
