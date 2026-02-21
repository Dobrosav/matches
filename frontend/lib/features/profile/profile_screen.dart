import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/material.dart';
import 'package:image_cropper/image_cropper.dart';
import 'package:image_picker/image_picker.dart';
import '../../services/auth_service.dart';
import '../../services/user_service.dart';
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
  final _userService = UserService();
  Future<Map<String, dynamic>>? _profileFuture;
  String? _profileImageUrl;
  bool _isUploading = false;

  @override
  void initState() {
    super.initState();
    _profileFuture = _authService.getProfile();
    _loadProfileImage();
  }

  Future<void> _loadProfileImage() async {
    try {
      // We need to wait for the profile to be loaded first to get the email
      // Or we can just get it again since it's cached/cheap
      final profile = await _authService.getProfile();
      final email = profile['email'];
      final images = await _authService.getUserImages(email);

      String? profileUrl;
      for (var img in images) {
        if (img['profileImage'] == true) {
          profileUrl =
              '${AuthService.baseUrl}/users/$email/images/${img['id']}';
          break;
        }
      }

      if (mounted) {
        setState(() {
          _profileImageUrl = profileUrl;
        });
      }
    } catch (e) {
      debugPrint('Error loading profile image: $e');
    }
  }

  Future<void> _pickAndUploadImage() async {
    final picker = ImagePicker();
    final XFile? pickedFile = await picker.pickImage(
      source: ImageSource.gallery,
    );

    if (pickedFile != null) {
      if (!mounted) return;

      XFile? fileToUpload;

      // Skip cropping on web due to UI issues, use direct upload
      if (kIsWeb) {
        fileToUpload = pickedFile;
      } else {
        // Use cropper on mobile platforms
        CroppedFile? croppedFile;
        try {
          croppedFile = await ImageCropper().cropImage(
            sourcePath: pickedFile.path,
            aspectRatio: const CropAspectRatio(ratioX: 1, ratioY: 1),
            compressQuality: 70,
            maxWidth: 800,
            maxHeight: 800,
            compressFormat: ImageCompressFormat.jpg,
            uiSettings: [
              AndroidUiSettings(
                toolbarTitle: 'Edit Photo',
                toolbarColor: Colors.deepPurple,
                toolbarWidgetColor: Colors.white,
                initAspectRatio: CropAspectRatioPreset.square,
                lockAspectRatio: true,
              ),
              IOSUiSettings(title: 'Edit Photo', aspectRatioLockEnabled: true),
            ],
          );
        } catch (e) {
          if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text(
                  'Failed to crop image: ${e.toString().replaceAll('Exception: ', '')}',
                ),
              ),
            );
          }
          return;
        }

        if (croppedFile != null) {
          fileToUpload = XFile(croppedFile.path);
        }
      }

      if (fileToUpload != null) {
        setState(() {
          _isUploading = true;
        });

        try {
          final profile = await _authService.getProfile();
          final email = profile['email'];

          await _authService.uploadImage(email, fileToUpload);

          if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Image uploaded successfully')),
            );
            _loadProfileImage();
          }
        } catch (e) {
          if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text(
                  'Failed to upload image: ${e.toString().replaceAll('Exception: ', '')}',
                ),
              ),
            );
          }
        } finally {
          if (mounted) {
            setState(() {
              _isUploading = false;
            });
          }
        }
      }
    }
  }

  void _showUpgradePremiumDialog(String email) {
    showDialog(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          title: const Text('Upgrade to Premium'),
          content: const Text(
            'Get unlimited likes, see who liked you, and much more! Do you want to upgrade your account to Premium?',
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialogContext),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () async {
                Navigator.pop(dialogContext); // Close dialog first
                try {
                  await _userService.setPremium(email, true);
                  if (mounted) {
                    setState(() {
                      _profileFuture = _authService.getProfile();
                    });
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(
                        content: Text('Successfully upgraded to Premium!'),
                        backgroundColor: Colors.green,
                      ),
                    );
                  }
                } catch (e) {
                  if (mounted) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(
                        content: Text('Failed to upgrade: $e'),
                        backgroundColor: Colors.red,
                      ),
                    );
                  }
                }
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.amber,
                foregroundColor: Colors.black,
              ),
              child: const Text('Upgrade Now'),
            ),
          ],
        );
      },
    );
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
                Center(
                  child: Stack(
                    children: [
                      CircleAvatar(
                        radius: 50,
                        backgroundColor: Colors.grey[200],
                        backgroundImage: _profileImageUrl != null
                            ? NetworkImage(
                                _profileImageUrl!,
                                headers: {
                                  'Authorization':
                                      'Bearer ${_authService.accessToken}',
                                },
                              )
                            : null,
                        child: _profileImageUrl == null
                            ? const Icon(
                                Icons.person,
                                size: 50,
                                color: Colors.grey,
                              )
                            : null,
                      ),
                      Positioned(
                        bottom: 0,
                        right: 0,
                        child: GestureDetector(
                          onTap: _isUploading ? null : _pickAndUploadImage,
                          child: Container(
                            padding: const EdgeInsets.all(4),
                            decoration: const BoxDecoration(
                              color: Colors.blue,
                              shape: BoxShape.circle,
                            ),
                            child: _isUploading
                                ? const SizedBox(
                                    width: 20,
                                    height: 20,
                                    child: CircularProgressIndicator(
                                      color: Colors.white,
                                      strokeWidth: 2,
                                    ),
                                  )
                                : const Icon(
                                    Icons.camera_alt,
                                    color: Colors.white,
                                    size: 20,
                                  ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),
                Center(
                  child: user['premium'] == true
                      ? Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 16,
                            vertical: 8,
                          ),
                          decoration: BoxDecoration(
                            color: Colors.amber.withOpacity(0.2),
                            borderRadius: BorderRadius.circular(20),
                            border: Border.all(color: Colors.amber),
                          ),
                          child: const Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Icon(Icons.star, color: Colors.amber),
                              SizedBox(width: 8),
                              Text(
                                'Premium Member',
                                style: TextStyle(
                                  color: Colors.amber,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ],
                          ),
                        )
                      : ElevatedButton.icon(
                          onPressed: () =>
                              _showUpgradePremiumDialog(user['email']),
                          icon: const Icon(Icons.star),
                          label: const Text('Upgrade to Premium'),
                          style: ElevatedButton.styleFrom(
                            backgroundColor: Colors.amber,
                            foregroundColor: Colors.black,
                            elevation: 4,
                          ),
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

class _CustomCropperDialog extends StatefulWidget {
  final Widget cropper;
  final VoidCallback initCropper;
  final Future<String?> Function() crop;
  final ValueChanged<RotationAngle> rotate;

  const _CustomCropperDialog({
    super.key,
    required this.cropper,
    required this.initCropper,
    required this.crop,
    required this.rotate,
  });

  @override
  State<_CustomCropperDialog> createState() => _CustomCropperDialogState();
}

class _CustomCropperDialogState extends State<_CustomCropperDialog> {
  bool _isCropping = false;
  bool _isCropperReady = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      await Future.delayed(const Duration(milliseconds: 500));
      if (!mounted) return;
      widget.initCropper();
      await Future.delayed(const Duration(milliseconds: 300));
      if (mounted) {
        setState(() {
          _isCropperReady = true;
        });
      }
    });
  }

  Future<void> _handleCrop() async {
    setState(() {
      _isCropping = true;
    });

    try {
      // Add a timeout to prevent infinite hanging
      final result = await widget.crop().timeout(
        const Duration(seconds: 10),
        onTimeout: () {
          throw Exception('Cropping timed out');
        },
      );

      if (!mounted) return;

      if (result != null) {
        Navigator.of(context).pop(result);
      } else {
        // Only show error if result is null (which might mean user cancelled or failure)
        // But usually crop() returns path if successful.
        // If it returns null, it might be just cancelled internally?
        // Let's assume null is "failed to crop" in this context since we are explicitly clicking Save
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Could not crop image (result was null)'),
          ),
        );
      }
    } catch (e) {
      if (!mounted) return;
      showDialog(
        context: context,
        builder: (ctx) => AlertDialog(
          title: const Text('Error'),
          content: Text('Failed to crop image: $e'),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(ctx).pop(),
              child: const Text('OK'),
            ),
          ],
        ),
      );
    } finally {
      if (mounted) {
        setState(() {
          _isCropping = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Builder(
        builder: (context) {
          return Container(
            width: 800,
            height: 600,
            padding: const EdgeInsets.all(16.0),
            child: Column(
              children: [
                Expanded(child: widget.cropper),
                const SizedBox(height: 16),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    IconButton(
                      onPressed: _isCropping
                          ? null
                          : () =>
                                widget.rotate(RotationAngle.counterClockwise90),
                      icon: const Icon(Icons.rotate_left),
                      tooltip: 'Rotate Left',
                    ),
                    IconButton(
                      onPressed: _isCropping
                          ? null
                          : () => widget.rotate(RotationAngle.clockwise90),
                      icon: const Icon(Icons.rotate_right),
                      tooltip: 'Rotate Right',
                    ),
                    const Spacer(),
                    TextButton(
                      onPressed: _isCropping
                          ? null
                          : () => Navigator.of(context).pop(),
                      child: const Text('Cancel'),
                    ),
                    const SizedBox(width: 16),
                    ElevatedButton(
                      onPressed: _isCropping ? null : _handleCrop,
                      child: _isCropping
                          ? const SizedBox(
                              width: 20,
                              height: 20,
                              child: CircularProgressIndicator(
                                strokeWidth: 2,
                                color: Colors.white,
                              ),
                            )
                          : const Text('Save'),
                    ),
                  ],
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
