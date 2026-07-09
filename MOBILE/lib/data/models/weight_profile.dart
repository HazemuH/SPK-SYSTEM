import 'package:equatable/equatable.dart';

/// A published weight profile (`ProfileView`) — used by the compare switcher and
/// the recommendation quiz's priority step. `id` is the profile code.
class WeightProfile extends Equatable {
  const WeightProfile({
    required this.id,
    required this.name,
    required this.shortName,
    this.icon = '',
  });

  factory WeightProfile.fromJson(Map<String, dynamic> json) {
    return WeightProfile(
      id: json['id']?.toString() ?? '',
      name: json['name']?.toString() ?? '',
      shortName: json['shortName']?.toString() ?? json['name']?.toString() ?? '',
      icon: json['icon']?.toString() ?? '',
    );
  }

  final String id;
  final String name;
  final String shortName;

  /// Backend icon hint (lucide-style name, e.g. `scale`, `shield`).
  final String icon;

  @override
  List<Object?> get props => [id, name, shortName, icon];
}
