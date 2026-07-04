import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../models/toy.dart';
import '../services/toy_api_service.dart';

final toyRepositoryProvider = Provider<ToyRepository>((ref) {
  return ToyRepository(ref.watch(toyApiServiceProvider));
});

/// Orchestrates toy data for the UI. Kept thin — it delegates to the service
/// and is the single seam the view models depend on.
class ToyRepository {
  const ToyRepository(this._service);

  final ToyApiService _service;

  Future<List<Toy>> getRecommendations() => _service.fetchRecommendations();
}
