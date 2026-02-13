import 'package:flutter/material.dart';
import '../feed/feed_screen.dart';
import '../profile/profile_screen.dart';
import '../../services/auth_service.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _selectedIndex = 0;
  final AuthService _authService = AuthService();
  String? _userEmail;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _fetchProfile();
  }

  Future<void> _fetchProfile() async {
    try {
      final profile = await _authService.getProfile();
      if (mounted) {
        setState(() {
          _userEmail = profile['email'];
          _isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
        // Ideally redirect to login
      }
    }
  }

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    if (_userEmail == null) {
      return const Scaffold(
        body: Center(child: Text("Failed to load profile")),
      );
    }

    final List<Widget> _widgetOptions = <Widget>[
      FeedScreen(userEmail: _userEmail!),
      const Center(child: Text('Matches (Coming Soon)')),
      const ProfileScreen(),
    ];

    return Scaffold(
      body: Center(child: _widgetOptions.elementAt(_selectedIndex)),
      bottomNavigationBar: BottomNavigationBar(
        items: const <BottomNavigationBarItem>[
          BottomNavigationBarItem(icon: Icon(Icons.home), label: 'Feed'),
          BottomNavigationBarItem(icon: Icon(Icons.favorite), label: 'Matches'),
          BottomNavigationBarItem(icon: Icon(Icons.person), label: 'Profile'),
        ],
        currentIndex: _selectedIndex,
        selectedItemColor: Colors.pinkAccent,
        onTap: _onItemTapped,
      ),
    );
  }
}
