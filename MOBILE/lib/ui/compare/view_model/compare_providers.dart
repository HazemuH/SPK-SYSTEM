import 'package:equatable/equatable.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../data/models/compare_result.dart';
import '../../../data/models/weight_profile.dart';
import '../../../data/repositories/catalog_repository.dart';

/// The toys + profile to compare — the family key for [compareProvider].
class CompareRequest extends Equatable {
  const CompareRequest({required this.toyIds, required this.profile});

  final List<String> toyIds;
  final String profile;

  @override
  List<Object?> get props => [toyIds, profile];
}

/// Published weight profiles (for the live profile switcher).
final compareProfilesProvider = FutureProvider.autoDispose<List<WeightProfile>>((ref) {
  return ref.watch(catalogRepositoryProvider).profiles();
});

/// The comparison table for the given toys + profile.
final compareProvider =
    FutureProvider.autoDispose.family<CompareResult, CompareRequest>((ref, req) {
  return ref.watch(catalogRepositoryProvider).compare(req.toyIds, profile: req.profile);
});
