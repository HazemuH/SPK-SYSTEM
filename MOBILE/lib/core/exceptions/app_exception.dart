/// Single, sealed error hierarchy shared across data and UI layers.
///
/// The data layer throws these; view models catch them with `on AppException`
/// and can `switch` exhaustively on the subtypes to build user-facing messages.
sealed class AppException implements Exception {
  const AppException(this.message);

  final String message;

  @override
  String toString() => '$runtimeType: $message';
}

/// 5xx responses or any unexpected server-side failure.
class ServerException extends AppException {
  const ServerException([super.message = 'Something went wrong. Please try again.']);
}

/// 401 responses. The auth token is cleared when this is thrown.
class UnauthorizedException extends AppException {
  const UnauthorizedException([super.message = 'Unauthorized']);
}

/// 422 responses. Holds field-level validation errors from the API.
class ValidationException extends AppException {
  const ValidationException({required this.errors})
      : super('Validation failed');

  final Map<String, List<String>> errors;

  /// The first available validation message, or a fallback.
  String get firstError {
    if (errors.isEmpty) return 'Validation failed';
    final first = errors.values.first;
    return first.isEmpty ? 'Validation failed' : first.first;
  }
}

/// No internet connection or a request timed out.
class NetworkException extends AppException {
  const NetworkException([super.message = 'No internet connection']);
}

/// Local cache / storage failures.
class CacheException extends AppException {
  const CacheException([super.message = 'Cache error occurred']);
}
