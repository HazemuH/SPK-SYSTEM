import 'package:equatable/equatable.dart';

import 'criterion.dart';
import 'ranked_toy.dart';
import 'toy.dart';

/// Full detail for one toy (`ToyDetail`): score/ranks, normalized r_ij per
/// criterion, strengths/weaknesses, and a next-best if it's out of stock.
class ToyDetail extends Equatable {
  const ToyDetail({
    required this.ranked,
    required this.globalRank,
    required this.categoryRank,
    required this.categoryTotal,
    required this.normalized,
    required this.strengths,
    required this.weaknesses,
    required this.nextBest,
  });

  factory ToyDetail.fromJson(Map<String, dynamic> json) {
    final norm = (json['normalized'] as Map?)?.map(
          (k, v) => MapEntry(k.toString(), (v as num?)?.toDouble() ?? 0),
        ) ??
        <String, double>{};
    List<Criterion> crits(String key) =>
        (json[key] as List?)?.map((e) => Criterion.fromJson(e as Map<String, dynamic>)).toList() ??
        const [];
    return ToyDetail(
      ranked: RankedToy.fromJson(json['ranked'] as Map<String, dynamic>),
      globalRank: (json['globalRank'] as num?)?.toInt() ?? 0,
      categoryRank: (json['categoryRank'] as num?)?.toInt() ?? 0,
      categoryTotal: (json['categoryTotal'] as num?)?.toInt() ?? 0,
      normalized: norm,
      strengths: crits('strengths'),
      weaknesses: crits('weaknesses'),
      nextBest: json['nextBest'] == null
          ? null
          : Toy.fromJson(json['nextBest'] as Map<String, dynamic>),
    );
  }

  final RankedToy ranked;
  final int globalRank;
  final int categoryRank;
  final int categoryTotal;
  final Map<String, double> normalized;
  final List<Criterion> strengths;
  final List<Criterion> weaknesses;
  final Toy? nextBest;

  Toy get toy => ranked.toy;

  @override
  List<Object?> get props =>
      [ranked, globalRank, categoryRank, categoryTotal, normalized, strengths, weaknesses, nextBest];
}
