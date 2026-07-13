import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/utils/formatter.dart';
import '../../../data/models/criterion.dart';
import '../../../data/models/toy_detail.dart';
import '../../../routing/routes.dart';
import '../../catalog/view_model/catalog_providers.dart';
import '../../core/themes/colors.dart';
import '../../core/themes/spacing.dart';
import '../../core/themes/typography.dart';
import '../../core/widgets/app_card.dart';
import '../../core/widgets/app_empty_state.dart';
import '../../core/widgets/app_error_view.dart';
import '../../core/widgets/app_loading.dart';
import '../../core/widgets/screen_wrapper.dart';
import '../view_model/detail_providers.dart';

/// Read-only toy detail: SAW score, ranks, "why", and normalized r_ij bars.
class DetailScreen extends ConsumerWidget {
  const DetailScreen({super.key, required this.toyId});

  final String toyId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(toyDetailProvider(toyId));
    final criteria = ref.watch(metaProvider).asData?.value.criteria ?? const <Criterion>[];

    return ScreenWrapper(
      padding: EdgeInsets.zero,
      appBar: AppBar(title: const Text('Detail Mainan')),
      child: state.when(
        loading: () => const AppLoading(),
        error: (_, _) => AppErrorView(
          message: 'Gagal memuat detail.',
          onRetry: () => ref.invalidate(toyDetailProvider(toyId)),
        ),
        data: (detail) => detail.normalized.isEmpty
            ? const AppEmptyState(
                icon: Icons.inventory_2_outlined,
                title: 'Detail belum tersedia',
                subtitle: 'Hasil AHP belum dipublikasikan admin.',
              )
            : _DetailBody(detail: detail, criteria: criteria),
      ),
    );
  }
}

class _DetailBody extends StatelessWidget {
  const _DetailBody({required this.detail, required this.criteria});

  final ToyDetail detail;
  final List<Criterion> criteria;

  @override
  Widget build(BuildContext context) {
    final toy = detail.toy;
    // Fall back to normalized keys if meta criteria haven't loaded yet.
    final rows = criteria.isNotEmpty
        ? criteria.map((c) => (label: c.name, code: c.code, isCost: c.isCost)).toList()
        : detail.normalized.keys.map((k) => (label: k, code: k, isCost: false)).toList();

    return ListView(
      padding: const EdgeInsets.all(AppSpacing.md),
      children: [
        Text(toy.name, style: AppTypography.headlineSmall),
        const SizedBox(height: AppSpacing.xs),
        Wrap(
          spacing: AppSpacing.sm,
          runSpacing: AppSpacing.xs,
          crossAxisAlignment: WrapCrossAlignment.center,
          children: [
            _Chip(toy.category),
            Text('${toy.ageMin}–${toy.ageMax} th', style: AppTypography.bodySmall),
            Text(Formatter.formatCurrency(toy.price), style: AppTypography.labelMedium),
            Text(
              toy.inStock ? 'Tersedia' : 'Habis',
              style: AppTypography.bodySmall.copyWith(
                color: toy.inStock ? AppColors.success : AppColors.error,
              ),
            ),
          ],
        ),
        const SizedBox(height: AppSpacing.md),
        Row(
          children: [
            _StatTile(label: 'Skor SAW', value: detail.ranked.score.toStringAsFixed(3), color: AppColors.primary),
            const SizedBox(width: AppSpacing.sm),
            _StatTile(label: 'Rank Global', value: '#${detail.globalRank}', color: AppColors.accent),
            const SizedBox(width: AppSpacing.sm),
            _StatTile(
              label: 'Di Kategori',
              value: '#${detail.categoryRank}/${detail.categoryTotal}',
              color: AppColors.success,
            ),
          ],
        ),
        const SizedBox(height: AppSpacing.md),
        AppCard(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  const Text('💡', style: TextStyle(fontSize: 16)),
                  const SizedBox(width: AppSpacing.xs),
                  Text('Kenapa direkomendasikan?', style: AppTypography.labelLarge),
                ],
              ),
              const SizedBox(height: AppSpacing.sm),
              _Reason(
                icon: Icons.check_circle_outline,
                color: AppColors.success,
                label: 'Unggul',
                value: detail.strengths.map((c) => c.name).join(', '),
              ),
              const SizedBox(height: AppSpacing.xs),
              _Reason(
                icon: Icons.info_outline,
                color: AppColors.warning,
                label: 'Lemah',
                value: detail.weaknesses.map((c) => c.name).join(', '),
              ),
            ],
          ),
        ),
        const SizedBox(height: AppSpacing.md),
        AppCard(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Skor Ternormalisasi (rᵢⱼ)', style: AppTypography.labelLarge),
              const SizedBox(height: 2),
              Text(
                'Skor SAW = Σ (bobot kriteria × nilai di bawah)',
                style: AppTypography.bodySmall,
              ),
              const SizedBox(height: AppSpacing.md),
              for (final r in rows) ...[
                _NormBar(
                  label: r.label,
                  value: detail.normalized[r.code] ?? 0,
                  isCost: r.isCost,
                ),
                const SizedBox(height: AppSpacing.sm),
              ],
            ],
          ),
        ),
        if (!toy.inStock && detail.nextBest != null) ...[
          const SizedBox(height: AppSpacing.md),
          _NextBestCard(detail: detail),
        ],
      ],
    );
  }
}

