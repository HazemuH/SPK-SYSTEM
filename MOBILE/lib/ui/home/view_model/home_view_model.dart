import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../data/models/toy.dart';
import '../../../data/repositories/toy_repository.dart';

final homeViewModelProvider =
    AsyncNotifierProvider<HomeViewModel, List<Toy>>(HomeViewModel.new);

/// Loads the read-only toy recommendations for the home screen. Exposes the
/// list as an [AsyncValue] so the view can render loading/error/data states.
class HomeViewModel extends AsyncNotifier<List<Toy>> {
  @override
  Future<List<Toy>> build() {
    return ref.read(toyRepositoryProvider).getRecommendations();
  }

  /// Re-fetches recommendations (e.g. pull-to-refresh / retry).
  Future<void> refresh() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(
      () => ref.read(toyRepositoryProvider).getRecommendations(),
    );
  }
}
