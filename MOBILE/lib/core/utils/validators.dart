class Validators {
  Validators._();

  static String? isNotEmpty(String? value) {
    if (value == null || value.trim().isEmpty) return 'Field ini tidak boleh kosong';
    return null;
  }

  static String? isValidEmail(String? value) {
    if (value == null || value.trim().isEmpty) return 'Email tidak boleh kosong';
    final emailRegex = RegExp(r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$');
    if (!emailRegex.hasMatch(value.trim())) return 'Format email tidak valid';
    return null;
  }

  static String? minLength(String? value, int min) {
    if (value == null || value.isEmpty) return 'Field ini tidak boleh kosong';
    if (value.length < min) return 'Minimal $min karakter';
    return null;
  }

  static String? isNumeric(String? value) {
    if (value == null || value.isEmpty) return 'Field ini tidak boleh kosong';
    if (double.tryParse(value) == null) return 'Harus berupa angka';
    return null;
  }
}
