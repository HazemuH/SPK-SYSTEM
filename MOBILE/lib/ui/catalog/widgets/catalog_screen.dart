import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../data/models/meta.dart';
import '../../../routing/routes.dart';
import '../../core/themes/colors.dart';
import '../../core/themes/spacing.dart';
import '../../core/themes/typography.dart';
import '../../core/widgets/app_empty_state.dart';
import '../../core/widgets/app_error_view.dart';
import '../../core/widgets/app_loading.dart';
import '../../core/widgets/app_text_field.dart';
import '../../core/widgets/ranked_toy_tile.dart';
import '../../core/widgets/screen_wrapper.dart';
import '../view_model/catalog_providers.dart';

/// Read-only catalog: sort by criterion + filter by category/stock + search.
class CatalogScreen extends ConsumerStatefulWidget {
  const CatalogScreen({super.key});

  @override
  ConsumerState<CatalogScreen> createState() => _CatalogScreenState();
}

class _CatalogScreenState extends ConsumerState<CatalogScreen> {
  CatalogParams _params = const CatalogParams();

  @override
  Widget build(BuildContext context) {
    final meta = ref.watch(metaProvider);
    final catalog = ref.watch(catalogProvider(_params));

    return ScreenWrapper(
      padding: EdgeInsets.zero,
      appBar: AppBar(title: const Text('Jelajah Katalog')),
      child: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(AppSpacing.md, AppSpacing.md, AppSpacing.md, AppSpacing.sm),
            child: AppTextField(
              hint: 'Cari mainan…',
              prefixIcon: Icons.search,
              textInputAction: TextInputAction.search,
              onSubmitted: (v) => setState(() => _params = _params.copyWith(search: v.trim())),
            ),
          ),
          _FilterBar(
            meta: meta.asData?.value,
            params: _params,
            onChanged: (p) => setState(() => _params = p),
          ),
          const Divider(height: 1, color: AppColors.border),
          Expanded(
            child: catalog.when(
              loading: () => const AppLoading(),
              error: (_, _) => AppErrorView(
                message: 'Gagal memuat katalog.',
                onRetry: () => ref.invalidate(catalogProvider(_params)),
              ),
              data: (items) {
                if (items.isEmpty) {
                  return const AppEmptyState(
                    icon: Icons.search_off,
                    title: 'Tidak ditemukan',
                    subtitle: 'Coba ubah pencarian atau filter.',
                  );
                }
                final maxScore = items.first.score;
                return ListView.separated(
                  padding: const EdgeInsets.all(AppSpacing.md),
                  itemCount: items.length,
                  separatorBuilder: (_, _) => const SizedBox(height: AppSpacing.sm),
                  itemBuilder: (_, i) => RankedToyTile(
                    item: items[i],
                    maxScore: maxScore,
                    onTap: () => context.push(Routes.detail(items[i].toy.id)),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}

class _FilterBar extends StatelessWidget {
  const _FilterBar({required this.meta, required this.params, required this.onChanged});

  final Meta? meta;
  final CatalogParams params;
  final ValueChanged<CatalogParams> onChanged;

  @override
  Widget build(BuildContext context) {
    final sortOptions = meta?.sortOptions ?? const <SortOption>[];
    final categories = meta?.categories ?? const <CategoryOption>[];

    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      padding: const EdgeInsets.symmetric(horizontal: AppSpacing.md, vertical: AppSpacing.xs),
      child: Row(
        children: [
          if (sortOptions.isNotEmpty)
            _Dropdown<String?>(
              icon: Icons.sort,
              value: params.sort,
              items: [
                for (final o in sortOptions) DropdownMenuItem(value: o.id, child: Text(o.label)),
              ],
              onChanged: (v) =>
                  onChanged(v == null ? params.copyWith(clearSort: true) : params.copyWith(sort: v)),
            ),
          const SizedBox(width: AppSpacing.sm),
          _Dropdown<String?>(
            icon: Icons.category_outlined,
            value: params.categoryId,
            items: [
              const DropdownMenuItem(value: null, child: Text('Semua Kategori')),
              for (final c in categories) DropdownMenuItem(value: c.id, child: Text(c.name)),
            ],
            onChanged: (v) => onChanged(
              v == null ? params.copyWith(clearCategory: true) : params.copyWith(categoryId: v),
            ),
          ),
          const SizedBox(width: AppSpacing.sm),
          FilterChip(
            label: const Text('Tersedia'),
            selected: params.inStock,
            onSelected: (v) => onChanged(params.copyWith(inStock: v)),
            showCheckmark: true,
            selectedColor: AppColors.success.withValues(alpha: 0.12),
          ),
        ],
      ),
    );
  }
}

class _Dropdown<T> extends StatelessWidget {
  const _Dropdown({
    required this.icon,
    required this.value,
    required this.items,
    required this.onChanged,
  });

  final IconData icon;
  final T value;
  final List<DropdownMenuItem<T>> items;
  final ValueChanged<T?> onChanged;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: AppSpacing.sm),
      decoration: BoxDecoration(
        border: Border.all(color: AppColors.border),
        borderRadius: BorderRadius.circular(999),
        color: AppColors.surface,
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 16, color: AppColors.textSecondary),
          const SizedBox(width: AppSpacing.xs),
          DropdownButtonHideUnderline(
            child: DropdownButton<T>(
              value: value,
              items: items,
              onChanged: onChanged,
              isDense: true,
              style: AppTypography.bodySmall.copyWith(color: AppColors.textPrimary),
            ),
          ),
        ],
      ),
    );
  }
}
