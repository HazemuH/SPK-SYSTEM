import 'package:flutter/material.dart';

import '../themes/colors.dart';

/// Centered loading indicator for in-progress async states.
class AppLoading extends StatelessWidget {
  const AppLoading({super.key, this.color});

  final Color? color;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: CircularProgressIndicator(
        color: color ?? AppColors.primary,
      ),
    );
  }
}
