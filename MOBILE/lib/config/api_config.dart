/// Network configuration: base URL, timeouts, and endpoint paths.
///
/// The mobile app is read-only and login-less: it only calls PUBLIC endpoints.
class ApiConfig {
  ApiConfig._();

  // Base URL is overridable at build time so the same code runs against the local
  // emulator (default) or the deployed backend, e.g.:
  //   flutter build apk --dart-define=API_BASE_URL=https://kidora.duckdns.org/v1
  // Default: Android emulator reaches the host at 10.0.2.2 (iOS simulator: localhost).
  static const String baseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8080/v1',
  );

  // Generous timeouts: the free-tier VM can be slow to answer the first request
  // after an idle period (JVM waking from swap on 1 GB RAM), so allow for that.
  static const Duration connectTimeout = Duration(seconds: 60);
  static const Duration receiveTimeout = Duration(seconds: 60);

  // Public endpoints (no auth).
  static const String top = '/public/top';
  static const String toys = '/public/toys'; // catalog + `/toys/{id}` detail
  static const String meta = '/public/meta';
  static const String profiles = '/public/profiles';
  static const String recommend = '/public/recommend';
  static const String compare = '/public/compare';
}
