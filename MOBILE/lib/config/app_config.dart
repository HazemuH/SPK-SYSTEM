/// App-wide static configuration values (name, version, durations).
class AppConfig {
  AppConfig._();

  static const String appName = 'KIDORA';
  static const String appVersion = '1.0.0';

  static const Duration splashDuration = Duration(milliseconds: 1500);
  static const Duration snackbarDuration = Duration(seconds: 3);
}
