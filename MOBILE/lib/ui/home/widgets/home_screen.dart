import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../routing/routes.dart';
import '../../core/themes/colors.dart';
import '../../core/themes/spacing.dart';
import '../../core/themes/typography.dart';
import '../../core/widgets/app_card.dart';
import '../../core/widgets/app_empty_state.dart';
import '../../core/widgets/app_error_view.dart';
import '../../core/widgets/app_loading.dart';
import '../../core/widgets/ranked_toy_tile.dart';
import '../view_model/home_providers.dart';

/// The app's entry hub. Read-only, no login: three modes + a Top-5 list of the
/// published AHP-SAW recommendations.
class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      backgroundColor: AppColors.background,
      body: Column(
        children: [
          const _Header(),
          Expanded(
            child: ListView(
              padding: const EdgeInsets.all(AppSpacing.md),
              children: [
                _ModeCard(
                  icon: Icons.gps_fixed,
                  title: 'Rekomendasi untuk Saya',
                  subtitle: 'Jawab preferensi → mainan terbaik',
                  onTap: () => context.push(Routes.recommendation),
                ),
                const SizedBox(height: AppSpacing.sm),
                _ModeCard(
                  icon: Icons.explore_outlined,
                  title: 'Jelajah Katalog',
                  subtitle: 'Telusuri semua mainan ter-ranking',
                  onTap: () => context.push(Routes.catalog),
                ),
                const SizedBox(height: AppSpacing.sm),
                _ModeCard(
                  icon: Icons.balance,
                  title: 'Bandingkan Mainan',
                  subtitle: 'Adu 2–4 mainan berdampingan',
                  onTap: () => context.push(Routes.compare),
                ),
                const SizedBox(height: AppSpacing.lg),
                const _TopSection(),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _Header extends StatelessWidget {
  const _Header();

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [Color(0xFF312E81), AppColors.primaryDark, AppColors.primary],
        ),
      ),
      child: SafeArea(
        bottom: false,
        child: Padding(
          padding: const EdgeInsets.fromLTRB(AppSpacing.lg, AppSpacing.md, AppSpacing.lg, AppSpacing.lg),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Container(
                    width: 38,
                    height: 38,
                    decoration: BoxDecoration(
                      color: AppColors.white.withValues(alpha: 0.2),
                      borderRadius: BorderRadius.circular(11),
                    ),
                    child: const Icon(Icons.grid_view_rounded, color: AppColors.white, size: 20),
                  ),
                  const SizedBox(width: AppSpacing.sm),
                  Text(
                    'ToyAdvisor',
                    style: AppTypography.headlineSmall.copyWith(color: AppColors.white),
                  ),
                ],
              ),
              const SizedBox(height: AppSpacing.md),
              Text(
                'Temukan & bandingkan\nmainan terbaik 🧸',
                style: AppTypography.headlineMedium.copyWith(color: AppColors.white, height: 1.2),
              ),
              const SizedBox(height: AppSpacing.xs),
              Text(
                'Didukung metode AHP-SAW — pilih cara kamu di bawah.',
                style: AppTypography.bodyMedium.copyWith(color: AppColors.white.withValues(alpha: 0.8)),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _ModeCard extends StatelessWidget {
  const _ModeCard({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.onTap,
  });

  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return AppCard(
      onTap: onTap,
      child: Row(
        children: [
          Container(
            width: 46,
            height: 46,
            decoration: BoxDecoration(
              color: AppColors.primary.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(13),
            ),
            child: Icon(icon, color: AppColors.primary, size: 22),
          ),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: AppTypography.labelLarge),
                const SizedBox(height: 2),
                Text(subtitle, style: AppTypography.bodySmall),
              ],
            ),
          ),
          const Icon(Icons.chevron_right, color: AppColors.textDisabled),
        ],
      ),
    );
  }
}

class _TopSection extends ConsumerWidget {
  const _TopSection();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(topToysProvider);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.only(bottom: AppSpacing.sm, left: AppSpacing.xs),
          child: Row(
            children: [
              const Text('⭐', style: TextStyle(fontSize: 16)),
              const SizedBox(width: AppSpacing.xs),
              Text('Top Rekomendasi', style: AppTypography.labelLarge),
            ],
          ),
        ),
        state.when(
          loading: () => const Padding(
            padding: EdgeInsets.symmetric(vertical: AppSpacing.xl),
            child: AppLoading(),
          ),
          error: (_, _) => AppErrorView(
            message: 'Gagal memuat rekomendasi.',
            onRetry: () => ref.invalidate(topToysProvider),
          ),
          data: (items) {
            if (items.isEmpty) {
              return const AppEmptyState(
                icon: Icons.toys_outlined,
                title: 'Belum ada rekomendasi',
                subtitle: 'Hasil AHP belum dipublikasikan admin.',
              );
            }
            final maxScore = items.first.score;
            return Column(
              children: [
                for (final item in items)
                  Padding(
                    padding: const EdgeInsets.only(bottom: AppSpacing.sm),
                    child: RankedToyTile(
                      item: item,
                      maxScore: maxScore,
                      onTap: () => context.push(Routes.detail(item.toy.id)),
                    ),
                  ),
              ],
            );
          },
        ),
      ],
    );
  }
}
