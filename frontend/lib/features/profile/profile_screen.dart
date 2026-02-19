import 'package:flutter/material.dart';
import '../../services/auth_service.dart';
import '../auth/login_screen.dart';
import '../../data/cities_data.dart'; // Import cities data

import '../../widgets/custom_text_field.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  final _authService = AuthService();
  Future<Map<String, dynamic>>? _profileFuture;

  @override
  void initState() {
    super.initState();
    _profileFuture = _authService.getProfile();
  }

  void _logout() {
    _authService.logout();
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (context) => const LoginScreen()),
      (route) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Profile'),
        actions: [
          IconButton(icon: const Icon(Icons.logout), onPressed: _logout),
        ],
      ),
      body: FutureBuilder<Map<String, dynamic>>(
        future: _profileFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          } else if (snapshot.hasError) {
            return Center(child: Text('Error: ${snapshot.error}'));
          } else if (!snapshot.hasData) {
            return const Center(child: Text('No profile data found'));
          }

          final user = snapshot.data!;
          return SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Center(
                  child: CircleAvatar(
                    radius: 50,
                    child: Icon(Icons.person, size: 50),
                  ),
                ),
                const SizedBox(height: 16),
                Center(
                  child: ElevatedButton(
                    onPressed: () => _showEditProfileDialog(user),
                    child: const Text('Edit Profile'),
                  ),
                ),
                const SizedBox(height: 24),
                _buildProfileItem('Username', user['username']),
                _buildProfileItem('Name', '${user['name']} ${user['surname']}'),
                _buildProfileItem('Email', user['email']),
                _buildProfileItem('Sex', user['sex']),
                if (user['location'] != null)
                  _buildProfileItem('Location', user['location']),
                if (user['bio'] != null) _buildProfileItem('Bio', user['bio']),
                if (user['interests'] != null)
                  _buildProfileItem('Interests', user['interests']),
                const SizedBox(height: 24),
                Center(
                  child: ElevatedButton(
                    onPressed: _logout,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.redAccent,
                      foregroundColor: Colors.white,
                    ),
                    child: const Text('Logout'),
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }

  void _showEditProfileDialog(Map<String, dynamic> user) {
    final bioController = TextEditingController(text: user['bio'] ?? '');
    final interestsController = TextEditingController(
      text: user['interests'] ?? '',
    );

    // Check if current location exists in our list
    String? selectedLocation = user['location'];
    bool locationExists = false;
    if (selectedLocation != null) {
      for (var cities in citiesByCountry.values) {
        if (cities.contains(selectedLocation)) {
          locationExists = true;
          break;
        }
      }
    }
    if (!locationExists) selectedLocation = null;

    final formKey = GlobalKey<FormState>();

    showDialog(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          title: const Text('Edit Profile'),
          content: SingleChildScrollView(
            child: Form(
              key: formKey,
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  DropdownButtonFormField<String>(
                    value: selectedLocation,
                    decoration: InputDecoration(
                      labelText: 'Location',
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                      filled: true,
                      fillColor: Colors.white,
                    ),
                    items: [
                      for (var entry in citiesByCountry.entries) ...[
                        DropdownMenuItem(
                          value: "HEADER_${entry.key}",
                          enabled: false,
                          child: Text(
                            entry.key,
                            style: const TextStyle(
                              fontWeight: FontWeight.bold,
                              color: Colors.grey,
                            ),
                          ),
                        ),
                        for (var city in entry.value)
                          DropdownMenuItem(
                            value: city,
                            child: Padding(
                              padding: const EdgeInsets.only(left: 16.0),
                              child: Text(city),
                            ),
                          ),
                      ],
                    ],
                    onChanged: (value) {
                      if (value != null && !value.startsWith("HEADER_")) {
                        selectedLocation = value;
                      }
                    },
                    validator: (value) {
                      if (value == null ||
                          value.isEmpty ||
                          value.startsWith("HEADER_")) {
                        return 'Please select your location';
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: 16),
                  CustomTextField(
                    controller: bioController,
                    label: 'Bio',
                    maxLines: 3,
                    keyboardType: TextInputType.multiline,
                    validator: (value) {
                      if (value != null && value.length > 100) {
                        return 'Bio must be 100 characters or less';
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: 16),
                  CustomTextField(
                    controller: interestsController,
                    label: 'Interests',
                    maxLines: 2,
                    keyboardType: TextInputType.multiline,
                    validator: (value) {
                      if (value != null && value.length > 255) {
                        return 'Interests must be 255 characters or less';
                      }
                      return null;
                    },
                  ),
                ],
              ),
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialogContext),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () async {
                if (formKey.currentState!.validate()) {
                  try {
                    await _authService.updateProfile(
                      bio: bioController.text,
                      interests: interestsController.text,
                      location: selectedLocation ?? '',
                    );
                    if (!context.mounted) return;
                    Navigator.pop(dialogContext);
                    setState(() {
                      _profileFuture = _authService.getProfile();
                    });
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(
                        content: Text('Profile updated successfully'),
                      ),
                    );
                  } catch (e) {
                    if (context.mounted) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(
                          content: Text(
                            'Failed to update profile: ${e.toString().replaceAll('Exception: ', '')}',
                          ),
                        ),
                      );
                    }
                  }
                }
              },
              child: const Text('Save'),
            ),
          ],
        );
      },
    );
  }

  Widget _buildProfileItem(String label, String? value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            label,
            style: const TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.bold,
              color: Colors.grey,
            ),
          ),
          const SizedBox(height: 4),
          Text(value ?? 'N/A', style: const TextStyle(fontSize: 16)),
          const Divider(),
        ],
      ),
    );
  }
}
