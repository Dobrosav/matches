import 'package:flutter/material.dart';
import '../../services/settings_service.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final settingsService = SettingsService();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Accessibility Settings'),
      ),
      body: ListenableBuilder(
        listenable: settingsService,
        builder: (context, _) {
          return ListView(
            padding: const EdgeInsets.all(16.0),
            children: [
              _buildSectionHeader('Appearance'),
              Card(
                child: Column(
                  children: [
                    RadioListTile<ThemeMode>(
                      title: const Text('System Theme'),
                      value: ThemeMode.system,
                      groupValue: settingsService.themeMode,
                      onChanged: (value) => settingsService.updateThemeMode(value!),
                      secondary: const Icon(Icons.brightness_auto),
                    ),
                    const Divider(height: 1),
                    RadioListTile<ThemeMode>(
                      title: const Text('Light Theme'),
                      value: ThemeMode.light,
                      groupValue: settingsService.themeMode,
                      onChanged: (value) => settingsService.updateThemeMode(value!),
                      secondary: const Icon(Icons.brightness_5),
                    ),
                    const Divider(height: 1),
                    RadioListTile<ThemeMode>(
                      title: const Text('Dark Theme'),
                      value: ThemeMode.dark,
                      groupValue: settingsService.themeMode,
                      onChanged: (value) => settingsService.updateThemeMode(value!),
                      secondary: const Icon(Icons.brightness_4),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),
              _buildSectionHeader('Text Size (Magnifier)'),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          const Text('Adjust App Text Size'),
                          Text(
                            '${(settingsService.textScaleFactor * 100).toInt()}%',
                            style: const TextStyle(fontWeight: FontWeight.bold),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                      Row(
                        children: [
                          const Icon(Icons.format_size, size: 16),
                          Expanded(
                            child: Slider(
                              value: settingsService.textScaleFactor,
                              min: 0.8,
                              max: 2.0,
                              divisions: 12, // 10% steps
                              onChanged: settingsService.updateTextScaleFactor,
                            ),
                          ),
                          const Icon(Icons.format_size, size: 32),
                        ],
                      ),
                      const SizedBox(height: 8),
                      const Text(
                        'This is a preview of how large the text will appear. Slide to adjust the scaling factor for improved readability.',
                        style: TextStyle(fontStyle: FontStyle.italic),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          );
        },
      ),
    );
  }

  Widget _buildSectionHeader(String title) {
    return Padding(
      padding: const EdgeInsets.only(left: 8.0, bottom: 8.0),
      child: Text(
        title,
        style: const TextStyle(
          fontSize: 18,
          fontWeight: FontWeight.bold,
          color: Colors.pinkAccent,
        ),
      ),
    );
  }
}
