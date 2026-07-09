import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../data/models/weight_profile.dart';
import '../../../routing/routes.dart';
import '../../core/themes/colors.dart';
import '../../core/themes/spacing.dart';
import '../../core/themes/typography.dart';
import '../../core/widgets/app_button.dart';
import '../../core/widgets/app_empty_state.dart';
import '../../core/widgets/app_error_view.dart';
import '../../core/widgets/app_loading.dart';
import '../../core/widgets/ranked_toy_tile.dart';
import '../../core/widgets/screen_wrapper.dart';
import '../view_model/recommendation_providers.dart';

class _Question {
  const _Question(this.id, this.title, this.subtitle, this.options);
  final String id;
  final String title;
  final String subtitle;
  final List<List<String>> options; // [value, label, emoji]
}

/// Static preference filters. The final "paling penting?" step is dynamic —
/// its options are the published AHP weight profiles (see [publishedProfilesProvider]).
const _quiz = <_Question>[
  _Question('usia', 'Usia si kecil?', 'Mainan di luar rentang usia disembunyikan', [
    ['0-2', '0–2 tahun', '👶'],
    ['3-5', '3–5 tahun', '🧒'],
    ['6-8', '6–8 tahun', '🧑'],
    ['9-12', '9–12 tahun', '👦'],
    ['13+', '13+ tahun', '🧑‍🎓'],
  ]),
  _Question('budget', 'Budget?', 'Hanya mainan dalam budget yang tampil', [
    ['100000', '< Rp100rb', '💸'],
    ['300000', 'Rp100–300rb', '💰'],
    ['500000', 'Rp300–500rb', '💳'],
    ['99999999', 'Tanpa batas', '🏆'],
  ]),
  _Question('tujuan', 'Tujuan utama?', 'Memprioritaskan kategori relevan', [
    ['edukatif', 'Belajar & Edukatif', '📚'],
    ['aktif', 'Aktif & Bergerak', '⚽'],
    ['kreatif', 'Kreasi & Seni', '🎨'],
    ['hiburan', 'Hiburan & Fun', '🎮'],
  ]),
];

const _prioritasKey = 'prioritas';

/// Emoji for a weight profile, keyed by the backend's lucide-style icon hint.
/// Unknown/admin-added profiles degrade to a generic target emoji.
String _profileEmoji(String icon) {
  switch (icon) {
    case 'scale':
      return '⚖️';
    case 'lock':
      return '🛡️';
    case 'doc':
      return '🎓';
    case 'tag':
      return '💵';
    case 'shield':
      return '💪';
    default:
      return '🎯';
  }
}

/// Preference quiz → recommendation. Read-only: the server filters/ranks the
/// published AHP-SAW result from the answers.
class RecommendationScreen extends ConsumerStatefulWidget {
  const RecommendationScreen({super.key});

  @override
  ConsumerState<RecommendationScreen> createState() => _RecommendationScreenState();
}

class _RecommendationScreenState extends ConsumerState<RecommendationScreen> {
  int _step = 0;
  final Map<String, String> _answers = {};

  // Static questions + one dynamic profile step at the end.
  int get _totalSteps => _quiz.length + 1;
  bool get _isProfileStep => _step == _quiz.length;
  bool get _isResult => _step >= _totalSteps;

  void _back() {
    if (_step > 0 && !_isResult) {
      setState(() => _step--);
    } else {
      context.pop();
    }
  }

  void _next() {
    setState(() => _step++);
  }

  void _restart() {
    setState(() {
      _step = 0;
      _answers.clear();
    });
  }

  @override
  Widget build(BuildContext context) {
    return ScreenWrapper(
      padding: EdgeInsets.zero,
      appBar: AppBar(
        title: const Text('Rekomendasi untuk Saya'),
        leading: IconButton(icon: const Icon(Icons.arrow_back), onPressed: _back),
      ),
      child: _isResult
          ? _buildResult()
          : _isProfileStep
          ? _buildProfileStep()
          : _buildQuiz(),
    );
  }

