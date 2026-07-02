import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../config/app_config.dart';
import '../../../routing/routes.dart';
import '../../auth/view_model/auth_view_model.dart';
import '../../core/themes/colors.dart';
import '../../core/themes/spacing.dart';
import '../../core/themes/typography.dart';
import '../../core/widgets/app_button.dart';

class DashboardScreen extends ConsumerWidget {
  const DashboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final user = ref.watch(authViewModelProvider).user;

    return Scaffold(
      appBar: AppBar(
        title: const Text(AppConfig.appName),
        actions: [
          AppButton(
            label: 'Keluar',
            variant: AppButtonVariant.ghost,
            size: AppButtonSize.small,
            leadingIcon: Icons.logout,
            onPressed: () async {
              await ref.read(authViewModelProvider.notifier).logout();
              if (context.mounted) context.go(Routes.login);
            },
          ),
          const SizedBox(width: AppSpacing.sm),
        ],
      ),
      body: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.dashboard_rounded, color: AppColors.primary, size: 72),
            const SizedBox(height: AppSpacing.lg),
            Text('Dashboard', style: AppTypography.headlineMedium),
            const SizedBox(height: AppSpacing.sm),
            Text(
              'Selamat datang${user != null ? ", ${user.name}" : ""}!',
              style: AppTypography.bodyLarge.copyWith(color: AppColors.textSecondary),
            ),
            const SizedBox(height: AppSpacing.sm),
            Text(
              'Coming Soon',
              style: AppTypography.bodyMedium.copyWith(color: AppColors.textDisabled),
            ),
          ],
        ),
      ),
    );
  }
}