class _StatTile extends StatelessWidget {
  const _StatTile({required this.label, required this.value, required this.color});

  final String label;
  final String value;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: AppSpacing.md, horizontal: AppSpacing.sm),
        decoration: BoxDecoration(
          color: color.withValues(alpha: 0.08),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: color.withValues(alpha: 0.2)),
        ),
        child: Column(
          children: [
            Text(label, style: AppTypography.bodySmall, textAlign: TextAlign.center),
            const SizedBox(height: AppSpacing.xs),
            Text(value, style: AppTypography.headlineSmall.copyWith(color: color)),
          ],
        ),
      ),
    );
  }
}

class _Reason extends StatelessWidget {
  const _Reason({required this.icon, required this.color, required this.label, required this.value});

  final IconData icon;
  final Color color;
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Icon(icon, size: 16, color: color),
        const SizedBox(width: AppSpacing.sm),
        Expanded(
          child: RichText(
            text: TextSpan(
              style: AppTypography.bodyMedium.copyWith(color: AppColors.textSecondary),
              children: [
                TextSpan(text: '$label: ', style: TextStyle(color: AppColors.textPrimary, fontWeight: FontWeight.w700)),
                TextSpan(text: value),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

class _NormBar extends StatelessWidget {
  const _NormBar({required this.label, required this.value, required this.isCost});

  final String label;
  final double value;
  final bool isCost;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        SizedBox(
          width: 110,
          child: Text(label, style: AppTypography.bodySmall, maxLines: 1, overflow: TextOverflow.ellipsis),
        ),
        Expanded(
          child: ClipRRect(
            borderRadius: BorderRadius.circular(4),
            child: LinearProgressIndicator(
              value: value.clamp(0.0, 1.0),
              minHeight: 6,
              backgroundColor: AppColors.surfaceVariant,
              valueColor: AlwaysStoppedAnimation(isCost ? AppColors.secondary : AppColors.primary),
            ),
          ),
        ),
        const SizedBox(width: AppSpacing.sm),
        SizedBox(
          width: 34,
          child: Text(
            value.toStringAsFixed(2),
            style: AppTypography.bodySmall.copyWith(color: AppColors.primary),
            textAlign: TextAlign.right,
          ),
        ),
      ],
    );
  }
}

class _NextBestCard extends ConsumerWidget {
  const _NextBestCard({required this.detail});

  final ToyDetail detail;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final next = detail.nextBest!;
    return AppCard(
      color: AppColors.accent.withValues(alpha: 0.08),
      onTap: () => context.push(Routes.detail(next.id)),
      child: Row(
        children: [
          const Icon(Icons.inventory_2_outlined, color: AppColors.warning),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Stok habis — alternatif terbaik:', style: AppTypography.bodySmall),
                const SizedBox(height: 2),
                Text(next.name, style: AppTypography.labelLarge, maxLines: 1, overflow: TextOverflow.ellipsis),
              ],
            ),
          ),
          const Icon(Icons.chevron_right, color: AppColors.textDisabled),
        ],
      ),
    );
  }
}

class _Chip extends StatelessWidget {
  const _Chip(this.label);

  final String label;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: AppSpacing.sm, vertical: 2),
      decoration: BoxDecoration(
        color: AppColors.primary.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        label,
        style: AppTypography.bodySmall.copyWith(color: AppColors.primary, fontWeight: FontWeight.w600),
      ),
    );
  }
}