  /// Progress header (shared by static questions and the profile step).
  Widget _progressHeader() {
    return Padding(
      padding: const EdgeInsets.all(AppSpacing.md),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Langkah ${_step + 1}/$_totalSteps', style: AppTypography.labelSmall),
          const SizedBox(height: AppSpacing.xs),
          ClipRRect(
            borderRadius: BorderRadius.circular(4),
            child: LinearProgressIndicator(
              value: (_step + 1) / _totalSteps,
              minHeight: 6,
              backgroundColor: AppColors.surfaceVariant,
              valueColor: const AlwaysStoppedAnimation(AppColors.primary),
            ),
          ),
        ],
      ),
    );
  }

  Widget _nextButton({required bool enabled}) {
    return Padding(
      padding: const EdgeInsets.all(AppSpacing.md),
      child: AppButton(
        label: _step < _totalSteps - 1 ? 'Lanjut' : 'Lihat Rekomendasi',
        isFullWidth: true,
        trailingIcon: Icons.arrow_forward,
        onPressed: enabled ? _next : null,
      ),
    );
  }

  Widget _buildQuiz() {
    final q = _quiz[_step];
    final selected = _answers[q.id];

    return Column(
      children: [
        _progressHeader(),
        Expanded(
          child: ListView(
            padding: const EdgeInsets.fromLTRB(AppSpacing.md, 0, AppSpacing.md, AppSpacing.md),
            children: [
              Text(q.title, style: AppTypography.headlineSmall),
              const SizedBox(height: AppSpacing.xs),
              Text(
                q.subtitle,
                style: AppTypography.bodyMedium.copyWith(color: AppColors.textSecondary),
              ),
              const SizedBox(height: AppSpacing.md),
              for (final opt in q.options)
                Padding(
                  padding: const EdgeInsets.only(bottom: AppSpacing.sm),
                  child: _OptionCard(
                    emoji: opt[2],
                    label: opt[1],
                    selected: selected == opt[0],
                    onTap: () => setState(() => _answers[q.id] = opt[0]),
                  ),
                ),
            ],
          ),
        ),
        _nextButton(enabled: selected != null),
      ],
    );
  }

  /// Dynamic final step: the user picks an AHP weight profile (managed on the web).
  /// The chosen profile code drives the AHP-SAW ranking.
  Widget _buildProfileStep() {
    final selected = _answers[_prioritasKey];
    final profiles = ref.watch(publishedProfilesProvider);

    return Column(
      children: [
        _progressHeader(),
        Expanded(
          child: profiles.when(
            loading: () => const AppLoading(),
            error: (_, _) => AppErrorView(
              message: 'Gagal memuat profil bobot.',
              onRetry: () => ref.invalidate(publishedProfilesProvider),
            ),
            data: (list) {
              if (list.isEmpty) {
                return const AppEmptyState(
                  icon: Icons.tune,
                  title: 'Belum ada profil bobot',
                  subtitle: 'Admin belum menyiapkan skenario penilaian.',
                );
              }
              return ListView(
                padding: const EdgeInsets.fromLTRB(
                  AppSpacing.md,
                  0,
                  AppSpacing.md,
                  AppSpacing.md,
                ),
                children: [
                  Text('Paling penting?', style: AppTypography.headlineSmall),
                  const SizedBox(height: AppSpacing.xs),
                  Text(
                    'Pilih skenario penilaian. Bobotnya berasal dari perbandingan AHP '
                    'yang sudah divalidasi admin — pilihanmu menentukan ranking.',
                    style: AppTypography.bodyMedium.copyWith(color: AppColors.textSecondary),
                  ),
                  const SizedBox(height: AppSpacing.md),
                  for (final WeightProfile p in list)
                    Padding(
                      padding: const EdgeInsets.only(bottom: AppSpacing.sm),
                      child: _OptionCard(
                        emoji: _profileEmoji(p.icon),
                        label: p.name,
                        selected: selected == p.id,
                        onTap: () => setState(() => _answers[_prioritasKey] = p.id),
                      ),
                    ),
                ],
              );
            },
          ),
        ),
        _nextButton(enabled: selected != null),
      ],
    );
  }

  Widget _buildResult() {
    final req = RecommendRequest(
      usia: _answers['usia']!,
      budget: _answers['budget']!,
      tujuan: _answers['tujuan']!,
      prioritas: _answers[_prioritasKey]!,
    );
    final state = ref.watch(recommendationProvider(req));

    return state.when(
      loading: () => const AppLoading(),
      error: (_, _) => AppErrorView(
        message: 'Gagal menyusun rekomendasi.',
        onRetry: () => ref.invalidate(recommendationProvider(req)),
      ),
      data: (rec) {
        final all = [...rec.primary, ...rec.others];
        if (all.isEmpty) {
          return Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const AppEmptyState(
                icon: Icons.search_off,
                title: 'Tidak ada mainan cocok',
                subtitle: 'Coba longgarkan preferensimu.',
              ),
              Padding(
                padding: const EdgeInsets.all(AppSpacing.md),
                child: AppButton(label: 'Ubah Preferensi', onPressed: _restart),
              ),
            ],
          );
        }
        final maxScore = all.first.score;
        return ListView(
          padding: const EdgeInsets.all(AppSpacing.md),
          children: [
            Container(
              padding: const EdgeInsets.all(AppSpacing.md),
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(14),
                gradient: const LinearGradient(
                  colors: [AppColors.accent, Color(0xFFD97706)],
                ),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    '🏆 Paling Cocok · Profil ${rec.profileName}',
                    style: AppTypography.labelSmall.copyWith(color: AppColors.white),
                  ),
                  const SizedBox(height: AppSpacing.xs),
                  Text(
                    all.first.toy.name,
                    style: AppTypography.headlineSmall.copyWith(color: AppColors.white),
                  ),
                  const SizedBox(height: AppSpacing.sm),
                  AppButton(
                    label: 'Lihat Detail',
                    variant: AppButtonVariant.secondary,
                    onPressed: () => context.push(Routes.detail(all.first.toy.id)),
                  ),
                ],
              ),
            ),
            const SizedBox(height: AppSpacing.md),
            if (rec.primary.isNotEmpty) ...[
              Text(
                'Sesuai pilihanmu · ${rec.primary.length} mainan',
                style: AppTypography.labelMedium,
              ),
              const SizedBox(height: AppSpacing.sm),
              for (final item in rec.primary)
                Padding(
                  padding: const EdgeInsets.only(bottom: AppSpacing.sm),
                  child: RankedToyTile(
                    item: item,
                    maxScore: maxScore,
                    onTap: () => context.push(Routes.detail(item.toy.id)),
                  ),
                ),
            ],
            if (rec.others.isNotEmpty) ...[
              const SizedBox(height: AppSpacing.sm),
              Text('Rekomendasi lain', style: AppTypography.labelMedium),
              const SizedBox(height: AppSpacing.sm),
              for (final item in rec.others.take(8))
                Padding(
                  padding: const EdgeInsets.only(bottom: AppSpacing.sm),
                  child: RankedToyTile(
                    item: item,
                    maxScore: maxScore,
                    onTap: () => context.push(Routes.detail(item.toy.id)),
                  ),
                ),
            ],
            const SizedBox(height: AppSpacing.sm),
            AppButton(
              label: 'Ubah Preferensi',
              variant: AppButtonVariant.outline,
              isFullWidth: true,
              onPressed: _restart,
            ),
          ],
        );
      },
    );
  }
}

class _OptionCard extends StatelessWidget {
  const _OptionCard({
    required this.emoji,
    required this.label,
    required this.selected,
    required this.onTap,
  });

  final String emoji;
  final String label;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Container(
        padding: const EdgeInsets.all(AppSpacing.md),
        decoration: BoxDecoration(
          color: selected ? AppColors.primary.withValues(alpha: 0.08) : AppColors.surface,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: selected ? AppColors.primary : AppColors.border,
            width: selected ? 2 : 1,
          ),
        ),
        child: Row(
          children: [
            Text(emoji, style: const TextStyle(fontSize: 22)),
            const SizedBox(width: AppSpacing.md),
            Expanded(
              child: Text(
                label,
                style: AppTypography.labelLarge.copyWith(
                  color: selected ? AppColors.primary : AppColors.textPrimary,
                ),
              ),
            ),
            Icon(
              selected ? Icons.radio_button_checked : Icons.radio_button_unchecked,
              color: selected ? AppColors.primary : AppColors.textDisabled,
              size: 20,
            ),
          ],
        ),
      ),
    );
  }
}
