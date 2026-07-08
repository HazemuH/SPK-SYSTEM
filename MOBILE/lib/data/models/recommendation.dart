import 'package:equatable/equatable.dart';

import 'ranked_toy.dart';

/// Result of the preference quiz (`Recommendation`): the chosen profile plus
/// the toys that match the preferences (primary) and the rest (others).
class Recommendation extends Equatable {
  const Recommendation({
    required this.profileId,
    required this.profileName,
    required this.baseCount,
    required this.usedFallback,
    required this.primary,
    required this.others,
  });

  factory Recommendation.fromJson(Map<String, dynamic> json) {
    List<RankedToy> parse(String key) =>
        (json[key] as List?)?.map((e) => RankedToy.fromJson(e as Map<String, dynamic>)).toList() ??
        const [];
    return Recommendation(
      profileId: json['profileId']?.toString() ?? '',
      profileName: json['profileName']?.toString() ?? '',
      baseCount: (json['baseCount'] as num?)?.toInt() ?? 0,
      usedFallback: json['usedFallback'] == true,
      primary: parse('primary'),
      others: parse('others'),
    );
  }

  final String profileId;
  final String profileName;
  final int baseCount;
  final bool usedFallback;
  final List<RankedToy> primary;
  final List<RankedToy> others;

  @override
  List<Object?> get props => [profileId, profileName, baseCount, usedFallback, primary, others];
}
