import 'package:flutter/material.dart';

import '../../../config/app_config.dart';
import '../themes/colors.dart';
import '../themes/spacing.dart';
import '../themes/typography.dart';

/// User feedback snackbars. Use this instead of `ScaffoldMessenger` directly.
class AppSnackbar {
  AppSnackbar._();

  static void showSuccess(BuildContext context, String message) {
    _show(context, message: message, icon: Icons.check_circle, color: AppColors.success);
  }

  static void showError(BuildContext context, String message) {
    _show(context, message: message, icon: Icons.error_outline, color: AppColors.error);
  }

  static void showWarning(BuildContext context, String message) {
    _show(context, message: message, icon: Icons.warning_amber_outlined, color: AppColors.warning);
  }

  static void showInfo(BuildContext context, String message) {
    _show(context, message: message, icon: Icons.info_outline, color: AppColors.info);
  }

  static void _show(
    BuildContext context, {
    required String message,
    required IconData icon,
    required Color color,
  }) {
    ScaffoldMessenger.of(context)
      ..hideCurrentSnackBar()
      ..showSnackBar(
        SnackBar(
          duration: AppConfig.snackbarDuration,
          behavior: SnackBarBehavior.floating,
          backgroundColor: AppColors.surface,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
            side: BorderSide(color: color.withValues(alpha: 0.3)),
          ),
          content: Row(
            children: [
              Icon(icon, color: color, size: 20),
              const SizedBox(width: AppSpacing.sm),
              Expanded(
                child: Text(
                  message,
                  style: AppTypography.bodyMedium.copyWith(color: AppColors.textPrimary),
                ),
              ),
            ],
          ),
        ),
      );
  }
}
