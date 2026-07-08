import 'package:equatable/equatable.dart';

/// An SPK criterion (`CriterionView`). `type` is "benefit" or "cost".
class Criterion extends Equatable {
  const Criterion({
    required this.code,
    required this.name,
    required this.type,
    required this.abbr,
  });

  factory Criterion.fromJson(Map<String, dynamic> json) {
    return Criterion(
      code: json['code']?.toString() ?? '',
      name: json['name']?.toString() ?? '',
      type: json['type']?.toString() ?? 'benefit',
      abbr: json['abbr']?.toString() ?? '',
    );
  }

  final String code;
  final String name;
  final String type;
  final String abbr;

  bool get isCost => type == 'cost';

  @override
  List<Object?> get props => [code, name, type, abbr];
}
