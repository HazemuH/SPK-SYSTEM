import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../ui/auth/view_model/auth_view_model.dart';
import '../ui/auth/widgets/login_screen.dart';
import '../ui/dashboard/widgets/dashboard_screen.dart';
import '../ui/splash/widgets/splash_screen.dart';
import 'routes.dart';

/// Provides the app [GoRouter]. Redirects are driven by [authViewModelProvider]
/// so navigation stays in sync with the auth session.
final routerProvider = Provider<GoRouter>((ref) {
  final auth = ref.watch(authViewModelProvider);

  return GoRouter(
    initialLocation: Routes.splash,
    redirect: (BuildContext context, GoRouterState state) {
      // Wait until the session has been restored before redirecting.
      if (!auth.isInitialized) return null;

      final isOnLogin = state.matchedLocation == Routes.login;
      final isOnSplash = state.matchedLocation == Routes.splash;

      if (!auth.isAuthenticated && !isOnLogin && !isOnSplash) {
        return Routes.login;
      }
      if (auth.isAuthenticated && (isOnLogin || isOnSplash)) {
        return Routes.dashboard;
      }
      return null;
    },
    routes: [
      GoRoute(
        path: Routes.splash,
        name: 'splash',
        builder: (_, _) => const SplashScreen(),
      ),
      GoRoute(
        path: Routes.login,
        name: 'login',
        builder: (_, _) => const LoginScreen(),
      ),
      GoRoute(
        path: Routes.dashboard,
        name: 'dashboard',
        builder: (_, _) => const DashboardScreen(),
      ),
    ],
  );
});
