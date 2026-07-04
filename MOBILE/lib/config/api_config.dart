/// Network configuration: base URL, timeouts, and endpoint paths.
///
/// The mobile app is read-only and login-less: it only calls PUBLIC endpoints.
class ApiConfig {
  ApiConfig._();

  // TODO: point this to the real backend before going to production.
  // Android emulator: use http://10.0.2.2:8080/v1 ; iOS simulator: localhost.
  static const String baseUrl = 'https://api.example.com/v1';

  static const Duration connectTimeout = Duration(seconds: 30);
  static const Duration receiveTimeout = Duration(seconds: 30);

  // Public endpoints (no auth).
  static const String toys = '/toys';
  static const String recommendations = '/toys/recommendations';
}
