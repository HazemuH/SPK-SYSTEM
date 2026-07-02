import 'package:equatable/equatable.dart';

/// Application user. In this lightweight architecture a single model serves as
/// both the domain object and the data-transfer object (it carries
/// (de)serialization). Split into separate entity/DTO classes only if the API
/// shape ever diverges significantly from what the UI needs.
class User extends Equatable {
  const User({
    required this.id,
    required this.name,
    required this.email,
    this.avatarUrl,
    this.role,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id']?.toString() ?? '',
      name: json['name']?.toString() ?? '',
      email: json['email']?.toString() ?? '',
      avatarUrl: json['avatar_url']?.toString(),
      role: json['role']?.toString(),
    );
  }

  final String id;
  final String name;
  final String email;
  final String? avatarUrl;
  final String? role;

  Map<String, dynamic> toJson() => {
        'id': id,
        'name': name,
        'email': email,
        'avatar_url': avatarUrl,
        'role': role,
      };

  User copyWith({
    String? id,
    String? name,
    String? email,
    String? avatarUrl,
    String? role,
  }) {
    return User(
      id: id ?? this.id,
      name: name ?? this.name,
      email: email ?? this.email,
      avatarUrl: avatarUrl ?? this.avatarUrl,
      role: role ?? this.role,
    );
  }

  @override
  List<Object?> get props => [id, name, email, avatarUrl, role];
}
