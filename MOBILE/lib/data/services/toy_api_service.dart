// The real API call below is intentionally kept but currently unreachable
// (mock returns first). Remove the mock block to enable it.
// ignore_for_file: dead_code
import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../config/api_config.dart';
import '../../core/exceptions/app_exception.dart';
import '../../core/network/api_client.dart';
import '../models/toy.dart';

final toyApiServiceProvider = Provider<ToyApiService>((ref) {
  return ToyApiService(ref.watch(apiClientProvider));
});

/// Talks to the public (no-auth) toy endpoints. Services are the only place
/// that knows about HTTP/Dio; they return plain models or throw [AppException].
class ToyApiService {
  const ToyApiService(this._dio);

  final Dio _dio;

  /// Returns the recommended toys, highest SPK score first.
  Future<List<Toy>> fetchRecommendations() async {
    // ---------------------------------------------------------------------
    // MOCK DATA — the backend `/toys/recommendations` endpoint does not exist
    // yet. Remove this block to use the real (public) API below.
    await Future<void>.delayed(const Duration(milliseconds: 500));
    return _mockToys;
    // ---------------------------------------------------------------------

    try {
      final response = await _dio.get(ApiConfig.recommendations);
      final list = (response.data as Map<String, dynamic>)['content'] as List;
      return list
          .map((e) => Toy.fromJson(e as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      final error = e.error;
      if (error is AppException) throw error;
      throw ServerException(e.message ?? 'Failed to load recommendations');
    }
  }
}

/// Placeholder recommendations, already ordered by descending score.
const List<Toy> _mockToys = [
  Toy(id: '1', name: 'Lego Classic', category: 'Konstruksi', price: 250000, score: 0.92),
  Toy(id: '2', name: 'Puzzle Kayu Edukasi', category: 'Edukasi', price: 60000, score: 0.87),
  Toy(id: '3', name: 'Rubik\'s Cube', category: 'Puzzle', price: 45000, score: 0.81),
  Toy(id: '4', name: 'Boneka Beruang', category: 'Boneka', price: 120000, score: 0.74),
  Toy(id: '5', name: 'Mobil Remote', category: 'Elektronik', price: 320000, score: 0.68),
];
