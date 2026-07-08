import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:spk_mainan/data/models/meta.dart';
import 'package:spk_mainan/data/models/ranked_toy.dart';
import 'package:spk_mainan/data/models/toy.dart';
import 'package:spk_mainan/data/models/toy_detail.dart';
import 'package:spk_mainan/data/repositories/catalog_repository.dart';
import 'package:spk_mainan/ui/home/widgets/home_screen.dart';

/// Fakes the repository so the hub renders deterministic data without HTTP.
class _FakeCatalogRepository implements CatalogRepository {
  @override
  Future<List<RankedToy>> top({String profile = 'balanced', int limit = 5}) async => const [
        RankedToy(
          rank: 1,
          score: 0.92,
          toy: Toy(
            id: '1',
            name: 'Lego Classic',
            categoryId: 'konstruksi',
            category: 'Konstruksi',
            price: 250000,
            ageMin: 6,
            ageMax: 12,
            stock: 5,
            tags: [],
          ),
        ),
      ];

  @override
  Future<List<RankedToy>> catalog({
    String profile = 'balanced',
    String? sort,
    String? categoryId,
    bool inStock = false,
    String search = '',
  }) =>
      throw UnimplementedError();

  @override
  Future<ToyDetail> detail(String id) => throw UnimplementedError();

  @override
  Future<Meta> meta() => throw UnimplementedError();
}

void main() {
  testWidgets('HomeScreen shows the top recommendations', (tester) async {
    await tester.pumpWidget(
      ProviderScope(
        overrides: [catalogRepositoryProvider.overrideWithValue(_FakeCatalogRepository())],
        child: const MaterialApp(home: HomeScreen()),
      ),
    );

    await tester.pumpAndSettle();

    expect(find.text('ToyAdvisor'), findsOneWidget);
    expect(find.text('Top Rekomendasi'), findsOneWidget);
    expect(find.text('Lego Classic'), findsOneWidget);
  });
}
