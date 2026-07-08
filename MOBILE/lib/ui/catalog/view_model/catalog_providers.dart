import 'package:equatable/equatable.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../data/models/meta.dart';
import '../../../data/models/ranked_toy.dart';
import '../../../data/repositories/catalog_repository.dart';

/// The catalog's active filters — the family key for [catalogProvider].
class CatalogParams extends Equatable {
  const CatalogParams({
    this.profile = 'balanced',
    this.sort,
    this.categoryId,
    this.inStock = false,
    this.search = '',
  });

  final String profile;
  final String? sort;
  final String? categoryId;
  final bool inStock;
  final String search;

  CatalogParams copyWith({
    String? sort,
    bool clearSort = false,
    String? categoryId,
    bool clearCategory = false,
    bool? inStock,
    String? search,
  }) {
    return CatalogParams(
      profile: profile,
      sort: clearSort ? null : (sort ?? this.sort),
      categoryId: clearCategory ? null : (categoryId ?? this.categoryId),
      inStock: inStock ?? this.inStock,
      search: search ?? this.search,
    );
  }

  @override
  List<Object?> get props => [profile, sort, categoryId, inStock, search];
}

/// Reference data (categories, criteria, sort options) for filters & labels.
final metaProvider = FutureProvider.autoDispose<Meta>((ref) {
  return ref.watch(catalogRepositoryProvider).meta();
});

/// The ranked catalog for the given filters.
final catalogProvider =
    FutureProvider.autoDispose.family<List<RankedToy>, CatalogParams>((ref, params) {
  return ref.watch(catalogRepositoryProvider).catalog(
        profile: params.profile,
        sort: params.sort,
        categoryId: params.categoryId,
        inStock: params.inStock,
        search: params.search,
      );
});
