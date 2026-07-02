import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import '../../core/exceptions/app_exception.dart';
import '../../core/network/api_client.dart';
import '../../core/storage/storage_keys.dart';
import '../models/user.dart';
import '../services/auth_api_service.dart';

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  return AuthRepository(
    api: ref.watch(authApiServiceProvider),
    secureStorage: ref.watch(secureStorageProvider),
  );
});

/// Owns the auth use cases: it coordinates the remote [AuthApiService] with the
/// local token store. In this lightweight setup there is a single concrete
/// repository (no abstract interface) — extract an interface only when you need
/// multiple implementations or to fake the whole repository in tests.
class AuthRepository {
  const AuthRepository({
    required AuthApiService api,
    required FlutterSecureStorage secureStorage,
  })  : _api = api,
        _secureStorage = secureStorage;

  final AuthApiService _api;
  final FlutterSecureStorage _secureStorage;

  /// Logs in and persists the token. Throws [AppException] on failure.
  Future<User> login({required String username, required String password}) async {
    final result = await _api.login(username: username, password: password);
    await _secureStorage.write(key: StorageKeys.authToken, value: result.token);
    return result.user;
  }

  Future<void> logout() async {
    await _api.logout();
    await _secureStorage.delete(key: StorageKeys.authToken);
  }

  /// Returns the current user if a valid token exists, otherwise `null`.
  Future<User?> currentUser() async {
    final token = await _secureStorage.read(key: StorageKeys.authToken);
    if (token == null) return null;
    try {
      return await _api.getProfile();
    } on AppException {
      return null;
    }
  }

  Future<String?> readToken() => _secureStorage.read(key: StorageKeys.authToken);

  Future<void> saveToken(String token) =>
      _secureStorage.write(key: StorageKeys.authToken, value: token);
}
