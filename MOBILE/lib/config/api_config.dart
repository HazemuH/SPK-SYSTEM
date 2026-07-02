/// Network configuration: base URL, timeouts, and endpoint paths.
class ApiConfig {
  ApiConfig._();

  // TODO: point this to the real backend before going to production.
  static const String baseUrl = 'https://api.example.com/v1';

  static const Duration connectTimeout = Duration(seconds: 30);
  static const Duration receiveTimeout = Duration(seconds: 30);

  // Endpoints
  static const String login = '/auth/login';
  static const String logout = '/auth/logout';
  static const String profile = '/auth/profile';
}
