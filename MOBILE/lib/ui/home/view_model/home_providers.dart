import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../data/models/ranked_toy.dart';
import '../../../data/repositories/catalog_repository.dart';

/// Top-5 recommended toys (Profil Seimbang) for the home hub.
final topToysProvider = FutureProvider.autoDispose<List<RankedToy>>((ref) {
  return ref.watch(catalogRepositoryProvider).top(limit: 5);
});
