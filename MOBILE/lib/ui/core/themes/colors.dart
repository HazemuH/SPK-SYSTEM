import 'package:flutter/material.dart';

/// App color tokens. Never use raw `Colors.*` or hex literals in widgets —
/// reference these instead.
class AppColors {
  AppColors._();

  static const Color primary = Color(0xFF4F46E5); // indigo (matches design)
  static const Color primaryLight = Color(0xFFC7D2FE);
  static const Color primaryDark = Color(0xFF4338CA);

  static const Color secondary = Color(0xFF8B5CF6); // violet
  static const Color accent = Color(0xFFF59E0B); // amber

  static const Color background = Color(0xFFF8FAFC);
  static const Color surface = Color(0xFFFFFFFF);
  static const Color surfaceVariant = Color(0xFFF1F5F9);

  static const Color textPrimary = Color(0xFF0F172A);
  static const Color textSecondary = Color(0xFF64748B);
  static const Color textDisabled = Color(0xFFCBD5E1);

  static const Color success = Color(0xFF16A34A);
  static const Color warning = Color(0xFFD97706);
  static const Color error = Color(0xFFDC2626);
  static const Color info = Color(0xFF0284C7);

  static const Color border = Color(0xFFE2E8F0);
  static const Color divider = Color(0xFFF1F5F9);

  static const Color white = Color(0xFFFFFFFF);
  static const Color black = Color(0xFF000000);
}
