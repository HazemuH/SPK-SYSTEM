import 'package:equatable/equatable.dart';

import 'toy.dart';

/// A toy plus its SPK rank and SAW score within a ranking.
class RankedToy extends Equatable {
  const RankedToy({required this.rank, required this.score, required this.toy});

  factory RankedToy.fromJson(Map<String, dynamic> json) {
    return RankedToy(
      rank: (json['rank'] as num?)?.toInt() ?? 0,
      score: (json['score'] as num?)?.toDouble() ?? 0,
      toy: Toy.fromJson(json['toy'] as Map<String, dynamic>),
    );
  }

  final int rank;
  final double score;
  final Toy toy;

  @override
  List<Object?> get props => [rank, score, toy];
}
