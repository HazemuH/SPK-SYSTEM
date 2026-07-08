import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../config/api_config.dart';
import '../../core/exceptions/app_exception.dart';
import '../../core/network/api_client.dart';
import '../models/meta.dart';
import '../models/ranked_toy.dart';
import '../models/toy_detail.dart';

final publicApiServiceProvider = Provider<PublicApiService>((ref) {
  return PublicApiService(ref.watch(apiClientProvider));
});

/// Talks to the public (no-auth) `/public/**` endpoints — the published
/// AHP-SAW results. The only place that knows about HTTP/Dio.
class PublicApiService {
  const PublicApiService(this._dio);

  final Dio _dio;

  /// Top-N recommended toys for a profile (home screen).
  Future<List<RankedToy>> top({String profile = 'balanced', int limit = 5}) {
    return _rankedList(
      () => _dio.get(ApiConfig.top, queryParameters: {'profile': profile, 'limit': limit}),
    );
  }

  /// Full ranked catalog with optional sort/filter/search.
  Future<List<RankedToy>> catalog({
    String profile = 'balanced',
    String? sort,
    String? categoryId,
    bool inStock = false,
    String search = '',
  }) {
    final qp = <String, dynamic>{'profile': profile, 'inStock': inStock};
    if (sort != null && sort.isNotEmpty) qp['sort'] = sort;
    if (categoryId != null && categoryId.isNotEmpty) qp['categoryId'] = categoryId;
    if (search.isNotEmpty) qp['search'] = search;
    return _rankedList(() => _dio.get(ApiConfig.toys, queryParameters: qp));
  }

  /// Detail for one toy: score, ranks, normalized r_ij, strengths/weaknesses.
  Future<ToyDetail> detail(String id) async {
    try {
      final response = await _dio.get('${ApiConfig.toys}/$id');
      return ToyDetail.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw _toAppException(e, 'Gagal memuat detail mainan');
    }
  }

  /// Reference data: categories, criteria, sort options.
  Future<Meta> meta() async {
    try {
      final response = await _dio.get(ApiConfig.meta);
      return Meta.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw _toAppException(e, 'Gagal memuat data');
    }
  }

  Future<List<RankedToy>> _rankedList(Future<Response<dynamic>> Function() call) async {
    try {
      final response = await call();
      final list = response.data as List;
      return list.map((e) => RankedToy.fromJson(e as Map<String, dynamic>)).toList();
    } on DioException catch (e) {
      throw _toAppException(e, 'Gagal memuat mainan');
    }
  }

  AppException _toAppException(DioException e, String fallback) {
    final error = e.error;
    if (error is AppException) return error;
    return ServerException(e.message ?? fallback);
  }
}
