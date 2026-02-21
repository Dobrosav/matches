class UserModel {
  final int id;
  final String name;
  final String surname;
  final String email;
  final String sex;
  final String username;
  final DateTime dateOfBirth;
  final String? bio;
  final String? interests;
  final String? location;
  final bool isPremium;

  UserModel({
    required this.id,
    required this.name,
    required this.surname,
    required this.email,
    required this.sex,
    required this.username,
    required this.dateOfBirth,
    this.bio,
    this.interests,
    this.location,
    this.isPremium = false,
  });

  factory UserModel.fromJson(Map<String, dynamic> json) {
    DateTime dob;
    if (json['dateOfBirth'] is int) {
      dob = DateTime.fromMillisecondsSinceEpoch(json['dateOfBirth']);
    } else {
      dob = DateTime.parse(json['dateOfBirth']);
    }

    return UserModel(
      id: json['id'],
      name: json['name'],
      surname: json['surname'],
      email: json['email'],
      sex: json['sex'],
      username: json['username'],
      dateOfBirth: dob,
      bio: json['bio'],
      interests: json['interests'],
      location: json['location'],
      isPremium: json['premium'] ?? false,
    );
  }
}
