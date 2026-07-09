import 'package:equatable/equatable.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../data/models/recommendation.dart';
import '../../../data/models/weight_profile.dart';
import '../../../data/repositories/catalog_repository.dart';

/// Published weight profiles — the options for the quiz's "paling penting?" step.
/// Choosing one selects an AHP-validated weight vector for the ranking.
final publishedProfilesProvider = FutureProvider.autoDispose<List<WeightProfile>>((ref) {
  return ref.watch(catalogRepositoryProvider).profiles();
});

/// The preference quiz answers — the family key for [recommendationProvider].
class RecommendRequest extends Equatable {
  const RecommendRequest({
    required this.usia,
    required this.budget,
    required this.tujuan,
    required this.prioritas,
  });

  final String usia;
  final String budget;
  final String tujuan;
  final String prioritas;

  @override
  List<Object?> get props => [usia, budget, tujuan, prioritas];
}

/// Recommendation for the given quiz answers.
final recommendationProvider =
    FutureProvider.autoDispose.family<Recommendation, RecommendRequest>((ref, req) {
  return ref.watch(catalogRepositoryProvider).recommend(
        usia: req.usia,
        budget: req.budget,
        tujuan: req.tujuan,
        prioritas: req.prioritas,
      );
});
