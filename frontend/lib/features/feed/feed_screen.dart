import 'package:flutter/material.dart';
import 'package:card_swiper/card_swiper.dart';
import '../../models/user_model.dart';
import '../../services/user_service.dart';
import '../../services/reaction_service.dart';
import '../../services/filter_service.dart';
import 'filter_dialog.dart';
import 'user_card.dart';

class FeedScreen extends StatefulWidget {
  final String userEmail;
  final bool isPremium;
  final String? userLocation;

  const FeedScreen({
    super.key,
    required this.userEmail,
    required this.isPremium,
    this.userLocation,
  });

  @override
  State<FeedScreen> createState() => _FeedScreenState();
}

class _FeedScreenState extends State<FeedScreen> {
  final UserService _userService = UserService();
  final ReactionService _reactionService = ReactionService();
  final FilterService _filterService = FilterService();
  final SwiperController _swiperController = SwiperController();

  List<UserModel> _users = [];
  int _currentIndex = 0;
  bool _isLoading = true;
  String? _errorMessage;

  String? _selectedGender;
  int? _minAge;
  int? _maxAge;
  String? _selectedLocation;

  @override
  void initState() {
    super.initState();
    _loadSavedFiltersAndFetch();
  }

  Future<void> _loadSavedFiltersAndFetch() async {
    final filters = await _filterService.loadFilters();
    setState(() {
      _selectedGender = filters['gender'];
      _minAge = filters['minAge'];
      _maxAge = filters['maxAge'];
      _selectedLocation = filters['location'];
    });
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
        location: _selectedLocation,
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
        initialLocation: _selectedLocation,
        isPremium: widget.isPremium,
        userLocation: widget.userLocation,
      ),
    );

    if (result != null) {
      // Save filters locally
      await _filterService.saveFilters(
        gender: result['gender'] == 'Any' ? null : result['gender'],
        minAge: result['minAge'],
        maxAge: result['maxAge'],
        location: result['location'] == 'Any' ? null : result['location'],
      );

      setState(() {
        _selectedGender = result['gender'] == 'Any' ? null : result['gender'];
        _minAge = result['minAge'];
        _maxAge = result['maxAge'];
        _selectedLocation = result['location'] == 'Any'
            ? null
            : result['location'];
      });
      _fetchUsers();
    }
  }

  Future<void> _handleReaction(String reaction) async {
    if (_users.isEmpty) return;

    final userToReact = _users[_currentIndex];
    try {
      await _reactionService.react(
        fromUserEmail: widget.userEmail,
        toUserEmail: userToReact.email,
        reaction: reaction,
      );
    } catch (e) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Failed to send reaction: $e')));
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
            tooltip: 'Filter users',
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
          : Column(
              children: [
                Expanded(
                  child: Swiper(
                    controller: _swiperController,
                    itemCount: _users.length,
                    itemBuilder: (context, index) {
                      final user = _users[index];
                      return UserCard(user: user);
                    },
                    onIndexChanged: (index) {
                      setState(() {
                        _currentIndex = index;
                      });
                    },
                    loop: false,
                    pagination: const SwiperPagination(
                      builder: DotSwiperPaginationBuilder(
                        activeColor: Colors.blue,
                        color: Colors.grey,
                      ),
                    ),
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    children: [
                      Semantics(
                        label: 'Pass user',
                        button: true,
                        child: IconButton(
                          icon: const Icon(
                            Icons.close,
                            color: Colors.red,
                            size: 40,
                          ),
                          tooltip: 'Pass',
                          onPressed: () {
                            _handleReaction('dislike');
                            _swiperController.next();
                          },
                        ),
                      ),
                      Semantics(
                        label: 'Like user',
                        button: true,
                        child: IconButton(
                          icon: const Icon(
                            Icons.favorite,
                            color: Colors.green,
                            size: 40,
                          ),
                          tooltip: 'Like',
                          onPressed: () {
                            _handleReaction('like');
                            _swiperController.next();
                          },
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
    );
  }
}
