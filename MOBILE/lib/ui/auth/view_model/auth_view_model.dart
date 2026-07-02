import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/exceptions/app_exception.dart';
import '../../../data/models/user.dart';
import '../../../data/repositories/auth_repository.dart';

/// Immutable auth session state observed by the router and auth screens.
class AuthState {
  const AuthState({
    this.user,
    this.token,
    this.isLoading = false,
    this.error,
    this.isAuthenticated = false,
    this.isInitialized = false,
  });

  final User? user;
  final String? token;
  final bool isLoading;
  final String? error;
  final bool isAuthenticated;

  /// Whether the initial session restore has finished. The router waits for
  /// this before deciding where to send the user.
  final bool isInitialized;

  AuthState copyWith({
    User? user,
    String? token,
    bool? isLoading,
    String? error,
    bool clearError = false,
    bool? isAuthenticated,
    bool? isInitialized,
  }) {
    return AuthState(
      user: user ?? this.user,
      token: token ?? this.token,
      isLoading: isLoading ?? this.isLoading,
      error: clearError ? null : error ?? this.error,
      isAuthenticated: isAuthenticated ?? this.isAuthenticated,
      isInitialized: isInitialized ?? this.isInitialized,
    );
  }
}

/// View model for the auth session. This is the modern Riverpod [Notifier]
/// (the replacement for `StateNotifier`); dependencies are read via [ref].
class AuthViewModel extends Notifier<AuthState> {
  @override
  AuthState build() => const AuthState();

  AuthRepository get _repository => ref.read(authRepositoryProvider);

  /// Restores the session on startup. Always finishes and marks
  /// [AuthState.isInitialized] — even if secure storage hangs or throws — so the
  /// splash screen can never get stuck waiting on it.
  Future<void> checkAuth() async {
    try {
      final token = await _repository
          .readToken()
          .timeout(const Duration(seconds: 5), onTimeout: () => null);
      if (token == null) {
        state = state.copyWith(isAuthenticated: false, isInitialized: true);
        return;
      }

      final user = await _repository.currentUser();
      state = state.copyWith(
        user: user,
        token: token,
        isAuthenticated: user != null,
        isInitialized: true,
      );
    } catch (_) {
      // Treat any storage/network failure as logged out, but finish init.
      state = state.copyWith(isAuthenticated: false, isInitialized: true);
    }
  }

  Future<void> login({required String username, required String password}) async {
    state = state.copyWith(isLoading: true, clearError: true);

    // MOCK AUTH: any non-empty credentials succeed. Remove this block and set
    // ApiConfig.baseUrl to the real backend to use live authentication.
    if (username.trim().isNotEmpty && password.isNotEmpty) {
      await Future.delayed(const Duration(milliseconds: 800));
      final mockUser = User(
        id: '1',
        name: username,
        email: username.contains('@') ? username : '$username@example.com',
      );
      const mockToken = 'mock-jwt-token-12345';
      await _repository.saveToken(mockToken);
      state = state.copyWith(
        user: mockUser,
        token: mockToken,
        isLoading: false,
        isAuthenticated: true,
      );
      return;
    }

    // Real flow (currently unreachable because of the mock block above).
    try {
      final user = await _repository.login(username: username, password: password);
      final token = await _repository.readToken();
      state = state.copyWith(
        user: user,
        token: token,
        isLoading: false,
        isAuthenticated: true,
      );
    } on AppException catch (e) {
      state = state.copyWith(isLoading: false, error: _messageFor(e));
    }
  }

  Future<void> logout() async {
    state = state.copyWith(isLoading: true);
    await _repository.logout();
    state = const AuthState(isInitialized: true);
  }

  void clearError() => state = state.copyWith(clearError: true);

  /// Maps a typed [AppException] to a user-facing Indonesian message.
  String _messageFor(AppException e) {
    return switch (e) {
      UnauthorizedException() => 'Username atau password salah',
      NetworkException() => 'Tidak ada koneksi internet',
      ValidationException() => e.firstError,
      ServerException() => 'Terjadi kesalahan server. Coba lagi.',
      CacheException() => 'Terjadi kesalahan. Coba lagi.',
    };
  }
}

/// App-level provider for the auth session.
final authViewModelProvider = NotifierProvider<AuthViewModel, AuthState>(
  AuthViewModel.new,
);
