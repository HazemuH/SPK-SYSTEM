import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/utils/formatter.dart';
import '../../../data/models/compare_result.dart';
import '../../../routing/routes.dart';
import '../../catalog/view_model/catalog_providers.dart';
import '../../core/themes/colors.dart';
import '../../core/themes/spacing.dart';
import '../../core/themes/typography.dart';
import '../../core/widgets/app_button.dart';
import '../../core/widgets/app_card.dart';
import '../../core/widgets/app_error_view.dart';
import '../../core/widgets/app_loading.dart';
import '../view_model/compare_providers.dart';

/// Read-only comparison: pick 2–4 toys, then a criteria table with a live
/// profile switcher (re-ranks server-side per profile).
class CompareScreen extends ConsumerStatefulWidget {
  const CompareScreen({super.key});

  @override
  ConsumerState<CompareScreen> createState() => _CompareScreenState();
}

class _CompareScreenState extends ConsumerState<CompareScreen> {
  final List<String> _picked = [];
  String _profile = 'balanced';
  bool _showTable = false;

  void _toggle(String id) {
    setState(() {
      if (_picked.contains(id)) {
        _picked.remove(id);
      } else if (_picked.length < 4) {
        _picked.add(id);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: Text(_showTable ? 'Bandingkan (${_picked.length})' : 'Bandingkan Mainan'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => _showTable ? setState(() => _showTable = false) : context.pop(),
        ),
      ),
      body: _showTable ? _buildTable() : _buildSelect(),
    );
  }

  Widget _buildSelect() {
    final catalog = ref.watch(catalogProvider(const CatalogParams()));
    return Column(
      children: [
        Expanded(
          child: catalog.when(
            loading: () => const AppLoading(),
            error: (_, _) => AppErrorView(
              message: 'Gagal memuat daftar mainan.',
              onRetry: () => ref.invalidate(catalogProvider(const CatalogParams())),
            ),
            data: (items) => ListView.separated(
              padding: const EdgeInsets.all(AppSpacing.md),
              itemCount: items.length,
              separatorBuilder: (_, _) => const SizedBox(height: AppSpacing.sm),
              itemBuilder: (_, i) {
                final toy = items[i].toy;
                final on = _picked.contains(toy.id);
                final disabled = !on && _picked.length >= 4;
                return Opacity(
                  opacity: disabled ? 0.5 : 1,
                  child: AppCard(
                    onTap: disabled ? null : () => _toggle(toy.id),
                    color: on ? AppColors.accent.withValues(alpha: 0.08) : null,
                    child: Row(
                      children: [
                        Icon(
                          on ? Icons.check_box : Icons.check_box_outline_blank,
                          color: on ? AppColors.accent : AppColors.textDisabled,
                        ),
                        const SizedBox(width: AppSpacing.md),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(toy.name, style: AppTypography.labelLarge, maxLines: 1, overflow: TextOverflow.ellipsis),
                              const SizedBox(height: 2),
                              Text('${toy.category} · ${Formatter.formatCurrency(toy.price)}', style: AppTypography.bodySmall),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
          ),
        ),
        SafeArea(
          top: false,
          child: Padding(
            padding: const EdgeInsets.all(AppSpacing.md),
            child: AppButton(
              label: _picked.length < 2 ? 'Pilih ${2 - _picked.length} lagi' : 'Bandingkan (${_picked.length})',
              isFullWidth: true,
              leadingIcon: Icons.balance,
              onPressed: _picked.length >= 2 ? () => setState(() => _showTable = true) : null,
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildTable() {
    final compare = ref.watch(compareProvider(CompareRequest(toyIds: _picked, profile: _profile)));
    final profiles = ref.watch(compareProfilesProvider);

    return Column(
      children: [
        // Live profile switcher.
        SizedBox(
          height: 52,
          child: profiles.when(
            loading: () => const SizedBox.shrink(),
            error: (_, _) => const SizedBox.shrink(),
            data: (list) => ListView(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: AppSpacing.md, vertical: AppSpacing.sm),
              children: [
                for (final p in list)
                  Padding(
                    padding: const EdgeInsets.only(right: AppSpacing.xs),
                    child: ChoiceChip(
                      label: Text(p.shortName),
                      selected: _profile == p.id,
                      onSelected: (_) => setState(() => _profile = p.id),
                      selectedColor: AppColors.secondary.withValues(alpha: 0.15),
                    ),
                  ),
              ],
            ),
          ),
        ),
        const Divider(height: 1, color: AppColors.border),
        Expanded(
          child: compare.when(
            loading: () => const AppLoading(),
            error: (_, _) => AppErrorView(
              message: 'Gagal membandingkan.',
              onRetry: () => ref.invalidate(compareProvider(CompareRequest(toyIds: _picked, profile: _profile))),
            ),
            data: (result) => _CompareTable(result: result),
          ),
        ),
      ],
    );
  }
}

class _CompareTable extends StatelessWidget {
  const _CompareTable({required this.result});

  final CompareResult result;

  @override
  Widget build(BuildContext context) {
    final winner = result.totals.where((t) => t.winner).firstOrNull;
    final winnerToy = winner == null
        ? null
        : result.toys.where((t) => t.id == winner.toyId).firstOrNull;

    return ListView(
      children: [
        SingleChildScrollView(
          scrollDirection: Axis.horizontal,
          child: DataTable(
            headingRowColor: WidgetStateProperty.all(AppColors.surfaceVariant),
            columns: [
              const DataColumn(label: Text('Kriteria')),
              for (final toy in result.toys)
                DataColumn(
                  label: SizedBox(
                    width: 72,
                    child: Text(
                      toy.name,
                      style: AppTypography.labelSmall,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ),
            ],
            rows: [
              for (final row in result.rows)
                DataRow(
                  cells: [
                    DataCell(Text(row.criterion.name, style: AppTypography.bodySmall)),
                    for (final cell in row.cells)
                      DataCell(
                        Text(
                          '${cell.best ? '● ' : ''}${cell.value.toStringAsFixed(2)}',
                          style: AppTypography.bodySmall.copyWith(
                            color: cell.best ? AppColors.success : AppColors.textSecondary,
                            fontWeight: cell.best ? FontWeight.w700 : FontWeight.w400,
                          ),
                        ),
                      ),
                  ],
                ),
              DataRow(
                color: WidgetStateProperty.all(AppColors.primary.withValues(alpha: 0.06)),
                cells: [
                  DataCell(Text('SKOR SAW', style: AppTypography.labelMedium.copyWith(color: AppColors.primary))),
                  for (final total in result.totals)
                    DataCell(
                      Text(
                        '${total.winner ? '🏆 ' : ''}${total.score.toStringAsFixed(3)}',
                        style: AppTypography.labelMedium.copyWith(
                          color: total.winner ? AppColors.accent : AppColors.primary,
                        ),
                      ),
                    ),
                ],
              ),
            ],
          ),
        ),
        if (winnerToy != null)
          Padding(
            padding: const EdgeInsets.all(AppSpacing.md),
            child: AppCard(
              color: AppColors.accent.withValues(alpha: 0.08),
              onTap: () => context.push(Routes.detail(winnerToy.id)),
              child: Row(
                children: [
                  const Text('🏆', style: TextStyle(fontSize: 22)),
                  const SizedBox(width: AppSpacing.md),
                  Expanded(
                    child: Text('${winnerToy.name} unggul pada profil ini',
                        style: AppTypography.labelLarge, maxLines: 2, overflow: TextOverflow.ellipsis),
                  ),
                  const Icon(Icons.chevron_right, color: AppColors.textDisabled),
                ],
              ),
            ),
          ),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: AppSpacing.md, vertical: AppSpacing.sm),
          child: Text(
            'Ganti profil di atas → skor & pemenang berubah (bobot AHP, sintesis SAW).',
            style: AppTypography.bodySmall,
            textAlign: TextAlign.center,
          ),
        ),
      ],
    );
  }
}
