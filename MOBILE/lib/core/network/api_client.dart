import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import '../../config/api_config.dart';
import '../exceptions/app_exception.dart';
import '../storage/storage_keys.dart';

/// Shared secure storage instance.
final secureStorageProvider = Provider<FlutterSecureStorage>(
  (_) => const FlutterSecureStorage(),
);

/// Configured [Dio] instance with auth + error-mapping interceptors.
final apiClientProvider = Provider<Dio>((ref) {
  return ApiClient(secureStorage: ref.watch(secureStorageProvider)).dio;
});

/// Wraps a single configured [Dio] instance.
///
/// Interceptors attach the bearer token on every request and translate HTTP
/// error responses into [AppException] subtypes so the rest of the app never
/// has to inspect status codes.
class ApiClient {
  ApiClient({required FlutterSecureStorage secureStorage})
      : _secureStorage = secureStorage {
    _dio = Dio(
      BaseOptions(
        baseUrl: ApiConfig.baseUrl,
        connectTimeout: ApiConfig.connectTimeout,
        receiveTimeout: ApiConfig.receiveTimeout,
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
      ),
    );

    _dio.interceptors.addAll([
      _authInterceptor(),
      if (kDebugMode) LogInterceptor(requestBody: true, responseBody: true),
    ]);
  }

  late final Dio _dio;
  final FlutterSecureStorage _secureStorage;

  Dio get dio => _dio;

  InterceptorsWrapper _authInterceptor() {
    return InterceptorsWrapper(
      onRequest: (options, handler) async {
        final token = await _secureStorage.read(key: StorageKeys.authToken);
        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        handler.next(options);
      },
      onError: (error, handler) async {
        final statusCode = error.response?.statusCode;

        if (statusCode == 401) {
          await _secureStorage.delete(key: StorageKeys.authToken);
          handler.reject(_wrap(error, const UnauthorizedException()));
          return;
        }

        if (statusCode == 422) {
          final errors = _parseValidationErrors(error.response?.data);
          handler.reject(_wrap(error, ValidationException(errors: errors)));
          return;
        }

        if (statusCode != null && statusCode >= 500) {
          handler.reject(_wrap(error, const ServerException()));
          return;
        }

        if (error.type == DioExceptionType.connectionTimeout ||
            error.type == DioExceptionType.receiveTimeout ||
            error.type == DioExceptionType.connectionError) {
          handler.reject(_wrap(error, const NetworkException()));
          return;
        }

        handler.next(error);
      },
    );
  }

  DioException _wrap(DioException source, AppException exception) {
    return DioException(
      requestOptions: source.requestOptions,
      response: source.response,
      error: exception,
      type: DioExceptionType.badResponse,
    );
  }

  Map<String, List<String>> _parseValidationErrors(dynamic data) {
    if (data is! Map) return {};
    final errors = data['errors'];
    if (errors is! Map) return {};
    return errors.map((key, value) {
      final messages = value is List
          ? value.map((e) => e.toString()).toList()
          : [value.toString()];
      return MapEntry(key.toString(), messages);
    });
  }
}
