import 'package:flutter/material.dart';
import '../../models/user_model.dart';
import '../../services/user_service.dart';
import '../../services/auth_service.dart';
import 'filter_dialog.dart';

class FeedScreen extends StatefulWidget {
  final String userEmail;

  const FeedScreen({super.key, required this.userEmail});

  @override
  State<FeedScreen> createState() => _FeedScreenState();
}

class _FeedScreenState extends State<FeedScreen> {
  final UserService _userService = UserService();
  List<UserModel> _users = [];
  bool _isLoading = true;
  String? _errorMessage;

  String? _selectedGender;
  int? _minAge;
  int? _maxAge;

  @override
  void initState() {
    super.initState();
    _fetchUsers();
  }

  Future<void> _fetchUsers() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final users = await _userService.getFilteredFeed(
        widget.userEmail,
        gender: _selectedGender,
        minAge: _minAge,
        maxAge: _maxAge,
      );

      setState(() {
        _users = users;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _errorMessage = e.toString();
        _isLoading = false;
      });
    }
  }

  void _showFilterDialog() async {
    final result = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (context) => FilterDialog(
        initialGender: _selectedGender,
        initialMinAge: _minAge,
        initialMaxAge: _maxAge,
      ),
    );

    if (result != null) {
      setState(() {
        _selectedGender = result['gender'];
        _minAge = result['minAge'];
        _maxAge = result['maxAge'];
      });
      _fetchUsers();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Matches'),
        actions: [
          IconButton(
            icon: const Icon(Icons.filter_list),
            onPressed: _showFilterDialog,
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _errorMessage != null
          ? Center(child: Text('Error: $_errorMessage'))
          : _users.isEmpty
          ? const Center(child: Text('No users found'))
          : ListView.builder(
              itemCount: _users.length,
              itemBuilder: (context, index) {
                final user = _users[index];
                return Card(
                  margin: const EdgeInsets.symmetric(
                    horizontal: 16,
                    vertical: 8,
                  ),
                  child: ListTile(
                    leading: CircleAvatar(child: Text(user.name[0])),
                    title: Text(
                      '${user.name}, ${DateTime.now().year - user.dateOfBirth.year}',
                    ),
                    subtitle: Text(user.bio ?? 'No bio'),
                  ),
                );
              },
            ),
    );
  }
}
