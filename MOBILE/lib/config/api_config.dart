/// Network configuration: base URL, timeouts, and endpoint paths.
///
/// The mobile app is read-only and login-less: it only calls PUBLIC endpoints.
class ApiConfig {
  ApiConfig._();

  // Android emulator reaches the host at 10.0.2.2; iOS simulator uses localhost.
  // TODO: point this to the real backend host before going to production.
  static const String baseUrl = 'http://10.0.2.2:8080/v1';

  static const Duration connectTimeout = Duration(seconds: 30);
  static const Duration receiveTimeout = Duration(seconds: 30);

  // Public endpoints (no auth).
  static const String top = '/public/top';
  static const String toys = '/public/toys'; // catalog + `/toys/{id}` detail
  static const String meta = '/public/meta';
  static const String profiles = '/public/profiles';
  static const String recommend = '/public/recommend';
  static const String compare = '/public/compare';
}
