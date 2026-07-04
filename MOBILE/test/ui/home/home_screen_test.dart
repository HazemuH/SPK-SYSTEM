import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:spk_mainan/data/models/toy.dart';
import 'package:spk_mainan/data/repositories/toy_repository.dart';
import 'package:spk_mainan/ui/home/widgets/home_screen.dart';

/// Fakes the repository so the screen renders deterministic data without HTTP.
class _FakeToyRepository implements ToyRepository {
  @override
  Future<List<Toy>> getRecommendations() async => const [
        Toy(
          id: '1',
          name: 'Lego Classic',
          category: 'Konstruksi',
          price: 250000,
          score: 0.92,
        ),
      ];
}

void main() {
  testWidgets('HomeScreen shows the recommended toys', (tester) async {
    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          toyRepositoryProvider.overrideWithValue(_FakeToyRepository()),
        ],
        child: const MaterialApp(home: HomeScreen()),
      ),
    );

    // Resolve the async load.
    await tester.pumpAndSettle();

    expect(find.text('Rekomendasi Mainan'), findsOneWidget);
    expect(find.text('Lego Classic'), findsOneWidget);
    expect(find.text('Konstruksi'), findsOneWidget);
  });
}
