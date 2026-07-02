import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../config/app_config.dart';
import '../../../routing/routes.dart';
import '../../auth/view_model/auth_view_model.dart';
import '../../core/themes/colors.dart';
import '../../core/themes/typography.dart';

class SplashScreen extends ConsumerStatefulWidget {
  const SplashScreen({super.key});

  @override
  ConsumerState<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends ConsumerState<SplashScreen> {
  @override
  void initState() {
    super.initState();
    _init();
  }

  Future<void> _init() async {
    await Future.wait([
      ref.read(authViewModelProvider.notifier).checkAuth(),
      Future.delayed(AppConfig.splashDuration),
    ]);

    if (!mounted) return;
    final isAuthenticated = ref.read(authViewModelProvider).isAuthenticated;
    context.go(isAuthenticated ? Routes.dashboard : Routes.login);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.primary,
      body: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 96,
              height: 96,
              decoration: BoxDecoration(
                color: AppColors.white.withValues(alpha: 0.15),
                borderRadius: BorderRadius.circular(24),
              ),
              child: const Icon(Icons.toys_rounded, color: AppColors.white, size: 52),
            ),
            const SizedBox(height: 24),
            Text(
              AppConfig.appName,
              style: AppTypography.headlineLarge.copyWith(color: AppColors.white),
            ),
          ],
        ),
      ),
    );
  }
}
