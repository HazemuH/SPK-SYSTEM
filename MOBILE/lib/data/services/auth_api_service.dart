import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../config/api_config.dart';
import '../../core/exceptions/app_exception.dart';
import '../../core/network/api_client.dart';
import '../models/user.dart';

final authApiServiceProvider = Provider<AuthApiService>((ref) {
  return AuthApiService(ref.watch(apiClientProvider));
});

/// Talks to the auth endpoints. Services are the only place that knows about
/// HTTP/Dio. They surface results as plain models or throw [AppException].
class AuthApiService {
  const AuthApiService(this._dio);

  final Dio _dio;

  Future<({User user, String token})> login({
    required String username,
    required String password,
  }) async {
    try {
      final response = await _dio.post(
        ApiConfig.login,
        data: {'username': username, 'password': password},
      );
      final data = response.data as Map<String, dynamic>;
      final user = User.fromJson(data['user'] as Map<String, dynamic>);
      final token = data['token']?.toString() ?? '';
      return (user: user, token: token);
    } on DioException catch (e) {
      throw _toAppException(e, fallback: 'Login failed');
    }
  }

  Future<void> logout() async {
    try {
      await _dio.post(ApiConfig.logout);
    } on DioException {
      // Logout is best-effort; ignore network errors.
    }
  }

  Future<User> getProfile() async {
    try {
      final response = await _dio.get(ApiConfig.profile);
      return User.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw _toAppException(e, fallback: 'Failed to fetch profile');
    }
  }

  /// Unwraps an [AppException] already attached by [ApiClient], or builds a
  /// generic [ServerException] from the raw Dio error.
  AppException _toAppException(DioException e, {required String fallback}) {
    final error = e.error;
    if (error is AppException) return error;
    return ServerException(e.message ?? fallback);
  }
}
