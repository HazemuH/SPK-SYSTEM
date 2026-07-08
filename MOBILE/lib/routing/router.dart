import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../ui/catalog/widgets/catalog_screen.dart';
import '../ui/detail/widgets/detail_screen.dart';
import '../ui/home/widgets/home_screen.dart';
import 'routes.dart';

/// Provides the app [GoRouter]. The app is read-only and login-less, so there
/// is no redirect logic — it opens straight to the home hub.
final routerProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    initialLocation: Routes.home,
    routes: [
      GoRoute(
        path: Routes.home,
        name: 'home',
        builder: (_, _) => const HomeScreen(),
      ),
      GoRoute(
        path: Routes.catalog,
        name: 'catalog',
        builder: (_, _) => const CatalogScreen(),
      ),
      GoRoute(
        path: Routes.detailPattern,
        name: 'detail',
        builder: (_, state) => DetailScreen(toyId: state.pathParameters['id']!),
      ),
    ],
  );
});
