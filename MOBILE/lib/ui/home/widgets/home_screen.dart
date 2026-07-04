import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/utils/formatter.dart';
import '../../../data/models/toy.dart';
import '../../core/themes/colors.dart';
import '../../core/themes/spacing.dart';
import '../../core/themes/typography.dart';
import '../../core/widgets/app_card.dart';
import '../../core/widgets/app_empty_state.dart';
import '../../core/widgets/app_error_view.dart';
import '../../core/widgets/app_loading.dart';
import '../../core/widgets/screen_wrapper.dart';
import '../view_model/home_view_model.dart';

/// The app's entry screen. Read-only: shows the recommended toys (SPK ranking)
/// for the end user / shop staff. No login.
class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(homeViewModelProvider);

    return ScreenWrapper(
      padding: EdgeInsets.zero,
      appBar: AppBar(title: const Text('Rekomendasi Mainan')),
      child: state.when(
        loading: () => const AppLoading(),
        error: (_, _) => AppErrorView(
          message: 'Gagal memuat rekomendasi. Coba lagi.',
          onRetry: () => ref.read(homeViewModelProvider.notifier).refresh(),
        ),
        data: (toys) {
          if (toys.isEmpty) {
            return const AppEmptyState(
              icon: Icons.toys_outlined,
              title: 'Belum ada rekomendasi',
              subtitle: 'Data mainan belum tersedia saat ini.',
            );
          }
          return RefreshIndicator(
            onRefresh: () => ref.read(homeViewModelProvider.notifier).refresh(),
            child: ListView.separated(
              padding: const EdgeInsets.all(AppSpacing.md),
              itemCount: toys.length,
              separatorBuilder: (_, _) => const SizedBox(height: AppSpacing.sm),
              itemBuilder: (_, index) => _ToyTile(rank: index + 1, toy: toys[index]),
            ),
          );
        },
      ),
    );
  }
}

/// A single recommendation row: rank badge + name/category + price/score.
class _ToyTile extends StatelessWidget {
  const _ToyTile({required this.rank, required this.toy});

  final int rank;
  final Toy toy;

  @override
  Widget build(BuildContext context) {
    return AppCard(
      child: Row(
        children: [
          _RankBadge(rank: rank),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(toy.name, style: AppTypography.labelLarge),
                const SizedBox(height: AppSpacing.xs),
                Text(toy.category, style: AppTypography.bodySmall),
              ],
            ),
          ),
          const SizedBox(width: AppSpacing.sm),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(
                Formatter.formatCurrency(toy.price),
                style: AppTypography.labelMedium,
              ),
              const SizedBox(height: AppSpacing.xs),
              Text(
                'Skor ${toy.score.toStringAsFixed(2)}',
                style: AppTypography.bodySmall.copyWith(color: AppColors.primary),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _RankBadge extends StatelessWidget {
  const _RankBadge({required this.rank});

  final int rank;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 36,
      height: 36,
      alignment: Alignment.center,
      decoration: const BoxDecoration(
        color: AppColors.surfaceVariant,
        shape: BoxShape.circle,
      ),
      child: Text('$rank', style: AppTypography.labelLarge),
    );
  }
}
