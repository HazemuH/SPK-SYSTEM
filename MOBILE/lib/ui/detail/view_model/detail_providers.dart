import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../data/models/toy_detail.dart';
import '../../../data/repositories/catalog_repository.dart';

/// Detail for one toy, keyed by its id.
final toyDetailProvider =
    FutureProvider.autoDispose.family<ToyDetail, String>((ref, id) {
  return ref.watch(catalogRepositoryProvider).detail(id);
});
