import 'package:equatable/equatable.dart';

/// A published weight profile (`ProfileView`) — used by the compare switcher.
class WeightProfile extends Equatable {
  const WeightProfile({required this.id, required this.name, required this.shortName});

  factory WeightProfile.fromJson(Map<String, dynamic> json) {
    return WeightProfile(
      id: json['id']?.toString() ?? '',
      name: json['name']?.toString() ?? '',
      shortName: json['shortName']?.toString() ?? json['name']?.toString() ?? '',
    );
  }

  final String id;
  final String name;
  final String shortName;

  @override
  List<Object?> get props => [id, name, shortName];
}
