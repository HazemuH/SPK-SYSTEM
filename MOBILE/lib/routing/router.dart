import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../ui/home/widgets/home_screen.dart';
import 'routes.dart';

/// Provides the app [GoRouter]. The app is read-only and login-less, so there
/// is no redirect logic — it opens straight to the home screen.
final routerProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    initialLocation: Routes.home,
    routes: [
      GoRoute(
        path: Routes.home,
        name: 'home',
        builder: (_, _) => const HomeScreen(),
      ),
    ],
  );
});
