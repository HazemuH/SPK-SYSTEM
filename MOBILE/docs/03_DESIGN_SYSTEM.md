# 03 — Design System

**Golden rule:** never hardcode colors, sizes, or `fontSize`, and never use raw Material widgets.
Always go through the tokens and widgets below. (See also the Widget Rules in [CLAUDE.md](../CLAUDE.md).)

Tokens live in `lib/ui/core/themes/`; shared widgets live in `lib/ui/core/widgets/`.

---

## Colors — `AppColors` (`themes/colors.dart`)

| Token | Hex | Use for |
|---|---|---|
| `primary` | `#2563EB` | primary actions, brand |
| `primaryLight` | `#93C5FD` | disabled primary button |
| `primaryDark` | `#1D4ED8` | pressed / darker variant |
| `secondary` | `#7C3AED` | secondary actions |
| `background` | `#F8FAFC` | scaffold background |
| `surface` | `#FFFFFF` | cards, sheets |
| `surfaceVariant` | `#F1F5F9` | alternate surface |
| `textPrimary` | `#0F172A` | primary text |
| `textSecondary` | `#64748B` | supporting text |
| `textDisabled` | `#CBD5E1` | disabled text |
| `success` / `warning` / `error` / `info` | green / orange / red / blue | feedback |
| `border` `#E2E8F0` / `divider` `#F1F5F9` | lines |
| `white` / `black` | — | absolutes |

```dart
color: AppColors.primary      // ✅
color: Colors.blue            // ❌
color: const Color(0xFF...)   // ❌
```

---

## Spacing — `AppSpacing` (multiples of 4)

| Token | px |
|---|---|
| `xs` | 4 |
| `sm` | 8 |
| `md` | 16 |
| `lg` | 24 |
| `xl` | 32 |
| `xxl` | 48 |

```dart
const SizedBox(height: AppSpacing.md)      // ✅
const EdgeInsets.all(AppSpacing.lg)        // ✅
const EdgeInsets.symmetric(horizontal: 24) // ❌
```

---

## Typography — `AppTypography`

| Style | Size / Weight |
|---|---|
| `displayLarge / Medium / Small` | 57 / 45 / 36 · w400 |
| `headlineLarge / Medium / Small` | 32 / 28 / 24 · w600 |
| `bodyLarge / Medium / Small` | 16 / 14 / 12 · w400 |
| `labelLarge / Medium / Small` | 14 / 12 / 11 · w600/w500 |

```dart
Text('Title', style: AppTypography.headlineMedium)                              // ✅
Text('Sub', style: AppTypography.bodyMedium.copyWith(color: AppColors.textSecondary)) // ✅
Text('x', style: TextStyle(fontSize: 18))                                       // ❌
```

---

## Widget catalog (`ui/core/widgets/`)

Always use these instead of raw Material widgets.

### `AppButton`
```dart
AppButton(
  label: 'Simpan',
  onPressed: _onSave,
  variant: AppButtonVariant.primary,   // primary | secondary | outline | ghost | danger
  size: AppButtonSize.medium,          // small | medium | large
  isLoading: false,                    // shows a spinner and disables taps
  isFullWidth: false,
  leadingIcon: Icons.save,             // optional
  trailingIcon: null,
)
```
→ Replaces `ElevatedButton` / `TextButton` / `OutlinedButton`.

### `AppTextField`
```dart
AppTextField(
  label: 'Email',
  hint: 'Masukkan email',
  controller: _controller,
  focusNode: _focus,
  prefixIcon: Icons.email_outlined,
  obscureText: false,        // true → adds a show/hide toggle automatically
  keyboardType: TextInputType.emailAddress,
  textInputAction: TextInputAction.next,
  onSubmitted: (_) => _next.requestFocus(),
  validator: Validators.isValidEmail,
  enabled: !isLoading,
  maxLines: 1,
)
```
→ Replaces `TextField` / `TextFormField`.

### `AppCard`
```dart
AppCard(
  onTap: () {},              // optional → makes it tappable
  elevation: 2,
  child: Text('Content'),
)
```

### `AppSnackbar` (static — replaces `ScaffoldMessenger`)
```dart
AppSnackbar.showSuccess(context, 'Berhasil disimpan');
AppSnackbar.showError(context, 'Gagal memuat data');
AppSnackbar.showWarning(context, 'Periksa input');
AppSnackbar.showInfo(context, 'Sinkronisasi selesai');
```

### State widgets (for data-loading screens)
```dart
const AppLoading()                                   // centered spinner

AppErrorView(message: 'Gagal', onRetry: () {...})    // icon + message + "Coba Lagi" button

AppEmptyState(
  icon: Icons.inventory_2_outlined,
  title: 'Belum ada data',
  subtitle: 'Tambahkan item pertama',   // optional
  actionLabel: 'Tambah',                // optional
  onAction: () {},                      // optional
)
```

### `ScreenWrapper`
A scaffold shell with `SafeArea` and default `AppSpacing.md` padding.
```dart
ScreenWrapper(
  appBar: AppBar(title: const Text('Title')),
  floatingActionButton: ...,            // optional
  child: ...,
)
```

---

## Validators (`core/utils/validators.dart`)

```dart
Validators.isNotEmpty(value)        // required
Validators.isValidEmail(value)      // email format
Validators.minLength(value, 6)      // minimum length
Validators.isNumeric(value)         // must be a number
```
Error messages are already in Indonesian. Pass them straight to `AppTextField`'s `validator`.
