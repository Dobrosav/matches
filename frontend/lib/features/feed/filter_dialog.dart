import 'package:flutter/material.dart';
import '../../data/cities_data.dart';

class FilterDialog extends StatefulWidget {
  final String? initialGender;
  final int? initialMinAge;
  final int? initialMaxAge;
  final String? initialLocation;
  final bool isPremium;
  final String? userLocation;

  const FilterDialog({
    super.key,
    this.initialGender,
    this.initialMinAge,
    this.initialMaxAge,
    this.initialLocation,
    required this.isPremium,
    this.userLocation,
  });

  @override
  State<FilterDialog> createState() => _FilterDialogState();
}

class _FilterDialogState extends State<FilterDialog> {
  late String _selectedGender;
  late RangeValues _ageRange;
  late String _selectedLocation;

  @override
  void initState() {
    super.initState();
    _selectedGender = widget.initialGender ?? 'Any';
    _ageRange = RangeValues(
      (widget.initialMinAge ?? 18).toDouble(),
      (widget.initialMaxAge ?? 99).toDouble(),
    );
    _selectedLocation = widget.initialLocation ?? 'Any';
  }

  @override
  Widget build(BuildContext context) {
    List<String> locationOptions = ["Any"];
    final List<String> allCities =
        citiesByCountry.values.expand((cities) => cities).toList()..sort();
    locationOptions.addAll(allCities);
    // Ensure selected location is in the list, otherwise default to 'Any'
    if (!locationOptions.contains(_selectedLocation)) {
      _selectedLocation = 'Any';
    }

    return AlertDialog(
      title: const Text('Filter Matches'),
      content: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('Gender'),
            DropdownButton<String>(
              value: _selectedGender,
              isExpanded: true,
              onChanged: (String? newValue) {
                setState(() {
                  _selectedGender = newValue!;
                });
              },
              items: <String>['Any', 'Male', 'Female', 'Others']
                  .map<DropdownMenuItem<String>>((String value) {
                    return DropdownMenuItem<String>(
                      value: value,
                      child: Text(value),
                    );
                  })
                  .toList(),
            ),
            const SizedBox(height: 16),
            const Text('Age Range'),
            RangeSlider(
              values: _ageRange,
              min: 18,
              max: 100,
              divisions: 82,
              labels: RangeLabels(
                _ageRange.start.round().toString(),
                _ageRange.end.round().toString(),
              ),
              onChanged: (RangeValues values) {
                setState(() {
                  _ageRange = values;
                });
              },
            ),
            Center(
              child: Text(
                '${_ageRange.start.round()} - ${_ageRange.end.round()}',
                style: const TextStyle(fontWeight: FontWeight.bold),
              ),
            ),
            const SizedBox(height: 16),
            const Text('Location'),
            DropdownButton<String>(
              value: _selectedLocation,
              isExpanded: true,
              onChanged: (String? newValue) {
                setState(() {
                  _selectedLocation = newValue!;
                });
              },
              items: locationOptions.map<DropdownMenuItem<String>>((
                String value,
              ) {
                return DropdownMenuItem<String>(
                  value: value,
                  child: Text(value),
                );
              }).toList(),
            ),
          ],
        ),
      ),
      actions: <Widget>[
        TextButton(
          child: const Text('Cancel'),
          onPressed: () {
            Navigator.of(context).pop();
          },
        ),
        ElevatedButton(
          child: const Text('Apply'),
          onPressed: () {
            Navigator.of(context).pop({
              'gender': _selectedGender,
              'minAge': _ageRange.start.round(),
              'maxAge': _ageRange.end.round(),
              'location': _selectedLocation,
            });
          },
        ),
      ],
    );
  }
}
