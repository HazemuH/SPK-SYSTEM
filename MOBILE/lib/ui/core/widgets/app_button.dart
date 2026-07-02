import 'package:flutter/material.dart';

import '../themes/colors.dart';
import '../themes/spacing.dart';
import '../themes/typography.dart';

enum AppButtonVariant { primary, secondary, outline, ghost, danger }

enum AppButtonSize { small, medium, large }

/// The single button widget for the app. Use this instead of raw
/// `ElevatedButton` / `TextButton` / `OutlinedButton`.
class AppButton extends StatelessWidget {
  const AppButton({
    super.key,
    required this.label,
    this.onPressed,
    this.variant = AppButtonVariant.primary,
    this.size = AppButtonSize.medium,
    this.isLoading = false,
    this.isFullWidth = false,
    this.leadingIcon,
    this.trailingIcon,
  });

  final String label;
  final VoidCallback? onPressed;
  final AppButtonVariant variant;
  final AppButtonSize size;
  final bool isLoading;
  final bool isFullWidth;
  final IconData? leadingIcon;
  final IconData? trailingIcon;

  @override
  Widget build(BuildContext context) {
    final style = _resolveStyle();
    final padding = _resolvePadding();
    final textStyle = _resolveTextStyle();

    final Widget child = isLoading
        ? SizedBox(
            width: _loaderSize,
            height: _loaderSize,
            child: CircularProgressIndicator(
              strokeWidth: 2,
              color: _resolveLoaderColor(),
            ),
          )
        : Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (leadingIcon != null) ...[
                Icon(leadingIcon, size: _iconSize),
                const SizedBox(width: AppSpacing.sm),
              ],
              Text(label, style: textStyle),
              if (trailingIcon != null) ...[
                const SizedBox(width: AppSpacing.sm),
                Icon(trailingIcon, size: _iconSize),
              ],
            ],
          );

    final button = switch (variant) {
      AppButtonVariant.outline || AppButtonVariant.ghost => OutlinedButton(
          onPressed: isLoading ? null : onPressed,
          style: style,
          child: Padding(padding: padding, child: child),
        ),
      _ => ElevatedButton(
          onPressed: isLoading ? null : onPressed,
          style: style,
          child: Padding(padding: padding, child: child),
        ),
    };

    if (isFullWidth) {
      return SizedBox(width: double.infinity, child: button);
    }
    return button;
  }

  double get _loaderSize => switch (size) {
        AppButtonSize.small => 14,
        AppButtonSize.medium => 18,
        AppButtonSize.large => 20,
      };

  double get _iconSize => switch (size) {
        AppButtonSize.small => 14,
        AppButtonSize.medium => 18,
        AppButtonSize.large => 20,
      };

  EdgeInsets _resolvePadding() => switch (size) {
        AppButtonSize.small =>
          const EdgeInsets.symmetric(horizontal: AppSpacing.md, vertical: AppSpacing.xs),
        AppButtonSize.medium =>
          const EdgeInsets.symmetric(horizontal: AppSpacing.lg, vertical: AppSpacing.md),
        AppButtonSize.large =>
          const EdgeInsets.symmetric(horizontal: AppSpacing.xl, vertical: AppSpacing.md),
      };

  TextStyle _resolveTextStyle() {
    final base = switch (size) {
      AppButtonSize.small => AppTypography.labelMedium,
      AppButtonSize.medium => AppTypography.labelLarge,
      AppButtonSize.large => AppTypography.labelLarge,
    };
    return base.copyWith(color: _resolveForegroundColor());
  }

  Color _resolveForegroundColor() => switch (variant) {
        AppButtonVariant.primary => AppColors.white,
        AppButtonVariant.secondary => AppColors.white,
        AppButtonVariant.outline => AppColors.primary,
        AppButtonVariant.ghost => AppColors.primary,
        AppButtonVariant.danger => AppColors.white,
      };

  Color _resolveLoaderColor() => switch (variant) {
        AppButtonVariant.outline || AppButtonVariant.ghost => AppColors.primary,
        _ => AppColors.white,
      };

  ButtonStyle _resolveStyle() {
    final radius = BorderRadius.circular(12);
    final shape = RoundedRectangleBorder(borderRadius: radius);

    return switch (variant) {
      AppButtonVariant.primary => ElevatedButton.styleFrom(
          backgroundColor: AppColors.primary,
          foregroundColor: AppColors.white,
          disabledBackgroundColor: AppColors.primaryLight,
          elevation: 0,
          shape: shape,
          padding: EdgeInsets.zero,
        ),
      AppButtonVariant.secondary => ElevatedButton.styleFrom(
          backgroundColor: AppColors.secondary,
          foregroundColor: AppColors.white,
          elevation: 0,
          shape: shape,
          padding: EdgeInsets.zero,
        ),
      AppButtonVariant.outline => OutlinedButton.styleFrom(
          foregroundColor: AppColors.primary,
          side: const BorderSide(color: AppColors.primary),
          shape: shape,
          padding: EdgeInsets.zero,
        ),
      AppButtonVariant.ghost => OutlinedButton.styleFrom(
          foregroundColor: AppColors.primary,
          side: BorderSide.none,
          shape: shape,
          padding: EdgeInsets.zero,
        ),
      AppButtonVariant.danger => ElevatedButton.styleFrom(
          backgroundColor: AppColors.error,
          foregroundColor: AppColors.white,
          elevation: 0,
          shape: shape,
          padding: EdgeInsets.zero,
        ),
    };
  }
}
