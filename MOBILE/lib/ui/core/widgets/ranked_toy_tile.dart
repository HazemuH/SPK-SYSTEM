import 'package:flutter/material.dart';

import '../../../core/utils/formatter.dart';
import '../../../data/models/ranked_toy.dart';
import '../themes/colors.dart';
import '../themes/spacing.dart';
import '../themes/typography.dart';
import 'app_card.dart';

/// A ranked toy row: rank badge + name/category + price and a score bar.
/// Reused by the home top-list and the catalog.
class RankedToyTile extends StatelessWidget {
  const RankedToyTile({super.key, required this.item, required this.maxScore, this.onTap});

  final RankedToy item;
  final double maxScore;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final toy = item.toy;
    final fraction = maxScore > 0 ? (item.score / maxScore).clamp(0.0, 1.0) : 0.0;
    final isTop = item.rank <= 3;

    return AppCard(
      onTap: onTap,
      child: Row(
        children: [
          Container(
            width: 34,
            height: 34,
            alignment: Alignment.center,
            decoration: BoxDecoration(
              color: isTop ? AppColors.primary.withValues(alpha: 0.12) : AppColors.surfaceVariant,
              shape: BoxShape.circle,
            ),
            child: Text(
              '${item.rank}',
              style: AppTypography.labelLarge.copyWith(
                color: isTop ? AppColors.primary : AppColors.textSecondary,
              ),
            ),
          ),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(toy.name, style: AppTypography.labelLarge, maxLines: 1, overflow: TextOverflow.ellipsis),
                const SizedBox(height: 2),
                Row(
                  children: [
                    Text(toy.category, style: AppTypography.bodySmall),
                    if (!toy.inStock) ...[
                      const SizedBox(width: AppSpacing.sm),
                      Text('· Habis', style: AppTypography.bodySmall.copyWith(color: AppColors.error)),
                    ],
                  ],
                ),
                const SizedBox(height: AppSpacing.xs),
                ClipRRect(
                  borderRadius: BorderRadius.circular(4),
                  child: LinearProgressIndicator(
                    value: fraction,
                    minHeight: 5,
                    backgroundColor: AppColors.surfaceVariant,
                    valueColor: AlwaysStoppedAnimation(
                      isTop ? AppColors.primary : AppColors.primaryLight,
                    ),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: AppSpacing.sm),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(Formatter.formatCurrency(toy.price), style: AppTypography.labelMedium),
              const SizedBox(height: AppSpacing.xs),
              Text(
                item.score.toStringAsFixed(3),
                style: AppTypography.bodySmall.copyWith(
                  color: AppColors.textSecondary,
                  fontFeatures: const [],
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
