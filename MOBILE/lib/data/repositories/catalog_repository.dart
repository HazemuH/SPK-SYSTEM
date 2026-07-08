import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../models/meta.dart';
import '../models/ranked_toy.dart';
import '../models/toy_detail.dart';
import '../services/public_api_service.dart';

final catalogRepositoryProvider = Provider<CatalogRepository>((ref) {
  return CatalogRepository(ref.watch(publicApiServiceProvider));
});

/// Orchestrates the read-only public catalog for the UI. Thin — it delegates to
/// the service and is the single seam the view models depend on.
class CatalogRepository {
  const CatalogRepository(this._api);

  final PublicApiService _api;

  Future<List<RankedToy>> top({String profile = 'balanced', int limit = 5}) =>
      _api.top(profile: profile, limit: limit);

  Future<List<RankedToy>> catalog({
    String profile = 'balanced',
    String? sort,
    String? categoryId,
    bool inStock = false,
    String search = '',
  }) =>
      _api.catalog(
        profile: profile,
        sort: sort,
        categoryId: categoryId,
        inStock: inStock,
        search: search,
      );

  Future<ToyDetail> detail(String id) => _api.detail(id);

  Future<Meta> meta() => _api.meta();
}
