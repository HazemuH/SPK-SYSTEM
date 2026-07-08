/// Centralized route paths. Reference these instead of hard-coding strings.
///
/// The app is login-less, so there is no auth/splash gate — it opens straight
/// to [home].
class Routes {
  Routes._();

  static const String home = '/';
  static const String catalog = '/katalog';

  /// Toy detail route pattern (`/mainan/:id`) and a builder for a concrete id.
  static const String detailPattern = '/mainan/:id';
  static String detail(String id) => '/mainan/$id';
}
